package org.openwes.wes.basic.main.data.infrastructure.persistence.mapper;

import org.openwes.wes.basic.main.data.infrastructure.persistence.po.WarehouseMainDataPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface WarehouseMainDataPORepository extends JpaRepository<WarehouseMainDataPO, Long> {

    WarehouseMainDataPO findByWarehouseCode(String warehouseCode);

    Collection<WarehouseMainDataPO> findByWarehouseCodeIn(Collection<String> warehouseCodes);

}
