package org.openwes.wes.api.stocktake.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StocktakeUnitTypeEnum implements IEnum {

    SHELF("SHELF", "按货架盘点"),
    CONTAINER("CONTAINER", "按料箱盘点"),
    SKU("SKU", "按商品盘点"),
    STOCK("STOCK", "按库存盘点"),
    ;

    private final String value;
    private final String label;
}
