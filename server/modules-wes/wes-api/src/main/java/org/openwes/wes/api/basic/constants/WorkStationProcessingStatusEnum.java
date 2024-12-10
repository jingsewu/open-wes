package org.openwes.wes.api.basic.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum WorkStationProcessingStatusEnum implements IEnum {

    NO_TASK("NO_TASK", "没有任务"),

    WAIT_CALL_CONTAINER("WAIT_CALL_CONTAINER", "等到呼叫容器,开始今天的工作"),

    WAIT_ROBOT("WAIT_ROBOT", "等待容器到达"),

    ;


    private final String value;
    private final String label;

}







