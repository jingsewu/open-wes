package org.openwes.wes.basic.warehouse.domain.service.impl;

import org.openwes.wes.basic.warehouse.domain.entity.Location;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseArea;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseAreaGroup;
import org.openwes.wes.basic.warehouse.domain.repository.LocationRepository;
import org.openwes.wes.basic.warehouse.domain.repository.WarehouseAreaRepository;
import org.openwes.wes.basic.warehouse.domain.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseAreaRepository warehouseAreaRepository;
    private final LocationRepository locationRepository;

    @Override
    public boolean validate(WarehouseAreaGroup warehouseAreaGroup) {

        List<WarehouseArea> warehouseAreas = warehouseAreaRepository
                .findAllByWarehouseAreaGroup(warehouseAreaGroup.getWarehouseAreaGroupCode(), warehouseAreaGroup.getWarehouseCode());

        return warehouseAreas.stream().anyMatch(warehouseArea -> {
            List<Location> locations = locationRepository.findAllByWarehouseAreaId(warehouseArea.getId());
            return locations.stream().anyMatch(Location::isOccupied);
        });
    }

    @Override
    public List<Location> getLocationsByWarehouseAreaGroup(WarehouseAreaGroup warehouseAreaGroup) {
        List<WarehouseArea> warehouseAreas = warehouseAreaRepository
                .findAllByWarehouseAreaGroup(warehouseAreaGroup.getWarehouseAreaGroupCode(), warehouseAreaGroup.getWarehouseCode());
        return warehouseAreas.stream().flatMap(warehouseArea ->
                locationRepository.findAllByWarehouseAreaId(warehouseArea.getId()).stream()).toList();
    }

}
