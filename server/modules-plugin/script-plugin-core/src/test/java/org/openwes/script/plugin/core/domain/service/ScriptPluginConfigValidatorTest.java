package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.Test;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScriptPluginConfigValidatorTest {

    private ConfigFieldDTO field(String name, String type, boolean required) {
        ConfigFieldDTO field = new ConfigFieldDTO();
        field.setField(name);
        field.setType(type);
        field.setRequired(required);
        return field;
    }

    @Test
    void validValues_pass() {
        List<ConfigFieldDTO> schema = List.of(
                field("maxPerWave", "INT", true),
                field("threshold", "NUMBER", false),
                field("enabled", "BOOLEAN", false),
                field("label", "STRING", false));

        assertDoesNotThrow(() -> ScriptPluginConfigValidator.validate(schema,
                Map.of("maxPerWave", 20, "threshold", 1.5, "enabled", true, "label", "wave")));
    }

    @Test
    void unknownKey_isRejected() {
        CommonException e = assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(List.of(field("known", "STRING", false)),
                        Map.of("other", "x")));

        assertEquals("unknown config field: other", e.getMessage());
    }

    @Test
    void missingRequiredField_isRejected() {
        CommonException e = assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(List.of(field("maxPerWave", "INT", true)),
                        Map.of()));

        assertEquals("config field 'maxPerWave' is required", e.getMessage());
    }

    @Test
    void intField_acceptsIntegralStringsAndRejectsFractions() {
        List<ConfigFieldDTO> schema = List.of(field("maxPerWave", "INT", false));

        assertDoesNotThrow(() -> ScriptPluginConfigValidator.validate(schema, Map.of("maxPerWave", "42")));
        assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(schema, Map.of("maxPerWave", 1.5)));
    }

    @Test
    void numberField_acceptsDecimalsAndRejectsText() {
        List<ConfigFieldDTO> schema = List.of(field("threshold", "NUMBER", false));

        assertDoesNotThrow(() -> ScriptPluginConfigValidator.validate(schema, Map.of("threshold", "1.25")));
        assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(schema, Map.of("threshold", "abc")));
    }

    @Test
    void booleanField_acceptsOnlyTrueOrFalse() {
        List<ConfigFieldDTO> schema = List.of(field("enabled", "BOOLEAN", false));

        assertDoesNotThrow(() -> ScriptPluginConfigValidator.validate(schema, Map.of("enabled", "false")));
        assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(schema, Map.of("enabled", "yes")));
    }

    @Test
    void stringField_requiresText() {
        List<ConfigFieldDTO> schema = List.of(field("label", "STRING", false));

        assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(schema, Map.of("label", 5)));
    }

    @Test
    void unsupportedType_isRejectedAsSchemaError() {
        CommonException e = assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(List.of(field("when", "DATE", false)),
                        Map.of("when", "2026-10-23")));

        assertEquals("unsupported config field type 'DATE' for field 'when'", e.getMessage());
    }
}
