package org.openwes.wes.api.basic.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum PutWallDisplayOrderEnum implements IEnum {

    /**
     * 从左到右显示播种墙槽口
     */
    LEFT_TO_RIGHT("LEFT_TO_RIGHT", "从左到右显示播种墙槽口"),

    /**
     * 从右到左显示播种墙槽口
     */
    RIGHT_TO_LEFT("RIGHT_TO_LEFT", "从右到左显示播种墙槽口");

    private final String value;
    private final String label;

    private final String name = "播种墙显示顺序";
}
