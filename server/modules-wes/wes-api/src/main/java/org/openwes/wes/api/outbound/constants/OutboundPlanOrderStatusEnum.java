package org.openwes.wes.api.outbound.constants;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum OutboundPlanOrderStatusEnum implements IEnum {
    NEW("NEW", "新单据"),
    SHORT_WAITING("SHORT_WAITING", "缺货等待"),
    ASSIGNED("ASSIGNED", "分配完成(库区)"),
    DISPATCHED("DISPATCHED", "派单完成"),
    PICKING("PICKING", "拣货中"),
    PICKED("PICKED", "拣货完成"),
    CANCELED("CANCELED", "已取消");

    private final String value;
    private final String label;

    private final String name = "出库订单状态";

    public static boolean cancellability(OutboundPlanOrderStatusEnum status) {
        return Lists.newArrayList(NEW, SHORT_WAITING, ASSIGNED).contains(status);
    }

    public static boolean isFinalStatues(OutboundPlanOrderStatusEnum status) {
        return Lists.newArrayList(PICKED, CANCELED).contains(status);
    }
}
