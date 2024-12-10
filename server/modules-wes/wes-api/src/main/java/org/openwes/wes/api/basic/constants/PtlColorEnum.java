package org.openwes.wes.api.basic.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum PtlColorEnum implements IEnum {

    RED("RED", "红色"),
    GREEN("GREEN", "绿色"),
    BLUE("BLUE", "蓝色"),
    YELLOW("YELLOW", "黄色"),
    PINK("PINK", "粉色"),
    CYAN("CYAN", "青色"),
    WHITE("WHITE", "白色"),
    GRAY("GRAY", "灰色");

    private final String value;
    private final String label;

    private final String name = "电子标签颜色";
}
