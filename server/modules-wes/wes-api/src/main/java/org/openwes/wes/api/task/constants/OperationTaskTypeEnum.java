package org.openwes.wes.api.task.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

import java.io.Serializable;
import java.util.List;

@Getter
@AllArgsConstructor
public enum OperationTaskTypeEnum implements Serializable, IEnum {

    PUT_AWAY("PUT_AWAY", "上架"),
    PICKING("PICKING", "拣选"),
    // pick sku from a container in the storage area to relocation area.
    TWO_STEP_RELOCATION("TWO_STEP_RELOCATION", "两步式理货"),
    ONE_STEP_RELOCATION("ONE_STEP_RELOCATION", "一步式理货"),

    STOCK_TAKE("STOCK_TAKE", "盘点"),

    RECHECK("RECHECK", "复核"),

    ADJUST("ADJUST", "调整"),
    ;

    private final String value;
    private final String label;

    private final String name = "操作任务任务";

    public static List<OperationTaskTypeEnum> replenishmentType() {
        return List.of(PUT_AWAY);
    }
}
