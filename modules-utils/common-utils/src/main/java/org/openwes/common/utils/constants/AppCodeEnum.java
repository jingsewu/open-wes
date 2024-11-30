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

    WMS("wms", "wms"),
    USER("user", "user"),
    API_PLATFORM("api_platform", "api_platform"),
    ;

    private final String value;
    private final String label;
}
