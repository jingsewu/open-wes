package org.openwes.wes.api.inbound.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PutAwayTaskStatusEnum implements IEnum {

    NEW("NEW", "新建"),

    PUTTING_AWAY("PUTTING_AWAY", "上架中"),

    PUTTED_AWAY("PUTTED_AWAY", "上架完成")
    ;


    private final String value;
    private final String label;


}
