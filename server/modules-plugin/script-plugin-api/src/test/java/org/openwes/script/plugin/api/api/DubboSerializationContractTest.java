package org.openwes.script.plugin.api.api;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.IScriptPluginApi;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Dubbo's fastjson2 serializer rejects non-Serializable response types. Every DTO that can
 * cross {@link IScriptPluginApi} must therefore implement {@link Serializable}.
 */
class DubboSerializationContractTest {

    @Test
    void everyDtoCrossingTheStationHandshakeIsSerializable() {
        assertSerializable(ScriptPluginBundleDTO.class);
        assertSerializable(ExtensionDeclarationDTO.class);
        assertSerializable(ConfigFieldDTO.class);
        assertSerializable(ValidationReportDTO.class);
        assertSerializable(ValidationReportDTO.ValidationFailureDTO.class);
        assertSerializable(ExecutionLogDTO.class);
    }

    private void assertSerializable(Class<?> type) {
        assertTrue(Serializable.class.isAssignableFrom(type), type.getSimpleName() + " must implement Serializable");
    }
}
