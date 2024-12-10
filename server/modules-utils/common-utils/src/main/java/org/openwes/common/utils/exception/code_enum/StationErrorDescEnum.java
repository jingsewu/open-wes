package org.openwes.common.utils.exception.code_enum;

import org.openwes.common.utils.constants.AppCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StationErrorDescEnum implements IBaseError {

    //station error SAT01
    STATION_ID_IS_NOT_CONFIGURED("SAT010001", "station id is not configured,please configure it using browser plugin", AppCodeEnum.WMS.name()),
    STATION_SSE_CONNECT_ERROR("SAT010002", "cannot find station sse connect error, station id : {}", AppCodeEnum.WMS.name()),
    STATION_HAS_TASK_ERROR("SAT010003", "please complete work before offline", AppCodeEnum.WMS.name()),
    STATION_IS_NOT_OFF_LINE_ERROR_4_ONLINE("SAT010004", "work station is not offline and can not online", AppCodeEnum.WMS.name()),
    STATION_IS_NOT_OFF_LINE_ERROR_4_DELETE("SAT010005", "work station is not offline and can not delete.", AppCodeEnum.WMS.name()),
    STATION_IS_NOT_OFF_LINE_ERROR_4_DISABLE("SAT010006", "work station is not offline and can not disable.", AppCodeEnum.WMS.name()),
    STATION_NOT_EXISTS_OR_ALREADY_OFF_LINE("SAT010007", "station already offline or not exits", AppCodeEnum.WMS.name()),
    STATION_ONLINE_OPERATION_TYPE_CAN_NOT_BE_NULL("SAT010008", "station online operation type must not be null", AppCodeEnum.WMS.name()),
    STATION_CACHE_IS_NULL("SAT010009", "stationCache {0} is null", AppCodeEnum.WMS.name()),

    //work station location error SAT02
    STATION_CONVEYER_LOCATION_ERROR("SAT020001", "cannot find conveyer location code", AppCodeEnum.WMS.name()),


    //put wall error SAT03
    PUT_WALL_SLOT_ORDERS_EXIST("SAT030001", "put wall slot orders exist", AppCodeEnum.WMS.name()),
    PUT_WALL_SLOT_NOT_EXIST("SAT030002", "put wall slot {0} not exist", AppCodeEnum.WMS.name()),
    PUT_WALL_SLOT_STATUS_ABNORMAL("SAT030004", "put wall slot {0} status {1} abnormal", AppCodeEnum.WMS.name()),
    PUT_WALL_SLOT_STATUS_OCCUPANCY("SAT030005", "the put wall slot {0} is not waiting to bind", AppCodeEnum.WMS.name()),
    PUT_WALL_SLOT_NOT_BOUND("SAT030006", "There are some put wall slot have not bound", AppCodeEnum.WMS.name()),


    //work station config error SAT04


    //work station rules error SAT05
    STATION_CANT_SUPPORT_THIS_TYPE("SAT050001", "work station {0} can not support this type", AppCodeEnum.WMS.name()),

    ;


    private final String code;
    private final String desc;
    private final String appCode;

}
