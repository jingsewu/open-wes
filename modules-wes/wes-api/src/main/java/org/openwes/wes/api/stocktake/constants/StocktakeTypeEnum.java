package org.openwes.wes.api.stocktake.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StocktakeTypeEnum implements IEnum {

    ORDINARY("ORDINARY", "普通盘点"),
    RANDOM("RANDOM", "随机盘点"),
    ACTIVE("ACTIVE", "动碰盘点"),
    DISCREPANCY_REVIEW("DISCREPANCY_REVIEW", "差异复盘");

    private final String value;
    private final String label;
}
