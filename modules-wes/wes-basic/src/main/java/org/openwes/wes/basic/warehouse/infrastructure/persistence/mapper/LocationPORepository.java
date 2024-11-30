package org.openwes.wes.basic.warehouse.infrastructure.persistence.mapper;

import org.openwes.wes.basic.warehouse.infrastructure.persistence.po.LocationPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface LocationPORepository extends JpaRepository<LocationPO, Long> {

    List<LocationPO> findByAisleCodeAndWarehouseAreaId(String aisleCode, Long warehouseAreaId);

    List<LocationPO> findByWarehouseAreaId(Long warehouseAreaId);

    List<LocationPO> findByWarehouseLogicId(Long warehouseLogicId);

    List<LocationPO> findByShelfCodeIn(Collection<String> shelfCodes);

    List<LocationPO> findAllByLocationCodeInAndWarehouseAreaId(List<String> locationCodes, Long warehouseAreaId);
}
