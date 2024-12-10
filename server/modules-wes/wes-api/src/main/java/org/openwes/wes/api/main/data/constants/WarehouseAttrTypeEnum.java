package org.openwes.wes.api.main.data.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.dictionary.IEnum;

@Getter
@AllArgsConstructor
public enum WarehouseAttrTypeEnum implements IEnum {

    NORMAL("NORMAL", "正常"),
    COLD_CHAIN("COLD_CHAIN", "冷链"),
    DANGEROUS("DANGEROUS", "危险"),
    BONDED("BONDED", "保税");

    private final String value;
    private final String label;
}
