package org.openwes.script.plugin.runtime.validation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptCompileException;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Enable-time validation stage ①–⑤ (spec §Enable-time validation). Runs on the node whose app
 * matches the plugin: in-process on wes, and on station through the pending-validation handshake.
 * Stages: sdkVersion, declarations (static legality), compile, context (dry-run materialization
 * with injection and @PostConstruct), smoke (declared method existence).
 */
@Component
@Slf4j
public class ScriptPluginMaterializationValidatorImpl implements ScriptPluginMaterializationValidator {

    private final GroovyScriptEngine scriptEngine;
    private final ScriptRevisionContextManager revisionContextManager;
    private final String targetApp;

    public ScriptPluginMaterializationValidatorImpl(GroovyScriptEngine scriptEngine,
                                                    ScriptRevisionContextManager revisionContextManager,
                                                    @Value("${spring.application.name:}") String targetApp) {
        this.scriptEngine = scriptEngine;
        this.revisionContextManager = revisionContextManager;
        this.targetApp = targetApp;
    }

    @Override
    public String targetApp() {
        return targetApp;
    }

    @Override
    public ValidationReportDTO validate(ScriptPluginBundleDTO bundle) {
        List<ValidationReportDTO.ValidationFailureDTO> failures = new ArrayList<>();

        if (!ScriptPluginApiConstants.CONTRACT_VERSION.equals(bundle.getSdkVersion())) {
            failures.add(failure("sdkVersion", "plugin sdk_version " + bundle.getSdkVersion()
                    + " does not equal platform contract " + ScriptPluginApiConstants.CONTRACT_VERSION));
        }
        validateDeclarations(bundle, failures);

        if (failures.isEmpty()) {
            dryRun(bundle, failures);
        }

        return new ValidationReportDTO(bundle.getPluginKey(), bundle.getRevisionId(), targetApp,
                failures.isEmpty(), failures, System.currentTimeMillis());
    }

    private void validateDeclarations(ScriptPluginBundleDTO bundle,
                                      List<ValidationReportDTO.ValidationFailureDTO> failures) {
        Map<String, Set<String>> httpEndpointsByPath = new HashMap<>();
        for (ExtensionDeclarationDTO declaration : bundle.getDeclarations()) {
            if (declaration.getType() == null
                    || PluginContractCatalog.archetype(declaration.getType()).isEmpty()) {
                failures.add(failure("declarations", "unknown extension type " + declaration.getType()));
                continue;
            }
            if (StringUtils.isBlank(declaration.getScript()) || StringUtils.isBlank(declaration.getMethod())) {
                failures.add(failure("declarations", declaration.getType() + " needs both script and method"));
                continue;
            }
            if (!bundle.getScripts().containsKey(declaration.getScript())) {
                failures.add(failure("declarations", declaration.getType() + " references unknown script '"
                        + declaration.getScript() + "'"));
                continue;
            }
            switch (declaration.getType()) {
                case REQUEST_VALIDATE, REQUEST_SUPPLY, REQUEST_AFTER_INVOKE, CUSTOM_REQUEST_INVOKE -> {
                    if (StringUtils.isBlank(declaration.getApiType())) {
                        failures.add(failure("declarations", declaration.getType() + " needs apiType"));
                    }
                }
                case LIFECYCLE_LISTENER -> {
                    if (StringUtils.isBlank(declaration.getEntityName())) {
                        failures.add(failure("declarations", "LIFECYCLE_LISTENER needs entityName"));
                    }
                }
                case CUSTOM_API -> {
                    if (StringUtils.isBlank(declaration.getCustomApiCode())) {
                        failures.add(failure("declarations", "CUSTOM_API needs customApiCode"));
                    }
                }
                case HTTP_ENDPOINT -> {
                    if (StringUtils.isBlank(declaration.getPath()) || !declaration.getPath().startsWith("/")
                            || declaration.getPath().endsWith("/")) {
                        failures.add(failure("declarations", "HTTP_ENDPOINT path must start with '/' and not end with '/'"));
                    } else if (isDuplicateHttpEndpoint(httpEndpointsByPath, declaration)) {
                        failures.add(failure("declarations", "duplicate HTTP endpoint "
                                + normalizeHttpMethod(declaration.getHttpMethod()) + " " + declaration.getPath()));
                    }
                }
                case CRON -> {
                    if (StringUtils.isBlank(declaration.getCron())) {
                        failures.add(failure("declarations", "CRON needs a cron expression"));
                    } else {
                        try {
                            CronExpression.parse(declaration.getCron());
                        } catch (IllegalArgumentException e) {
                            failures.add(failure("declarations", "invalid cron '" + declaration.getCron()
                                    + "': " + e.getMessage()));
                        }
                    }
                }
                default -> {
                }
            }
        }
    }

    /**
     * Mirrors {@code ScriptPluginRegistry#matchesHttpMethod}: a blank method is a wildcard and
     * method comparison is case-insensitive, so "*" overlaps every method declared on the path.
     */
    private boolean isDuplicateHttpEndpoint(Map<String, Set<String>> endpointsByPath,
                                            ExtensionDeclarationDTO declaration) {
        String method = normalizeHttpMethod(declaration.getHttpMethod());
        Set<String> methods = endpointsByPath.computeIfAbsent(declaration.getPath(), path -> new HashSet<>());
        boolean duplicate = methods.contains("*") || (method.equals("*") && !methods.isEmpty())
                || methods.contains(method);
        methods.add(method);
        return duplicate;
    }

    private String normalizeHttpMethod(String httpMethod) {
        return StringUtils.defaultIfBlank(httpMethod, "*").toUpperCase(Locale.ROOT);
    }

    private void dryRun(ScriptPluginBundleDTO bundle, List<ValidationReportDTO.ValidationFailureDTO> failures) {
        try (ScriptRevisionContextManager.ValidationScope scope = revisionContextManager
                .openValidationScope(bundle.getPluginKey(), bundle.getScripts())) {
            for (ExtensionDeclarationDTO declaration : bundle.getDeclarations()) {
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
