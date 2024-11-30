package org.openwes.wes.api.basic.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WarehouseAreaWorkTypeEnum implements IEnum {

    MANUAL("MANUAL", "人工区"),
    ROBOT("ROBOT", "机器人区");

    private final String value;
    private final String label;
}
