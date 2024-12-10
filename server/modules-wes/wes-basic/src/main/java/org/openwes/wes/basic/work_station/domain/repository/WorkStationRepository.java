package org.openwes.wes.basic.work_station.domain.repository;

import org.openwes.wes.basic.work_station.domain.entity.WorkStation;

import java.util.List;

public interface WorkStationRepository {

    void save(WorkStation workStation);

    WorkStation findById(Long id);

    List<WorkStation> findAllByWarehouseCode(String warehouseCode);

}
