package org.openwes.script.plugin.runtime.registry;

import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;

import java.util.List;

/**
 * Node-side view of the management plane. `local` mode injects the management service bean
 * (wes hosts script-plugin-core); `remote` mode talks to it over Dubbo (station).
 */
public interface ScriptPluginApiAccessor {

    List<ScriptPluginBundleDTO> getEnabledPlugins(String applySystem);

    long getRegistryVersion(String applySystem);

    ScriptPluginBundleDTO getPendingValidation(String applySystem);

    void reportValidation(ValidationReportDTO report);

    void reportExecution(ExecutionLogDTO executionLog);

    void disable(String pluginKey, String reason);
}
