package org.openwes.wes.api.basic.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum PtlUpdownEnum implements IEnum {

    /**
     * 表示电子标签亮灯后不能拍灭
     */
    UNTAPABLE("UNTAPABLE", "不允许拍灭"),

    /**
     * 表示电子标签亮灯后可以拍灭
     */
    TAPABLE("TAPABLE", "允许拍灭");


    private final String value;
    private final String label;

    private final String name = "PTL_UPDOWN";
}
