package org.openwes.wes.api.config.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConfigTypeEnum implements IEnum {
    BOOLEAN("BOOLEAN", "布尔"),
    ENUM("ENUM", "枚举"),
    INTEGER("INTEGER", "数值");

    private final String value;
    private final String label;
}
