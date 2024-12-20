package org.openwes.wes.api.ems.proxy.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum ContainerOperationTypeEnum implements IEnum {

    MOVE_OUT("MOVE_OUT", "取出"),
    ABNORMAL("ABNORMAL", "流向异常口"),
    LEAVE("LEAVE", "离开");

    private final String value;
    private final String label;

    private final String name = "容器操作类型";
}
