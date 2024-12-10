package org.openwes.wes.api.outbound.constants;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum PickingOrderStatusEnum implements IEnum {

    NEW("NEW", "新单据"),
    DISPATCHED("DISPATCHED", "派单完成"),
    PICKING("PICKING", "拣货中"),
    HANGING("HANGING", "挂起"),
    PICKED("PICKED", "拣货完成"),
    CANCELED("CANCELED", "已取消");

    private final String value;
    private final String label;

    private final String name = "拣货单状态";

    public static boolean isFinalStatues(PickingOrderStatusEnum status) {
        return Lists.newArrayList(PICKED, CANCELED, HANGING).contains(status);
    }
}
