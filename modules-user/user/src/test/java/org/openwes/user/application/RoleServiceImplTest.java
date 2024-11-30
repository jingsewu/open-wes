package org.openwes.user.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.openwes.user.application.impl.RoleServiceImpl;
import org.openwes.user.config.prop.SystemProp;
import org.openwes.user.controller.param.role.RoleDTO;
import org.openwes.user.controller.param.role.RoleMenuUpdateParam;
import org.openwes.user.domain.entity.Role;
import org.openwes.user.domain.repository.RoleMapper;
import org.openwes.user.domain.repository.RoleMenuMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleServiceImplTest {

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private RoleMenuService roleMenuService;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private SystemProp systemProp;

    @Mock
    private RoleMenuMapper roleMenuMapper;

    @InjectMocks
    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        when(systemProp.getSuperAdminId()).thenReturn(520L);
        when(systemProp.getSuperRoleCode()).thenReturn("SUPER_ROLE");
    }

    @Test
    void exist_RoleCodeIsEmpty_ReturnFalse() {
        assertFalse(roleService.exist(""));
    }

    @Test
    void exist_RoleCodeExists_ReturnTrue() {
        when(roleMapper.findByCode("roleCode")).thenReturn(new Role());
        assertTrue(roleService.exist("roleCode"));
    }

    @Test
    void addRole_ValidRoleDTO_AddRoleSuccessfully() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setCode("newRole");
        roleDTO.setName("New Role");
        roleDTO.setWarehouseCodes("1");

        roleService.addRole(roleDTO);

        verify(roleMapper, times(1)).save(any(Role.class));
    }

    @Test
    void updateRole_RoleCodeChanged_CheckRoleCode() {
        RoleDTO param = new RoleDTO();
        param.setId(1L);
        param.setCode("newCode");
        param.setWarehouseCodes("1");

        Role role = new Role();
        role.setId(1L);
        role.setCode("oldCode");

        when(roleMapper.findById(anyLong())).thenReturn(Optional.of(role));

        roleService.updateRole(param);

        verify(roleMapper, times(1)).save(any(Role.class));
    }

    @Test
    void
    deleteRole_ValidRoleId_DeleteRoleSuccessfully() {
        Long roleId = 1L;

        Role role = new Role();
        role.setId(roleId);
        role.setStatus(1);

        when(roleMapper.findById(roleId)).thenReturn(Optional.of(role));
        doNothing().when(roleMapper).delete(any(Role.class));
        doNothing().when(roleMenuService).removeByRoleId(roleId);
        doNothing().when(userRoleService).removeByRoleId(roleId);

        roleService.deleteRole(roleId);

        verify(roleMapper, times(1)).delete(any(Role.class));
    }

    @Test
    void updateStatus_ValidRoleId_UpdateStatusSuccessfully() {
        Long roleId = 1L;
        Integer newStatus = 0;

        Role role = new Role();
        role.setId(roleId);
        role.setStatus(1);

        when(roleMapper.findById(roleId)).thenReturn(Optional.of(role));

        roleService.updateStatus(roleId, newStatus);

        verify(roleMapper, times(1)).save(any(Role.class));
    }

    @Test
    void updateRoleMenu_ValidParam_UpdateRoleMenuSuccessfully() {
        Long roleId = 1L;
        Set<Long> menuSet = new HashSet<>();
        menuSet.add(1L);

        RoleMenuUpdateParam param = new RoleMenuUpdateParam();
        param.setRoleId(roleId);
        param.setMenuSet(menuSet);

        Role role = new Role();
        role.setId(roleId);

        when(roleMapper.findById(roleId)).thenReturn(Optional.of(role));
        doNothing().when(roleMenuService).removeByRoleId(roleId);
        when(roleMenuMapper.saveAll(anyList())).thenReturn(null);

        roleService.updateRoleMenu(param);

    }

    @Test
    void getRole_ValidId_ReturnRole() {
        Long roleId = 1L;

        Role role = new Role();
        role.setId(roleId);

        when(roleMapper.findById(roleId)).thenReturn(Optional.of(role));

        Role result = roleService.getRole(roleId);

        assertNotNull(result);
        assertEquals(roleId, result.getId());
    }
}
