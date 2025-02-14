package org.openwes.user.controller.param.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserInfoUpdatedParam {

    @Schema(name = "name", example = "用户名称")
    @Length(max = 128, message = "姓名不能超过32位")
    private String name;

    @Schema(name = "phone", example = "手机号")
    @Length(max = 64, message = "手机号不能超过64位")
    private String phone;

    @Schema(name = "email", example = "邮箱")
    @Length(max = 128, message = "邮箱不能超过128位")
    private String email;

}
