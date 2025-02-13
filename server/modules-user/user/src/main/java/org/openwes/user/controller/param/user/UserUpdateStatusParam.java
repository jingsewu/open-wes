package org.openwes.user.controller.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(title = "修改用户状态参数")
public class UserUpdateStatusParam {

    @Schema(name = "userId", title = "用户id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "用户id不能为空")
    private Long userId;

    @Schema(name = "status", title = "帐号状态（1启用, 0停用，参考枚举YesOrNo）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "帐号状态不能为空")
    private Integer status;
}
