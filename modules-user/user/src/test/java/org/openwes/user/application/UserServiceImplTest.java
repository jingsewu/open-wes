package org.openwes.user.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.UserErrorDescEnum;
import org.openwes.common.utils.user.AuthConstants;
import org.openwes.common.utils.user.UserContext;
import org.openwes.user.api.dto.constants.UserTypeEnum;
import org.openwes.user.application.impl.UserServiceImpl;
import org.openwes.user.application.model.PermissionGrantedAuthority;
import org.openwes.user.config.prop.SystemProp;
import org.openwes.user.controller.param.user.UserDTO;
import org.openwes.user.domain.entity.Menu;
import org.openwes.user.domain.entity.Role;
import org.openwes.user.domain.entity.RoleMenu;
import org.openwes.user.domain.entity.User;
import org.openwes.user.domain.entity.UserRole;
import org.openwes.user.domain.repository.MenuMapper;
import org.openwes.user.domain.repository.RoleMapper;
import org.openwes.user.domain.repository.RoleMenuMapper;
import org.openwes.user.domain.repository.UserMapper;
import org.openwes.user.domain.repository.UserRoleMapper;
import org.openwes.user.domain.transfer.UserTransfer;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceImplTest {
    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private UserRoleService userRoleService;

    @Mock
    private SystemProp systemProp;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserTransfer userTransfer;

    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private RoleMenuMapper roleMenuMapper;
    @Mock
    private MenuMapper menuMapper;

    @BeforeEach
    public void setUp() {
        when(systemProp.getSuperAdminId()).thenReturn(520L);
        when(systemProp.getSuperRoleCode()).thenReturn("SUPER_ROLE");

        UserContext.setAccount("test");

        User user = new User();
        user.setAccount("test");
        when(userMapper.findByAccount("test")).thenReturn(user);
    }

    @Test
    public void testAddUser_UserExists_ThrowsException() {
        UserDTO userDTO = new UserDTO();
        userDTO.setAccount("existingUser");
        when(userMapper.findByAccount("existingUser")).thenReturn(new User());

        WmsException exception = assertThrows(WmsException.class, () -> userService.addUser(userDTO));
        assertEquals(UserErrorDescEnum.ERR_USER_NAME_EXISTS.getCode(), exception.getCode());
    }

    @Test
    public void testAddUser_Success() {
        UserDTO userDTO = new UserDTO();
        userDTO.setAccount("newUser");
        userDTO.setRoleIds("1");

        Role role = new Role();
        role.setCode("test");
        role.setStatus(1);

        when(userMapper.findByAccount("newUser")).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userMapper.save(any(User.class))).thenReturn(new User());
        when(userRoleMapper.saveAll(anyList())).thenReturn(null);
        when(roleMapper.findAllById(anySet())).thenReturn(Lists.newArrayList(role));

        assertDoesNotThrow(() -> userService.addUser(userDTO));
    }

    @Test
    public void testUpdateUser_UserNotFound_ThrowsException() {
        UserDTO param = new UserDTO();
        param.setId(1L);
        param.setAccount("newAccount");

        when(userMapper.findById(1L)).thenReturn(Optional.empty());

        WmsException exception = assertThrows(WmsException.class, () -> userService.updateUser(param));
        assertEquals(UserErrorDescEnum.NO_AUTHED_USER_FOUND.getCode(), exception.getCode());
    }

    @Test
    public void testUpdateUser_UsernameExists_ThrowsException() {
        User user = new User();
        user.setId(1L);
        user.setAccount("existingUser");

        UserDTO param = new UserDTO();
        param.setId(1L);
        param.setAccount("newAccount");
        when(userMapper.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.findByAccount("newAccount")).thenReturn(new User());

        WmsException exception = assertThrows(WmsException.class, () -> userService.updateUser(param));
        assertEquals(UserErrorDescEnum.ERR_USER_NAME_EXISTS.getCode(), exception.getCode());
    }

    @Test
    public void testUpdateUser_Success() {
        User user = new User();
        user.setId(1L);
        user.setAccount("existingUser");
        user.setType(UserTypeEnum.NORMAL.getValue());

        UserDTO param = new UserDTO();
        param.setId(1L);
        param.setAccount("newAccount");
        param.setRoleIds("1");

        when(userMapper.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.findByAccount("newAccount")).thenReturn(null);
        when(userMapper.save(any(User.class))).thenReturn(new User());
        doNothing().when(userRoleService).removeByUserId(1L);
        doNothing().when(userRoleService).add(1L, Collections.singleton(1L));

        assertDoesNotThrow(() -> userService.updateUser(param));
    }

    @Test
    public void testUpdateStatus_UserNotFound_ThrowsException() {
        when(userMapper.findById(1L)).thenReturn(Optional.empty());

        WmsException exception = assertThrows(WmsException.class, () -> userService.updateStatus(1L, 0));
        assertEquals(UserErrorDescEnum.NO_AUTHED_USER_FOUND.getCode(), exception.getCode());
    }

    @Test
    public void testUpdateStatus_StatusUnchanged_DoesNothing() {
        User user = new User();
        user.setId(2L);
        user.setStatus(0);

        when(userMapper.findById(1L)).thenReturn(Optional.of(user));

        userService.updateStatus(1L, 0);

        verify(userMapper, never()).save(any(User.class));
    }

    @Test
    public void testUpdateStatus_Success() {
        User user = new User();
        user.setId(1L);
        user.setStatus(1);

        when(userMapper.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.save(any(User.class))).thenReturn(new User());

        assertDoesNotThrow(() -> userService.updateStatus(1L, 0));
    }

    @Test
    public void testResetPassword_UserNotFound_ThrowsException() {
        when(userMapper.findById(1L)).thenReturn(Optional.empty());

        WmsException exception = assertThrows(WmsException.class, () -> userService.resetPassword(1L, "newPassword"));
        assertEquals(UserErrorDescEnum.NO_AUTHED_USER_FOUND.getCode(), exception.getCode());
    }

    @Test
    public void testResetPassword_Success() {
        User user = new User();
        user.setId(1L);

        when(userMapper.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedPassword");
        when(userMapper.save(any(User.class))).thenReturn(new User());

        assertDoesNotThrow(() -> userService.resetPassword(1L, "newPassword"));
    }

    @Test
    public void testDelete_UserNotFound_DoesNothing() {
        when(userMapper.findById(1L)).thenReturn(Optional.empty());

        assertThrows(WmsException.class, () -> userService.delete(1L));
        verify(userMapper, never()).delete(any(User.class));
    }

    @Test
    public void testDelete_Success() {
        User user = new User();
        user.setId(1L);
        user.setStatus(0);

        UserContext.setAccount("test");

        when(userMapper.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRoleService).removeByUserId(1L);
        when(userMapper.save(any(User.class))).thenReturn(new User());

        assertDoesNotThrow(() -> userService.delete(1L));
    }

    @Test
    public void testGetPermissionModels_UserHasSuperRole_ReturnsSuperPermission() {
        User user = new User();
        user.setId(1L);

        UserRole userRole = new UserRole();
        userRole.setUserId(1L);
        userRole.setRoleId(1L);

        Role role = new Role();
        role.setId(1L);
        role.setCode("SUPER_ROLE");

        when(userRoleMapper.findByUserId(1L)).thenReturn(Collections.singletonList(userRole));
        when(roleMapper.findAllById(anyList())).thenReturn(Collections.singletonList(role));
        when(systemProp.getSuperRoleCode()).thenReturn("SUPER_ROLE");

        Set<PermissionGrantedAuthority> authorities = userService.getPermissionModels(user);

        assertEquals(1, authorities.size());
        assertTrue(authorities.contains(new PermissionGrantedAuthority(AuthConstants.SUPPER_PERMISSION)));
    }

    @Test
    public void testGetPermissionModels_UserHasNoRoles_ReturnsEmptySet() {
        User user = new User();
        user.setId(2L);

        when(userRoleMapper.findByUserId(1L)).thenReturn(Collections.emptyList());

        Set<PermissionGrantedAuthority> authorities = userService.getPermissionModels(user);

        assertTrue(authorities.isEmpty());
    }

    @Test
    void testGetPermissionModels_UserHasPermissions_ReturnsPermissions() {
        User user = new User();
        user.setId(2L);

        UserRole userRole = new UserRole();
        userRole.setUserId(2L);
        userRole.setRoleId(1L);

        Role role = new Role();
        role.setId(1L);

        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRoleId(1L);
        roleMenu.setMenuId(1L);

        Menu menu = new Menu();
        menu.setId(1L);
        menu.setPermissions("PERM1,PERM2");

        when(userRoleMapper.findByUserId(2L)).thenReturn(Collections.singletonList(userRole));
        when(roleMapper.findAllById(anyList())).thenReturn(Collections.singletonList(role));
        when(roleMenuMapper.findByRoleIdIn(anyList())).thenReturn(Collections.singletonList(roleMenu));
        when(menuMapper.findAllById(anyList())).thenReturn(Collections.singletonList(menu));

        Set<PermissionGrantedAuthority> authorities = userService.getPermissionModels(user);

        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new PermissionGrantedAuthority("PERM1")));
        assertTrue(authorities.contains(new PermissionGrantedAuthority("PERM2")));
    }
}
