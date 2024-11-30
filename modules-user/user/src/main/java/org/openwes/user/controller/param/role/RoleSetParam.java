package org.openwes.user.controller.param.role;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
@ApiModel("角色集合参数")
public class RoleSetParam {

    @ApiModelProperty(name = "roleCodes", value = "角色编号集合", required = true)
    @NotNull(message = "角色集合不能为空")
    private Set<String> roleCodes;

}
