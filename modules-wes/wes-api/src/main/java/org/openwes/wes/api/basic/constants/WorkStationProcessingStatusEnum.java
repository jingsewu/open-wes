package org.openwes.wes.api.basic.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WorkStationProcessingStatusEnum implements IEnum {

    NOT_TASK("NOT_TASK", "未任务"),

    WAIT_CALL_CONTAINER("WAIT_CALL_CONTAINER", "等到呼叫容器,开始今天的工作"),

    WAIT_ROBOT("WAIT_ROBOT", "等待容器到达"),

    ;


    private final String value;
    private final String label;

}







