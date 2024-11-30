package org.openwes.wes.api.basic.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WarehouseAreaCodeEnum implements IEnum {

    /**
     * 上架暂存区
     */
    PUTTING_ON_THE_STAGING_AREA("PUTTING_ON_THE_STAGING_AREA", "上架暂存区"),

    /**
     * 下架暂存区
     */
    OFF_SHELF_TEMPORARY_STORAGE_AREA("OFF_SHELF_TEMPORARY_STORAGE_AREA", "下架暂存区");

    private final String value;
    private final String label;
}
