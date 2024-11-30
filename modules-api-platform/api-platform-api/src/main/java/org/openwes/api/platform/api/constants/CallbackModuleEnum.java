package org.openwes.api.platform.api.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CallbackModuleEnum implements IEnum {

    INBOUND("INBOUND", "入库"),
    OUTBOUND("OUTBOUND", "出库"),
    STOCKTAKE("STOCKTAKE", "盘点"),
    INVENTORY("INVENTORY", "库存"),
    BASE_INFO("BASE_INFO", "基础信息");

    private final String value;
    private final String label;

}
