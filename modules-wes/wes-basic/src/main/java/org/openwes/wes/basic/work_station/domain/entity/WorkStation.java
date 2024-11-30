package org.openwes.wes.basic.work_station.domain.entity;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.StationErrorDescEnum;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.constants.WorkStationStatusEnum;
import org.openwes.wes.api.basic.dto.PositionDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Data
@Slf4j
public class WorkStation {

    private Long id;
    private String stationCode;
    private String stationName;

    private WorkStationStatusEnum workStationStatus;

    private String warehouseCode;
    private Long warehouseAreaId;
    private boolean enable;

    private WorkStationModeEnum workStationMode;

    private List<WorkStationModeEnum> allowWorkStationModes;

    private List<WorkStationDTO.WorkLocation<? extends WorkStationDTO.WorkLocationSlot>> workLocations;

    private PositionDTO position;

    private boolean deleted;

    private Long version;

    public void enable() {
        this.enable = true;
    }

    public void disable() {
        log.info("work station id: {} stationCode: {} disable", this.id, this.stationCode);

        if (workStationStatus != WorkStationStatusEnum.OFFLINE) {
            throw WmsException.throwWmsException(StationErrorDescEnum.STATION_IS_NOT_OFF_LINE_ERROR_4_DISABLE);
        }
        this.enable = false;
    }

    public void delete() {
        log.info("work station id: {} stationCode: {} delete", this.id, this.stationCode);

        if (workStationStatus != WorkStationStatusEnum.OFFLINE) {
            throw WmsException.throwWmsException(StationErrorDescEnum.STATION_IS_NOT_OFF_LINE_ERROR_4_DELETE);
        }
        this.deleted = true;
    }

    public void online(WorkStationModeEnum workStationMode) {
        log.info("work station id: {} stationCode: {} online", this.id, this.stationCode);

        if (workStationStatus != WorkStationStatusEnum.OFFLINE) {
            throw WmsException.throwWmsException(StationErrorDescEnum.STATION_IS_NOT_OFF_LINE_ERROR_4_ONLINE);
        }
        this.workStationMode = workStationMode;
        this.workStationStatus = WorkStationStatusEnum.ONLINE;
    }

    public void offline() {
        log.info("work station id: {} stationCode: {} offline", this.id, this.stationCode);

        this.workStationStatus = WorkStationStatusEnum.OFFLINE;
        this.workStationMode = null;
    }

    public void pause() {
        log.info("work station id: {} stationCode: {} pause", this.id, this.stationCode);

        this.workStationStatus = WorkStationStatusEnum.PAUSED;
    }

    public void resume() {
        log.info("work station id: {} stationCode: {} resume", this.id, this.stationCode);

        this.workStationStatus = WorkStationStatusEnum.ONLINE;
    }

}
