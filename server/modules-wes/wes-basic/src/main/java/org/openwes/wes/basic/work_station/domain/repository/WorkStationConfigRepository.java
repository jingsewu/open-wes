package org.openwes.wes.basic.work_station.domain.repository;

import org.openwes.wes.basic.work_station.domain.entity.WorkStationConfig;

public interface WorkStationConfigRepository {

    void save(WorkStationConfig workStationConfig);

    WorkStationConfig findByWorkStationId(Long workStationId);
}
