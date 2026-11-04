package org.openwes.script.plugin.core.controller;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;
import org.openwes.script.plugin.core.domain.service.ScriptPluginTestRunService;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginTestRunControllerTest {

    private final ScriptPluginTestRunService service = mock(ScriptPluginTestRunService.class);
    private final ScriptPluginTestRunController controller = new ScriptPluginTestRunController(service);

    @Test
    void run_delegatesToTheService() {
        TestRunRequestDTO request = new TestRunRequestDTO();
        TestRunResultDTO result = new TestRunResultDTO(true, "{}", false, null, 3);
        when(service.run("packing-wave", request)).thenReturn(result);

        assertSame(result, controller.run("packing-wave", request).getData());
        verify(service).run("packing-wave", request);
    }
}
