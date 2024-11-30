package org.openwes.wes.basic.work_station.domain.service.impl;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.StationErrorDescEnum;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.basic.work_station.domain.entity.PutWall;
import org.openwes.wes.basic.work_station.domain.entity.WorkStation;
import org.openwes.wes.basic.work_station.domain.service.WorkStationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkStationServiceImpl implements WorkStationService {

    @Override
    public void validateOffline(WorkStation workStation, List<PutWall> putWalls) {
        if (putWalls == null) {
            return;
        }

        if (putWalls.stream().filter(v -> v.getPutWallSlots() != null).flatMap(v -> v.getPutWallSlots().stream())
                .anyMatch(v -> v.getPutWallSlotStatus() != PutWallSlotStatusEnum.IDLE)) {
            throw WmsException.throwWmsException(StationErrorDescEnum.STATION_HAS_TASK_ERROR);
        }
    }
}
