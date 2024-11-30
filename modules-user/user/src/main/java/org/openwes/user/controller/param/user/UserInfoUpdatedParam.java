package org.openwes.user.controller.param.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@ApiModel("修改当前用户信息")
public class UserInfoUpdatedParam {

    @ApiModelProperty(name = "name", value = "用户名称")
    @Length(max = 128, message = "姓名不能超过32位")
    private String name;

    @ApiModelProperty(name = "phone", value = "手机号")
    @Length(max = 64, message = "手机号不能超过64位")
    private String phone;

    @ApiModelProperty(name = "email", value = "邮箱")
    @Length(max = 128, message = "邮箱不能超过128位")
    private String email;

}
