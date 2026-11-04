package org.openwes.script.plugin.runtime.validation;

import lombok.extern.slf4j.Slf4j;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptCompileException;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.validation.ScriptPluginStaticValidator;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Enable-time validation stage ①–⑤ (spec §Enable-time validation). Runs on the node whose app
 * matches the plugin: in-process on wes, and on station through the pending-validation handshake.
 * Stages: sdkVersion and declarations/compile (delegated to {@link ScriptPluginStaticValidator}),
 * then context (dry-run materialization with injection and @PostConstruct) and smoke (declared
 * method existence).
 */
@Component
@Slf4j
public class ScriptPluginMaterializationValidatorImpl implements ScriptPluginMaterializationValidator {

    private final GroovyScriptEngine scriptEngine;
    private final ScriptRevisionContextManager revisionContextManager;
    private final String targetApp;
    private final ScriptPluginStaticValidator staticValidator;

    public ScriptPluginMaterializationValidatorImpl(GroovyScriptEngine scriptEngine,
                                                    ScriptRevisionContextManager revisionContextManager,
                                                    @Value("${spring.application.name:}") String targetApp) {
        this.scriptEngine = scriptEngine;
        this.revisionContextManager = revisionContextManager;
        this.targetApp = targetApp;
        this.staticValidator = new ScriptPluginStaticValidator(scriptEngine);
    }

    @Override
    public String targetApp() {
        return targetApp;
    }

    @Override
    public ValidationReportDTO validate(ScriptPluginBundleDTO bundle) {
        List<ValidationReportDTO.ValidationFailureDTO> failures = staticValidator.validate(bundle);
        if (failures.isEmpty()) {
            dryRun(bundle, failures);
        }
        return new ValidationReportDTO(bundle.getPluginKey(), bundle.getRevisionId(), targetApp,
                failures.isEmpty(), failures, System.currentTimeMillis());
    }

    private void dryRun(ScriptPluginBundleDTO bundle, List<ValidationReportDTO.ValidationFailureDTO> failures) {
        List<ExtensionDeclarationDTO> declarations = bundle.getDeclarations() == null
                ? List.of() : bundle.getDeclarations();
        try (ScriptRevisionContextManager.ValidationScope scope = revisionContextManager
                .openValidationScope(bundle.getPluginKey(), bundle.getScripts())) {
            for (ExtensionDeclarationDTO declaration : declarations) {
                Object instance = scope.instances().get(declaration.getScript());
                if (instance == null || !scriptEngine.respondsTo(instance, declaration.getMethod())) {
                    failures.add(failure("smoke", declaration.getScript() + " does not declare method '"
                            + declaration.getMethod() + "'"));
                }
            }
        } catch (ScriptCompileException e) {
            failures.add(failure("compile", e.getMessage()));
        } catch (Exception e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            failures.add(failure("context", message));
        }
    }

    private ValidationReportDTO.ValidationFailureDTO failure(String stage, String message) {
        return new ValidationReportDTO.ValidationFailureDTO(stage, message);
    }
}
