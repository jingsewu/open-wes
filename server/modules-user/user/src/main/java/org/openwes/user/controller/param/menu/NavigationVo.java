package org.openwes.user.controller.param.menu;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("导航模型")
public class NavigationVo {

    @ApiModelProperty("导航信息")
    private List<NavigationInfo> navigationInfos;
}
