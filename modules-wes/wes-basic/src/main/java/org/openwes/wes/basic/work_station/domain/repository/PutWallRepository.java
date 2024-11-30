package org.openwes.wes.basic.work_station.domain.repository;

import org.openwes.wes.basic.work_station.domain.entity.PutWall;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;

public interface PutWallRepository {

    PutWall save(PutWall putWall);

    void saveAll(List<PutWall> putWalls, @NotNull Long workStationId);

    PutWall findById(Long id);

    List<PutWall> findAllByWorkStationIds(Collection<Long> workStationIds);

    List<PutWall> findAllByWorkStationId(Long workStationId);
}
