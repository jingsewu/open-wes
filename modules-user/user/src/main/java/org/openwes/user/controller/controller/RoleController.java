package org.openwes.user.controller.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.openwes.common.utils.http.Response;
import org.openwes.user.application.MenuService;
import org.openwes.user.application.RoleService;
import org.openwes.user.controller.common.BaseResource;
import org.openwes.user.controller.common.vo.RoleMenuVo;
import org.openwes.user.controller.param.role.RoleDTO;
import org.openwes.user.controller.param.role.RoleMenuUpdateParam;
import org.openwes.user.controller.param.role.RoleVO;
import org.openwes.user.domain.entity.Menu;
import org.openwes.user.domain.entity.Role;
import org.openwes.user.domain.entity.RoleMenu;
import org.openwes.user.domain.repository.RoleMenuMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping(BaseResource.API + "role")
@Api(tags = "角色接口")
@AllArgsConstructor
@Slf4j
@Tag(name = "User Module Api")
public class RoleController extends BaseResource {

    private final RoleService roleService;
    private final MenuService menuService;
    private final RoleMenuMapper roleMenuMapper;

    @GetMapping("/getRoleMenu/{id}")
    @ApiOperation(value = "分配角色时, 查询当前角色的菜单和权限", response = RoleMenuVo.class)
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
    @ApiOperation("分配角色菜单和权限")
    public Object updateRoleMenu(@PathVariable long id, @RequestBody @Valid RoleMenuUpdateParam param) {
        param.setRoleId(id);
        roleService.updateRoleMenu(param);
        return Response.success();
    }

    @PostMapping("/add")
    @ApiOperation("添加角色")
    public Object add(@RequestBody @Valid RoleDTO param) {
        roleService.addRole(param);
        return Response.success();
    }

    @PostMapping("update")
    @ApiOperation("修改角色")
    public Object update(@RequestBody @Valid RoleDTO param) {
        roleService.updateRole(param);
        return Response.success();
    }

    @DeleteMapping("{id}")
    @ApiOperation("删除角色")
    public Object delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Response.success();
    }

    @GetMapping("{id}")
    @ApiOperation("查询角色")
    public Object getRole(@PathVariable Long id) {
        Role role = roleService.getRole(id);
        RoleVO roleVO = new RoleVO();
        BeanUtils.copyProperties(role, roleVO);
        return Response.success(roleVO);
    }

}
