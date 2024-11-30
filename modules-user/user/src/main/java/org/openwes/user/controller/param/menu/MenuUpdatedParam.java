package org.openwes.user.controller.param.menu;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel("修改菜单参数")
public class MenuUpdatedParam extends MenuAddParam {
    @ApiModelProperty(name = "menuId", value = "菜单id", required = true)
    @NotNull(message = "菜单id不能为空")
    private Long id;
}
