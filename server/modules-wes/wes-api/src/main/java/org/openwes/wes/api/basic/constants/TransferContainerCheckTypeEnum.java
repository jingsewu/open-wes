package org.openwes.wes.api.basic.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransferContainerCheckTypeEnum implements IEnum {
    INTERFACE("INTERFACE", "接口"),
    INTERNAL("INTERNAL", "内部"),
    AUTO("AUTO", "自动"),
    NO("NO", "不创建");

    private final String value;
    private final String label;
}
