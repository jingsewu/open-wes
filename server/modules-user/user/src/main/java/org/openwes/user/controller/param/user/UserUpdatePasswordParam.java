package org.openwes.user.controller.param.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@ApiModel("修改当前用户密码")
public class UserUpdatePasswordParam {

    @ApiModelProperty(value = "旧密码", required = true)
    @NotEmpty(message = "旧密码不能为空")
    private String oldPassword;

    @ApiModelProperty(value = "新密码", required = true)
    @NotEmpty(message = "新密码不能为空")
    @Size(min = 6, message = "新密码长度不能小于6位")
    private String newPassword;

    @ApiModelProperty(value = "确认新密码", required = true)
    @NotEmpty(message = "确认新密码不能为空")
    private String confirmNewPassword;

}
