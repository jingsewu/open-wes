package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.plugin.api.ScriptPluginTestRunner;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.TEST_RUN;

@ExtendWith(MockitoExtension.class)
class ScriptPluginTestRunServiceTest {

    @Mock
    private ScriptPluginService pluginService;
    @Mock
    private PluginWorkbenchAccessService accessService;
    @Mock
    private ScriptPluginOperationAuditService auditService;
    @Mock
    private ScriptPluginTestRunner testRunner;

    private ScriptPluginTestRunService service;

    @BeforeEach
    void setUp() {
        service = new ScriptPluginTestRunService(pluginService, accessService, auditService, testRunner);
    }

    private ScriptPluginDTO plugin(String applySystem) {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        dto.setPluginKey("packing-wave");
        dto.setApplySystem(applySystem);
        dto.setCurrentRevisionId(11L);
        return dto;
    }

    private ScriptPluginBundleDTO bundle(String script, String method) {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        declaration.setScript(script);
        declaration.setMethod(method);
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("packing-wave");
        bundle.setRevisionId(11L);
        bundle.setDeclarations(List.of(declaration));
        return bundle;
    }

    private TestRunRequestDTO request() {
        TestRunRequestDTO request = new TestRunRequestDTO();
        request.setExtensionType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        request.setScript("Wave.groovy");
        request.setMethod("wave");
        return request;
    }

    @Test
    void run_executesDeclaredExtensionAndAuditsTheOutcome() {
        when(accessService.requireDeveloper("packing-wave", TEST_RUN)).thenReturn("alice");
        when(pluginService.detail("packing-wave")).thenReturn(plugin("wes"));
        when(pluginService.getBundle("packing-wave")).thenReturn(bundle("Wave.groovy", "wave"));
        TestRunResultDTO result = new TestRunResultDTO(true, "{}", false, null, 5);
        when(testRunner.run("packing-wave", bundle("Wave.groovy", "wave"), request()))
                .thenReturn(result);

        assertSame(result, service.run("packing-wave", request()));

        verify(auditService).recordAs("packing-wave", TEST_RUN, 11L, true,
                "test-run OUTBOUND_WAVE_WAVE_ACTION Wave.groovy#wave durationMs=5", "alice");
    }

    @Test
    void run_rejectsStationPluginsBeforeLoadingTheBundle() {
        when(accessService.requireDeveloper("packing-wave", TEST_RUN)).thenReturn("alice");
        when(pluginService.detail("packing-wave")).thenReturn(plugin("station"));

        assertThrows(CommonException.class, () -> service.run("packing-wave", request()));

        verify(pluginService, never()).getBundle(any());
        verifyNoInteractions(testRunner);
    }

    @Test
    void run_rejectsUndeclaredExtensionTuples() {
        when(accessService.requireDeveloper("packing-wave", TEST_RUN)).thenReturn("alice");
        when(pluginService.detail("packing-wave")).thenReturn(plugin("wes"));
        when(pluginService.getBundle("packing-wave")).thenReturn(bundle("Other.groovy", "wave"));

        assertThrows(CommonException.class, () -> service.run("packing-wave", request()));

        verifyNoInteractions(testRunner);
    }

    @Test
    void run_auditsRunnerFailuresAsUnsuccessful() {
        when(accessService.requireDeveloper("packing-wave", TEST_RUN)).thenReturn("alice");
        when(pluginService.detail("packing-wave")).thenReturn(plugin("wes"));
        when(pluginService.getBundle("packing-wave")).thenReturn(bundle("Wave.groovy", "wave"));
        when(testRunner.run("packing-wave", bundle("Wave.groovy", "wave"), request()))
                .thenReturn(new TestRunResultDTO(false, null, false, "boom", 5));

        service.run("packing-wave", request());

        verify(auditService).recordAs("packing-wave", TEST_RUN, 11L, false,
                "test-run OUTBOUND_WAVE_WAVE_ACTION Wave.groovy#wave durationMs=5", "alice");
    }
}
