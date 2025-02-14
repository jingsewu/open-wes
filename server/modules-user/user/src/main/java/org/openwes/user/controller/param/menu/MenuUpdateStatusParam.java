package org.openwes.user.controller.param.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MenuUpdateStatusParam {

    @Schema(name = "menuId", title = "菜单id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜单id不能为空")
    private Long menuId;

    @Schema(name = "enable", title = "是否启用（1启用, 0停用，参考枚举YesOrNo）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "是否启用不能为空")
    private Integer enable;
}
