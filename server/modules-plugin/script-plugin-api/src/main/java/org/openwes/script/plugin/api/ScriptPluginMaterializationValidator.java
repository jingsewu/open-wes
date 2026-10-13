package org.openwes.script.plugin.api;

import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;

/**
 * Implemented by the script-plugin runtime on each node (Plan 1b). The core orchestrator picks the
 * validator whose targetApp matches the plugin's applySystem and runs it in-process; for remote
 * apps the request travels over the Redis management channel and the node reports back via
 * IScriptPluginApi.reportValidation.
 */
public interface ScriptPluginMaterializationValidator {

    String targetApp();

    ValidationReportDTO validate(ScriptPluginBundleDTO bundle);
}
