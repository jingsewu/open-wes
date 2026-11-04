package org.openwes.script.plugin.core.domain.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ExecutionStatsDTO;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginExecutionLog;
import org.openwes.script.plugin.core.domain.repository.ExecutionTypeStatsView;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginExecutionLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScriptPluginExecutionQueryService {

    static final int MAX_PAGE_SIZE = 100;
    static final int MAX_HOURS = 720;

    private final ScriptPluginExecutionLogRepository repository;
    private final PluginWorkbenchAccessService accessService;

    public PageResultDTO<ExecutionLogDTO> page(String pluginKey, int page, int pageSize,
                                               Boolean success, ExtensionType extensionType) {
        accessService.requireDeveloper();
        int normalizedPage = Math.max(page, 1);
        int normalizedSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(normalizedPage - 1, normalizedSize,
                Sort.by(Sort.Direction.DESC, "id"));
        Specification<ScriptPluginExecutionLog> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("pluginKey"), pluginKey));
            if (success != null) {
                predicates.add(cb.equal(root.get("success"), success));
            }
            if (extensionType != null) {
                predicates.add(cb.equal(root.get("extensionType"), extensionType));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Page<ScriptPluginExecutionLog> rows = repository.findAll(specification, pageable);
        return PageResultDTO.of(rows.getContent().stream().map(this::toDto).toList(),
                rows.getTotalElements(), normalizedPage, normalizedSize);
    }

    public ExecutionStatsDTO stats(String pluginKey, int hours) {
        accessService.requireDeveloper();
        long since = System.currentTimeMillis() - normalizeHours(hours) * 3_600_000L;
        List<ExecutionTypeStatsView> rows = repository.statsByType(pluginKey, since);

        long total = rows.stream().mapToLong(ExecutionTypeStatsView::getTotal).sum();
        long failed = rows.stream().mapToLong(ExecutionTypeStatsView::getFailed).sum();
        double weightedAverage = rows.stream()
                .mapToDouble(row -> safe(row.getAvgDurationMs()) * row.getTotal()).sum();
        long avg = total == 0 ? 0 : Math.round(weightedAverage / total);
        long max = rows.stream()
                .map(row -> row.getMaxDurationMs() == null ? 0L : row.getMaxDurationMs())
                .mapToLong(Long::longValue).max().orElse(0);
        double failureRate = total == 0 ? 0 : Math.round(failed * 10_000.0 / total) / 100.0;
        List<ExecutionStatsDTO.TypeStats> byType = rows.stream()
                .map(row -> new ExecutionStatsDTO.TypeStats(row.getExtensionType(), row.getTotal(),
                        row.getFailed(), Math.round(safe(row.getAvgDurationMs())),
                        row.getMaxDurationMs() == null ? 0 : row.getMaxDurationMs()))
                .toList();
        return new ExecutionStatsDTO(total, failed, failureRate, avg, max, byType);
    }

    static int normalizeHours(int hours) {
        return Math.min(Math.max(hours, 1), MAX_HOURS);
    }

    private static double safe(Double value) {
        return value == null ? 0 : value;
    }

    private ExecutionLogDTO toDto(ScriptPluginExecutionLog row) {
        ExecutionLogDTO dto = new ExecutionLogDTO();
        dto.setId(row.getId());
        dto.setPluginKey(row.getPluginKey());
        dto.setRevisionId(row.getRevisionId());
        dto.setExtensionType(row.getExtensionType());
        dto.setSuccess(row.isSuccess());
        dto.setDurationMs(row.getDurationMs());
        dto.setErrorMessage(row.getErrorMessage());
        dto.setCreatedAt(row.getCreatedAt());
        return dto;
    }
}
