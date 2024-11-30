package org.openwes.wes.api.config.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ExecuteTimeEnum implements IEnum {
    SCAN_CONTAINER("SCAN_CONTAINER", "扫容器"),
    SCAN_SKU("SCAN_SKU", "扫SKU"),
    SCAN_SN("SCAN_SN", "扫SN");

    private final String value;
    private final String label;
}
