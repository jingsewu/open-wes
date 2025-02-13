package org.openwes.user.controller.param.menu;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class NavigationInfo {

    @Schema(title = "菜单唯一id")
    private Long id;

    @Schema(title = "父菜单id")
    private Long parentId;

    @Schema(title = "菜单唯一key")
    private String key;

    @Schema(title = "组件名称")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String component;

    @Schema(title = "目录默认重定向的地址")
    private String redirect;

    @Schema(title = "路径")
    private String path;

    @Schema(title = "meta属性")
    private Meta meta;

    @Data
    public static class Meta {

        public static final String TARGET_BLANK = "_blank";

        private String title;

        private Boolean show;

        private String target;

        private String icon;
    }
}
