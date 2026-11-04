package org.openwes.script.plugin.core.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.openwes.script.plugin.api.IScriptPluginApi;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.core.domain.service.ScriptPluginExecutionLogService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginWorkbenchService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@DubboService
@RequiredArgsConstructor
@Slf4j
public class ScriptPluginApiImpl implements IScriptPluginApi {

    private final ScriptPluginService scriptPluginService;
    private final ScriptPluginExecutionLogService executionLogService;
    private final ScriptPluginWorkbenchService workbenchService;

    @Override
    public List<ScriptPluginBundleDTO> getEnabledPlugins(String applySystem) {
        return scriptPluginService.getEnabledBundles(applySystem);
    }

    @Override
    public long getRegistryVersion(String applySystem) {
        return scriptPluginService.getRegistryVersion(applySystem);
    }

    @Override
    public ScriptPluginBundleDTO getPendingValidation(String applySystem) {
        return scriptPluginService.findPendingValidation(applySystem);
    }

    @Override
    public void reportValidation(ValidationReportDTO report) {
        scriptPluginService.reportValidation(report);
    }

    @Override
    public void reportExecution(ExecutionLogDTO executionLog) {
        try {
            executionLogService.report(executionLog);
        } catch (Exception e) {
            // log shipping must never break the plugin invocation path on the reporting node
            log.error("failed to persist execution log for {}", executionLog.getPluginKey(), e);
        }
    }

    @Override
    public void disable(String pluginKey, String reason) {
        workbenchService.disableBySystem(pluginKey, reason);
    }
}
