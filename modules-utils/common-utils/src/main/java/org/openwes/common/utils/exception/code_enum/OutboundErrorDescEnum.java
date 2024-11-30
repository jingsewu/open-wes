package org.openwes.common.utils.exception.code_enum;

import org.openwes.common.utils.constants.AppCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OutboundErrorDescEnum implements IBaseError {

    /**
     * 出站基本错误
     */
    OUTBOUND_BASE_ERROR("OUT001001", "outbound base error", AppCodeEnum.WMS.name()),

    /**
     * 拣货工作站错误
     */
    OUTBOUND_CANNOT_FIND_ARRIVED_CONTAINER("OUT002001", "cannot find arrived container", AppCodeEnum.WMS.name()),
    OUTBOUND_INCRRECT_PUT_WALL_SLOT_CODE("OUT002002", "the slot code {0} is incorrect", AppCodeEnum.WMS.name()),
    OUTBOUND_CANNOT_FIND_SCANNED_SKU("OUT002003", "please scan a sku code or a barcode", AppCodeEnum.WMS.name()),
    OUTBOUND_CANNOT_FIND_PICKING_ORDER("OUT002004", "cannot find picking order", AppCodeEnum.WMS.name()),
    OUTBOUND_CANNOT_FIND_PICKING_ORDER_DETAIL("OUT002004", "cannot find picking order detail", AppCodeEnum.WMS.name()),
    OUTBOUND_RECEIVED_TASK("OUT002005", "please complete received task", AppCodeEnum.WMS.name()),
    OUTBOUND_TRANSFER_CONTAINER_IS_NOT_BOUNDED("OUT002006", "the transfer container {0} is not bounded", AppCodeEnum.WMS.name()),
    OUTBOUND_RECEIVED_TASK_HAS_BEEN_RECEIVED("OUT002007", "the task has been received", AppCodeEnum.WMS.name()),
    OUTBOUND_RECEIVED_PICKING_CONTAINER_WRONG("OUT002008", "the transfer container has another task", AppCodeEnum.WMS.name()),
    OUTBOUND_CANNOT_FIND_OPERATION_TASK("OUT002009", "cannot find operation task", AppCodeEnum.WMS.name()),

    OUTBOUND_INVOKE_ALGO_DISPATCH_FAILED("OUT003001", "invoke algo dispatch failed, message: {0}", AppCodeEnum.WMS.name()),
    OUTBOUND_INVOKE_ALGO_DISPATCH_EMPTY("OUT003002", "invoke algo dispatch failed, result is empty", AppCodeEnum.WMS.name()),
    OUTBOUND_INVOKE_ALGO_INVENTORY_ALLOCATION_EMPTY("OUT003003", "invoke algo inventory allocation failed, result is empty", AppCodeEnum.WMS.name()),
    ;

    private final String code;
    private final String desc;
    private final String appCode;

}
