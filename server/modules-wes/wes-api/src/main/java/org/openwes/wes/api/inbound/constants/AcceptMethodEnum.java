package org.openwes.wes.api.inbound.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AcceptMethodEnum implements IEnum {

    BOX_CONTENT("box_content", "box_content"),
    LOOSE_INVENTORY("loose_inventory", "loose_inventory"),
    CONTAINER("container", "container");

    private final String value;
    private final String label;

    private final String name = "验收类型";
}
