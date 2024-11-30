package org.openwes.wes.api.stocktake.constants;

import com.google.common.collect.Lists;
import org.openwes.common.utils.dictionary.IEnum;
import org.openwes.wes.api.stock.constants.StockAbnormalReasonEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @see StockAbnormalReasonEnum
 */
@Getter
@AllArgsConstructor
public enum StocktakeAbnormalReasonEnum implements IEnum {

    NONE("NONE", "无异常"),
    LESS_ENTITY("LESS_ENTITY", "缺实物"),
    MORE_ENTITY("MORE_ENTITY", "多实物"),
    MORE_SKU("MORE_SKU", "多商品（盈品）"),
    ;

    private final String value;
    private final String label;

    public static final List<StocktakeAbnormalReasonEnum> ABNORMAL_LIST = Lists.newArrayList(LESS_ENTITY, MORE_ENTITY, MORE_SKU);

    public static boolean isAbnormal(StocktakeAbnormalReasonEnum reason) {
        return ABNORMAL_LIST.contains(reason);
    }
}
