package org.openwes.wes.api.config.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UnionLocationEnum implements IEnum {
    LEFT("LEFT", "左"), RIGHT("RIGHT", "右");

    private final String value;
    private final String label;
}
