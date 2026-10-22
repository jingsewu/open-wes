package org.openwes.script.plugin.api.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptPluginStaticValidatorTest {

    private ScriptPluginStaticValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ScriptPluginStaticValidator(new GroovyScriptEngine());
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

    private boolean failedOn(List<ValidationReportDTO.ValidationFailureDTO> failures, String stage) {
        return failures.stream().anyMatch(failure -> failure.getStage().equals(stage));
    }

    @Test
    void validBundle_returnsNoFailures() {
        String source = "class Wave { Object wave(Object orders, Object ctx) { return [] } }";

        List<ValidationReportDTO.ValidationFailureDTO> failures =
                validator.validate(bundle(Map.of("Wave.groovy", source), waveDeclaration()));

        assertTrue(failures.isEmpty(), failures.toString());
    }

    @Test
    void wrongSdkVersion_reportsSdkVersionStage() {
        ScriptPluginBundleDTO bundle = bundle(
                Map.of("Wave.groovy", "class Wave { Object wave(Object o, Object ctx) { return [] } }"),
                waveDeclaration());
        bundle.setSdkVersion("0.9");

        assertTrue(failedOn(validator.validate(bundle), "sdkVersion"));
    }

    @Test
    void declarationWithWrongMethod_reportsDeclarationsStage() {
        ExtensionDeclarationDTO declaration = waveDeclaration();
        declaration.setMethod("split");

        List<ValidationReportDTO.ValidationFailureDTO> failures = validator.validate(bundle(
                Map.of("Wave.groovy", "class Wave { Object split(Object o, Object ctx) { return [] } }"),
                declaration));

        assertTrue(failedOn(failures, "declarations"));
        assertTrue(failures.stream().anyMatch(f -> f.getMessage().contains("requires script method 'wave'")));
    }

    @Test
    void declarationReferencingUnknownScript_reportsDeclarationsStage() {
        List<ValidationReportDTO.ValidationFailureDTO> failures = validator.validate(bundle(
                Map.of("Other.groovy", "class Other { Object wave(Object o, Object ctx) { return [] } }"),
                waveDeclaration()));

        assertTrue(failedOn(failures, "declarations"));
        assertTrue(failures.stream().anyMatch(f -> f.getMessage().contains("unknown script 'Wave.groovy'")));
    }

    @Test
    void duplicateHttpEndpoints_reportsDeclarationsStage() {
        ExtensionDeclarationDTO wildcard = httpEndpoint(null);
        ExtensionDeclarationDTO get = httpEndpoint("GET");
        ScriptPluginBundleDTO bundle = bundle(
                Map.of("Http.groovy", "class Http { Object handle(Object body, Object ctx) { return 'ok' } }"),
                wildcard);
        bundle.setDeclarations(List.of(wildcard, get));

        assertTrue(failedOn(validator.validate(bundle), "declarations"));
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
    void invalidCron_reportsDeclarationsStage() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.CRON);
        declaration.setScript("Job.groovy");
        declaration.setMethod("run");
        declaration.setCron("not a cron");

        List<ValidationReportDTO.ValidationFailureDTO> failures = validator.validate(bundle(
                Map.of("Job.groovy", "class Job { Object run(Object ctx) { return 1 } }"), declaration));

        assertTrue(failedOn(failures, "declarations"));
    }

    @Test
    void compileError_reportsCompileStageWithTheScriptName() {
        List<ValidationReportDTO.ValidationFailureDTO> failures = validator.validate(bundle(
                Map.of("Wave.groovy", "class Wave {"), waveDeclaration()));

        assertTrue(failedOn(failures, "compile"));
        assertTrue(failures.stream().anyMatch(f -> f.getMessage().startsWith("Wave.groovy:")));
    }

    @Test
    void compileReportsEveryBrokenScript() {
        ScriptPluginBundleDTO bundle = bundle(Map.of(
                "Wave.groovy", "class Wave {",
                "Helper.groovy", "class Helper {"), waveDeclaration());
        bundle.setDeclarations(List.of());

        List<ValidationReportDTO.ValidationFailureDTO> failures = validator.validate(bundle);

        assertEquals(2, failures.stream().filter(f -> f.getStage().equals("compile")).count());
    }

    @Test
    void stageCallback_reportsDeclarationsThenCompile() {
        List<String> stages = new ArrayList<>();

        validator.validate(bundle(
                Map.of("Wave.groovy", "class Wave { Object wave(Object o, Object ctx) { return [] } }"),
                waveDeclaration()), stages::add);

        assertEquals(List.of("declarations", "compile"), stages);
    }

    @Test
    void missingScripts_reportsDeclarationsWithoutThrowing() {
        ScriptPluginBundleDTO bundle = bundle(Map.of(), waveDeclaration());

        List<ValidationReportDTO.ValidationFailureDTO> failures = validator.validate(bundle);

        assertFalse(failures.isEmpty());
        assertTrue(failedOn(failures, "declarations"));
        assertTrue(failures.stream().anyMatch(f -> f.getMessage().contains("at least one script")));
    }
}
