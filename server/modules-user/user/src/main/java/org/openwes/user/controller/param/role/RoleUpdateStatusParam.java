package org.openwes.user.controller.param.role;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel("修改角色状态参数")
public class RoleUpdateStatusParam {

    @ApiModelProperty(name = "roleId", value = "角色id", required = true)
    @NotNull(message = "角色id不能为空")
    private Long roleId;

    @ApiModelProperty(name = "status", value = "是否启用（1-是、0-否，参考枚举YesOrNo）", required = true)
    @NotNull(message = "是否启用不能为空")
    private Integer status;
}
