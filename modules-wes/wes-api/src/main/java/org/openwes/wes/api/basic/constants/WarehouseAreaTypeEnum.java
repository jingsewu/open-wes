package org.openwes.wes.api.basic.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WarehouseAreaTypeEnum implements IEnum {

    STORAGE_AREA("STORAGE_AREA", "存储区"),

    STORAGE_CACHE("STORAGE_CACHE", "暂存区"),

    PICKING_STORAGE_CACHE("PICKING_STORAGE_CACHE", "下架暂存区"),

    ABNORMAL_AREA("ABNORMAL_AREA", "异常区");

    private final String value;
    private final String label;
}
