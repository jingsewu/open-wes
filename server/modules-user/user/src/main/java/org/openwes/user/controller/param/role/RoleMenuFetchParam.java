package org.openwes.user.controller.param.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(title = "查询当前角色的菜单和权限参数")
public class RoleMenuFetchParam {
    @Schema(name = "roleId", title = "角色Id")
    @NotNull(message = "角色id不能为空")
    private Long roleId;
}
