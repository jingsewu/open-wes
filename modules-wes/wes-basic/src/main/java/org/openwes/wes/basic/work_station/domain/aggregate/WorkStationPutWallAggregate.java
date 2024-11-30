package org.openwes.wes.basic.work_station.domain.aggregate;

import org.openwes.wes.basic.work_station.domain.entity.PutWall;
import org.openwes.wes.basic.work_station.domain.entity.WorkStation;
import org.openwes.wes.basic.work_station.domain.repository.PutWallRepository;
import org.openwes.wes.basic.work_station.domain.repository.WorkStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkStationPutWallAggregate {

    private final WorkStationRepository workStationRepository;
    private final PutWallRepository putWallRepository;

    @Transactional(rollbackFor = Exception.class)
    public void online(List<PutWall> putWalls, WorkStation workStation) {
        putWalls.forEach(PutWall::occupy);
        putWallRepository.saveAll(putWalls, workStation.getId());
        workStationRepository.save(workStation);
    }

    @Transactional(rollbackFor = Exception.class)
    public void offline(List<PutWall> putWalls, WorkStation workStation) {
        putWalls.forEach(PutWall::release);
        putWallRepository.saveAll(putWalls, workStation.getId());
        workStationRepository.save(workStation);
    }
}
