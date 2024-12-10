package org.openwes.wes.api.main.data.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WarehouseLevelEnum implements IEnum {
    A("A", "A"),
    B("B", "B"),
    C("C", "C"),
    D("D", "D"),
    E("E", "E"),
    F("F", "F");

    private final String value;
    private final String label;
}
