package org.openwes.script.plugin.core.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.user.AuthConstants;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginMetadataDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginOperationAuditDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.ARCHIVE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.CREATE_DRAFT;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.DISABLE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.ENABLE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.METADATA_UPDATE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.ROLLBACK;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.SAVE_REVISION;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.VALIDATE;

/**
 * The management-plane use-case boundary. Every operation authorizes first and audits the outcome;
 * controllers stay thin and the domain service stays free of user/role concerns.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScriptPluginWorkbenchService {

    private final ScriptPluginService pluginService;
    private final PluginWorkbenchAccessService accessService;
    private final ScriptPluginOperationAuditService auditService;

    public List<ScriptPluginDTO> list() {
        accessService.requireDeveloper();
        return pluginService.list();
    }

    public ScriptPluginDTO detail(String pluginKey) {
        accessService.requireDeveloper();
        return pluginService.detail(pluginKey);
    }

    public List<ScriptPluginRevisionDTO> revisions(String pluginKey) {
        accessService.requireDeveloper();
        return pluginService.revisions(pluginKey);
    }

    public ScriptPluginDTO createDraft(ScriptPluginDTO dto) {
        String actor = accessService.requireDeveloper(dto.getPluginKey(), CREATE_DRAFT);
        ScriptPluginDTO created = pluginService.createDraft(dto);
        auditService.recordAs(created.getPluginKey(), CREATE_DRAFT, null, true,
                "draft created", actor);
        return created;
    }

    public ScriptPluginRevisionDTO saveRevision(String pluginKey, ScriptPluginRevisionDTO dto) {
        String actor = accessService.requireDeveloper(pluginKey, SAVE_REVISION);
        ScriptPluginRevisionDTO saved = pluginService.saveRevision(pluginKey, dto,
                RevisionOriginEnum.MANUAL, null, null, actor);
        auditService.recordAs(pluginKey, SAVE_REVISION, saved.getId(), true,
                "revision " + saved.getRevisionNo(), actor);
        return saved;
    }

    public ValidationReportDTO validate(String pluginKey) {
        String actor = accessService.requireDeveloper();
        try {
            ValidationReportDTO report = pluginService.validate(pluginKey);
            auditService.recordAs(pluginKey, VALIDATE, report.getRevisionId(), report.isPassed(),
                    report.isPassed() ? "validation passed"
                            : "validation failed at stage(s): " + failureStages(report),
                    actor);
            return report;
        } catch (RuntimeException e) {
            auditService.recordAs(pluginKey, VALIDATE, null, false,
                    "validation failed before a report was produced", actor);
            throw e;
        }
    }

    public ScriptPluginDTO enable(String pluginKey) {
        String actor = accessService.requireAdmin(pluginKey, ENABLE);
        try {
            ScriptPluginDTO enabled = pluginService.enable(pluginKey);
            auditService.recordAs(pluginKey, ENABLE, enabled.getCurrentRevisionId(), true,
                    "enabled revision " + enabled.getCurrentRevisionId(), actor);
            return enabled;
        } catch (RuntimeException e) {
            auditService.recordAs(pluginKey, ENABLE, null, false,
                    "enable failed: " + e.getClass().getSimpleName(), actor);
            throw e;
        }
    }

    public ScriptPluginDTO disable(String pluginKey, String reason) {
        String actor = accessService.requireAdmin(pluginKey, DISABLE);
        String effectiveReason = StringUtils.defaultIfBlank(reason, "disabled by user");
        ScriptPluginDTO disabled = pluginService.disable(pluginKey, effectiveReason);
        auditService.recordAs(pluginKey, DISABLE, disabled.getCurrentRevisionId(), true,
                "disabled: " + effectiveReason, actor);
        return disabled;
    }

    /** Internal path used by the Dubbo API (for example the cron circuit breaker); no user role. */
    public ScriptPluginDTO disableBySystem(String pluginKey, String reason) {
        String effectiveReason = StringUtils.defaultIfBlank(reason, "disabled by system");
        ScriptPluginDTO disabled = pluginService.disable(pluginKey, effectiveReason);
        auditService.recordAs(pluginKey, DISABLE, disabled.getCurrentRevisionId(), true,
                "disabled by system: " + effectiveReason, AuthConstants.USERNAME_SYSTEM);
        return disabled;
    }

    public ScriptPluginDTO archive(String pluginKey) {
        String actor = accessService.requireAdmin(pluginKey, ARCHIVE);
        ScriptPluginDTO archived = pluginService.archive(pluginKey);
        auditService.recordAs(pluginKey, ARCHIVE, archived.getCurrentRevisionId(), true,
                "archived", actor);
        return archived;
    }

    public ScriptPluginDTO rollback(String pluginKey, long revisionId) {
        String actor = accessService.requireAdmin(pluginKey, ROLLBACK);
        ScriptPluginDTO rolledBack = pluginService.rollback(pluginKey, revisionId);
        auditService.recordAs(pluginKey, ROLLBACK, revisionId, true,
                "rolled back to revision " + revisionId, actor);
        return rolledBack;
    }

    public ScriptPluginDTO updateMetadata(String pluginKey, ScriptPluginMetadataDTO dto) {
        String actor = accessService.requireDeveloper(pluginKey, METADATA_UPDATE);
        ScriptPluginDTO before = pluginService.detail(pluginKey);
        ScriptPluginDTO updated = pluginService.updateMetadata(pluginKey,
                dto.getName(), dto.getDescription(), dto.getApplyModule());
        List<String> changed = new ArrayList<>();
        if (!Objects.equals(before.getName(), updated.getName())) {
            changed.add("name");
        }
        if (!Objects.equals(normalize(before.getDescription()), normalize(updated.getDescription()))) {
            changed.add("description");
        }
        if (!Objects.equals(normalize(before.getApplyModule()), normalize(updated.getApplyModule()))) {
            changed.add("applyModule");
        }
        auditService.recordAs(pluginKey, METADATA_UPDATE, null, true,
                changed.isEmpty() ? "no field changed" : "changed fields: " + String.join(", ", changed),
                actor);
        return updated;
    }

    public PageResultDTO<ScriptPluginOperationAuditDTO> auditForPlugin(String pluginKey,
                                                                       int page, int pageSize) {
        accessService.requireDeveloper();
        return auditService.pageForPlugin(pluginKey, page, pageSize);
    }

    public PageResultDTO<ScriptPluginOperationAuditDTO> allAudit(int page, int pageSize) {
        accessService.requireAdmin();
        return auditService.pageAll(page, pageSize);
    }

    private static String failureStages(ValidationReportDTO report) {
        if (report.getFailures() == null || report.getFailures().isEmpty()) {
            return "unknown";
        }
        return report.getFailures().stream()
                .map(ValidationReportDTO.ValidationFailureDTO::getStage)
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
