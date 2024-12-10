package org.openwes.common.utils.exception.code_enum;

import org.openwes.common.utils.constants.AppCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MovementErrorDescEnum implements IBaseError {
    MOVEMENT_ORDER_NOT_EXIST("MOV001001", "movement order not exist", AppCodeEnum.WMS.name()),

    ;

    private final String code;
    private final String desc;
    private final String appCode;

}
