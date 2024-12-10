package org.openwes.wes.printer.domain.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PrintNodeEnum implements IEnum {

    PRINT_NODE_CLICK_PRINT("PRINT_NODE_CLICK_PRINT", "点击打印"),
    PRINT_NODE_SPLIT_CONTAINER("PRINT_NODE_SPLIT_CONTAINER", "拆箱"),
    PRINT_NODE_SEAL_CONTAINER("PRINT_NODE_SEAL_CONTAINER", "封箱");

    private String value;
    private String label;
}
