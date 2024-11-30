package org.openwes.wes.basic.warehouse.domain.repository;

import org.openwes.wes.basic.warehouse.domain.entity.WarehouseConfig;

public interface WarehouseConfigRepository {
    void save(WarehouseConfig toWarehouseConfig);

    WarehouseConfig findByWarehouseCode(String warehouseCode);
}
