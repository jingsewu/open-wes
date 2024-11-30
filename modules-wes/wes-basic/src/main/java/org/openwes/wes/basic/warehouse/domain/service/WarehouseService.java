package org.openwes.wes.basic.warehouse.domain.service;

import org.openwes.wes.basic.warehouse.domain.entity.Location;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseAreaGroup;

import java.util.List;

public interface WarehouseService {

    boolean validate(WarehouseAreaGroup warehouseAreaGroup);

    List<Location> getLocationsByWarehouseAreaGroup(WarehouseAreaGroup warehouseAreaGroup);

}
