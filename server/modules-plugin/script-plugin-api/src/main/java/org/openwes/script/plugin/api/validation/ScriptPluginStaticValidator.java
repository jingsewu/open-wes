package org.openwes.script.plugin.api.validation;

import org.apache.commons.lang3.StringUtils;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptCompileException;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.springframework.scheduling.support.CronExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

/**
 * The host-independent half of the enable-time validation: contract version, declaration legality
 * and Groovy compilation. Used by the runtime's materialization validator and by the AI draft
 * pipeline before a revision exists. Stages are reported as "declarations" and "compile".
 */
public class ScriptPluginStaticValidator {

    private static final Consumer<String> NO_STAGES = stage -> { };

    private final GroovyScriptEngine scriptEngine;

    public ScriptPluginStaticValidator(GroovyScriptEngine scriptEngine) {
        this.scriptEngine = scriptEngine;
    }

    public List<ValidationReportDTO.ValidationFailureDTO> validate(ScriptPluginBundleDTO bundle) {
        return validate(bundle, NO_STAGES);
    }

    public List<ValidationReportDTO.ValidationFailureDTO> validate(ScriptPluginBundleDTO bundle,
                                                                   Consumer<String> onStage) {
        List<ValidationReportDTO.ValidationFailureDTO> failures = new ArrayList<>();

        if (!ScriptPluginApiConstants.CONTRACT_VERSION.equals(bundle.getSdkVersion())) {
            failures.add(failure("sdkVersion", "plugin sdk_version " + bundle.getSdkVersion()
                    + " does not equal platform contract " + ScriptPluginApiConstants.CONTRACT_VERSION));
        }

        onStage.accept("declarations");
        Map<String, String> scripts = bundle.getScripts() == null ? Map.of() : bundle.getScripts();
        List<ExtensionDeclarationDTO> declarations = bundle.getDeclarations() == null
                ? List.of() : bundle.getDeclarations();
        if (scripts.isEmpty()) {
            failures.add(failure("declarations", "a revision needs at least one script"));
        }
        validateDeclarations(declarations, scripts, failures);

        if (failures.isEmpty()) {
            onStage.accept("compile");
            compile(scripts, failures);
        }
        return failures;
    }

    private void validateDeclarations(List<ExtensionDeclarationDTO> declarations, Map<String, String> scripts,
                                      List<ValidationReportDTO.ValidationFailureDTO> failures) {
        Map<String, Set<String>> httpEndpointsByPath = new HashMap<>();
        for (ExtensionDeclarationDTO declaration : declarations) {
            if (declaration.getType() == null
                    || PluginContractCatalog.archetype(declaration.getType()).isEmpty()) {
                failures.add(failure("declarations", "unknown extension type " + declaration.getType()));
                continue;
            }
            if (StringUtils.isBlank(declaration.getScript()) || StringUtils.isBlank(declaration.getMethod())) {
                failures.add(failure("declarations", declaration.getType() + " needs both script and method"));
                continue;
            }
            if (!scripts.containsKey(declaration.getScript())) {
                failures.add(failure("declarations", declaration.getType() + " references unknown script '"
                        + declaration.getScript() + "'"));
                continue;
            }
            String requiredMethod = PluginContractCatalog.archetype(declaration.getType())
                    .map(PluginContractCatalog.ArchetypeDescriptor::scriptMethod).orElse(null);
            if (requiredMethod != null && !requiredMethod.equals(declaration.getMethod())) {
                failures.add(failure("declarations", declaration.getType() + " requires script method '"
                        + requiredMethod + "' but declaration uses '" + declaration.getMethod() + "'"));
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
                case HTTP_ENDPOINT -> validateHttpEndpoint(declaration, httpEndpointsByPath, failures);
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

    private void validateHttpEndpoint(ExtensionDeclarationDTO declaration,
                                      Map<String, Set<String>> endpointsByPath,
                                      List<ValidationReportDTO.ValidationFailureDTO> failures) {
        if (StringUtils.isBlank(declaration.getPath()) || !declaration.getPath().startsWith("/")
                || declaration.getPath().endsWith("/")) {
            failures.add(failure("declarations", "HTTP_ENDPOINT path must start with '/' and not end with '/'"));
        } else if (isDuplicateHttpEndpoint(endpointsByPath, declaration)) {
            failures.add(failure("declarations", "duplicate HTTP endpoint "
                    + normalizeHttpMethod(declaration.getHttpMethod()) + " " + declaration.getPath()));
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

    private void compile(Map<String, String> scripts,
                         List<ValidationReportDTO.ValidationFailureDTO> failures) {
        // Sorted for deterministic failure order across JVM runs.
        for (Map.Entry<String, String> script : new TreeMap<>(scripts).entrySet()) {
            try {
                scriptEngine.compile(script.getValue());
            } catch (ScriptCompileException e) {
                failures.add(failure("compile", script.getKey() + ": " + e.getMessage()));
            }
        }
    }

    private ValidationReportDTO.ValidationFailureDTO failure(String stage, String message) {
        return new ValidationReportDTO.ValidationFailureDTO(stage, message);
    }
}
