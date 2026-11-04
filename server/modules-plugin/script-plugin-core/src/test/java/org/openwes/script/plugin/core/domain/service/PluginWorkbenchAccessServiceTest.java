package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.plugin.api.dto.PluginCapabilitiesDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;
import org.openwes.user.api.IUserRoleApi;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginWorkbenchAccessServiceTest {

    @Mock
    private IUserRoleApi userRoleApi;
    @Mock
    private ScriptPluginOperationAuditService auditService;

    private PluginWorkbenchAccessService access;

    @BeforeEach
    void setUp() {
        access = new PluginWorkbenchAccessService(userRoleApi, auditService,
                "pluginDeveloper", "pluginAdmin", "superAdmin");
        UserContext.setAccount("alice");
    }

    @AfterEach
    void tearDown() {
        UserContext.removeUser();
    }

    @Test
    void capabilities_developerRole_canDevelopOnly() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of("pluginDeveloper"));

        PluginCapabilitiesDTO capabilities = access.capabilities();

        assertTrue(capabilities.isCanDevelop());
        assertFalse(capabilities.isCanAdminister());
        assertEquals(Set.of("pluginDeveloper"), capabilities.getRoles());
    }

    @Test
    void capabilities_adminRole_impliesDeveloper() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of("pluginAdmin"));

        PluginCapabilitiesDTO capabilities = access.capabilities();

        assertTrue(capabilities.isCanDevelop());
        assertTrue(capabilities.isCanAdminister());
    }

    @Test
    void capabilities_bothRoles_areReturnedAndBothChecksPass() {
        when(userRoleApi.findRoleCodes("alice"))
                .thenReturn(Set.of("pluginDeveloper", "pluginAdmin"));

        PluginCapabilitiesDTO capabilities = access.capabilities();

        assertTrue(capabilities.isCanDevelop());
        assertTrue(capabilities.isCanAdminister());
        assertEquals(Set.of("pluginDeveloper", "pluginAdmin"), capabilities.getRoles());
    }

    @Test
    void capabilities_superRole_isDeveloperAndAdmin() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of("superAdmin"));

        PluginCapabilitiesDTO capabilities = access.capabilities();

        assertTrue(capabilities.isCanDevelop());
        assertTrue(capabilities.isCanAdminister());
    }

    @Test
    void capabilities_withoutRoles_isReadOnly() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of());

        PluginCapabilitiesDTO capabilities = access.capabilities();

        assertFalse(capabilities.isCanDevelop());
        assertFalse(capabilities.isCanAdminister());
        assertTrue(capabilities.getRoles().isEmpty());
    }

    @Test
    void blankUser_isRejectedWithAuthenticatedUserCode() {
        UserContext.removeUser();

        WmsException e = assertThrows(WmsException.class, () -> access.requireAuthenticated());

        assertEquals("PL002003", e.getCode());
        verifyNoInteractions(auditService);
    }

    @Test
    void requireDeveloper_denialIsAuditedBeforeThrowing() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of());

        WmsException e = assertThrows(WmsException.class,
                () -> access.requireDeveloper("packing-wave", ScriptPluginAuditActionEnum.SAVE_REVISION));

        assertEquals("PL002001", e.getCode());
        verify(auditService).recordAs(eq("packing-wave"),
                eq(ScriptPluginAuditActionEnum.SAVE_REVISION), isNull(), eq(false),
                contains("pluginDeveloper"), eq("alice"));
    }

    @Test
    void requireDeveloper_readOnlyDenialIsNotAudited() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of());

        assertThrows(WmsException.class, () -> access.requireDeveloper());

        verifyNoInteractions(auditService);
    }

    @Test
    void requireAdmin_withDeveloperRole_isRejectedAndAudited() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of("pluginDeveloper"));

        WmsException e = assertThrows(WmsException.class,
                () -> access.requireAdmin("packing-wave", ScriptPluginAuditActionEnum.ENABLE));

        assertEquals("PL002002", e.getCode());
        verify(auditService).recordAs(eq("packing-wave"),
                eq(ScriptPluginAuditActionEnum.ENABLE), isNull(), eq(false),
                contains("pluginAdmin"), eq("alice"));
    }

    @Test
    void adminRole_passesBothDeveloperAndAdminChecks() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of("pluginAdmin"));

        assertEquals("alice", access.requireDeveloper());
        assertEquals("alice", access.requireAdmin());
        verifyNoInteractions(auditService);
    }

    @Test
    void configuredRoleLists_areCommaSeparatedAndTrimmed() {
        PluginWorkbenchAccessService configured = new PluginWorkbenchAccessService(
                userRoleApi, auditService, " devA, devB ", "adminA", "root");
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of("devB"));

        assertTrue(configured.capabilities().isCanDevelop());
        assertFalse(configured.capabilities().isCanAdminister());
    }
}
