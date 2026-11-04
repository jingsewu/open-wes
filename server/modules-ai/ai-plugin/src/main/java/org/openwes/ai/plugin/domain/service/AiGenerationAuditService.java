package org.openwes.ai.plugin.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiGeneration;
import org.openwes.ai.plugin.domain.repository.ScriptPluginAiGenerationRepository;
import org.openwes.ai.plugin.dto.AiGenerationDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationAuditService {

    private final ScriptPluginAiGenerationRepository repository;

    public ScriptPluginAiGeneration start(String pluginKey, String userName,
                                          String model, String promptVersion) {
        return repository.save(ScriptPluginAiGeneration.start(pluginKey, userName, model, promptVersion));
    }

    public void recordAttempt(Long id, int attempts) {
        update(id, row -> row.recordAttempt(attempts));
    }

    public void succeed(Long id, int attempts, Integer promptTokens, Integer completionTokens, long durationMs) {
        update(id, row -> row.succeed(attempts, promptTokens, completionTokens, durationMs));
    }

    public void fail(Long id, int attempts, String errorMessage, long durationMs) {
        update(id, row -> row.fail(attempts, errorMessage, durationMs));
    }

    public void cancel(Long id, int attempts, long durationMs) {
        update(id, row -> row.cancel(attempts, durationMs));
    }

    public List<AiGenerationDTO> recent(String pluginKey) {
        return repository.findTop50ByPluginKeyOrderByIdDesc(pluginKey).stream()
                .map(this::toDto).toList();
    }

    /**
     * Audit updates are best-effort: a generation that succeeded must still return its revision if
     * the audit table is temporarily unavailable. Failures are logged, never swallowed silently.
     */
    private void update(Long id, java.util.function.Consumer<ScriptPluginAiGeneration> mutation) {
        if (id == null) {
            return;
        }
        try {
            repository.findById(id).ifPresent(row -> {
                mutation.accept(row);
                repository.save(row);
            });
        } catch (RuntimeException e) {
            log.error("failed to update AI generation audit row {}", id, e);
        }
    }

    private AiGenerationDTO toDto(ScriptPluginAiGeneration row) {
        return AiGenerationDTO.builder()
                .id(row.getId())
                .pluginKey(row.getPluginKey())
                .userName(row.getUserName())
                .model(row.getModel())
                .promptVersion(row.getPromptVersion())
                .status(row.getStatus().name())
                .attempts(row.getAttempts())
                .promptTokens(row.getPromptTokens())
                .completionTokens(row.getCompletionTokens())
                .durationMs(row.getDurationMs())
                .errorMessage(row.getErrorMessage())
                .createdAt(row.getCreatedAt())
                .finishedAt(row.getFinishedAt())
                .build();
    }
}
