package org.openwes.user.application;

import org.openwes.user.controller.param.menu.MenuAddParam;
import org.openwes.user.controller.param.menu.MenuUpdatedParam;
import org.openwes.user.controller.param.menu.NavigationVo;
import org.openwes.user.domain.entity.Menu;

import java.util.List;
import java.util.Set;

public interface MenuService {

    NavigationVo getUserNav(Set<String> roleCodes);

    List<Menu> getAllNav(boolean haveDisable);

    List<Menu> getMenuTree();

    List<Menu> getMenuTreeByUser(String currentUsername);

    void updateStatus(Long menuId, int status);

    void removeMenuById(Long menuId);

    void updateMenu(MenuUpdatedParam menu);

    void addMenu(MenuAddParam menu);
}
