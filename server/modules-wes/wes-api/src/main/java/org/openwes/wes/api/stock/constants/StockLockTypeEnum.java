package org.openwes.wes.api.stock.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum StockLockTypeEnum implements IEnum {

    INBOUND("INBOUND", "入库"),
    OUTBOUND("OUTBOUND", "出库"),
    STOCK_MOVE_IN_WAREHOUSE("STOCK_MOVE_IN_WAREHOUSE", "库内"),
    STOCK_ABNORMAL("STOCK_ABNORMAL", "库存异常"),
    ADJUSTMENT("ADJUSTMENT", "调整"),
    ;

    private final String value;
    private final String label;
}
