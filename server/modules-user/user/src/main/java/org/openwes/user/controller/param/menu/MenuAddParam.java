package org.openwes.user.controller.param.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MenuAddParam {
    @Schema(name = "systemCode", title = "所属系统（参考枚举AppCodeEnum）")
    private String systemCode;

    @Schema(name = "parentId", title = "父菜单id, 如果为顶级菜单, 则设置为空")
    private Long parentId;

    @Schema(name = "type", title = "菜单类型（1: 系统、2: 菜单、3: 权限，参考枚举MenuTypeEnum）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "类型不能为空")
    private Integer type;

    @Schema(name = "title", title = "菜单名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "菜单名称不能为空")
    @Size(max = 128, message = "名称不能超过128位")
    private String title;

    @Schema(name = "description", title = "描述")
    @Size(max = 256, message = "描述不能超过256位")
    private String description;

    @Schema(name = "permissions", title = "权限标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "权限标识不能为空")
    @Size(max = 128, message = "权限标识不能超过128位")
    private String permissions;

    @Schema(name = "component", title = "vue组件名称")
    private String component;

    @Schema(name = "orderNum", title = "排序", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序不能为空")
    @Min(1)
    private Integer orderNum;

    @Schema(name = "icon", title = "图标")
    @Size(max = 32, message = "图标不能超过32位")
    private String icon;

    @Schema(name = "path", title = "路径地址")
    @Size(max = 256, message = "路径地址不能超过256位")
    private String path;

    @Schema(name = "iframeShow", title = "是否以 iframe 的方式显示")
    private Integer iframeShow;
}
