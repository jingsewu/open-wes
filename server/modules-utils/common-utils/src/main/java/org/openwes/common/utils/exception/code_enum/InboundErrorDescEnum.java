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
public enum InboundErrorDescEnum implements IBaseError {

    // inbound plan order
    INBOUND_LPN_CODE_REPEATED("IN001000", "inbound lpn code {0} repeated", AppCodeEnum.WMS.name()),
    INBOUND_CST_ORDER_NO_REPEATED("IN001001", "inbound customer order no {0} repeated", AppCodeEnum.WMS.name()),
    INBOUND_STATUS_ERROR("IN001002", "operation failed cause inbound status {0} error", AppCodeEnum.WMS.name()),
    INBOUND_OVER_ACCEPT_ERROR("IN001003", "inbound sku {0} over accept", AppCodeEnum.WMS.name()),
    INBOUND_BOX_NO_ERROR("IN001004", "inbound box no {0} error , can not find", AppCodeEnum.WMS.name()),
    INBOUND_BOX_NO_EXIST("IN001005", "inbound box no existing", AppCodeEnum.WMS.name()),
    INBOUND_NOT_ALLOWED_MULTI_ARRIVALS("IN001006", "inbound order {0} does not allow multiple arrivals", AppCodeEnum.WMS.name()),
    INBOUND_IDENTIFY_NO_NOT_FOUND("IN001007", "inbound order identify no {0} cannot be found", AppCodeEnum.WMS.name()),
    INBOUND_CLOSE_ACCEPT_ORDER_NOT_COMPLETED("IN001008", "inbound order has accept orders not completed, can't be close", AppCodeEnum.WMS.name()),

    /**
     * 入库订单详细信息不存在
     */
    INBOUND_ORDER_DETAIL_NOT_EXISTS("IN001009", "inbound order detail not exists", AppCodeEnum.WMS.name()),
    /**
     * 入库订单不能重复接收
     */
    INBOUND_ORDER_CAN_NOT_REPEAT_RECEIVE("IN001010", "inbound order {0} station [{1}] already received can not repeat receive ", AppCodeEnum.WMS.name()),
    HAVE_SAME_DETAIL("IN001018", "inbound order {0} have same detail ", AppCodeEnum.WMS.name()),

    /**
     * 请先收货
     */
    PLEASE_RECEIPT_OF_RECEIPT_FIRST("IN001011", "please receipt of receipt first", AppCodeEnum.WMS.name()),
    PLEASE_SCAN_ORDER_OR_LPN_FIRST("IN001012", "please scan order or lpn first", AppCodeEnum.WMS.name()),
    SWITCH_TYPE_PLEASE_CLEAR_SCAN_CODE_FIRST("IN001013", "Before switching types, please clear the scanned barcode or complete the task of scanning the barcode.", AppCodeEnum.WMS.name()),
    /**
     * 整箱只接收新订单
     */
    WHOLE_BOX_RECEIVE_ONLY_NEW_ORDER("IN001014", "only new documents can be accepted for full box shipments", AppCodeEnum.WMS.name()),
    INBOUND_ORDER_FINISHED("IN001015", "The receipt document has been completed and the document cannot be collected", AppCodeEnum.WMS.name()),
    INBOUND_RECEIVE_ORDER_LIMIT("IN001016", "exceeded the usage limit {0}", AppCodeEnum.WMS.name()),
    INBOUND_ORDER_EXISTS_PLEASE_CANCEL_OR_FINISH_FIRST("IN001017", "lpn {0} exists ,cancel first please. lpn {1} exists, finish first please", AppCodeEnum.WMS.name()),


    // receive order
    INBOUND_SKU_NOT_MATCH_ERROR("IN002001", "inbound sku {0} not match or finished", AppCodeEnum.WMS.name()),
    INBOUND_MUST_RECEIVE_FIRST("IN002002", "You must first receive the warehousing document or confirm skuCodes before calling the container", AppCodeEnum.WMS.name()),


    // accept order
    ACCEPT_ORDER_HAD_COMPLETED("IN003001", "accept order {0} had completed", AppCodeEnum.WMS.name()),
    ACCEPT_BOX_ALREADY("IN003002", "accept box no {0} already", AppCodeEnum.WMS.name()),

    //replenish,
    MUST_CONFIRM_SKU_INFO("IN004001", "When calling the container, the sku information must be confirmed", AppCodeEnum.WMS.name()),
    PLEASE_PUT_THE_SKU_INTO_THE_EMPTY_SLOT("IN004002", "Please put the sku into the empty slot", AppCodeEnum.WMS.name()),
    THERE_IS_THE_SAME_SKU_ON_THE_OTHER_SIDE_OF_THE_CONTAINER("IN004003", "There is the same sku on the other side of the container", AppCodeEnum.WMS.name()),
    THE_SHELVES_ON_THE_ROAD_HAVE_THE_SAME_SKU("IN004004", "The container on the road have the same sku", AppCodeEnum.WMS.name()),
    NO_MATCH_CONTAINER("IN004004", "no matching container", AppCodeEnum.WMS.name());


    private final String code;
    private final String desc;
    private final String appCode;

}
