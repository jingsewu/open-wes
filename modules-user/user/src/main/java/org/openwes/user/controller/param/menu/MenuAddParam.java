package org.openwes.user.controller.param.menu;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@ApiModel("添加菜单参数")
public class MenuAddParam {
    @ApiModelProperty(name = "systemCode", value = "所属系统（参考枚举AppCodeEnum）")
    private String systemCode;

    @ApiModelProperty(name = "parentId", value = "父菜单id, 如果为顶级菜单, 则设置为空")
    private Long parentId;

    @ApiModelProperty(name = "type", value = "菜单类型（1: 系统、2: 菜单、3: 权限，参考枚举MenuTypeEnum）", required = true)
    @NotNull(message = "类型不能为空")
    private Integer type;

    @ApiModelProperty(name = "title", value = "菜单名称", required = true)
    @NotEmpty(message = "菜单名称不能为空")
    @Size(max = 128, message = "名称不能超过128位")
    private String title;

    @ApiModelProperty(name = "description", value = "描述")
    @Size(max = 256, message = "描述不能超过256位")
    private String description;

    @ApiModelProperty(name = "permissions", value = "权限标识", required = true)
    @NotEmpty(message = "权限标识不能为空")
    @Size(max = 128, message = "权限标识不能超过128位")
    private String permissions;

    @ApiModelProperty(name = "component", value = "vue组件名称")
    private String component;

    @ApiModelProperty(name = "orderNum", value = "排序", required = true)
    @NotNull(message = "排序不能为空")
    @Min(1)
    private Integer orderNum;

    @ApiModelProperty(name = "icon", value = "图标")
    @Size(max = 32, message = "图标不能超过32位")
    private String icon;

    @ApiModelProperty(name = "path", value = "路径地址")
    @Size(max = 256, message = "路径地址不能超过256位")
    private String path;

    @ApiModelProperty(name = "iframeShow", value = "是否以 iframe 的方式显示")
    private Integer iframeShow;
}
