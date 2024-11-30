package org.openwes.wes.api.basic.constants;

public enum WorkStationModeEnum {

    RECEIVE,

    SELECT_CONTAINER_PUT_AWAY,
    RECOMMENDED_CONTAINER_PUT_AWAY,

    PICKING,

    STOCKTAKE,

    ONE_STEP_RELOCATION,

    TWO_STEP_RELOCATION;;

    public static boolean isPutAwayMode(WorkStationModeEnum workStationMode) {
        return workStationMode == SELECT_CONTAINER_PUT_AWAY || workStationMode == RECOMMENDED_CONTAINER_PUT_AWAY;
    }
}
