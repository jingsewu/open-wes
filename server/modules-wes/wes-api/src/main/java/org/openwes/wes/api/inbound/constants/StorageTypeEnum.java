package org.openwes.wes.api.inbound.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 存储类型枚举类型
 */
@Getter
@AllArgsConstructor
public enum StorageTypeEnum implements IEnum {

    STORAGE("storage", "存储"),
    OVERSTOCK("overstock", "越库"),
    IN_TRANSIT("in_transit", "在途"),
    ;

    private String value;
    private String label;
}
