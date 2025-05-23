package org.openwes.wes.api.print.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.dictionary.IEnum;

@Slf4j
@Getter
@AllArgsConstructor
public enum PrintStatusEnum implements IEnum {

    PRINT_SUCCESS("PRINT_SUCCESS", "打印成功"),
    PRINT_FAIL("PRINT_FAIL", "打印失败"),
    NOT_PRINTED("NOT_PRINTED", "未打印"),
    ;

    private final String value;
    private final String label;

    private final String name = "打印状态";

}
