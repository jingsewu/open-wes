package org.openwes.station.api.constants;

public enum ApiCodeEnum {

    /**
     * common api codes
     */
    ONLINE,
    OFFLINE,
    PAUSE,
    RESUME,
    CALL_ROBOT,
    CALL_CONTAINER,

    CONTAINER_ARRIVED,
    CONTAINER_REFRESH,
    TAP_PTL,
    INPUT,
    UNBIND,
    TAP_PUT_WALL_SLOT,
    SCAN_BARCODE,
    CHOOSE_AREA,
    CHOOSE_PUT_WALL,
    CLOSE_TIP,
    EMPTY_CONTAINER_HANDLE,

    SCAN_BARCODE_RESET,


    /**
     * Outbound api codes
     */
    SPLIT_TASKS,
    REPORT_ABNORMAL_TIP,
    REPORT_ABNORMAL,
    VALIDATE_TRANSFER_CONTAINER_PICKING,

    /**
     * Stocktake api codes
     */
    STOCKTAKE_SUBMIT,

    /**
     * Custom api codes
     */
    CUSTOM_API,
    ;
}
