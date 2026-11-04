package org.openwes.ai.plugin.validation;

import org.openwes.ai.plugin.domain.model.AiDraftDocument;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.api.validation.ScriptPluginStaticValidator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Validates an unsaved draft with the same rules as enable-time validation. Static stages run for
 * both apps. On wes, the in-process materializer additionally proves context creation, injection
 * and declared methods; station drafts are static-only until Phase 3 adds a preview handshake.
 */
@Component
public class AiDraftValidator {

    public record Result(boolean passed, boolean materializationVerified,
                         List<ValidationReportDTO.ValidationFailureDTO> failures) {
    }

    private final ScriptPluginStaticValidator staticValidator;
    private final List<ScriptPluginMaterializationValidator> materializationValidators;

    public AiDraftValidator(GroovyScriptEngine scriptEngine,
                            List<ScriptPluginMaterializationValidator> materializationValidators) {
        this.staticValidator = new ScriptPluginStaticValidator(scriptEngine);
        this.materializationValidators = materializationValidators;
    }

    public Result validate(ScriptPluginDTO plugin, AiDraftDocument document, Consumer<String> onStage) {
        List<ValidationReportDTO.ValidationFailureDTO> failures = new ArrayList<>();
        List<ExtensionType> allowed = PluginContractCatalog.typesForApp(plugin.getApplySystem());
        for (ExtensionDeclarationDTO extension : document.getExtensions()) {
            if (!allowed.contains(extension.getType())) {
                failures.add(failure("declarations", extension.getType()
                        + " is not supported for app " + plugin.getApplySystem()));
            }
        }
        if (!failures.isEmpty()) {
            return new Result(false, false, failures);
        }

        ScriptPluginBundleDTO bundle = toBundle(plugin, document);
        for (ValidationReportDTO.ValidationFailureDTO failure
                : staticValidator.validate(bundle, stage -> onStage.accept(mapStage(stage)))) {
            failures.add(failure(mapStage(failure.getStage()), failure.getMessage()));
        }
        if (!failures.isEmpty()) {
            return new Result(false, false, failures);
        }

        if (!ScriptPluginApiConstants.APP_WES.equals(plugin.getApplySystem())) {
            return new Result(true, false, failures);
        }

        Optional<ScriptPluginMaterializationValidator> local = materializationValidators.stream()
                .filter(validator -> ScriptPluginApiConstants.APP_WES.equals(validator.targetApp()))
                .findFirst();
        if (local.isEmpty()) {
            failures.add(failure("wiring",
                    "wes materialization validator is unavailable; cannot verify the draft"));
            return new Result(false, false, failures);
        }

        onStage.accept("wiring");
        ValidationReportDTO report = local.get().validate(bundle);
        report.getFailures().forEach(failure ->
                failures.add(failure(mapStage(failure.getStage()), failure.getMessage())));
        return new Result(failures.isEmpty(), true, failures);
    }

    private ScriptPluginBundleDTO toBundle(ScriptPluginDTO plugin, AiDraftDocument document) {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey(plugin.getPluginKey());
        bundle.setName(plugin.getName());
        bundle.setApplySystem(plugin.getApplySystem());
        bundle.setApplyModule(plugin.getApplyModule());
        bundle.setSdkVersion(ScriptPluginApiConstants.CONTRACT_VERSION);
        bundle.setDeclarations(document.getExtensions());
        bundle.setConfigSchema(document.getConfigSchema());
        bundle.setScripts(document.getScripts());
        bundle.setConfig(Map.of());
        return bundle;
    }

    /** UI/SSE stage vocabulary; every failure maps onto declarations, compiling or wiring. */
    private String mapStage(String stage) {
        return switch (stage) {
            case "sdkVersion", "declarations" -> "declarations";
            case "compile" -> "compiling";
            case "context", "smoke" -> "wiring";
            default -> stage;
        };
    }

    private ValidationReportDTO.ValidationFailureDTO failure(String stage, String message) {
        return new ValidationReportDTO.ValidationFailureDTO(stage, message);
    }
}
