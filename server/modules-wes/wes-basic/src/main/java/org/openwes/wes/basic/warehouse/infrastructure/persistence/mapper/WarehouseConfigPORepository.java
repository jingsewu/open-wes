package org.openwes.wes.basic.warehouse.infrastructure.persistence.mapper;

import org.openwes.wes.basic.warehouse.infrastructure.persistence.po.WarehouseConfigPO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WarehouseConfigPORepository extends JpaRepository<WarehouseConfigPO, Long> {

    WarehouseConfigPO findByWarehouseCode(String warehouseCode);
}
