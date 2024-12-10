package org.openwes.wes.printer.domain.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ModuleEnum implements IEnum {

    INBOUND("INBOUND", "INBOUND"),
    OUTBOUND("OUTBOUND", "OUTBOUND"),
    STOCKTAKE("STOCKTAKE", "STOCKTAKE");

    private String value;
    private String label;
}
