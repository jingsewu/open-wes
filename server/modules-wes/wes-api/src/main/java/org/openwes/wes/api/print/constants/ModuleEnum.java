package org.openwes.wes.api.print.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ModuleEnum implements IEnum {

    INBOUND("INBOUND", "INBOUND"),
    OUTBOUND("OUTBOUND", "OUTBOUND"),
    STOCKTAKE("STOCKTAKE", "STOCKTAKE");

    private final String value;
    private final String label;
}
