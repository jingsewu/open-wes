package org.openwes.script.plugin.runtime.validation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptPluginMaterializationValidatorImplTest {

    private final AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext();
    private ScriptPluginMaterializationValidatorImpl validator;

    @BeforeEach
    void setUp() {
        parent.registerBean(String.class, () -> "injected");
        parent.refresh();
        validator = new ScriptPluginMaterializationValidatorImpl(new GroovyScriptEngine(),
                new ScriptRevisionContextManager(parent, new GroovyScriptEngine()), "wes");
    }

    @AfterEach
    void tearDown() {
        parent.close();
    }

    private ExtensionDeclarationDTO waveDeclaration() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        declaration.setScript("Wave.groovy");
        declaration.setMethod("wave");
        return declaration;
    }

    private ScriptPluginBundleDTO bundle(Map<String, String> scripts, ExtensionDeclarationDTO declaration) {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("packing-wave");
        bundle.setApplySystem("wes");
        bundle.setSdkVersion(ScriptPluginApiConstants.CONTRACT_VERSION);
        bundle.setRevisionId(11L);
        bundle.setScripts(scripts);
        bundle.setDeclarations(List.of(declaration));
        bundle.setConfig(Map.of());
        bundle.setConfigSchema(List.of());
        return bundle;
    }

    private boolean failedOn(ValidationReportDTO report, String stage) {
        return report.getFailures().stream().anyMatch(failure -> failure.getStage().equals(stage));
    }

    @Test
    void validate_withSpringInjectionAvailable_passes() {
        String source = """
                import org.springframework.beans.factory.annotation.Autowired
                class Wave {
                    @Autowired String greeting
                    Object wave(Object orders, Object ctx) { return [] }
                }
                """;

        ValidationReportDTO report = validator.validate(bundle(Map.of("Wave.groovy", source), waveDeclaration()));

        assertTrue(report.isPassed(), report.failureSummary());
        assertTrue(report.getTargetApp().equals("wes"));
    }

    @Test
    void validate_compileError_reportsCompileStage() {
        ValidationReportDTO report = validator.validate(bundle(Map.of("Wave.groovy", "class Wave {"), waveDeclaration()));

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "compile"));
    }

    @Test
    void validate_unsatisfiedInjection_reportsContextStage() {
        String source = """
                import org.springframework.beans.factory.annotation.Autowired
                class Wave {
                    @Autowired java.time.Clock missing
                    Object wave(Object orders, Object ctx) { return [] }
                }
                """;

        ValidationReportDTO report = validator.validate(bundle(Map.of("Wave.groovy", source), waveDeclaration()));

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "context"));
    }

    @Test
    void validate_missingScriptMethod_reportsSmokeStage() {
        String source = "class Wave { Object split(Object orders, Object ctx) { return [] } }";

        ValidationReportDTO report = validator.validate(bundle(Map.of("Wave.groovy", source), waveDeclaration()));

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "smoke"));
    }

    @Test
    void validate_wrongSdkVersion_reportsSdkVersionStage() {
        ScriptPluginBundleDTO bundle = bundle(Map.of("Wave.groovy", "class Wave { Object wave(Object o, Object ctx) { return [] } }"),
                waveDeclaration());
        bundle.setSdkVersion("0.9");

        ValidationReportDTO report = validator.validate(bundle);

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "sdkVersion"));
    }

    @Test
    void validate_declarationReferencingUnknownScript_reportsDeclarationsStage() {
        ScriptPluginBundleDTO bundle = bundle(Map.of("Other.groovy", "class Other { Object wave(Object o, Object ctx) { return [] } }"),
                waveDeclaration());

        ValidationReportDTO report = validator.validate(bundle);

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "declarations"));
    }

    @Test
    void validate_httpEndpointWithoutPath_reportsDeclarationsStage() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.HTTP_ENDPOINT);
        declaration.setScript("Http.groovy");
        declaration.setMethod("handle");
        String source = "class Http { Object handle(Object body, Object ctx) { return 'ok' } }";

        ValidationReportDTO report = validator.validate(bundle(Map.of("Http.groovy", source), declaration));

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "declarations"));
    }

    private ExtensionDeclarationDTO httpEndpoint(String httpMethod) {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.HTTP_ENDPOINT);
        declaration.setScript("Http.groovy");
        declaration.setMethod("handle");
        declaration.setHttpMethod(httpMethod);
        declaration.setPath("/x");
        return declaration;
    }

    @Test
    void validate_duplicateHttpEndpointWithWildcardMethod_reportsDeclarationsStage() {
        ExtensionDeclarationDTO wildcard = httpEndpoint(null);
        ExtensionDeclarationDTO get = httpEndpoint("GET");
        ScriptPluginBundleDTO bundle = bundle(Map.of("Http.groovy",
                "class Http { Object handle(Object body, Object ctx) { return 'ok' } }"), wildcard);
        bundle.setDeclarations(List.of(wildcard, get));

        ValidationReportDTO report = validator.validate(bundle);

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "declarations"));
    }

    @Test
    void validate_duplicateHttpEndpointCaseInsensitiveMethod_reportsDeclarationsStage() {
        ExtensionDeclarationDTO lower = httpEndpoint("get");
        ExtensionDeclarationDTO upper = httpEndpoint("GET");
        ScriptPluginBundleDTO bundle = bundle(Map.of("Http.groovy",
                "class Http { Object handle(Object body, Object ctx) { return 'ok' } }"), lower);
        bundle.setDeclarations(List.of(lower, upper));

        ValidationReportDTO report = validator.validate(bundle);

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "declarations"));
    }

    @Test
    void validate_validSingleHttpEndpoint_passes() {
        String source = "class Http { Object handle(Object body, Object ctx) { return 'ok' } }";

        ValidationReportDTO report = validator.validate(bundle(Map.of("Http.groovy", source), httpEndpoint("GET")));

        assertTrue(report.isPassed(), report.failureSummary());
    }

    @Test
    void validate_invalidCron_reportsDeclarationsStage() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.CRON);
        declaration.setScript("Job.groovy");
        declaration.setMethod("run");
        declaration.setCron("not a cron");
        String source = "class Job { Object run(Object ctx) { return 1 } }";

        ValidationReportDTO report = validator.validate(bundle(Map.of("Job.groovy", source), declaration));

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "declarations"));
    }
}
