package org.openwes.user.controller.param.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("修改用户参数")
public class UserDTO {

    @ApiModelProperty(name = "id", value = "用户id")
    private Long id;

    @ApiModelProperty(name = "roleIds", value = "角色id集合", required = true)
    @NotNull(message = "角色id集合不能为空")
    private String roleIds;

    @ApiModelProperty(name = "name", value = "用户名称", required = true)
    @NotEmpty(message = "用户名称不能为空")
    @Size(max = 128, message = "用户名称不能超过128位")
    private String name;

    @ApiModelProperty(name = "account", value = "账号", required = true)
    @NotEmpty
    @Size(max = 128, message = "账号称不能超过128位")
    private String account;

    @ApiModelProperty(name = "status", value = "帐号状态（1启用, 0停用，参考枚举YesOrNo）", required = true)
    @NotNull(message = "帐号状态不能为空")
    private Integer status;

    @ApiModelProperty(name = "phone", value = "手机号")
    private String phone;

    @ApiModelProperty(name = "email", value = "邮箱")
    private String email;

    public Set<Long> getRoleIds() {
        if (StringUtils.isEmpty(this.roleIds)) {
            return Collections.emptySet();
        }
        return Arrays.stream(this.roleIds.split(",")).map(Long::parseLong).collect(Collectors.toSet());
    }
}
