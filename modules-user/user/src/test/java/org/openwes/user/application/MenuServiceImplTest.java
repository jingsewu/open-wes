package org.openwes.user.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anySet;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.user.application.impl.MenuServiceImpl;
import org.openwes.user.config.prop.SystemProp;
import org.openwes.user.controller.param.menu.MenuAddParam;
import org.openwes.user.controller.param.menu.MenuUpdatedParam;
import org.openwes.user.controller.param.menu.NavigationVo;
import org.openwes.user.domain.entity.Menu;
import org.openwes.user.domain.entity.Role;
import org.openwes.user.domain.entity.RoleMenu;
import org.openwes.user.domain.repository.MenuMapper;
import org.openwes.user.domain.repository.RoleMapper;
import org.openwes.user.domain.repository.RoleMenuMapper;
import org.openwes.user.domain.repository.UserRoleMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MenuServiceImplTest {

    @Mock
    private SystemProp systemProp;

    @Mock
    private RoleMenuService roleMenuService;

    @Mock
    private UserService userService;

    @Mock
    private MenuMapper menuMapper;

    @Mock
    private UserRoleMapper userRoleMapper;

    @Mock
    private RoleMapper roleMapper;

    @Mock
    private RoleMenuMapper roleMenuMapper;

    @InjectMocks
    private MenuServiceImpl menuService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getUserNav_EmptyRoleCodes_ReturnsEmptyNavigation() {
        Set<String> roleCodes = Collections.emptySet();
        NavigationVo result = menuService.getUserNav(roleCodes);
        assertNull(result.getNavigationInfos());
    }

    @Test
    void getUserNav_SuperRoleCode_ReturnsAllNav() {
        Set<String> roleCodes = Collections.singleton("superRoleCode");
        when(systemProp.getSuperRoleCode()).thenReturn("superRoleCode");
        List<Menu> allNav = Collections.singletonList(new Menu());
        when(menuMapper.findByTypeInAndEnableOrderByOrderNum(anyList(), anyInt())).thenReturn(allNav);

        NavigationVo result = menuService.getUserNav(roleCodes);

        assertNotNull(result);
        assertFalse(result.getNavigationInfos().isEmpty());
    }

    @Test
    void getUserNav_NonSuperRoleCodes_ReturnsMenuByRoleCodes() {
        Set<String> roleCodes = Collections.singleton("roleCode");
        when(systemProp.getSuperRoleCode()).thenReturn("superRoleCode");
        List<Menu> menus = Collections.singletonList(new Menu());
        when(roleMapper.findByCodeIn(anySet())).thenReturn(Collections.singletonList(new Role()));
        when(roleMenuMapper.findByRoleIdIn(anyList())).thenReturn(Collections.singletonList(new RoleMenu()));
        when(menuMapper.findAllByIdInOrderByOrderNum(anyList())).thenReturn(menus);

        NavigationVo result = menuService.getUserNav(roleCodes);

        assertNotNull(result);
        assertFalse(result.getNavigationInfos().isEmpty());
    }

    @Test
    void getAllNav_ReturnsExpectedMenus() {
        List<Menu> expectedMenus = Collections.singletonList(new Menu());
        when(menuMapper.findByTypeInAndEnableOrderByOrderNum(anyList(), anyInt())).thenReturn(expectedMenus);

        List<Menu> result = menuService.getAllNav(false);

        assertNotNull(result);
        assertEquals(expectedMenus, result);
    }

    @Test
    void getMenuTree_NoMenus_ReturnsEmptyList() {
        when(menuMapper.findAll()).thenReturn(Collections.emptyList());

        List<Menu> result = menuService.getMenuTree();

        assertTrue(result.isEmpty());
    }

    @Test
    void getMenuTreeByUser_EmptyUsername_ThrowsException() {
        assertThrows(WmsException.class, () -> menuService.getMenuTreeByUser(""));
    }

    @Test
    void getMenuTreeByUser_UserNotFound_ReturnsEmptyList() {
        when(userService.getByAccount(anyString())).thenReturn(null);

        List<Menu> result = menuService.getMenuTreeByUser("username");

        assertTrue(result.isEmpty());
    }

    @Test
    void updateStatus_ValidInput_UpdatesStatus() {
        Long menuId = 1L;
        int status = 0;
        Menu menu = new Menu();
        menu.setId(menuId);
        menu.setEnable(status);
        when(menuMapper.findById(anyLong())).thenReturn(Optional.of(menu));
        when(menuMapper.saveAll(anyList())).thenReturn(Collections.singletonList(menu));

        menuService.updateStatus(menuId, status);

        assertEquals(status, menu.getEnable());
    }

    @Test
    void removeMenuById_ValidInput_DeletesMenu() {
        Long menuId = 1L;
        Set<Long> menuIds = new HashSet<>(Collections.singleton(menuId));
        List<Menu> menus = Collections.singletonList(new Menu());
        when(menuMapper.findAllById(anySet())).thenReturn(menus);

        menuService.removeMenuById(menuId);

        verify(menuMapper, times(1)).deleteAll(anyList());
        verify(roleMenuService, times(1)).removeByMenuId(anySet());
    }

    @Test
    void updateMenu_ValidInput_UpdatesMenu() {
        Long menuId = 1L;
        MenuUpdatedParam param = new MenuUpdatedParam();
        param.setId(menuId);
        Menu menu = new Menu();
        menu.setId(menuId);
        when(menuMapper.findById(anyLong())).thenReturn(Optional.of(menu));
        when(menuMapper.save(any(Menu.class))).thenReturn(menu);

        menuService.updateMenu(param);

        assertEquals(param.getId(), menu.getId());
    }

    @Test
    void addMenu_ValidInput_AddsMenu() {
        MenuAddParam param = new MenuAddParam();
        Menu menu = new Menu();
        when(menuMapper.save(any(Menu.class))).thenReturn(menu);

        menuService.addMenu(param);

        assertNotNull(menu);
    }


}
