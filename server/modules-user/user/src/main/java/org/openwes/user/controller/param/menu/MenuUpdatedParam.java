package org.openwes.user.controller.param.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class MenuUpdatedParam extends MenuAddParam {
    @Schema(name = "menuId", title = "菜单id", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "菜单id不能为空")
    private Long id;
}
