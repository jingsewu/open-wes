package org.openwes.user.controller.param.role;

import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("修改角色参数")
public class RoleDTO {

    @ApiModelProperty(name = "id", value = "角色id")
    private Long id;

    @ApiModelProperty(name = "name", value = "角色名称", required = true)
    @NotEmpty(message = "角色名称不能为空")
    @Size(max = 64, message = "角色名称不能超过64位")
    private String name;

    @ApiModelProperty(name = "code", value = "角色编码", required = true)
    @NotEmpty(message = "角色编码不能为空")
    @Size(max = 32, message = "角色编码不能超过32位")
    private String code;

    @ApiModelProperty(name = "status", value = "状态（1启用, 0停用，参考枚举YesOrNo）", required = true)
    @NotNull(message = "状态不能为空")
    private Integer status;

    @ApiModelProperty(name = "description", value = "角色描述")
    @Size(max = 128, message = "描述不能超过32位")
    private String description;

    @ApiModelProperty(value = "仓库权限")
    @NotEmpty
    private String warehouseCodes;

    public List<String> getWarehouseCodes() {
        return Lists.newArrayList(warehouseCodes.split(","));
    }
}
