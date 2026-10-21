package org.openwes.script.plugin.runtime.registry;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.IScriptPluginApi;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalScriptPluginApiAccessorTest {

    private final IScriptPluginApi api = mock(IScriptPluginApi.class);
    private final LocalScriptPluginApiAccessor accessor = new LocalScriptPluginApiAccessor(api);

    @Test
    void delegatesEveryManagementCall() {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("packing-wave");
        ValidationReportDTO report = ValidationReportDTO.passed("packing-wave", 11L, "wes");
        ExecutionLogDTO executionLog = new ExecutionLogDTO();
        when(api.getEnabledPlugins("wes")).thenReturn(List.of(bundle));
        when(api.getRegistryVersion("wes")).thenReturn(7L);
        when(api.getPendingValidation("wes")).thenReturn(bundle);

        assertEquals(List.of(bundle), accessor.getEnabledPlugins("wes"));
        assertEquals(7L, accessor.getRegistryVersion("wes"));
        assertEquals(bundle, accessor.getPendingValidation("wes"));
        accessor.reportValidation(report);
        accessor.reportExecution(executionLog);
        accessor.disable("packing-wave", "reason");

        verify(api).reportValidation(report);
        verify(api).reportExecution(executionLog);
        verify(api).disable("packing-wave", "reason");
    }
}
