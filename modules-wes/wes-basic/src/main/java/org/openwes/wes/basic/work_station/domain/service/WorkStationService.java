package org.openwes.wes.basic.work_station.domain.service;

import org.openwes.wes.basic.work_station.domain.entity.PutWall;
import org.openwes.wes.basic.work_station.domain.entity.WorkStation;

import java.util.List;

public interface WorkStationService {
    void validateOffline(WorkStation workStation, List<PutWall> putWalls);
}
