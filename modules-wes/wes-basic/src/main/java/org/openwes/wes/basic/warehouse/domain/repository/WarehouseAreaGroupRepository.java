package org.openwes.wes.basic.warehouse.domain.repository;

import org.openwes.wes.basic.warehouse.domain.entity.WarehouseAreaGroup;

public interface WarehouseAreaGroupRepository {

    void save(WarehouseAreaGroup warehouseAreaGroup);

    WarehouseAreaGroup findById(Long id);

}
