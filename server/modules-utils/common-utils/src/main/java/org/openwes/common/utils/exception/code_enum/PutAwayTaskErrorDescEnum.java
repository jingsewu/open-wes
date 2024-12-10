package org.openwes.common.utils.exception.code_enum;

import org.openwes.common.utils.constants.AppCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PutAwayTaskErrorDescEnum implements IBaseError {
    PUT_AWAY_TASK_NOT_EXISTS("PUT001001", "put away task not exists", AppCodeEnum.WMS.name()),
    PUTTING_AWAY_CAN_NOT_SWITCH("PUT001002", "putting away cannot switch putAway type", AppCodeEnum.WMS.name()),
    PUTTING_AWAY_CONTAINER_SLOT_CODE_EMPTY("PUT001003", "please select a container storage location", AppCodeEnum.WMS.name()),
    PUTTING_AWAY_QTY_LESS_THAN_ONE("PUT001004", "The quantity on the shelf cannot be less than 1", AppCodeEnum.WMS.name()),
    PUTTING_AWAY_QTY_MORE_THAN_RESTOCKED("PUT001005", "The quantity on the shelf cannot exceed the required quantity", AppCodeEnum.WMS.name()),

    ;

    private final String code;
    private final String desc;
    private final String appCode;

}
