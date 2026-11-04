package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginExecutionLogRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginExecutionLogServiceTest {

    private final ScriptPluginExecutionLogRepository repository = mock(ScriptPluginExecutionLogRepository.class);
    private final ScriptPluginExecutionLogService service = new ScriptPluginExecutionLogService(repository);

    private ExecutionLogDTO log() {
        ExecutionLogDTO dto = new ExecutionLogDTO();
        dto.setPluginKey("packing-wave");
        dto.setRevisionId(11L);
        dto.setExtensionType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        dto.setSuccess(true);
        dto.setDurationMs(5);
        return dto;
    }

    @Test
    void report_savesTheRow() {
        when(repository.countByPluginKey("packing-wave")).thenReturn(10L);

        service.report(log());

        verify(repository).save(any());
        verify(repository, never()).pruneToKeep(any(), anyInt());
    }

    @Test
    void report_prunesWhenCapExceeded() {
        when(repository.countByPluginKey("packing-wave")).thenReturn(10_001L);

        service.report(log());

        verify(repository).pruneToKeep(eq("packing-wave"), eq(10_000));
    }
}
