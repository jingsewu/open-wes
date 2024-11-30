package org.openwes.wes.api.stock.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockAdjustmentOrderStatusEnum implements IEnum {

    NEW("NEW", "新单据"),
    COMPLETE("COMPLETE", "调整完成"),
    CLOSED("CLOSED", "关闭");

    private final String value;
    private final String label;
}
