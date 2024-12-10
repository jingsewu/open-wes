package org.openwes.wes.api.main.data.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum WarehouseBusinessTypeEnum implements IEnum {
    TOB("TOB", "TOB"),
    TOC("TOC", "TOC"),
    RETURN("RETURN", "退货"),
    CROSS_BORDER("CROSS_BORDER", "越库"),
    CONSUMABLES("CONSUMABLES", "耗材");

    private final String value;
    private final String label;
}
