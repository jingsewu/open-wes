package org.openwes.api.platform.api.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConverterTypeEnum implements IEnum {

    NONE("NONE", "NONE"),
    JS("JS", "javascript"),
    TEMPLATE("TEMPLATE", "template");

    private final String value;
    private final String label;

}
