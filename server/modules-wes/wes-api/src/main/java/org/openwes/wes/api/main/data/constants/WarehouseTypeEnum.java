package org.openwes.wes.api.main.data.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum WarehouseTypeEnum implements IEnum {
    CENTER_WAREHOUSE("CENTER_WAREHOUSE", "中央仓库"),
    DELIVERY_WAREHOUSE("DELIVERY_WAREHOUSE", "分发仓"),
    TRANSIT_WAREHOUSE("TRANSIT_WAREHOUSE", "中转仓"),
    HUB_WAREHOUSE("HUB_WAREHOUSE", "枢纽仓"),
    FACTORY_WAREHOUSE("FACTORY_WAREHOUSE", "工厂仓"),
    RETURN_WAREHOUSE("RETURN_WAREHOUSE", "退货仓"),
    FRONT_WAREHOUSE("FRONT_WAREHOUSE", "前置仓");

    private final String value;
    private final String label;
}
