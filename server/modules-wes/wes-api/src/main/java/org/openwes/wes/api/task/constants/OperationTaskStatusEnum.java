package org.openwes.wes.api.task.constants;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

import java.util.Collection;

@Getter
@AllArgsConstructor
public enum OperationTaskStatusEnum implements IEnum {

    NEW("NEW", "新任务"),
    PROCESSING("PROCESSING", "处理中"),
    PROCESSED("PROCESSED", "已完成"),
    CANCELED("CANCELED", "取消"),
    ;

    private final String value;
    private final String label;

    private final String name = "操作任务状态";

    public static boolean isStatusComplete(OperationTaskStatusEnum status) {
        return status == OperationTaskStatusEnum.PROCESSED || status == OperationTaskStatusEnum.CANCELED;
    }

    public static Collection<OperationTaskStatusEnum> unCompletedStatues() {
        return Lists.newArrayList(NEW, PROCESSING);
    }
}
