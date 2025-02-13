package org.openwes.user.controller.param.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
public class RoleVO {
    @Schema(name = "id")
    private Long id;

    @Schema(name = "createUser")
    private String createUser;

    @Schema(name = "gmtCreated")
    private Long gmtCreated;

    @Schema(name = "modifiedUser")
    private String modifiedUser;

    @Schema(name = "gmtModified")
    private Long gmtModified;

    @Schema(name = "角色名称")
    private String name;

    @Schema(name = "角色编码")
    private String code;

    @Schema(name = "帐号状态（1启用, 0停用）")
    private Integer status;

    @Schema(name = "角色描述")
    private String description;

    @Schema(name = "仓库权限")
    @NotNull
    private List<String> warehouseCodes;

    public String getWarehouseCodes() {
        if (warehouseCodes == null) {
            return "";
        }
        return StringUtils.join(warehouseCodes, ",");
    }
}
