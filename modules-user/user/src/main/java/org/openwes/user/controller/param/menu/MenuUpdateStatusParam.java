package org.openwes.user.controller.param.menu;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@ApiModel("修改菜单状态参数")
public class MenuUpdateStatusParam {

    @ApiModelProperty(name = "menuId", value = "菜单id", required = true)
    @NotNull(message = "菜单id不能为空")
    private Long menuId;

    @ApiModelProperty(name = "enable", value = "是否启用（1启用, 0停用，参考枚举YesOrNo）", required = true)
    @NotNull(message = "是否启用不能为空")
    private Integer enable;
}
