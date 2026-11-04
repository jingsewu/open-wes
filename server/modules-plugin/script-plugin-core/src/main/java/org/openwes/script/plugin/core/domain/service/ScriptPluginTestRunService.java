package org.openwes.script.plugin.core.domain.service;

import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.ScriptPluginTestRunner;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.TEST_RUN;

@Service
@RequiredArgsConstructor
public class ScriptPluginTestRunService {

    private final ScriptPluginService pluginService;
    private final PluginWorkbenchAccessService accessService;
    private final ScriptPluginOperationAuditService auditService;
    private final ScriptPluginTestRunner testRunner;

    public TestRunResultDTO run(String pluginKey, TestRunRequestDTO request) {
        String actor = accessService.requireDeveloper(pluginKey, TEST_RUN);
        ScriptPluginDTO plugin = pluginService.detail(pluginKey);
        if (!ScriptPluginApiConstants.APP_WES.equals(plugin.getApplySystem())) {
            throw new CommonException("test-run is only available for wes plugins on this node");
        }
        ScriptPluginBundleDTO bundle = pluginService.getBundle(pluginKey);
        if (bundle == null) {
            throw new CommonException("plugin has no revision to test-run");
        }
        boolean declared = bundle.getDeclarations() != null && bundle.getDeclarations().stream()
                .anyMatch(declaration -> declaration.getType() == request.getExtensionType()
                        && Objects.equals(request.getScript(), declaration.getScript())
                        && Objects.equals(request.getMethod(), declaration.getMethod()));
        if (!declared) {
            throw new CommonException("the current revision does not declare this extension");
        }
        TestRunResultDTO result;
        try {
            result = testRunner.run(pluginKey, bundle, request);
        } catch (RuntimeException e) {
            auditService.recordAs(pluginKey, TEST_RUN, bundle.getRevisionId(), false,
                    "test-run " + request.getExtensionType() + " " + request.getScript()
                            + "#" + request.getMethod() + " failed: " + e.getClass().getSimpleName(),
                    actor);
            throw e;
        }
        auditService.recordAs(pluginKey, TEST_RUN, bundle.getRevisionId(), result.isSuccess(),
                "test-run " + request.getExtensionType() + " " + request.getScript()
                        + "#" + request.getMethod() + " durationMs=" + result.getDurationMs(), actor);
        return result;
    }
}
