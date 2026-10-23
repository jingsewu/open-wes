package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginOperationAuditDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginOperationAudit;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginOperationAuditRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScriptPluginOperationAuditServiceTest {

    @Mock
    private ScriptPluginOperationAuditRepository repository;

    private ScriptPluginOperationAuditService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new ScriptPluginOperationAuditService(repository);
    }

    @AfterEach
    void tearDown() {
        UserContext.removeUser();
    }

    @Test
    void recordAs_usesExplicitActorAndTruncatesColumns() {
        service.recordAs("packing-wave", ScriptPluginAuditActionEnum.SAVE_REVISION, 11L,
                true, "d".repeat(2000), "u".repeat(100));

        ArgumentCaptor<ScriptPluginOperationAudit> captor =
                ArgumentCaptor.forClass(ScriptPluginOperationAudit.class);
        verify(repository).save(captor.capture());
        ScriptPluginOperationAudit audit = captor.getValue();
        assertEquals("packing-wave", audit.getPluginKey());
        assertEquals(ScriptPluginAuditActionEnum.SAVE_REVISION, audit.getAction());
        assertEquals(11L, audit.getRevisionId());
        assertEquals(64, audit.getOperator().length());
        assertEquals(1024, audit.getDetail().length());
        assertEquals(true, audit.isSuccess());
    }

    @Test
    void record_usesCurrentUserFromContext() {
        UserContext.setAccount("alice");

        service.record("packing-wave", ScriptPluginAuditActionEnum.ENABLE, 11L, true, "enabled");

        ArgumentCaptor<ScriptPluginOperationAudit> captor =
                ArgumentCaptor.forClass(ScriptPluginOperationAudit.class);
        verify(repository).save(captor.capture());
        assertEquals("alice", captor.getValue().getOperator());
    }

    @Test
    void record_blankUserFallsBackToSystem() {
        service.record("packing-wave", ScriptPluginAuditActionEnum.DISABLE, 11L, true, "disabled");

        ArgumentCaptor<ScriptPluginOperationAudit> captor =
                ArgumentCaptor.forClass(ScriptPluginOperationAudit.class);
        verify(repository).save(captor.capture());
        assertEquals("system", captor.getValue().getOperator());
    }

    @Test
    void record_blankPluginKeyUsesUnknownPlaceholder() {
        service.recordAs(" ", ScriptPluginAuditActionEnum.CREATE_DRAFT, null,
                false, "missing role pluginDeveloper", "bob");

        ArgumentCaptor<ScriptPluginOperationAudit> captor =
                ArgumentCaptor.forClass(ScriptPluginOperationAudit.class);
        verify(repository).save(captor.capture());
        assertEquals("unknown", captor.getValue().getPluginKey());
        assertNull(captor.getValue().getRevisionId());
    }

    @Test
    void record_repositoryFailureNeverMasksTheBusinessOperation() {
        when(repository.save(any())).thenThrow(new IllegalStateException("db down"));

        assertDoesNotThrow(() -> service.record("packing-wave",
                ScriptPluginAuditActionEnum.ENABLE, 11L, true, "enabled"));
    }

    @Test
    void pageForPlugin_clampsPageAndPageSizeAndMapsRows() {
        ScriptPluginOperationAudit row = ScriptPluginOperationAudit.record(
                "packing-wave", ScriptPluginAuditActionEnum.ENABLE, 11L, "admin", true, "enabled");
        Page<ScriptPluginOperationAudit> page =
                new PageImpl<>(List.of(row), PageRequest.of(0, 100), 1);
        when(repository.findByPluginKeyOrderByIdDesc(eq("packing-wave"), any(Pageable.class)))
                .thenReturn(page);

        PageResultDTO<ScriptPluginOperationAuditDTO> result =
                service.pageForPlugin("packing-wave", 0, 1000);

        assertEquals(1, result.getPage());
        assertEquals(100, result.getPageSize());
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());
        assertEquals("admin", result.getItems().get(0).getOperator());
        assertEquals(ScriptPluginAuditActionEnum.ENABLE, result.getItems().get(0).getAction());
    }

    @Test
    void pageAll_returnsAllRows() {
        Page<ScriptPluginOperationAudit> page =
                new PageImpl<>(List.of(), PageRequest.of(1, 20), 0);
        when(repository.findAllByOrderByIdDesc(any(Pageable.class))).thenReturn(page);

        PageResultDTO<ScriptPluginOperationAuditDTO> result = service.pageAll(2, 20);

        assertEquals(2, result.getPage());
        assertEquals(20, result.getPageSize());
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getItems().size());
    }

    @Test
    void truncate_keepsShortValuesAndCapsLongValues() {
        assertNull(ScriptPluginOperationAuditService.truncate(null, 5));
        assertEquals("abc", ScriptPluginOperationAuditService.truncate("abc", 5));
        assertEquals("abcde", ScriptPluginOperationAuditService.truncate("abcdefg", 5));
    }
}
