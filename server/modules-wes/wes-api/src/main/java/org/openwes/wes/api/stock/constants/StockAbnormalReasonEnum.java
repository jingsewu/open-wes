package org.openwes.wes.api.stock.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum StockAbnormalReasonEnum implements IEnum {

    LESS_ENTITY("LESS_ENTITY", "缺实物"),
    MORE_ENTITY("MORE_ENTITY", "多实物"),
    MORE_SKU("MORE_SKU", "多商品（盈品）");

    private final String value;
    private final String label;

}
