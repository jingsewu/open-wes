package org.openwes.wes.basic.work_station.infrastructure.persistence.mapper;

import org.openwes.wes.basic.work_station.infrastructure.persistence.po.PutWallPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PutWallPORepository extends JpaRepository<PutWallPO, Long> {

    List<PutWallPO> findAllByWorkStationIdIn(Collection<Long> workStationIds);

    List<PutWallPO> findAllByWorkStationId(Long workStationId);
}
