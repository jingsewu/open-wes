package org.openwes.wes.api.inbound.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum EmptyContainerInboundWayEnum implements IEnum {

    SCAN("scan", "扫描"),
    IMPORT("import", "导入");

    private final String value;
    private final String label;
}
