package org.openwes.script.plugin.core.controller;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ExecutionStatsDTO;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.core.domain.service.ScriptPluginExecutionQueryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginExecutionControllerTest {

    private final ScriptPluginExecutionQueryService queryService =
            mock(ScriptPluginExecutionQueryService.class);
    private final ScriptPluginExecutionController controller =
            new ScriptPluginExecutionController(queryService);

    @Test
    void executions_delegatesAllFilters() {
        PageResultDTO<ExecutionLogDTO> page = PageResultDTO.of(List.of(), 0, 1, 20);
        when(queryService.page("packing-wave", 1, 20, true,
                ExtensionType.OUTBOUND_WAVE_WAVE_ACTION)).thenReturn(page);

        assertEquals(page, controller.executions("packing-wave", 1, 20, true,
                ExtensionType.OUTBOUND_WAVE_WAVE_ACTION).getData());
    }

    @Test
    void stats_delegatesHours() {
        ExecutionStatsDTO stats = new ExecutionStatsDTO(0, 0, 0, 0, 0, List.of());
        when(queryService.stats("packing-wave", 48)).thenReturn(stats);

        assertEquals(stats, controller.stats("packing-wave", 48).getData());
        verify(queryService).stats("packing-wave", 48);
    }
}
