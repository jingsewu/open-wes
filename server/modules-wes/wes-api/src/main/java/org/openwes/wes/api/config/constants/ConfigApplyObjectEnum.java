package org.openwes.wes.api.config.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConfigApplyObjectEnum implements IEnum {
    SYSTEM("SYSTEM", "系统"),
    WAREHOUSE("WAREHOUSE", "仓库"),
    OWNER("OWNER", "货主");

    private final String value;
    private final String label;
}
