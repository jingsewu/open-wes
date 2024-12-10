package org.openwes.wes.basic.work_station.infrastructure.persistence.mapper;

import org.openwes.wes.basic.work_station.infrastructure.persistence.po.WorkStationConfigPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkStationConfigPORepository extends JpaRepository<WorkStationConfigPO, Long> {
    WorkStationConfigPO findByWorkStationId(Long workStationId);
}
