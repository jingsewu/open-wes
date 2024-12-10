package org.openwes.wes.api.inbound.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

import java.util.List;

@Getter
@AllArgsConstructor
public enum InboundPlanOrderStatusEnum implements IEnum {

    NEW("NEW", "新单据"),

    ACCEPTING("ACCEPTING", "收货中"),

    ACCEPTED("ACCEPTED", "收货完成"),

    CANCEL("CANCEL", "取消"),

    CLOSED("CLOSED", "关闭"),
    ;

    private final String value;
    private final String label;

    private final String name = "入库单状态";

    public static boolean isCompleted(InboundPlanOrderStatusEnum status) {
        return List.of(ACCEPTED, CANCEL, CLOSED).contains(status);
    }

}
