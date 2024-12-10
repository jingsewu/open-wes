package org.openwes.user.controller.param.login;

import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @ApiModelProperty(name = "account", value = "账号", required = true)
    @NotEmpty(message = "账号不能为空")
    @Size(max = 128, message = "账号不能超过128位")
    private String username;

    @ApiModelProperty(name = "password", value = "密码", required = true)
    @NotEmpty(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能小于6位")
    private String password;
}
