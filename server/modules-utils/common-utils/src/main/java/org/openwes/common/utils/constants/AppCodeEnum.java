package org.openwes.common.utils.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sws
 */
@Getter
@AllArgsConstructor
public enum AppCodeEnum implements IEnum {

    WMS("wms", "智能仓库管理系统"),
    USER("user", "用户中心"),
    API_PLATFORM("api_platform", "接口平台"),
    PLUGIN_PLATFORM("plugin_platform", "插件平台"),
    ;

    private final String value;
    private final String label;

    private final String name = "应用编码";
}
