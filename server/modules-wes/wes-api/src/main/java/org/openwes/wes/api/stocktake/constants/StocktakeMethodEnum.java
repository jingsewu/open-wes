package org.openwes.wes.api.stocktake.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StocktakeMethodEnum implements IEnum {

    INFORMED("INFORMED", "明盘"),
    BLIND("BLIND", "暗盘");

    private final String value;
    private final String label;
}
