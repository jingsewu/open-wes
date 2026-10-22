package org.openwes.ai.plugin;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiGeneration;
import org.openwes.ai.plugin.domain.model.AiSettings;
import org.openwes.ai.plugin.domain.service.AiDraftDocumentParser;
import org.openwes.ai.plugin.domain.service.AiDraftGenerationService;
import org.openwes.ai.plugin.domain.service.AiGenerationAuditService;
import org.openwes.ai.plugin.domain.service.AiQuotaService;
import org.openwes.ai.plugin.domain.service.AiSettingsService;
import org.openwes.ai.plugin.dto.AiDraftRequest;
import org.openwes.ai.plugin.dto.AiDraftStreamEvent;
import org.openwes.ai.plugin.grounding.PluginGroundingService;
import org.openwes.ai.plugin.prompt.AiPromptAssembler;
import org.openwes.ai.plugin.prompt.PluginPromptTemplates;
import org.openwes.ai.plugin.validation.AiDraftValidator;
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
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiDraftPipelineEndToEndTest {

    private final ScriptPluginService scriptPluginService = mock(ScriptPluginService.class);
    private final AiSettingsService settingsService = mock(AiSettingsService.class);
    private final AiQuotaService quotaService = mock(AiQuotaService.class);
    private final AiGenerationAuditService auditService = mock(AiGenerationAuditService.class);
    private final PluginGroundingService groundingService = mock(PluginGroundingService.class);
    private final GroovyScriptEngine engine = new GroovyScriptEngine();
    private final AiPluginProperties properties = new AiPluginProperties();
    private AnnotationConfigApplicationContext parent;

    @BeforeEach
    void setUp() {
        parent = new AnnotationConfigApplicationContext();
        parent.refresh();
        properties.setModel("fake-model");
        UserContext.setAccount("alice");

        when(settingsService.current()).thenReturn(new AiSettings(20, 4096, 3));
        when(quotaService.tryAcquire(anyString(), anyInt())).thenReturn(true);
        when(groundingService.retrieve(anyString())).thenReturn(List.of());

        ScriptPluginDTO plugin = new ScriptPluginDTO();
        plugin.setPluginKey("packing-wave");
        plugin.setName("Packing wave");
        plugin.setApplySystem("wes");
        plugin.setStatus(ScriptPluginStatusEnum.DRAFT);
        when(scriptPluginService.detail("packing-wave")).thenReturn(plugin);
        when(scriptPluginService.revisions("packing-wave")).thenReturn(List.of());

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
    }

    @AfterEach
    void tearDown() {
        UserContext.removeUser();
        parent.close();
    }

    @Test
    void invalidThenValidModelOutput_savesOneAiRevisionAndNeverEnablesIt() {
        String invalid = """
                {"meta":{"name":"x","changeSummary":"wrong"},
                 "extensions":[{"type":"OUTBOUND_WAVE_WAVE_ACTION","script":"Wave.groovy","method":"split"}],
                 "configSchema":[],"scripts":{"Wave.groovy":"class Wave { Object split(Object o, Object ctx) { return o } }"}}
                """;
        String valid = """
                {"meta":{"name":"x","changeSummary":"waves"},
                 "extensions":[{"type":"OUTBOUND_WAVE_WAVE_ACTION","script":"Wave.groovy","method":"wave"}],
                 "configSchema":[],"scripts":{"Wave.groovy":"class Wave { Object wave(Object orders, Object ctx) { return orders } }"}}
                """;
        AtomicInteger index = new AtomicInteger();
        ChatModel model = new ChatModel() {
            @Override
            public ChatResponse call(Prompt prompt) {
                throw new UnsupportedOperationException("stream only");
            }

            @Override
            public Flux<ChatResponse> stream(Prompt prompt) {
                String output = index.getAndIncrement() == 0 ? invalid : valid;
                return Flux.just(new ChatResponse(List.of(new Generation(new AssistantMessage(output)))));
            }
        };

        AiDraftValidator validator = new AiDraftValidator(engine, List.of(
                new ScriptPluginMaterializationValidatorImpl(engine,
                        new ScriptRevisionContextManager(parent, engine), "wes")));
        AiDraftGenerationService service = new AiDraftGenerationService(scriptPluginService, settingsService,
                quotaService, auditService, new AiPromptAssembler(properties), new AiDraftDocumentParser(),
                validator, groundingService, model, properties);

        AiDraftRequest request = new AiDraftRequest();
        request.setPluginKey("packing-wave");
        request.setRequirement("group outbound orders into waves");

        List<AiDraftStreamEvent> events = service.stream(request).collectList().block(Duration.ofSeconds(10));

        assertTrue(events != null && events.stream()
                .anyMatch(event -> "stage".equals(event.getType()) && "retry#2".equals(event.getText())));
        AiDraftStreamEvent result = events.get(events.size() - 1);
        assertEquals("result", result.getType());
        assertEquals(55L, result.getResult().getRevisionId());
        assertTrue(result.getResult().isMaterializationVerified());
        assertEquals(Map.of("Wave.groovy",
                        "class Wave { Object wave(Object orders, Object ctx) { return orders } }"),
                result.getResult().getScripts());
        verify(scriptPluginService).saveRevision(eq("packing-wave"), any(), eq(RevisionOriginEnum.AI),
                eq("fake-model"), eq(PluginPromptTemplates.PROMPT_VERSION));
        verify(scriptPluginService, never()).enable(anyString());
    }
}
