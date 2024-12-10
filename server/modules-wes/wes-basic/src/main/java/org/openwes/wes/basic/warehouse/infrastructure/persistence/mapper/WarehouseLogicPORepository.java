package org.openwes.wes.basic.warehouse.infrastructure.persistence.mapper;

import org.openwes.wes.basic.warehouse.infrastructure.persistence.po.WarehouseLogicPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface WarehouseLogicPORepository extends JpaRepository<WarehouseLogicPO, Long> {

    WarehouseLogicPO findByWarehouseLogicCode(String warehouseLogicCode);

    List<WarehouseLogicPO> findByWarehouseAreaIdIn(Collection<Long> warehouseAreaIds);
}
