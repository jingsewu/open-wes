package org.openwes.wes.api.stock.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockAbnormalTypeEnum implements IEnum {
    PICKING("PICKING", "拣货异常"),
    STOCK_TAKE("STOCK_TAKE", "盘点异常"),
    TOTE_RELOCATION("TOTE_RELOCATION", "理库异常");

    private final String value;
    private final String label;

}
