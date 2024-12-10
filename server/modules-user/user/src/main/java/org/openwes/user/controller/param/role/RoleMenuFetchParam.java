package org.openwes.user.controller.param.role;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel("查询当前角色的菜单和权限参数")
public class RoleMenuFetchParam {
    @ApiModelProperty(name = "roleId", value = "角色Id")
    @NotNull(message = "角色id不能为空")
    private Long roleId;
}
