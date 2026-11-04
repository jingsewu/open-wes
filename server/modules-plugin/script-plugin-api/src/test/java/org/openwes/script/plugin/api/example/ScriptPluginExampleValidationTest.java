package org.openwes.script.plugin.api.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.plugin.api.RepositoryRoot;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.api.validation.ScriptPluginStaticValidator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptPluginExampleValidationTest {

    @Test
    void sampleBundle_passesStaticValidation() throws IOException {
        List<ValidationReportDTO.ValidationFailureDTO> failures =
                new ScriptPluginStaticValidator(new GroovyScriptEngine()).validate(loadSampleBundle());

        assertTrue(failures.isEmpty(), () -> failures.stream()
                .map(failure -> failure.getStage() + ": " + failure.getMessage())
                .collect(Collectors.joining("\n")));
    }

    @Test
    void sampleDeclarations_areAllowedForTheTargetApp() throws IOException {
        ScriptPluginBundleDTO bundle = loadSampleBundle();
        List<ExtensionType> allowed = PluginContractCatalog.typesForApp(bundle.getApplySystem());

        assertFalse(bundle.getDeclarations().isEmpty(), "the sample must declare at least one extension");
        for (ExtensionDeclarationDTO declaration : bundle.getDeclarations()) {
            assertTrue(allowed.contains(declaration.getType()),
                    declaration.getType() + " is not allowed for applySystem " + bundle.getApplySystem());
        }
    }

    private ScriptPluginBundleDTO loadSampleBundle() throws IOException {
        Path sample = RepositoryRoot.find().resolve("server/modules-plugin/plugin-example");
        ScriptPluginBundleDTO bundle = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(Files.readString(sample.resolve("plugin.json"), StandardCharsets.UTF_8),
                        ScriptPluginBundleDTO.class);

        Map<String, String> scripts = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(sample)) {
            for (Path script : files.filter(file -> file.getFileName().toString().endsWith(".groovy")).toList()) {
                scripts.put(script.getFileName().toString(), Files.readString(script, StandardCharsets.UTF_8));
            }
        }
        bundle.setScripts(scripts);
        return bundle;
    }
}
