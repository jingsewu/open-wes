package org.openwes.wes.api.main.data.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OwnerTypeEnum implements IEnum {
    SELF("SELF", "自营"),
    THIRD_PARTY("THIRD_PARTY", "第三方");

    private final String value;
    private final String label;
}
