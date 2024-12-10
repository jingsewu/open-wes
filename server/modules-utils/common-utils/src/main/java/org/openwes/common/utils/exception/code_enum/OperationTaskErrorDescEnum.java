package org.openwes.common.utils.exception.code_enum;

import org.openwes.common.utils.constants.AppCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * InboundErrorDescEnum
 *
 * @author sws
 * @date 2023/02/24
 */
@Getter
@AllArgsConstructor
public enum OperationTaskErrorDescEnum implements IBaseError {

    REPEAT_REPORT_STOCK_ABNORMAL("OT010001", "do not repeat report stock abnormal.", AppCodeEnum.WMS.name()),

    INCORRECT_BARCODE("OT010002", "please check bar code.", AppCodeEnum.WMS.name()),

    BOUNDED_TRANSFER_CONTAINER("OT010003", "the transfer container {0} is bounded on another slot", AppCodeEnum.WMS.name()),

    UNAVAILABLE_TRANSFER_CONTAINER("OT010004", "the transfer container is unavailable", AppCodeEnum.WMS.name()),

    CANNOT_FIND_OPERATION_TASK("OT010005", "cannot find operation tasks", AppCodeEnum.WMS.name()),

    PUT_WALL_SLOT_STATUS_NOT_BOUNDED("OT010006", "put wall slot {0} is not bounded,can't be unbounded", AppCodeEnum.WMS.name()),

    CONTAINER_IS_DISPATCHED("OT010007", "container {0} is dispatched,can't be unbounded", AppCodeEnum.WMS.name()),

    OPERATION_TASK_IS_PROCESSED("OT010008", "operation task {0} is processed, can't to be operation", AppCodeEnum.WMS.name()),

    OPERATE_MULTI_CONTAINER_STOCK("OT010009", "only operate one container stock at once", AppCodeEnum.WMS.name()),

    CANNOT_REPORT_FULL_STOCK_ABNORMAL("OT010010", "cannot report full pick abnormal", AppCodeEnum.WMS.name()),
    ;

    private final String code;
    private final String desc;
    private final String appCode;

}
