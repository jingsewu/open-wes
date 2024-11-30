package org.openwes.wes.api.basic.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AisleTypeEnum implements IEnum {
    NORMAL_RACK("NORMAL_RACK", "普通货架"),
    PALLET_RACK("PALLET_RACK", "托盘货架");

    private final String value;
    private final String label;
}
