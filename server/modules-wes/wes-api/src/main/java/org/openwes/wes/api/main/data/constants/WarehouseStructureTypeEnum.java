package org.openwes.wes.api.main.data.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WarehouseStructureTypeEnum implements IEnum {

    STEEL("STEEL", "STEEL"),
    FLOOR("FLOOR", "FLOOR"),
    FLAT("FLAT", "FLAT"),
    OUTSIDE("OUTSIDE", "OUTSIDE");

    private final String value;
    private final String label;
}
