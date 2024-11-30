package org.openwes.api.platform.api.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiCallTypeEnum implements IEnum {

    NOTIFY("NOTIFY", "下发"),
    CALLBACK("CALLBACK", "回传");

    private final String value;
    private final String label;

    private final String name = "接口调用类型";
}
