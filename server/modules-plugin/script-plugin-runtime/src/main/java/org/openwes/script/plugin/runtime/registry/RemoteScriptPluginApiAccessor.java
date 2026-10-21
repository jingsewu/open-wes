package org.openwes.script.plugin.runtime.registry;

import org.apache.dubbo.config.annotation.DubboReference;
import org.openwes.script.plugin.api.IScriptPluginApi;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "script-plugin.registry.mode", havingValue = "remote")
public class RemoteScriptPluginApiAccessor implements ScriptPluginApiAccessor {

    @DubboReference
    private IScriptPluginApi scriptPluginApi;

    @Override
    public List<ScriptPluginBundleDTO> getEnabledPlugins(String applySystem) {
        return scriptPluginApi.getEnabledPlugins(applySystem);
    }

    @Override
    public long getRegistryVersion(String applySystem) {
        return scriptPluginApi.getRegistryVersion(applySystem);
    }

    @Override
    public ScriptPluginBundleDTO getPendingValidation(String applySystem) {
        return scriptPluginApi.getPendingValidation(applySystem);
    }

    @Override
    public void reportValidation(ValidationReportDTO report) {
        scriptPluginApi.reportValidation(report);
    }

    @Override
    public void reportExecution(ExecutionLogDTO executionLog) {
        scriptPluginApi.reportExecution(executionLog);
    }

    @Override
    public void disable(String pluginKey, String reason) {
        scriptPluginApi.disable(pluginKey, reason);
    }
}
