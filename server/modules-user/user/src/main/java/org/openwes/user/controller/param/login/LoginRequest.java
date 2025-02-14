package org.openwes.user.controller.param.login;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @Schema(name = "account", title = "账号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "账号不能为空")
    @Size(max = 128, message = "账号不能超过128位")
    private String username;

    @Schema(name = "password", title = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "密码不能为空")
    @Size(min = 6, message = "密码长度不能小于6位")
    private String password;
}
