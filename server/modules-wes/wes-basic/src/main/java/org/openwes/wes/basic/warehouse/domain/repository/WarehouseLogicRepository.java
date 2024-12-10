package org.openwes.wes.basic.warehouse.domain.repository;

import org.openwes.wes.basic.warehouse.domain.entity.WarehouseLogic;

import java.util.List;

public interface WarehouseLogicRepository {

    void save(WarehouseLogic warehouseLogic);

    void saveAll(List<WarehouseLogic> warehouseLogics);

    WarehouseLogic findById(Long id);

    WarehouseLogic findByCode(String warehouseLogicCode);

    List<WarehouseLogic> findAllByWarehouseAreaIds(List<Long> warehouseAreaIds);
}
