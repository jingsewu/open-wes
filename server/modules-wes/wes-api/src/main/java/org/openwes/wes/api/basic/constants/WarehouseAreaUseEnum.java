package org.openwes.wes.api.basic.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WarehouseAreaUseEnum implements IEnum {
    RECEIVE("RECEIVE", "收货"),
    PUT_AWAY_HOLDER("PUT_AWAY_HOLDER", "上架暂存"),
    PICK("PICK", "拣选");

    private final String value;
    private final String label;
}
