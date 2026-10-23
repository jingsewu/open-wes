package org.openwes.ai.plugin.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiGeneration;
import org.openwes.ai.plugin.domain.model.AiDraftDocument;
import org.openwes.ai.plugin.domain.model.AiSettings;
import org.openwes.ai.plugin.dto.AiDraftRequest;
import org.openwes.ai.plugin.dto.AiDraftResult;
import org.openwes.ai.plugin.dto.AiDraftStreamEvent;
import org.openwes.ai.plugin.dto.AiGenerationDTO;
import org.openwes.ai.plugin.exception.AiDraftParseException;
import org.openwes.ai.plugin.exception.AiPluginErrorDescEnum;
import org.openwes.ai.plugin.grounding.PluginGroundingService;
import org.openwes.ai.plugin.prompt.AiPromptAssembler;
import org.openwes.ai.plugin.prompt.PluginPromptTemplates;
import org.openwes.ai.plugin.validation.AiDraftValidator;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.service.PluginWorkbenchAccessService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginOperationAuditService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginService;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiDraftGenerationService {

    private final ScriptPluginService scriptPluginService;
    private final PluginWorkbenchAccessService workbenchAccessService;
    private final ScriptPluginOperationAuditService operationAuditService;
    private final AiSettingsService settingsService;
    private final AiQuotaService quotaService;
    private final AiGenerationAuditService auditService;
    private final AiPromptAssembler promptAssembler;
    private final AiDraftDocumentParser parser;
    private final AiDraftValidator validator;
    private final PluginGroundingService groundingService;
    private final ChatModel chatModel;
    private final AiPluginProperties properties;

    public Flux<AiDraftStreamEvent> stream(AiDraftRequest request) {
        String user = workbenchAccessService.requireDeveloper(
                request.getPluginKey(), ScriptPluginAuditActionEnum.SAVE_REVISION);
        ScriptPluginDTO plugin = scriptPluginService.detail(request.getPluginKey());
        if (plugin.getStatus() != ScriptPluginStatusEnum.DRAFT
                && plugin.getStatus() != ScriptPluginStatusEnum.DISABLED) {
            throw new WmsException(AiPluginErrorDescEnum.AI_PLUGIN_NOT_GENERATABLE,
                    new Object[]{plugin.getPluginKey(), plugin.getStatus()});
        }
        ScriptPluginRevisionDTO baseRevision = resolveBaseRevision(plugin.getPluginKey(),
                request.getBaseRevisionId());

        AiSettings settings = settingsService.current();
        if (!quotaService.tryAcquire(user, settings.dailyGenerationLimit())) {
            throw new WmsException(AiPluginErrorDescEnum.AI_QUOTA_EXCEEDED,
                    new Object[]{settings.dailyGenerationLimit(), user});
        }
        int maxTokens = Math.min(settings.maxTokensPerCall(), properties.getMaxTokensCeiling());

        ScriptPluginAiGeneration audit = auditService.start(plugin.getPluginKey(), user,
                properties.getModel(), PluginPromptTemplates.PROMPT_VERSION);
        GenerationContext context = new GenerationContext(request.getRequirement(), plugin, baseRevision,
                settings, maxTokens, audit.getId(), user);

        Sinks.Many<AiDraftStreamEvent> sink = Sinks.many().unicast().onBackpressureBuffer();
        AtomicBoolean terminal = new AtomicBoolean(false);
        Disposable worker = Schedulers.boundedElastic().schedule(() -> {
            // The worker runs on a pooled thread. Rebind the acting user so JPA auditing
            // (AuditorAwareImpl reads UserContext) records the same user on the AI revision.
            UserContext.setAccount(user);
            try {
                run(context, sink, terminal);
            } finally {
                UserContext.removeUser();
            }
        });

        return sink.asFlux().doOnCancel(() -> {
            if (terminal.compareAndSet(false, true)) {
                auditService.cancel(context.auditId, context.attempts.get(), context.elapsed());
            }
            worker.dispose();
        });
    }

    public List<AiGenerationDTO> generations(String pluginKey) {
        workbenchAccessService.requireDeveloper();
        return auditService.recent(pluginKey);
    }

    public int refreshCorpus() {
        workbenchAccessService.requireDeveloper();
        return groundingService.refreshCorpus();
    }

    private ScriptPluginRevisionDTO resolveBaseRevision(String pluginKey, Long revisionId) {
        if (revisionId == null) {
            return null;
        }
        return scriptPluginService.revisions(pluginKey).stream()
                .filter(revision -> revisionId.equals(revision.getId()))
                .findFirst()
                .orElseThrow(() -> new WmsException(AiPluginErrorDescEnum.AI_BASE_REVISION_NOT_FOUND,
                        new Object[]{revisionId, pluginKey}));
    }

    private void run(GenerationContext context, Sinks.Many<AiDraftStreamEvent> sink, AtomicBoolean terminal) {
        List<String> previousErrors = new ArrayList<>();
        try {
            List<String> grounding = groundingService.retrieve(context.requirement);
            for (int attempt = 1; attempt <= context.settings.maxAttempts(); attempt++) {
                if (terminal.get()) {
                    return;
                }
                if (attempt > 1 && context.elapsed() + properties.getRequestTimeoutMs()
                        > properties.getStreamBudgetMs()) {
                    previousErrors.add("stream budget exhausted before attempt " + attempt);
                    break;
                }
                context.attempts.set(attempt);
                auditService.recordAttempt(context.auditId, attempt);
                if (attempt > 1 && !emit(sink, AiDraftStreamEvent.stage("retry#" + attempt), terminal)) {
                    return;
                }

                String content;
                try {
                    content = callModel(context, grounding, previousErrors, sink, terminal);
                } catch (RuntimeException e) {
                    if (terminal.get()) {
                        log.debug("AI draft stream for {} cancelled", context.plugin.getPluginKey());
                        return;
                    }
                    previousErrors.add("model call: " + message(e));
                    continue;
                }
                if (terminal.get()) {
                    return;
                }

                AiDraftDocument document;
                try {
                    document = parser.parse(content, context.plugin.getApplySystem());
                } catch (AiDraftParseException e) {
                    previousErrors.add(e.getMessage());
                    continue;
                }

                AiDraftValidator.Result validation = validator.validate(context.plugin, document,
                        stage -> emit(sink, AiDraftStreamEvent.stage(stage), terminal));
                if (terminal.get()) {
                    return;
                }
                if (!validation.passed()) {
                    previousErrors.addAll(validation.failures().stream()
                            .map(failure -> failure.getStage() + ": " + failure.getMessage()).toList());
                    continue;
                }

                ScriptPluginRevisionDTO saved = save(context, document);
                if (terminal.get()) {
                    return;
                }
                terminal.set(true);
                auditService.succeed(context.auditId, attempt, context.promptTokens,
                        context.completionTokens, context.elapsed());
                emit(sink, AiDraftStreamEvent.result(toResult(saved, context,
                        validation.materializationVerified(), attempt)), terminal);
                sink.tryEmitComplete();
                return;
            }

            String lastError = previousErrors.isEmpty()
                    ? "model produced no usable draft" : previousErrors.get(previousErrors.size() - 1);
            terminal.set(true);
            auditService.fail(context.auditId, context.attempts.get(), String.join("\n", previousErrors),
                    context.elapsed());
            emit(sink, AiDraftStreamEvent.error(AiPluginErrorDescEnum.AI_GENERATION_FAILED.getCode(),
                    "AI draft generation failed after " + context.attempts.get() + " attempts: " + lastError),
                    terminal);
            sink.tryEmitComplete();
        } catch (RuntimeException e) {
            if (terminal.get()) {
                log.debug("AI draft stream for {} cancelled", context.plugin.getPluginKey());
                return;
            }
            log.error("AI draft generation for plugin {} failed unexpectedly",
                    context.plugin.getPluginKey(), e);
            terminal.set(true);
            auditService.fail(context.auditId, context.attempts.get(), message(e), context.elapsed());
            emit(sink, AiDraftStreamEvent.error(AiPluginErrorDescEnum.AI_INTERNAL_ERROR.getCode(),
                    "AI draft generation failed: " + message(e)), terminal);
            sink.tryEmitComplete();
        }
    }

    private String callModel(GenerationContext context, List<String> grounding, List<String> previousErrors,
                             Sinks.Many<AiDraftStreamEvent> sink, AtomicBoolean terminal) {
        Prompt prompt = promptAssembler.assemble(context.requirement, context.plugin, context.baseRevision,
                grounding, previousErrors, context.maxTokens);
        StringBuilder content = new StringBuilder();
        Flux<ChatResponse> responses = chatModel.stream(prompt)
                .timeout(Duration.ofMillis(properties.getRequestTimeoutMs()));

        for (ChatResponse response : responses.toIterable()) {
            if (terminal.get()) {
                return content.toString();
            }
            String text = extractText(response);
            if (!text.isEmpty()) {
                content.append(text);
                if (!emit(sink, AiDraftStreamEvent.chunk(text), terminal)) {
                    return content.toString();
                }
            }
            recordUsage(context, response);
        }
        return content.toString();
    }

    private String extractText(ChatResponse response) {
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return "";
        }
        String text = response.getResult().getOutput().getText();
        return text == null ? "" : text;
    }

    private void recordUsage(GenerationContext context, ChatResponse response) {
        if (response.getMetadata() == null || response.getMetadata().getUsage() == null) {
            return;
        }
        Usage usage = response.getMetadata().getUsage();
        // EmptyUsage reports 0/0 for responses built without metadata; storing zeros would claim
        // a measured usage that was never reported by the provider.
        if (usage instanceof EmptyUsage) {
            return;
        }
        if (usage.getPromptTokens() != null) {
            context.promptTokens = usage.getPromptTokens();
        }
        if (usage.getCompletionTokens() != null) {
            context.completionTokens = usage.getCompletionTokens();
        }
    }

    private ScriptPluginRevisionDTO save(GenerationContext context, AiDraftDocument document) {
        ScriptPluginRevisionDTO revision = new ScriptPluginRevisionDTO();
        revision.setScripts(document.getScripts());
        revision.setExtensionPoints(document.getExtensions());
        revision.setConfigSchema(document.getConfigSchema());
        revision.setChangeSummary(document.getMeta().getChangeSummary());
        ScriptPluginRevisionDTO saved = scriptPluginService.saveRevision(
                context.plugin.getPluginKey(), revision, RevisionOriginEnum.AI,
                properties.getModel(), PluginPromptTemplates.PROMPT_VERSION, context.user);
        operationAuditService.recordAs(context.plugin.getPluginKey(),
                ScriptPluginAuditActionEnum.SAVE_REVISION, saved.getId(), true,
                "AI revision " + saved.getRevisionNo(), context.user);
        return saved;
    }

    private AiDraftResult toResult(ScriptPluginRevisionDTO revision, GenerationContext context,
                                   boolean materializationVerified, int attempts) {
        return AiDraftResult.builder()
                .pluginKey(context.plugin.getPluginKey())
                .revisionId(revision.getId())
                .revisionNo(revision.getRevisionNo())
                .changeSummary(revision.getChangeSummary())
                .extensions(revision.getExtensionPoints())
                .configSchema(revision.getConfigSchema())
                .scripts(revision.getScripts())
                .materializationVerified(materializationVerified)
                .attempts(attempts)
                .model(properties.getModel())
                .promptVersion(PluginPromptTemplates.PROMPT_VERSION)
                .build();
    }

    private boolean emit(Sinks.Many<AiDraftStreamEvent> sink, AiDraftStreamEvent event,
                         AtomicBoolean terminal) {
        Sinks.EmitResult result = sink.tryEmitNext(event);
        if (!result.isSuccess()) {
            terminal.set(true);
            log.debug("stopped emitting AI draft events: {}", result);
            return false;
        }
        return true;
    }

    private String message(Throwable error) {
        return error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
    }

    private static final class GenerationContext {
        private final String requirement;
        private final ScriptPluginDTO plugin;
        private final ScriptPluginRevisionDTO baseRevision;
        private final AiSettings settings;
        private final int maxTokens;
        private final Long auditId;
        private final String user;
        private final AtomicInteger attempts = new AtomicInteger();
        private final long startedAt = System.currentTimeMillis();
        private volatile Integer promptTokens;
        private volatile Integer completionTokens;

        private GenerationContext(String requirement, ScriptPluginDTO plugin,
                                  ScriptPluginRevisionDTO baseRevision, AiSettings settings,
                                  int maxTokens, Long auditId, String user) {
            this.requirement = requirement;
            this.plugin = plugin;
            this.baseRevision = baseRevision;
            this.settings = settings;
            this.maxTokens = maxTokens;
            this.auditId = auditId;
            this.user = user;
        }

        private long elapsed() {
            return System.currentTimeMillis() - startedAt;
        }
    }
}
