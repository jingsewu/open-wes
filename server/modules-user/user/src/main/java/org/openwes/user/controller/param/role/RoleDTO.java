package org.openwes.user.controller.param.role;

import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@Schema(title = "修改角色参数")
public class RoleDTO {

    @Schema(name = "id", title = "角色id")
    private Long id;

    @Schema(name = "name", title = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称不能超过64位")
    private String name;

    @Schema(name = "code", title = "角色编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "角色编码不能为空")
    @Size(max = 32, message = "角色编码不能超过32位")
    private String code;

    @Schema(name = "status", title = "状态（1启用, 0停用，参考枚举YesOrNo）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(name = "description", title = "角色描述")
    @Size(max = 128, message = "描述不能超过32位")
    private String description;

    @Schema(title = "仓库权限")
    @NotEmpty
    private String warehouseCodes;

    public List<String> getWarehouseCodes() {
        return Lists.newArrayList(warehouseCodes.split(","));
    }
}
