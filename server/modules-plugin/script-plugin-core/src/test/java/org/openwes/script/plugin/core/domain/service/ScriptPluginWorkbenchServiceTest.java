package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginMetadataDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginOperationAuditDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
import org.openwes.script.plugin.core.exception.ScriptPluginWorkbenchErrorDescEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.ARCHIVE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.CONFIG_SAVE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.CREATE_DRAFT;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.DISABLE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.ENABLE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.METADATA_UPDATE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.ROLLBACK;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.SAVE_REVISION;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.VALIDATE;

@ExtendWith(MockitoExtension.class)
class ScriptPluginWorkbenchServiceTest {

    @Mock
    private ScriptPluginService pluginService;
    @Mock
    private PluginWorkbenchAccessService accessService;
    @Mock
    private ScriptPluginOperationAuditService auditService;

    private ScriptPluginWorkbenchService workbench;

    @BeforeEach
    void setUp() {
        workbench = new ScriptPluginWorkbenchService(pluginService, accessService, auditService);
    }

    private ScriptPluginDTO pluginDto() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        dto.setPluginKey("packing-wave");
        dto.setName("Packing status rule");
        dto.setDescription("");
        dto.setApplyModule("");
        dto.setCurrentRevisionId(11L);
        return dto;
    }

    @Test
    void list_requiresDeveloperRole() {
        when(pluginService.list()).thenReturn(List.of(pluginDto()));

        assertTrue(workbench.list().size() == 1);
        verify(accessService).requireDeveloper();
    }

    @Test
    void detail_requiresDeveloperRole() {
        ScriptPluginDTO dto = pluginDto();
        when(accessService.requireDeveloper()).thenReturn("alice");
        when(pluginService.detail("packing-wave")).thenReturn(dto);

        assertSame(dto, workbench.detail("packing-wave"));
    }

    @Test
    void createDraft_authorizesAndAudits() {
        ScriptPluginDTO request = pluginDto();
        ScriptPluginDTO created = pluginDto();
        when(accessService.requireDeveloper("packing-wave", CREATE_DRAFT)).thenReturn("alice");
        when(pluginService.createDraft(request)).thenReturn(created);

        assertSame(created, workbench.createDraft(request));
        verify(auditService).recordAs(eq("packing-wave"), eq(CREATE_DRAFT), isNull(),
                eq(true), anyString(), eq("alice"));
    }

    @Test
    void saveRevision_passesActorAndAuditsRevisionId() {
        ScriptPluginRevisionDTO request = new ScriptPluginRevisionDTO();
        ScriptPluginRevisionDTO saved = new ScriptPluginRevisionDTO();
        saved.setId(55L);
        saved.setRevisionNo(4);
        when(accessService.requireDeveloper("packing-wave", SAVE_REVISION)).thenReturn("alice");
        when(pluginService.saveRevision("packing-wave", request, RevisionOriginEnum.MANUAL,
                null, null, "alice")).thenReturn(saved);

        assertSame(saved, workbench.saveRevision("packing-wave", request));
        verify(auditService).recordAs("packing-wave", SAVE_REVISION, 55L, true, "revision 4", "alice");
    }

    @Test
    void validate_auditsPassedReport() {
        ValidationReportDTO report = ValidationReportDTO.passed("packing-wave", 11L, "wes");
        when(accessService.requireDeveloper()).thenReturn("alice");
        when(pluginService.validate("packing-wave")).thenReturn(report);

        assertSame(report, workbench.validate("packing-wave"));
        verify(auditService).recordAs("packing-wave", VALIDATE, 11L, true, "validation passed", "alice");
    }

    @Test
    void validate_auditsFailedReport() {
        ValidationReportDTO report = ValidationReportDTO.failed(
                "packing-wave", 11L, "wes", "compile", "unexpected token");
        when(accessService.requireDeveloper()).thenReturn("alice");
        when(pluginService.validate("packing-wave")).thenReturn(report);

        workbench.validate("packing-wave");

        verify(auditService).recordAs("packing-wave", VALIDATE, 11L, false,
                "validation failed at stage(s): compile", "alice");
    }

    @Test
    void enable_requiresAdminAndAuditsSuccess() {
        when(accessService.requireAdmin("packing-wave", ENABLE)).thenReturn("admin");
        when(pluginService.enable("packing-wave")).thenReturn(pluginDto());

        workbench.enable("packing-wave");

        verify(auditService).recordAs("packing-wave", ENABLE, 11L, true,
                "enabled revision 11", "admin");
    }

    @Test
    void enable_failureIsAuditedThenRethrown() {
        when(accessService.requireAdmin("packing-wave", ENABLE)).thenReturn("admin");
        when(pluginService.enable("packing-wave")).thenThrow(new CommonException("validation failed"));

        assertThrows(CommonException.class, () -> workbench.enable("packing-wave"));

        verify(auditService).recordAs("packing-wave", ENABLE, null, false,
                "enable failed: CommonException", "admin");
    }

    @Test
    void enable_developerRoleIsRejectedBeforeCallingTheDomainService() {
        when(accessService.requireAdmin("packing-wave", ENABLE))
                .thenThrow(new WmsException(
                        ScriptPluginWorkbenchErrorDescEnum.ADMIN_ROLE_REQUIRED));

        assertThrows(WmsException.class, () -> workbench.enable("packing-wave"));

        verifyNoInteractions(pluginService);
    }

    @Test
    void disable_defaultsBlankReasonAndAudits() {
        when(accessService.requireAdmin("packing-wave", DISABLE)).thenReturn("admin");
        when(pluginService.disable("packing-wave", "disabled by user")).thenReturn(pluginDto());

        workbench.disable("packing-wave", " ");

        verify(auditService).recordAs("packing-wave", DISABLE, 11L, true,
                "disabled: disabled by user", "admin");
    }

    @Test
    void disableBySystem_skipsAuthorizationAndAuditsSystem() {
        when(pluginService.disable("packing-wave", "host shutdown")).thenReturn(pluginDto());

        workbench.disableBySystem("packing-wave", "host shutdown");

        verifyNoInteractions(accessService);
        verify(auditService).recordAs("packing-wave", DISABLE, 11L, true,
                "disabled by system: host shutdown", "system");
    }

    @Test
    void archive_andRollback_requireAdminAndAudit() {
        when(accessService.requireAdmin("packing-wave", ARCHIVE)).thenReturn("admin");
        when(accessService.requireAdmin("packing-wave", ROLLBACK)).thenReturn("admin");
        when(pluginService.archive("packing-wave")).thenReturn(pluginDto());
        when(pluginService.rollback("packing-wave", 7L)).thenReturn(pluginDto());

        workbench.archive("packing-wave");
        workbench.rollback("packing-wave", 7L);

        verify(auditService).recordAs("packing-wave", ARCHIVE, 11L, true, "archived", "admin");
        verify(auditService).recordAs("packing-wave", ROLLBACK, 7L, true,
                "rolled back to revision 7", "admin");
    }

    @Test
    void metadataUpdate_auditsOnlyChangedFieldNames() {
        ScriptPluginDTO before = pluginDto();
        before.setDescription("old");
        ScriptPluginDTO after = pluginDto();
        after.setDescription("new");
        after.setApplyModule("mod");
        ScriptPluginMetadataDTO request = new ScriptPluginMetadataDTO();
        request.setName("Packing status rule");
        request.setDescription("new");
        request.setApplyModule("mod");
        when(accessService.requireDeveloper("packing-wave", METADATA_UPDATE)).thenReturn("alice");
        when(pluginService.detail("packing-wave")).thenReturn(before);
        when(pluginService.updateMetadata("packing-wave", "Packing status rule", "new", "mod"))
                .thenReturn(after);

        assertSame(after, workbench.updateMetadata("packing-wave", request));

        verify(auditService).recordAs("packing-wave", METADATA_UPDATE, null, true,
                "changed fields: description, applyModule", "alice");
    }

    @Test
    void auditPages_areGatedByRole() {
        PageResultDTO<ScriptPluginOperationAuditDTO> page = PageResultDTO.of(List.of(), 0, 1, 20);
        when(auditService.pageForPlugin("packing-wave", 1, 20)).thenReturn(page);
        when(auditService.pageAll(1, 20)).thenReturn(page);

        workbench.auditForPlugin("packing-wave", 1, 20);
        workbench.allAudit(1, 20);

        verify(accessService).requireDeveloper();
        verify(accessService).requireAdmin();
    }
}
