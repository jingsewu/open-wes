package org.openwes.ai.plugin.domain.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.openwes.ai.plugin.domain.model.AiDraftDocument;
import org.openwes.ai.plugin.exception.AiDraftParseException;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns raw model text into a draft document. Model output is untrusted: tolerates fences and
 * prose, rejects everything the static validator or the target app would reject, and reports one
 * actionable message per problem so the retry prompt can fix it.
 */
@Component
public class AiDraftDocumentParser {

    private static final Pattern FENCED_BLOCK =
            Pattern.compile("```[A-Za-z0-9_-]*\\s*\\n?(.*?)```", Pattern.DOTALL);
    private static final Set<String> HTTP_METHODS =
            Set.of("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public AiDraftDocument parse(String raw, String expectedApp) {
        AiDraftDocument document = readDocument(raw);
        validate(document, expectedApp);
        return document;
    }

    private AiDraftDocument readDocument(String raw) {
        if (StringUtils.isBlank(raw)) {
            throw new AiDraftParseException("model returned an empty response");
        }
        AiDraftDocument direct = tryRead(raw.trim());
        if (direct != null) {
            return direct;
        }

        Matcher matcher = FENCED_BLOCK.matcher(raw);
        while (matcher.find()) {
            AiDraftDocument fenced = tryRead(matcher.group(1).trim());
            if (fenced != null) {
                return fenced;
            }
        }

        int first = raw.indexOf('{');
        int last = raw.lastIndexOf('}');
        if (first >= 0 && last > first) {
            try {
                return objectMapper.readValue(raw.substring(first, last + 1), AiDraftDocument.class);
            } catch (Exception e) {
                throw new AiDraftParseException("model output is not a valid draft JSON");
            }
        }
        throw new AiDraftParseException("model output contains no JSON object that matches the draft shape");
    }

    private AiDraftDocument tryRead(String candidate) {
        try {
            return objectMapper.readValue(candidate, AiDraftDocument.class);
        } catch (Exception e) {
            return null;
        }
    }

    private void validate(AiDraftDocument document, String expectedApp) {
        // Nulls can only come from an explicit JSON null; normalize so the checks below are total.
        if (document.getMeta() == null) {
            document.setMeta(new AiDraftDocument.Meta());
        }
        if (document.getScripts() == null) {
            document.setScripts(new LinkedHashMap<>());
        }
        if (document.getExtensions() == null) {
            document.setExtensions(new ArrayList<>());
        }
        if (document.getConfigSchema() == null) {
            document.setConfigSchema(new ArrayList<>());
        }

        List<String> errors = new ArrayList<>();
        if (document.getScripts().isEmpty()) {
            errors.add("scripts must contain at least one script");
        } else {
            document.getScripts().forEach((name, source) -> {
                if (StringUtils.isBlank(name)) {
                    errors.add("script file names must not be blank");
                }
                if (StringUtils.isBlank(source)) {
                    errors.add("script '" + name + "' has no source");
                }
            });
        }
        if (document.getExtensions().isEmpty()) {
            errors.add("extensions must contain at least one extension");
        }

        List<ExtensionType> allowed = PluginContractCatalog.typesForApp(expectedApp);
        if (allowed.isEmpty()) {
            errors.add("app '" + expectedApp + "' does not host script plugins");
        }

        for (ExtensionDeclarationDTO extension : document.getExtensions()) {
            validateExtension(extension, document, allowed, expectedApp, errors);
        }
        if (!errors.isEmpty()) {
            throw new AiDraftParseException(String.join("; ", errors));
        }

        if (StringUtils.isBlank(document.getMeta().getName())) {
            document.getMeta().setName("AI draft");
        }
        if (StringUtils.isBlank(document.getMeta().getChangeSummary())) {
            document.getMeta().setChangeSummary("AI generated draft");
        }
    }

    private void validateExtension(ExtensionDeclarationDTO extension, AiDraftDocument document,
                                   List<ExtensionType> allowed, String expectedApp, List<String> errors) {
        if (extension == null || extension.getType() == null) {
            errors.add("every extension needs a type");
            return;
        }
        if (!allowed.contains(extension.getType())) {
            errors.add("extension type " + extension.getType() + " is not supported for app " + expectedApp);
            return;
        }
        if (StringUtils.isBlank(extension.getScript()) || StringUtils.isBlank(extension.getMethod())) {
            errors.add(extension.getType() + " needs both script and method");
            return;
        }
        if (!document.getScripts().containsKey(extension.getScript())) {
            errors.add(extension.getType() + " references unknown script '" + extension.getScript() + "'");
            return;
        }
        switch (extension.getType()) {
            case REQUEST_VALIDATE, REQUEST_SUPPLY, REQUEST_AFTER_INVOKE, CUSTOM_REQUEST_INVOKE -> {
                if (StringUtils.isBlank(extension.getApiType())) {
                    errors.add(extension.getType() + " needs apiType");
                }
            }
            case CUSTOM_API -> {
                if (StringUtils.isBlank(extension.getCustomApiCode())) {
                    errors.add("CUSTOM_API needs customApiCode");
                }
            }
            case LIFECYCLE_LISTENER -> {
                if (StringUtils.isBlank(extension.getEntityName())
                        || !PluginContractCatalog.LIFECYCLE_ENTITY_NAMES.contains(extension.getEntityName())) {
                    errors.add("LIFECYCLE_LISTENER entityName must be one of "
                            + PluginContractCatalog.LIFECYCLE_ENTITY_NAMES + " but was '"
                            + extension.getEntityName() + "'");
                }
            }
            case HTTP_ENDPOINT -> {
                if (StringUtils.isBlank(extension.getHttpMethod())
                        || !HTTP_METHODS.contains(extension.getHttpMethod().toUpperCase(Locale.ROOT))) {
                    errors.add("HTTP_ENDPOINT needs httpMethod in " + HTTP_METHODS);
                }
                if (StringUtils.isBlank(extension.getPath()) || !extension.getPath().startsWith("/")) {
                    errors.add("HTTP_ENDPOINT path must start with '/'");
                }
            }
            case CRON -> {
                if (StringUtils.isBlank(extension.getCron())) {
                    errors.add("CRON needs a cron expression");
                }
            }
            default -> {
            }
        }
    }
}
