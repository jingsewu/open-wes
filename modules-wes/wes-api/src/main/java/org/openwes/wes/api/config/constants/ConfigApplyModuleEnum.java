package org.openwes.wes.api.config.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConfigApplyModuleEnum implements IEnum {
    INBOUND("INBOUND", "入库"),
    OUTBOUND("OUTBOUND", "出库"),
    STOCK_TAKE("STOCK_TAKE", "盘点"),
    STOCK("STOCK", "库存"),
    RELOCATION("RELOCATION", "理货");

    private final String value;
    private final String label;
}
