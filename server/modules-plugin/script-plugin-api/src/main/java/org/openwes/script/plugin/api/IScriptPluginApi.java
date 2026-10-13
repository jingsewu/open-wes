package org.openwes.script.plugin.api;

import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;

import java.util.List;

/**
 * Management-plane API consumed by every node's script-plugin runtime.
 * Provided by the wes node (script-plugin-core is hosted there).
 */
public interface IScriptPluginApi {

    /** All ENABLED bundles for one app, ordered by pluginKey for deterministic materialization. */
    List<ScriptPluginBundleDTO> getEnabledPlugins(String applySystem);

    /** Monotonic registry stamp for one app (max plugin updateTime); used by the periodic safety poll. */
    long getRegistryVersion(String applySystem);

    /** The validation request waiting for this app, or null. Cleared when the node reports back. */
    ScriptPluginBundleDTO getPendingValidation(String applySystem);

    void reportValidation(ValidationReportDTO report);

    void reportExecution(ExecutionLogDTO executionLog);

    /** Runtime-triggered disable (e.g. cron auto-disable after repeated failures). */
    void disable(String pluginKey, String reason);
}
