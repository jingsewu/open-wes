package org.openwes.wes.api.config.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum ExecuteTimeEnum implements IEnum {
    SCAN_CONTAINER("SCAN_CONTAINER", "扫容器"),
    SCAN_SKU("SCAN_SKU", "扫SKU");

    private final String value;
    private final String label;
}
