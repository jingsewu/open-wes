package org.openwes.ai.plugin.domain.service;

import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiGeneration;
import org.openwes.ai.plugin.domain.enums.AiGenerationStatusEnum;
import org.openwes.ai.plugin.domain.repository.ScriptPluginAiGenerationRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiGenerationAuditServiceTest {

    private final ScriptPluginAiGenerationRepository repository =
            mock(ScriptPluginAiGenerationRepository.class);
    private final AiGenerationAuditService service = new AiGenerationAuditService(repository);

    @Test
    void start_createsARunningRow() {
        when(repository.save(any(ScriptPluginAiGeneration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ScriptPluginAiGeneration row = service.start("packing-wave", "alice", "GLM-4-air", "v1");

        assertEquals(AiGenerationStatusEnum.RUNNING, row.getStatus());
        assertEquals("packing-wave", row.getPluginKey());
        assertEquals("alice", row.getUserName());
        assertEquals(0, row.getAttempts());
        assertTrue(row.getCreatedAt() > 0);
    }

    @Test
    void succeed_completesTheRowWithUsage() {
        ScriptPluginAiGeneration row = ScriptPluginAiGeneration.start(
                "packing-wave", "alice", "GLM-4-air", "v1");
        row.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(row));
        when(repository.save(row)).thenReturn(row);

        service.succeed(1L, 2, 120, 80, 1500L);

        assertEquals(AiGenerationStatusEnum.SUCCEEDED, row.getStatus());
        assertEquals(2, row.getAttempts());
        assertEquals(120, row.getPromptTokens());
        assertEquals(80, row.getCompletionTokens());
        assertEquals(1500L, row.getDurationMs());
        assertTrue(row.getFinishedAt() > 0);
        verify(repository).save(row);
    }

    @Test
    void fail_truncatesTheErrorMessage() {
        ScriptPluginAiGeneration row = ScriptPluginAiGeneration.start(
                "packing-wave", "alice", "GLM-4-air", "v1");
        row.setId(2L);
        when(repository.findById(2L)).thenReturn(Optional.of(row));
        when(repository.save(row)).thenReturn(row);

        service.fail(2L, 3, "x".repeat(2000), 900L);

        assertEquals(AiGenerationStatusEnum.FAILED, row.getStatus());
        assertEquals(1024, row.getErrorMessage().length());
    }

    @Test
    void recent_mapsRowsInRepositoryOrder() {
        ScriptPluginAiGeneration row = ScriptPluginAiGeneration.start(
                "packing-wave", "alice", "GLM-4-air", "v1");
        row.setId(3L);
        when(repository.findTop50ByPluginKeyOrderByIdDesc("packing-wave")).thenReturn(List.of(row));

        var result = service.recent("packing-wave");

        assertEquals(1, result.size());
        assertEquals(3L, result.get(0).getId());
        assertEquals("RUNNING", result.get(0).getStatus());
    }
}
