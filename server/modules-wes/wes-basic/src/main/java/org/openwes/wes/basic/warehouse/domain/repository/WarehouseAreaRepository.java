package org.openwes.wes.basic.warehouse.domain.repository;

import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseArea;

import java.util.Collection;
import java.util.List;

public interface WarehouseAreaRepository {

    void save(WarehouseArea warehouseArea);

    void saveAll(List<WarehouseArea> warehouseAreas);

    WarehouseArea findById(Long warehouseAreaId);

    WarehouseArea findByCode(String warehouseAreaCode);

    List<WarehouseArea> findAllByIds(Collection<Long> warehouseAreaIds);

    List<WarehouseArea> findAllByWarehouseAreaGroup(String warehouseAreaGroupCode, String warehouseCode);

    List<WarehouseArea> findAllByWarehouseCode(String warehouseCode);

    List<WarehouseArea> findAllByWarehouseCodes(Collection<String> warehouseCodes);

    List<WarehouseArea> findAllByWarehouseAreaWorkType(WarehouseAreaWorkTypeEnum warehouseAreaWorkTypeEnum);
}
