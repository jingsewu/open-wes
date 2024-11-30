package org.openwes.wes.basic.main.data.domain.repository;

import org.openwes.wes.basic.main.data.domain.entity.WarehouseMainData;

import java.util.Collection;

public interface WarehouseMainDataRepository {

    void save(WarehouseMainData warehouse);

    WarehouseMainData findById(Long id);

    WarehouseMainData findByWarehouseCode(String warehouseCode);

    Collection<WarehouseMainData> findAllByWarehouseCodes(Collection<String> warehouseCodes);

    Collection<WarehouseMainData> findAll();
}
