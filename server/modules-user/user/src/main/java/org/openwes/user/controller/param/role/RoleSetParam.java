package org.openwes.user.controller.param.role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Set;

@Data
@Schema(title = "角色集合参数")
public class RoleSetParam {

    @Schema(name = "roleCodes", title = "角色编号集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "角色集合不能为空")
    private Set<String> roleCodes;

}
