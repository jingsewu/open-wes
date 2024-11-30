package org.openwes.user.controller.controller;


import org.openwes.common.utils.http.Response;
import org.openwes.user.application.UserRoleService;
import org.openwes.user.application.UserService;
import org.openwes.user.controller.common.BaseResource;
import org.openwes.user.controller.param.user.UserDTO;
import org.openwes.user.controller.param.user.UserUpdateStatusParam;
import org.openwes.user.domain.entity.User;
import org.openwes.user.domain.entity.UserRole;
import org.openwes.user.domain.model.UserHasRole;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(BaseResource.API + "user")
@Api(tags = "用户接口")
@AllArgsConstructor
@Slf4j
@Tag(name = "User Module Api")
public class UserController extends BaseResource {

    private final UserService userService;
    private final UserRoleService userRoleService;

    @GetMapping("/{id}")
    @ApiOperation(value = "查询用户", response = UserHasRole.class, responseContainer = "List")
    public Object getUserById(@Valid @PathVariable Long id) {
        User user = userService.getById(id);
        List<UserRole> userRoles = userRoleService.getByUserId(id);

        UserHasRole userRole = new UserHasRole();
        BeanUtils.copyProperties(user, userRole);
        userRole.setRoleIds(StringUtils.join(userRoles.stream().map(UserRole::getRoleId).toList(), ","));
        return Response.success(userRole);
    }

    @PostMapping("/add")
    @ApiOperation("添加用户")
    public Response<Object> add(@RequestBody @Valid UserDTO param) {
        userService.addUser(param);
        return Response.success();
    }

    @PostMapping("/update")
    @ApiOperation("修改用户")
    public Object update(@RequestBody @Valid UserDTO param) {
        userService.updateUser(param);
        return Response.success();
    }

    @PostMapping("/updateStatus")
    @ApiOperation("修改用户状态")
    public Object updateStatus(@RequestBody UserUpdateStatusParam param) {
        userService.updateStatus(param.getUserId(), param.getStatus());
        return Response.success();
    }


    @PostMapping("/resetPassword/{id}")
    @ApiOperation("重置密码")
    public Object resetPassword(@PathVariable long id) {
        userService.resetPassword(id, "123456");
        return Response.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除用户")
    public Object deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return Response.success();
    }

}
