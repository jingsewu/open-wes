package org.openwes.wes.basic.warehouse.infrastructure.persistence.mapper;

import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.po.WarehouseAreaPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface WarehouseAreaPORepository extends JpaRepository<WarehouseAreaPO, Long> {

    WarehouseAreaPO getByWarehouseAreaCode(String warehouseAreaCode);

    List<WarehouseAreaPO> findByWarehouseGroupCodeAndWarehouseCode(String warehouseGroupCode, String warehouseCode);

    List<WarehouseAreaPO> findByWarehouseCode(String warehouseCode);

    List<WarehouseAreaPO> findByWarehouseCodeIn(Collection<String> warehouseCodes);

    List<WarehouseAreaPO> findAllByWarehouseAreaWorkType(WarehouseAreaWorkTypeEnum warehouseAreaWorkTypeEnum);
}
