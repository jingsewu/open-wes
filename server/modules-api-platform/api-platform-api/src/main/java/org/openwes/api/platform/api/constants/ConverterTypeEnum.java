package org.openwes.api.platform.api.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConverterTypeEnum implements IEnum {

    NONE("NONE", "NONE"),
    JS("javascript", "javascript"),
    TEMPLATE("template", "template");

    private final String value;
    private final String label;

}
