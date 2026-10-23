package org.openwes.script.plugin.core.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.user.AuthConstants;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginOperationAuditDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginOperationAudit;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginOperationAuditRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScriptPluginOperationAuditService {

    static final int DETAIL_MAX_LENGTH = 1024;
    static final int OPERATOR_MAX_LENGTH = 64;
    static final int PLUGIN_KEY_MAX_LENGTH = 64;
    static final int MAX_PAGE_SIZE = 100;

    private final ScriptPluginOperationAuditRepository repository;

    public void record(String pluginKey, ScriptPluginAuditActionEnum action, Long revisionId,
                       boolean success, String detail) {
        recordAs(pluginKey, action, revisionId, success, detail, UserContext.getCurrentUser());
    }

    public void recordAs(String pluginKey, ScriptPluginAuditActionEnum action, Long revisionId,
                         boolean success, String detail, String actor) {
        String operator = StringUtils.defaultIfBlank(actor, AuthConstants.USERNAME_SYSTEM);
        try {
            repository.save(ScriptPluginOperationAudit.record(
                    StringUtils.defaultIfBlank(pluginKey, "unknown"),
                    action,
                    revisionId,
                    truncate(operator, OPERATOR_MAX_LENGTH),
                    success,
                    truncate(detail, DETAIL_MAX_LENGTH)));
        } catch (RuntimeException e) {
            // An audit failure must never mask the business result.
            log.warn("failed to record script plugin audit action {} for plugin {}",
                    action, pluginKey, e);
        }
    }

    public PageResultDTO<ScriptPluginOperationAuditDTO> pageForPlugin(String pluginKey,
                                                                      int page, int pageSize) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizePageSize(pageSize);
        Pageable pageable = PageRequest.of(normalizedPage - 1, normalizedSize);
        Page<ScriptPluginOperationAudit> rows =
                repository.findByPluginKeyOrderByIdDesc(pluginKey, pageable);
        return toPageResult(rows, normalizedPage, normalizedSize);
    }

    public PageResultDTO<ScriptPluginOperationAuditDTO> pageAll(int page, int pageSize) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizePageSize(pageSize);
        Pageable pageable = PageRequest.of(normalizedPage - 1, normalizedSize);
        Page<ScriptPluginOperationAudit> rows = repository.findAllByOrderByIdDesc(pageable);
        return toPageResult(rows, normalizedPage, normalizedSize);
    }

    private PageResultDTO<ScriptPluginOperationAuditDTO> toPageResult(
            Page<ScriptPluginOperationAudit> rows, int page, int pageSize) {
        return PageResultDTO.of(rows.getContent().stream().map(this::toDto).toList(),
                rows.getTotalElements(), page, pageSize);
    }

    private ScriptPluginOperationAuditDTO toDto(ScriptPluginOperationAudit audit) {
        return ScriptPluginOperationAuditDTO.builder()
                .id(audit.getId())
                .pluginKey(audit.getPluginKey())
                .action(audit.getAction())
                .revisionId(audit.getRevisionId())
                .operator(audit.getOperator())
                .success(audit.isSuccess())
                .detail(audit.getDetail())
                .createdAt(audit.getCreatedAt())
                .build();
    }

    static int normalizePage(int page) {
        return Math.max(page, 1);
    }

    static int normalizePageSize(int pageSize) {
        return Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
    }

    static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
