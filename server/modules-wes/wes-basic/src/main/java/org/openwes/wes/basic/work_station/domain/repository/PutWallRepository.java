package org.openwes.wes.basic.work_station.domain.repository;

import org.openwes.wes.basic.work_station.domain.entity.PutWall;

import java.util.Collection;
import java.util.List;

public interface PutWallRepository {

    PutWall save(PutWall putWall);

    void saveAll(List<PutWall> putWalls, Long id);

    PutWall findById(Long id);

    List<PutWall> findAllByWorkStationIds(Collection<Long> workStationIds);

    List<PutWall> findAllPutWallsByWorkStationId(Long workStationId);

    List<PutWall> findAllByWorkStationId(Long workStationId);

    boolean existByContainerSpecCode(String containerSpecCode, String warehouseCode);
}
