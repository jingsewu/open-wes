package org.openwes.wes.api.basic.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum PtlModeEnum implements IEnum {

    ON("ON", "点亮"),
    OFF("OFF", "灭灯"),
    FLASH("FLASH", "闪烁");

    private final String value;
    private final String label;

    private final String name = "电子标签模式";
}
