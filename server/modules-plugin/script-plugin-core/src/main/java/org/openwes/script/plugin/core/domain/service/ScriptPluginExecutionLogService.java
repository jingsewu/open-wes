package org.openwes.script.plugin.core.domain.service;

import lombok.RequiredArgsConstructor;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginExecutionLog;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginExecutionLogRepository;
import org.springframework.stereotype.Service;

/**
 * Ingests execution-log reports from every node and enforces the capped-count retention
 * (spec §Data model: retention by capped count with periodic pruning).
 */
@Service
@RequiredArgsConstructor
public class ScriptPluginExecutionLogService {

    static final int RETENTION_CAP = 10_000;

    private final ScriptPluginExecutionLogRepository repository;

    public void report(ExecutionLogDTO dto) {
        ScriptPluginExecutionLog row = new ScriptPluginExecutionLog();
        row.setPluginKey(dto.getPluginKey());
        row.setRevisionId(dto.getRevisionId() == null ? 0L : dto.getRevisionId());
        row.setExtensionType(dto.getExtensionType());
        row.setSuccess(dto.isSuccess());
        row.setDurationMs(dto.getDurationMs());
        row.setErrorMessage(dto.getErrorMessage() == null ? null : dto.getErrorMessage().substring(0,
                Math.min(1024, dto.getErrorMessage().length())));
        row.setCreatedAt(dto.getCreatedAt() == null ? System.currentTimeMillis() : dto.getCreatedAt());
        repository.save(row);

        if (repository.countByPluginKey(dto.getPluginKey()) > RETENTION_CAP) {
            repository.pruneToKeep(dto.getPluginKey(), RETENTION_CAP);
        }
    }
}
