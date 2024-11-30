package org.openwes.wes.api.basic.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PutWallStatusEnum implements IEnum {

    IDLE("IDLE", "空闲"),

    WORKING("WORKING", "工作中");

    private final String value;
    private final String label;
}
