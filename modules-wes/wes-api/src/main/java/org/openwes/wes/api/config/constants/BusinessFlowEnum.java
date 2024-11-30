package org.openwes.wes.api.config.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BusinessFlowEnum implements IEnum {

    INBOUND("INBOUND", "入库"),
    OUTBOUND("OUTBOUND", "出库"),
    STOCK_TAKE("STOCK_TAKE", "盘点");

    private final String value;
    private final String label;
}
