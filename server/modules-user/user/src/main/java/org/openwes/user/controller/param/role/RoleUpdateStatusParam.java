package org.openwes.user.controller.param.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(title = "修改角色状态参数")
public class RoleUpdateStatusParam {

    @Schema(name = "roleId", title = "角色id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "角色id不能为空")
    private Long roleId;

    @Schema(name = "status", title = "是否启用（1-是、0-否，参考枚举YesOrNo）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否启用不能为空")
    private Integer status;
}
