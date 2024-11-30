package org.openwes.wes.basic.work_station.infrastructure.persistence.mapper;

import org.openwes.wes.basic.work_station.infrastructure.persistence.po.WorkStationPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkStationPORepository extends JpaRepository<WorkStationPO, Long> {

    List<WorkStationPO> findAllByWarehouseCode(String warehouseCode);

}
