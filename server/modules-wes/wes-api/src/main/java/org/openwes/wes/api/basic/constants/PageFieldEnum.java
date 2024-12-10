package org.openwes.wes.api.basic.constants;

import com.google.common.collect.Lists;
import org.openwes.common.utils.constants.FieldColors;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public enum PageFieldEnum {

    MAX_PRIORITY(Lists.newArrayList(OperationTaskTypeEnum.PICKING), "maxPriority", "优先级", false, FieldColors.BLACK.getName(), false),
    REQUIRED_QTY(Lists.newArrayList(OperationTaskTypeEnum.PICKING), "requiredQty", "出库数量", false, FieldColors.BLACK.getName(), false),
    TO_BE_OPERATED_QTY(Lists.newArrayList(OperationTaskTypeEnum.PICKING), "tobeOperatedQty", "拣货数量", false, FieldColors.BLACK.getName(), false),
    OPERATED_QTY(Lists.newArrayList(OperationTaskTypeEnum.PICKING), "operatedQty", "已拣货数量", false, FieldColors.BLACK.getName(), false);

    private final List<OperationTaskTypeEnum> supportTaskTypes;
    private final String fieldName;
    private final String fieldDesc;
    private final boolean display;
    private final String color;
    private final boolean bold;

}
