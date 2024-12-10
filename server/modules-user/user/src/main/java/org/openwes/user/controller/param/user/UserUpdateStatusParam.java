package org.openwes.user.controller.param.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel("修改用户状态参数")
public class UserUpdateStatusParam {

    @ApiModelProperty(name = "userId", value = "用户id", required = true)
    @NotNull(message = "用户id不能为空")
    private Long userId;

    @ApiModelProperty(name = "status", value = "帐号状态（1启用, 0停用，参考枚举YesOrNo）", required = true)
    @NotNull(message = "帐号状态不能为空")
    private Integer status;
}
