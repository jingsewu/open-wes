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

    STORAGE("STORAGE", "存储"),
    OVERSTOCK("OVERSTOCK", "越库"),
    IN_TRANSIT("IN_TRANSIT", "在途"),
    ;

    private final String value;
    private final String label;
}
