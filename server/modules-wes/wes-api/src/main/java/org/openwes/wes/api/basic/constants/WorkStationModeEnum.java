package org.openwes.wes.api.basic.constants;

public enum WorkStationModeEnum {

    /**
     * RECEIVING
     */
    RECEIVE,
    NO_ORDER_RECEIVE,

    /**
     * PUT AWAY
     */
    SELECT_CONTAINER_PUT_AWAY,
    RECOMMENDED_CONTAINER_PUT_AWAY,
    CONTAINER_PUT_AWAY,

    /**
     * OUTBOUND
     */
    PICKING,
    EMPTY_CONTAINER_OUTBOUND,

    /**
     * STOCKTAKE
     */
    STOCKTAKE,

    /**
     * RELOCATION
     */
    ONE_STEP_RELOCATION,
    TWO_STEP_RELOCATION;;

    public static boolean isPutAwayMode(WorkStationModeEnum workStationMode) {
        return workStationMode == SELECT_CONTAINER_PUT_AWAY || workStationMode == RECOMMENDED_CONTAINER_PUT_AWAY
                || workStationMode == RECEIVE || workStationMode == NO_ORDER_RECEIVE;
    }
}
