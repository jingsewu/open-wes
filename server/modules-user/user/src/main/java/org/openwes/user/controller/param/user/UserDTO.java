package org.openwes.user.controller.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class UserDTO {

    @Schema(name = "id", example = "用户id")
    private Long id;

    @Schema(name = "roleIds", example = "角色id集合", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "角色id集合不能为空")
    private String roleIds;

    @Schema(name = "name", example = "用户名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "用户名称不能为空")
    @Size(max = 128, message = "用户名称不能超过128位")
    private String name;

    @Schema(name = "account", example = "账号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    @Size(max = 128, message = "账号称不能超过128位")
    private String account;

    @Schema(name = "status", example = "帐号状态（1启用, 0停用，参考枚举YesOrNo）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "帐号状态不能为空")
    private Integer status;

    @Schema(name = "phone", example = "手机号")
    private String phone;

    @Schema(name = "email", example = "邮箱")
    private String email;

    public Set<Long> getRoleIds() {
        if (StringUtils.isEmpty(this.roleIds)) {
            return Collections.emptySet();
        }
        return Arrays.stream(this.roleIds.split(",")).map(Long::parseLong).collect(Collectors.toSet());
    }
}
