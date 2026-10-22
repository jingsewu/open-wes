package org.openwes.ai.plugin.domain.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiGeneration;
import org.openwes.ai.plugin.domain.model.AiSettings;
import org.openwes.ai.plugin.dto.AiDraftRequest;
import org.openwes.ai.plugin.dto.AiDraftStreamEvent;
import org.openwes.ai.plugin.grounding.PluginGroundingService;
import org.openwes.ai.plugin.prompt.AiPromptAssembler;
import org.openwes.ai.plugin.prompt.PluginPromptTemplates;
import org.openwes.ai.plugin.validation.AiDraftValidator;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.service.ScriptPluginService;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.openwes.script.plugin.runtime.validation.ScriptPluginMaterializationValidatorImpl;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiDraftGenerationServiceTest {

    private final ScriptPluginService scriptPluginService = mock(ScriptPluginService.class);
    private final AiSettingsService settingsService = mock(AiSettingsService.class);
    private final AiQuotaService quotaService = mock(AiQuotaService.class);
    private final AiGenerationAuditService auditService = mock(AiGenerationAuditService.class);
    private final PluginGroundingService groundingService = mock(PluginGroundingService.class);
    private final GroovyScriptEngine engine = new GroovyScriptEngine();
    private final AiPluginProperties properties = new AiPluginProperties();

    private AnnotationConfigApplicationContext parent;
    private AiDraftValidator validator;
    private AiDraftGenerationService service;

    @BeforeEach
    void setUp() {
        parent = new AnnotationConfigApplicationContext();
        parent.refresh();
        validator = new AiDraftValidator(engine, List.of(new ScriptPluginMaterializationValidatorImpl(
                engine, new ScriptRevisionContextManager(parent, engine), "wes")));
        properties.setModel("fake-model");

        when(settingsService.current()).thenReturn(new AiSettings(20, 4096, 3));
        when(quotaService.tryAcquire(anyString(), anyInt())).thenReturn(true);
        when(scriptPluginService.detail("packing-wave")).thenReturn(plugin());
        when(scriptPluginService.revisions("packing-wave")).thenReturn(List.of());
        when(groundingService.retrieve(anyString())).thenReturn(List.of());

        ScriptPluginAiGeneration audit = ScriptPluginAiGeneration.start(
                "packing-wave", "alice", "fake-model", PluginPromptTemplates.PROMPT_VERSION);
        audit.setId(1L);
        when(auditService.start(anyString(), anyString(), anyString(), anyString())).thenReturn(audit);

        when(scriptPluginService.saveRevision(anyString(), any(), eq(RevisionOriginEnum.AI),
                anyString(), anyString())).thenAnswer(invocation -> {
            ScriptPluginRevisionDTO dto = invocation.getArgument(1);
            dto.setId(55L);
            dto.setRevisionNo(4);
            return dto;
        });

        UserContext.setAccount("alice");
    }

    @AfterEach
    void tearDown() {
        UserContext.removeUser();
        parent.close();
    }

    private ScriptPluginDTO plugin() {
        ScriptPluginDTO plugin = new ScriptPluginDTO();
        plugin.setPluginKey("packing-wave");
        plugin.setName("Packing wave");
        plugin.setApplySystem("wes");
        plugin.setStatus(ScriptPluginStatusEnum.DRAFT);
        return plugin;
    }

    private AiDraftRequest request() {
        AiDraftRequest request = new AiDraftRequest();
        request.setPluginKey("packing-wave");
        request.setRequirement("group outbound orders into waves");
        return request;
    }

    private ChatModel modelReturning(String... outputs) {
        AtomicInteger index = new AtomicInteger();
        return new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                throw new UnsupportedOperationException("stream only");
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                String output = outputs[Math.min(index.getAndIncrement(), outputs.length - 1)];
                return Flux.just(new ChatResponse(List.of(new Generation(new AssistantMessage(output)))));
            }
        };
    }

    private AiDraftGenerationService serviceWith(ChatModel chatModel) {
        return new AiDraftGenerationService(scriptPluginService, settingsService, quotaService,
                auditService, new AiPromptAssembler(properties), new AiDraftDocumentParser(),
                validator, groundingService, chatModel, properties);
    }

    private String invalidWaveDraft() {
        return """
                {"meta":{"name":"x","changeSummary":"wrong method"},
                 "extensions":[{"type":"OUTBOUND_WAVE_WAVE_ACTION","script":"Wave.groovy","method":"split"}],
                 "configSchema":[],"scripts":{"Wave.groovy":"class Wave { Object split(Object o, Object ctx) { return o } }"}}
                """;
    }

    private String validWaveDraft() {
        return """
                {"meta":{"name":"x","changeSummary":"waves of ten"},
                 "extensions":[{"type":"OUTBOUND_WAVE_WAVE_ACTION","script":"Wave.groovy","method":"wave"}],
                 "configSchema":[{"field":"maxPerWave","type":"INT","required":false,"defaultValue":"10"}],
                 "scripts":{"Wave.groovy":"class Wave { Object wave(Object orders, Object ctx) { return orders } }"}}
                """;
    }

    @Test
    void succeedsOnTheSecondAttemptAndSavesAnAiRevision() {
        List<AiDraftStreamEvent> events = serviceWith(modelReturning(invalidWaveDraft(), validWaveDraft()))
                .stream(request()).collectList().block(Duration.ofSeconds(10));

        assertNotNull(events);
        assertTrue(events.stream().anyMatch(e -> e.getType().equals("stage") && e.getText().equals("retry#2")));
        assertTrue(events.stream().anyMatch(e -> e.getType().equals("stage") && e.getText().equals("compiling")));
        assertTrue(events.stream().anyMatch(e -> e.getType().equals("stage") && e.getText().equals("wiring")));

        AiDraftStreamEvent last = events.get(events.size() - 1);
        assertEquals("result", last.getType());
        assertEquals(55L, last.getResult().getRevisionId());
        assertEquals(4, last.getResult().getRevisionNo());
        assertEquals(2, last.getResult().getAttempts());
        assertTrue(last.getResult().isMaterializationVerified());
        assertEquals("fake-model", last.getResult().getModel());
        assertEquals(PluginPromptTemplates.PROMPT_VERSION, last.getResult().getPromptVersion());

        verify(scriptPluginService).saveRevision(eq("packing-wave"), any(), eq(RevisionOriginEnum.AI),
                eq("fake-model"), eq(PluginPromptTemplates.PROMPT_VERSION));
        verify(scriptPluginService, never()).enable(anyString());
        verify(auditService).recordAttempt(1L, 1);
        verify(auditService).recordAttempt(1L, 2);
        verify(auditService).succeed(eq(1L), eq(2), isNull(), isNull(), anyLong());
    }

    @Test
    void retriesAtMostThreeTimesThenEmitsAnErrorEvent() {
        List<AiDraftStreamEvent> events = serviceWith(modelReturning("not a draft"))
                .stream(request()).collectList().block(Duration.ofSeconds(10));

        assertNotNull(events);
        AiDraftStreamEvent last = events.get(events.size() - 1);
        assertEquals("error", last.getType());
        assertEquals("PL001006", last.getCode());
        assertTrue(last.getText().contains("after 3 attempts"));
        verify(auditService).fail(eq(1L), eq(3), anyString(), anyLong());
        verify(scriptPluginService, never()).saveRevision(anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void quotaExhaustion_isRejectedBeforeTheStreamStarts() {
        when(quotaService.tryAcquire("alice", 20)).thenReturn(false);

        assertThrows(WmsException.class, () -> serviceWith(modelReturning(validWaveDraft())).stream(request()));
        verify(auditService, never()).start(anyString(), anyString(), anyString(), anyString());
        verify(scriptPluginService, never()).saveRevision(anyString(), any(), any(), anyString(), anyString());
    }

    @Test
    void enabledPlugins_areRejectedBeforeTheStreamStarts() {
        ScriptPluginDTO enabled = plugin();
        enabled.setStatus(ScriptPluginStatusEnum.ENABLED);
        when(scriptPluginService.detail("packing-wave")).thenReturn(enabled);

        assertThrows(WmsException.class, () -> serviceWith(modelReturning(validWaveDraft())).stream(request()));
        verify(auditService, never()).start(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void unknownBaseRevision_isRejectedBeforeTheStreamStarts() {
        AiDraftRequest request = request();
        request.setBaseRevisionId(999L);

        assertThrows(WmsException.class, () -> serviceWith(modelReturning(validWaveDraft())).stream(request));
    }

    @Test
    void stopsRetryingWhenTheStreamBudgetIsExhausted() {
        properties.setStreamBudgetMs(0);

        List<AiDraftStreamEvent> events = serviceWith(modelReturning("not a draft"))
                .stream(request()).collectList().block(Duration.ofSeconds(10));

        assertNotNull(events);
        AiDraftStreamEvent last = events.get(events.size() - 1);
        assertEquals("error", last.getType());
        assertTrue(last.getText().contains("after 1 attempts"));
        verify(auditService).fail(eq(1L), eq(1), anyString(), anyLong());
    }

    @Test
    void modelCallFailure_isFedBackIntoTheRetryPrompt() {
        AtomicInteger calls = new AtomicInteger();
        ChatModel flaky = new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                throw new UnsupportedOperationException("stream only");
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                if (calls.getAndIncrement() == 0) {
                    return Flux.error(new IllegalStateException("model unavailable"));
                }
                return Flux.just(new ChatResponse(List.of(new Generation(new AssistantMessage(validWaveDraft())))));
            }
        };

        List<AiDraftStreamEvent> events = serviceWith(flaky).stream(request())
                .collectList().block(Duration.ofSeconds(10));

        assertNotNull(events);
        assertEquals("result", events.get(events.size() - 1).getType());
        verify(auditService).succeed(eq(1L), eq(2), isNull(), isNull(), anyLong());
    }
}
