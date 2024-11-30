package org.openwes.api.platform.api.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum ApiLogStatusEnum implements IEnum {

    NEW("NEW", "未处理"),
    SUCCESS("SUCCESS", "处理成功"),
    FAIL("FAIL", "处理失败");

    private final String value;
    private final String label;

    private final String name = "接口日志状态";
}
