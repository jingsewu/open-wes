package org.openwes.wes.api.config.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum TransferContainerReleaseMethodEnum implements IEnum {

    /**
     * release by upstream interface
     */
    INTERFACE("INTERFACE", "接口释放"),

    /**
     * release by automated process with a scheduler
     */
    AUTOMATED("AUTOMATED", "自动释放"),

    ;
    private final String value;
    private final String label;
}
