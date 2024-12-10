package org.openwes.wes.api.stocktake.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StocktakeCreateMethodEnum implements IEnum {

    MANUAL("MANUAL", "手工"),
    IMPORT("IMPORT", "导入"),
    EXTERNAL_API("EXTERNAL_API", "外部接口");

    private final String value;
    private final String label;
}
