package org.openwes.wes.api.outbound.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum OutboundPlanOrderDetailStatusEnum implements IEnum {

    NEW("NEW", "新单据"),
    PICKED("PICKED", "拣选完成"),
    CANCELED("CANCELED", "取消"),
    ;

    private final String value;
    private final String label;

    private final String name = "出库订单明细状态";

}
