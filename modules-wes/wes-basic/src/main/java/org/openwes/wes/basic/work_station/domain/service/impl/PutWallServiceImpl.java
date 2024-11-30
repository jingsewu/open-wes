package org.openwes.wes.basic.work_station.domain.service.impl;

import org.openwes.wes.api.basic.constants.WorkStationStatusEnum;
import org.openwes.wes.basic.work_station.domain.entity.PutWall;
import org.openwes.wes.basic.work_station.domain.entity.WorkStation;
import org.openwes.wes.basic.work_station.domain.repository.WorkStationRepository;
import org.openwes.wes.basic.work_station.domain.service.PutWallService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PutWallServiceImpl implements PutWallService {

    private final WorkStationRepository workStationRepository;

    @Override
    public boolean checkDisablePutWall(PutWall putWall) {
        WorkStation workStation = workStationRepository.findById(putWall.getWorkStationId());
        return workStation == null || workStation.getWorkStationStatus() == WorkStationStatusEnum.OFFLINE;
    }

}
