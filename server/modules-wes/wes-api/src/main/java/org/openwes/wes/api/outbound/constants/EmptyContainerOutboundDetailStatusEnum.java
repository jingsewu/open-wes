package org.openwes.wes.api.outbound.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum EmptyContainerOutboundDetailStatusEnum implements IEnum {

    UNDO("UNDO", "未执行"),

    DONE("DONE", "完成"),

    CANCELED("CANCELED", "已取消");

    private final String value;
    private final String label;

    private final String name = "空箱出库订单详情状态";
}
