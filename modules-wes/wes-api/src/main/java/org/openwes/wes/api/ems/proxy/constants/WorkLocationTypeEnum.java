package org.openwes.wes.api.ems.proxy.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum WorkLocationTypeEnum implements IEnum {

    BUFFER_SHELVING("BUFFER_SHELVING", "缓存货架"),

    ROBOT("ROBOT", "机器人"),

    CONVEYOR("CONVEYOR", "输送线");

    private final String value;
    private final String label;

    private final String name = "工作位类型";

}
