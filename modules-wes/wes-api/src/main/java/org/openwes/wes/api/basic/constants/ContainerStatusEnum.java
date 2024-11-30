package org.openwes.wes.api.basic.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ContainerStatusEnum implements IEnum {

    IN_SIDE("IN_SIDE", "在库内"),

    OUT_SIDE("OUT_SIDE", "在库外");

    private final String value;
    private final String label;

}
