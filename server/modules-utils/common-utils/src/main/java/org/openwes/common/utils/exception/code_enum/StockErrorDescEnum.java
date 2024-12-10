package org.openwes.common.utils.exception.code_enum;

import org.openwes.common.utils.constants.AppCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StockErrorDescEnum implements IBaseError{
    THIS_CONTAINER_NOT_EXISTS_STOCK("ST0000001", "this container not exists stock", AppCodeEnum.WMS.name()),
    CONTAINER_STOCK_QTY_IS_ERROR("ST0000002", "wrong inventory quantity", AppCodeEnum.WMS.name()),



    ;

    private final String code;
    private final String desc;
    private final String appCode;



}
