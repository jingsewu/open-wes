package org.openwes.common.utils.exception.code_enum;

import org.openwes.common.utils.constants.AppCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmptyContainerInboundErrorDescEnum implements IBaseError {

    CAN_NOT_FIND_EMPTY_CONTAINER_INBOUND_ORDER("ECI001001", "cannot find empty container inbound order", AppCodeEnum.WMS.name()),
    CAN_NOT_FIND_EMPTY_CONTAINER_INBOUND_ORDER_DETAIL("ECI001002", "cannot find empty container inbound order details", AppCodeEnum.WMS.name()),

    ;

    private final String code;
    private final String desc;
    private final String appCode;
}
