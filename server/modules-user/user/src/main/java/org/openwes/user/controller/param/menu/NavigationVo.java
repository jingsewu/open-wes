package org.openwes.user.controller.param.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class NavigationVo {

    @Schema(title = "导航信息")
    private List<NavigationInfo> navigationInfos;
}
