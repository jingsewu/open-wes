package org.openwes.wes.api.inbound.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AcceptTypeEnum implements IEnum {

    DIRECT("direct", "direct"),
    RECEIVE("receive", "receive"),
    IN_WAREHOUSE("in_warehouse", "in_warehouse"),
    WAYBILL("waybill", "waybill"),
    WITHOUT_ORDER("without_order", "without_order"),
    ;

    private String value;
    private String label;

}
