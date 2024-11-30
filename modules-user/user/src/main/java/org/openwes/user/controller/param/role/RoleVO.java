package org.openwes.user.controller.param.role;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class RoleVO {
    @ApiModelProperty("id")
    private Long id;

    @ApiModelProperty("createUser")
    private String createUser;

    @ApiModelProperty("gmtCreated")
    private Long gmtCreated;

    @ApiModelProperty("modifiedUser")
    private String modifiedUser;

    @ApiModelProperty("gmtModified")
    private Long gmtModified;

    @ApiModelProperty("角色名称")
    private String name;

    @ApiModelProperty("角色编码")
    private String code;

    @ApiModelProperty("帐号状态（1启用, 0停用）")
    private Integer status;

    @ApiModelProperty("角色描述")
    private String description;

    @ApiModelProperty(value = "仓库权限")
    @NotNull
    private List<String> warehouseCodes;

    public String getWarehouseCodes() {
        if (warehouseCodes == null) {
            return "";
        }
        return StringUtils.join(warehouseCodes, ",");
    }
}
