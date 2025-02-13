package org.openwes.user.controller.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "修改当前用户密码")
public class UserUpdatePasswordParam {

    @Schema(title = "旧密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "旧密码不能为空")
    private String oldPassword;

    @Schema(title = "新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "新密码不能为空")
    @Size(min = 6, message = "新密码长度不能小于6位")
    private String newPassword;

    @Schema(title = "确认新密码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "确认新密码不能为空")
    private String confirmNewPassword;

}
