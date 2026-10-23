package org.openwes.script.plugin.core.domain.service;

import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stateless config-schema validation. Error messages name the field and expected type; they never
 * echo submitted values, which may be sensitive.
 */
public final class ScriptPluginConfigValidator {

    private ScriptPluginConfigValidator() {
    }

    public static void validate(List<ConfigFieldDTO> schema, Map<String, Object> values) {
        List<ConfigFieldDTO> fields = schema == null ? List.of() : schema;
        Map<String, Object> submitted = values == null ? Map.of() : values;
        Set<String> known = new HashSet<>();
        for (ConfigFieldDTO field : fields) {
            if (field == null || StringUtils.isBlank(field.getField())) {
                throw new CommonException("config schema contains a field without a name");
            }
            if (!known.add(field.getField())) {
                throw new CommonException("config schema contains duplicate field '" + field.getField() + "'");
            }
        }
        for (String key : submitted.keySet()) {
            if (!known.contains(key)) {
                throw new CommonException("unknown config field: " + key);
            }
        }
        for (ConfigFieldDTO field : fields) {
            Object value = submitted.get(field.getField());
            if (field.isRequired() && value == null) {
                throw new CommonException("config field '" + field.getField() + "' is required");
            }
            if (value != null) {
                validateType(field, value);
            }
        }
    }

    private static void validateType(ConfigFieldDTO field, Object value) {
        String type = StringUtils.upperCase(StringUtils.trimToEmpty(field.getType()));
        boolean valid = switch (type) {
            case "STRING" -> value instanceof CharSequence;
            case "INT", "INTEGER" -> isInteger(value);
            case "NUMBER" -> isNumber(value);
            case "BOOLEAN" -> isBoolean(value);
            default -> throw new CommonException("unsupported config field type '"
                    + field.getType() + "' for field '" + field.getField() + "'");
        };
        if (!valid) {
            throw new CommonException("config field '" + field.getField() + "' must be a " + type);
        }
    }

    private static boolean isInteger(Object value) {
        try {
            new BigDecimal(value.toString()).intValueExact();
            return true;
        } catch (NumberFormatException | ArithmeticException e) {
            return false;
        }
    }

    private static boolean isNumber(Object value) {
        try {
            new BigDecimal(value.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isBoolean(Object value) {
        if (value instanceof Boolean) {
            return true;
        }
        if (!(value instanceof String text)) {
            return false;
        }
        return "true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text);
    }
}
