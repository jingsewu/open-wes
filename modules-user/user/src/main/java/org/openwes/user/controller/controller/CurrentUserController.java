package org.openwes.user.controller.controller;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.openwes.common.utils.http.Response;
import org.openwes.common.utils.user.UserContext;
import org.openwes.user.application.CurrentUserService;
import org.openwes.user.application.MenuService;
import org.openwes.user.application.UserRoleService;
import org.openwes.user.controller.common.BaseResource;
import org.openwes.user.controller.param.user.UserInfoUpdatedParam;
import org.openwes.user.controller.param.user.UserUpdatePasswordParam;
import org.openwes.user.domain.entity.Menu;
import org.openwes.user.domain.entity.Role;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(BaseResource.API + "currentUser")
@Api(tags = "当前认证的用户接口")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Module Api")
public class CurrentUserController extends BaseResource {

    private final CurrentUserService userService;
    private final MenuService menuService;
    private final UserRoleService userRoleService;

    @GetMapping("/getAuth")
    @ApiOperation("查询菜单树")
    public Object getAuth() {
        List<Role> roles = userRoleService.getByUserName(UserContext.getCurrentUser());
        Set<String> warehouseSet = roles.stream()
                .filter(v -> v.getWarehouseCodes() != null)
                .flatMap(v -> v.getWarehouseCodes().stream()).collect(Collectors.toSet());

        Map<String, Object> authMap = Maps.newHashMap();
        authMap.put("warehouses", warehouseSet);

        List<Menu> menuTrees = menuService.getMenuTreeByUser(UserContext.getCurrentUser());
        Map<String, Menu> menuMap = menuTrees.stream()
                .collect(Collectors.toMap(Menu::getSystemCode, v -> v, (a, b) -> a, LinkedHashMap::new));
        authMap.put("menus", menuMap);
        return authMap;
    }

    @PostMapping("/searchMenuTree")
    @ApiOperation(value = "查询菜单树(前端使用)", response = Menu.class)
    public Object searchMenuTree(@RequestParam Integer pageIndex,
                                 @RequestParam Integer pageSize) {
        if (pageIndex == null || pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 10;
        }
        Map<String, Object> result = Maps.newHashMap();
        List<Menu> menuTrees = menuService.getMenuTreeByUser(UserContext.getCurrentUser());
        if (null != menuTrees && menuTrees.size() >= pageSize) {
            int fromIndex = (pageIndex - 1) * pageSize;
            int toIndex = pageIndex * pageSize;
            if (fromIndex > menuTrees.size() - 1) {
                fromIndex = menuTrees.size() - 1;
            }
            if (toIndex > menuTrees.size()) {
                toIndex = menuTrees.size();
            }
            result.put("results", menuTrees.subList(fromIndex, toIndex));
        } else {
            result.put("results", menuTrees);
        }
        if (menuTrees != null) {
            result.put("total", String.valueOf(menuTrees.size()));
        }
        return result;
    }

    @PostMapping("/password")
    @ApiOperation("修改密码")
    public Object updatePassword(@RequestBody @Valid UserUpdatePasswordParam param) {
        Preconditions.checkState(Objects.equals(param.getNewPassword(), param.getConfirmNewPassword()));
        userService.updateCurrentUserPassword(UserContext.getCurrentUser(), param.getOldPassword(), param.getNewPassword());
        return Response.success();
    }

    @PostMapping("/updateUserInfo")
    @ApiOperation("修改当前用户信息")
    public Object updateUserInfo(@RequestBody @Valid UserInfoUpdatedParam param) {
        userService.updateInfo(UserContext.getCurrentUser(), param);
        return Response.success();
    }
}
