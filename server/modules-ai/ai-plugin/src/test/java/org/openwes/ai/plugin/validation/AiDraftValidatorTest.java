package org.openwes.ai.plugin.validation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.domain.model.AiDraftDocument;
import org.openwes.ai.plugin.domain.service.AiDraftDocumentParser;
import org.openwes.ai.plugin.prompt.PluginPromptTemplates;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.openwes.script.plugin.runtime.validation.ScriptPluginMaterializationValidatorImpl;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiDraftValidatorTest {

    private final AiDraftDocumentParser parser = new AiDraftDocumentParser();
    private final GroovyScriptEngine engine = new GroovyScriptEngine();
    private AnnotationConfigApplicationContext parent;
    private ScriptPluginMaterializationValidator wesMaterializer;
    private AiDraftValidator validator;

    @BeforeEach
    void setUp() {
        parent = new AnnotationConfigApplicationContext();
        parent.refresh();
        wesMaterializer = new ScriptPluginMaterializationValidatorImpl(engine,
                new ScriptRevisionContextManager(parent, engine), "wes");
        validator = new AiDraftValidator(engine, List.of(wesMaterializer));
    }

    @AfterEach
    void tearDown() {
        parent.close();
    }

    private ScriptPluginDTO plugin(String app) {
        ScriptPluginDTO plugin = new ScriptPluginDTO();
        plugin.setPluginKey("packing-wave");
        plugin.setName("Packing wave");
        plugin.setApplySystem(app);
        plugin.setStatus(ScriptPluginStatusEnum.DRAFT);
        return plugin;
    }

    @Test
    void wesDraft_passesStaticValidationAndInProcessMaterialization() {
        AiDraftDocument document = parser.parse(PluginPromptTemplates.outputExample(), "wes");
        List<String> stages = new ArrayList<>();

        AiDraftValidator.Result result = validator.validate(plugin("wes"), document, stages::add);

        assertTrue(result.passed(), result.failures().toString());
        assertTrue(result.materializationVerified());
        assertEquals(List.of("declarations", "compiling", "wiring"), stages);
    }

    @Test
    void stationDraft_passesStaticValidationWithoutMaterialization() {
        AiDraftDocument document = parser.parse(PluginPromptTemplates.outputExample(), "station");
        List<String> stages = new ArrayList<>();

        AiDraftValidator.Result result = validator.validate(plugin("station"), document, stages::add);

        assertTrue(result.passed(), result.failures().toString());
        assertFalse(result.materializationVerified());
        assertEquals(List.of("declarations", "compiling"), stages);
    }

    @Test
    void missingScriptMethod_failsAtTheWiringStage() {
        AiDraftDocument document = parser.parse("""
                {"meta":{"name":"x","changeSummary":"y"},
                 "extensions":[{"type":"HTTP_ENDPOINT","script":"A.groovy","method":"handle","httpMethod":"GET","path":"/x"}],
                 "configSchema":[],"scripts":{"A.groovy":"class A { Object other(Object b, Object ctx) { return 1 } }"}}
                """, "wes");
        List<String> stages = new ArrayList<>();

        AiDraftValidator.Result result = validator.validate(plugin("wes"), document, stages::add);

        assertFalse(result.passed());
        assertTrue(result.materializationVerified());
        assertTrue(stages.contains("wiring"));
        assertTrue(result.failures().stream().anyMatch(f -> f.getStage().equals("wiring")
                && f.getMessage().contains("does not declare method 'handle'")));
    }

    @Test
    void compileError_failsAtTheCompilingStage() {
        AiDraftDocument document = parser.parse("""
                {"meta":{"name":"x","changeSummary":"y"},
                 "extensions":[{"type":"HTTP_ENDPOINT","script":"A.groovy","method":"handle","httpMethod":"GET","path":"/x"}],
                 "configSchema":[],"scripts":{"A.groovy":"class A {"}}
                """, "wes");
        List<String> stages = new ArrayList<>();

        AiDraftValidator.Result result = validator.validate(plugin("wes"), document, stages::add);

        assertFalse(result.passed());
        assertTrue(stages.contains("compiling"));
        assertTrue(result.failures().stream().anyMatch(f -> f.getStage().equals("compiling")));
    }

    @Test
    void wesWithoutAMaterializer_failsClosed() {
        AiDraftValidator noMaterializer = new AiDraftValidator(engine, List.of());

        AiDraftValidator.Result result = noMaterializer.validate(plugin("wes"),
                parser.parse(PluginPromptTemplates.outputExample(), "wes"), stage -> { });

        assertFalse(result.passed());
        assertFalse(result.materializationVerified());
        assertTrue(result.failures().stream().anyMatch(f -> f.getStage().equals("wiring")));
    }

    @Test
    void materializerFailureStages_areMappedToWiring() {
        ScriptPluginMaterializationValidator alwaysContextFailure = new ScriptPluginMaterializationValidator() {
            @Override
            public String targetApp() {
                return "wes";
            }

            @Override
            public ValidationReportDTO validate(ScriptPluginBundleDTO bundle) {
                return ValidationReportDTO.failed("packing-wave", null, "wes", "context", "@PostConstruct failed");
            }
        };
        AiDraftValidator mapped = new AiDraftValidator(engine, List.of(alwaysContextFailure));

        AiDraftValidator.Result result = mapped.validate(plugin("wes"),
                parser.parse(PluginPromptTemplates.outputExample(), "wes"), stage -> { });

        assertFalse(result.passed());
        assertTrue(result.failures().stream().anyMatch(f -> f.getStage().equals("wiring")
                && f.getMessage().contains("@PostConstruct")));
    }
}
