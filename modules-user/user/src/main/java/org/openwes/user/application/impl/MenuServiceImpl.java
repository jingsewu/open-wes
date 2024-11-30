package org.openwes.user.application.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.UserErrorDescEnum;
import org.openwes.user.api.dto.constants.MenuTypeEnum;
import org.openwes.user.api.dto.constants.YesOrNoEnum;
import org.openwes.user.application.MenuService;
import org.openwes.user.application.RoleMenuService;
import org.openwes.user.application.UserService;
import org.openwes.user.config.prop.SystemProp;
import org.openwes.user.controller.param.menu.MenuAddParam;
import org.openwes.user.controller.param.menu.MenuUpdatedParam;
import org.openwes.user.controller.param.menu.NavigationInfo;
import org.openwes.user.controller.param.menu.NavigationVo;
import org.openwes.user.domain.entity.Menu;
import org.openwes.user.domain.entity.Role;
import org.openwes.user.domain.entity.RoleMenu;
import org.openwes.user.domain.entity.User;
import org.openwes.user.domain.entity.UserRole;
import org.openwes.user.domain.repository.MenuMapper;
import org.openwes.user.domain.repository.RoleMapper;
import org.openwes.user.domain.repository.RoleMenuMapper;
import org.openwes.user.domain.repository.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final SystemProp systemProp;
    private final RoleMenuService roleMenuService;
    private final UserService userService;
    private final MenuMapper menuMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public NavigationVo getUserNav(Set<String> roleCodes) {
        NavigationVo navigationVo = new NavigationVo();
        if (roleCodes.isEmpty()) {
            return navigationVo;
        }
        List<Menu> menus;
        if (roleCodes.contains(systemProp.getSuperRoleCode())) {
            menus = getAllNav(false);
        } else {
            menus = getMenuByRoleCodes(roleCodes);
        }

        if (menus == null || menus.isEmpty()) {
            return navigationVo;
        }
        List<NavigationInfo> navigationInfos = Lists.newArrayList();
        Map<Long, Menu> notTopMenus = Maps.newHashMap();
        List<NavigationInfo> topNavigationInfo = Lists.newArrayListWithCapacity(4);

        for (Menu menu : menus) {
            if (menu == null) {
                continue;
            }
            Long parentId = menu.getParentId();
            NavigationInfo navigationInfo = getNavigationVo(menu);
            if (Objects.equals(parentId, 0L)) {
                topNavigationInfo.add(navigationInfo);
            } else {
                notTopMenus.putIfAbsent(parentId, menu);
            }
            // 非插件菜单
            navigationInfos.add(navigationInfo);
        }
        setTopRedirect(topNavigationInfo, notTopMenus);
        navigationVo.setNavigationInfos(navigationInfos);
        return navigationVo;
    }


    @Override
    public List<Menu> getAllNav(boolean haveDisable) {
        ArrayList<Integer> types = Lists.newArrayList(Integer.valueOf(MenuTypeEnum.MENU.getValue()),
            Integer.valueOf(MenuTypeEnum.PAGE.getValue()));
        return menuMapper.findByTypeInAndEnableOrderByOrderNum(types, Integer.valueOf(YesOrNoEnum.YES.getValue()));
    }

    @Override
    public List<Menu> getMenuTree() {
        List<Menu> menus = menuMapper.findAll();
        if (menus.isEmpty()) {
            return Collections.emptyList();
        }
        return buildMenuTree(menus, 0L);
    }

    @Override
    public List<Menu> getMenuTreeByUser(String currentUsername) {
        if (StringUtils.isEmpty(currentUsername)) {
            throw new WmsException(UserErrorDescEnum.NO_AUTHED_USER_FOUND);
        }
        User user = userService.getByAccount(currentUsername);
        if (null == user) {
            return Collections.emptyList();
        }

        List<UserRole> userRoles = userRoleMapper.findByUserId(user.getId());
        List<Role> roles = roleMapper.findAllById(userRoles.stream().map(UserRole::getRoleId).toList());
        Set<String> roleCodes = roles.stream().map(Role::getCode).collect(Collectors.toSet());
        boolean containSuperRole = roleCodes.stream().anyMatch(u -> u.equals(systemProp.getSuperRoleCode()));
        if (containSuperRole) {
            // 为超级管理员
            List<Menu> menus = menuMapper.findAll(Sort.by(Sort.Order.asc("orderNum")))
                .stream()
                .filter(v -> v.getType() == 1 || v.getType() == 2)
                .toList();
            return buildMenuTree(menus, 0L);
        }
        if (roleCodes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Menu> menus = getMenuByRoleCodes(roleCodes).stream().filter(v -> v.getType() == 1 || v.getType() == 2).toList();
        return buildMenuTree(menus, 0L);
    }

    @Transactional
    @Override
    public void updateStatus(Long menuId, int status) {
        Set<Long> menuIds = Sets.newHashSet(menuId);
        resolveChildMenu(menuId, m -> menuIds.add(m.getId()));

        List<Menu> menus = menuMapper.findAllById(menuIds);
        menus.forEach(menu -> menu.setEnable(status));
        menuMapper.saveAll(menus);
    }

    @Transactional
    @Override
    public void removeMenuById(Long menuId) {
        if (null == menuId) {
            return;
        }
        Set<Long> menuIds = Sets.newHashSet(menuId);
        resolveChildMenu(menuId, m -> menuIds.add(m.getId()));
        List<Menu> menus = menuMapper.findAllById(menuIds);
        menuMapper.deleteAll(menus);
        roleMenuService.removeByMenuId(menuIds);
    }

    @Override
    public void updateMenu(MenuUpdatedParam param) {
        Menu menu = menuMapper.findById(param.getId()).orElseThrow();
        BeanUtils.copyProperties(param, menu);
        menuMapper.save(menu);
    }

    private void resolveChildMenu(Long parentMenuId, Consumer<Menu> consumer) {
        List<Menu> childMenu = this.getChildMenu(parentMenuId);
        if (childMenu.isEmpty()) {
            return;
        }
        for (Menu menu : childMenu) {
            if (menu == null) {
                continue;
            }
            consumer.accept(menu);
            resolveChildMenu(menu.getId(), consumer);
        }
    }

    private List<Menu> getChildMenu(Long menuId) {
        if (null == menuId) {
            return Collections.emptyList();
        }
        return menuMapper.findByParentId(menuId);
    }

    @Transactional
    @Override
    public void addMenu(MenuAddParam param) {
        Menu menu = new Menu();
        BeanUtils.copyProperties(param, menu);
        if (null == menu.getParentId()) {
            menu.setParentId(0L);
        } else if (StringUtils.isBlank(menu.getSystemCode())) {
            Menu parentMenu = menuMapper.findById(param.getParentId()).orElseThrow();
            menu.setSystemCode(parentMenu.getSystemCode());
        }
        menu.setEnable(Integer.valueOf(YesOrNoEnum.YES.getValue()));
        menuMapper.save(menu);
    }

    /**
     * 得到导航模型
     *
     * @param menu 菜单
     *
     * @return NavigationVo
     */
    private NavigationInfo getNavigationVo(Menu menu) {
        NavigationInfo navigationInfo = new NavigationInfo();
        navigationInfo.setId(menu.getId());
        navigationInfo.setParentId(menu.getParentId());
        navigationInfo.setKey(menu.getPermissions());
        navigationInfo.setPath(menu.getPath());
        NavigationInfo.Meta meta = new NavigationInfo.Meta();
        meta.setTitle(menu.getTitle());
        meta.setShow(true);
        meta.setIcon(menu.getIcon());
        navigationInfo.setMeta(meta);
        return navigationInfo;
    }

    /**
     * 设置顶级菜单的跳转路径
     *
     * @param topNavigationInfo 顶级菜单列表
     * @param notTopMenus       非顶级菜单
     */
    private void setTopRedirect(List<NavigationInfo> topNavigationInfo, Map<Long, Menu> notTopMenus) {
        if (topNavigationInfo.isEmpty()) {
            return;
        }
        for (NavigationInfo navigationInfo : topNavigationInfo) {
            String firstRedirect = getFirstRedirect(navigationInfo.getId(), notTopMenus);
            if (StringUtils.isEmpty(firstRedirect)) {
                continue;
            }
            navigationInfo.setRedirect(firstRedirect);
        }
    }

    /**
     * 递归获取第一个菜单型的路径
     *
     * @param menuParentId 菜单的父id
     * @param notTopMenus  非顶级菜单
     *
     * @return 菜单路径
     */
    private String getFirstRedirect(Long menuParentId, Map<Long, Menu> notTopMenus) {
        Menu menu = notTopMenus.get(menuParentId);
        if (menu == null) {
            return null;
        }
        if (Objects.equals(menu.getType(), Integer.valueOf(MenuTypeEnum.PAGE.getValue()))) {
            return menu.getPath();
        } else {
            return getFirstRedirect(menu.getId(), notTopMenus);
        }
    }

    private final RoleMenuMapper roleMenuMapper;

    private final RoleMapper roleMapper;

    private List<Menu> getMenuByRoleCodes(Collection<String> roleCodes) {
        List<Role> roles = roleMapper.findByCodeIn(roleCodes);
        List<RoleMenu> roleMenus = roleMenuMapper.findByRoleIdIn(roles.stream().map(Role::getId).toList());
        return menuMapper.findAllByIdInOrderByOrderNum(roleMenus.stream().map(RoleMenu::getMenuId).toList());
    }


    public static List<Menu> buildMenuTree(List<Menu> menuList, Long parentId) {
        List<Menu> menuTree = new ArrayList<>();

        for (Menu menu : menuList) {
            if (Objects.equals(menu.getParentId(), parentId)) {
                List<Menu> subMenu = buildMenuTree(menuList, menu.getId());
                menu.setChildren(subMenu);
                menuTree.add(menu);
            }
        }

        return menuTree;
    }
}
