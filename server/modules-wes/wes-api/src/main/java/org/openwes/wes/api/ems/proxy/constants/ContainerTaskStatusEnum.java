package org.openwes.wes.api.ems.proxy.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

import java.util.Collection;
import java.util.List;

@Getter
@AllArgsConstructor
public enum ContainerTaskStatusEnum implements IEnum {

    NEW("NEW", "新任务"),
    PROCESSING("PROCESSING", "处理中"),
    /**
     * means that the container task is finished by wcs system. like container is transported by robot from the shelf to station
     */
    WCS_SUCCEEDED("WCS Succeeded", "WCS回调任务完成"),

    WCS_FAILED("WCS Failed", "WCS回调任务失败"),
    /**
     * means that the container task is finished by wms system. like container is picked by operator.
     */
    COMPLETED("COMPLETED", "已完成"),

    CANCELED("CANCELED", "取消"),
    ;

    private final String value;
    private final String label;

    private final String name = "容器任务状态";

    public static final Collection<ContainerTaskStatusEnum> processingStates = List.of(NEW, PROCESSING);


}
