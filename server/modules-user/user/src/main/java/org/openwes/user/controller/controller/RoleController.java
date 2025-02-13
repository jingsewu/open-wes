package org.openwes.user.controller.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.http.Response;
import org.openwes.user.application.MenuService;
import org.openwes.user.application.RoleService;
import org.openwes.user.controller.common.BaseResource;
import org.openwes.user.controller.param.role.RoleDTO;
import org.openwes.user.controller.param.role.RoleMenuUpdateParam;
import org.openwes.user.controller.param.role.RoleVO;
import org.openwes.user.domain.entity.Menu;
import org.openwes.user.domain.entity.Role;
import org.openwes.user.domain.entity.RoleMenu;
import org.openwes.user.domain.repository.RoleMenuMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(BaseResource.API + "role")
@AllArgsConstructor
@Slf4j
@Tag(name = "User Module Api")
public class RoleController extends BaseResource {

    private final RoleService roleService;
    private final MenuService menuService;
    private final RoleMenuMapper roleMenuMapper;

    @GetMapping("/getRoleMenu/{id}")
    @Operation(summary = "分配角色时, 查询当前角色的菜单和权限")
    public Object getRoleMenu(@PathVariable Long id) {
        List<Menu> menuTree = menuService.getMenuTree();
        List<RoleMenu> roleMenus = roleMenuMapper.findByRoleIdIn(Lists.newArrayList(id));
        Set<Long> menuIds = roleMenus.stream().map(RoleMenu::getMenuId).collect(Collectors.toSet());
        Map<String, Object> result = Maps.newHashMap();
        result.put("value", StringUtils.join(menuIds, ","));
        result.put("options", menuTree);
        return result;
    }

    @PostMapping("/updateRoleMenu/{id}")
    @Operation(summary = "分配角色菜单和权限")
    public Object updateRoleMenu(@PathVariable long id, @RequestBody @Valid RoleMenuUpdateParam param) {
        param.setRoleId(id);
        roleService.updateRoleMenu(param);
        return Response.success();
    }

    @PostMapping("/add")
    @Operation(summary = "添加角色")
    public Object add(@RequestBody @Valid RoleDTO param) {
        roleService.addRole(param);
        return Response.success();
    }

    @PostMapping("update")
    @Operation(summary = "修改角色")
    public Object update(@RequestBody @Valid RoleDTO param) {
        roleService.updateRole(param);
        return Response.success();
    }

    @DeleteMapping("{id}")
    @Operation(summary = "删除角色")
    public Object delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Response.success();
    }

    @GetMapping("{id}")
    @Operation(summary = "查询角色")
    public Object getRole(@PathVariable Long id) {
        Role role = roleService.getRole(id);
        RoleVO roleVO = new RoleVO();
        BeanUtils.copyProperties(role, roleVO);
        return Response.success(roleVO);
    }

}
