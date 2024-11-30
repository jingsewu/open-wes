package org.openwes.wes.api.basic.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContainerTypeEnum implements IEnum {
    CONTAINER("CONTAINER", "料箱"),
    TRANSFER_CONTAINER("TRANSFER_CONTAINER", "周转箱"),
    SHELF("SHELF", "料架"),
    PUT_WALL("PUT_WALL", "播种墙");

    private final String value;
    private final String label;
}
