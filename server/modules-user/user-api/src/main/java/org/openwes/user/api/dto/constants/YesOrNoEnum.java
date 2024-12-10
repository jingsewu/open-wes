package org.openwes.user.api.dto.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum YesOrNoEnum implements IEnum {
    YES("1", "是"),
    NO("0", "否");

    private final String value;
    private final String label;

}
