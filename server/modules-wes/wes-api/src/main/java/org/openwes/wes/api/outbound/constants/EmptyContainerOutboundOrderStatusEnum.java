package org.openwes.wes.api.outbound.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum EmptyContainerOutboundOrderStatusEnum implements IEnum {

    NEW("NEW", "新单据"),

    PENDING("PENDING", "出库中"),

    FINISHED("FINISHED", "已完成"),

    ABNORMAL("ABNORMAL", "异常状态"),

    CANCELED("CANCELED", "已取消");

    private final String value;
    private final String label;

    private final String name = "空箱出库订单状态";

}
