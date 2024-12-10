package org.openwes.wes.basic.warehouse.infrastructure.repository.impl;

import org.openwes.wes.basic.warehouse.domain.entity.Aisle;
import org.openwes.wes.basic.warehouse.domain.entity.Location;
import org.openwes.wes.basic.warehouse.domain.repository.LocationRepository;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.mapper.AislePORepository;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.mapper.LocationPORepository;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.transfer.LocationPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationRepositoryImpl implements LocationRepository {

    private final LocationPORepository locationPORepository;
    private final AislePORepository aislePORepository;
    private final LocationPOTransfer locationPOTransfer;

    @Override
    public List<Location> findAllByIds(List<Long> locationIds) {
        return locationPOTransfer.toDOs(locationPORepository.findAllById(locationIds));
    }

    @Override
    @Transactional
    public void save(List<Aisle> aisles, List<Location> locations) {
        aislePORepository.saveAll(locationPOTransfer.toAislePOs(aisles));
        locationPORepository.saveAll(locationPOTransfer.toPOs(locations));
    }

    @Override
    public void save(Location location) {
        locationPORepository.save(locationPOTransfer.toPO(location));
    }

    @Override
    @Transactional
    public void saveAll(List<Location> locations) {
        locationPORepository.saveAll(locationPOTransfer.toPOs(locations));
    }

    @Override
    public Location findById(Long id) {
        return locationPOTransfer.toDO(locationPORepository.findById(id).orElseThrow());
    }

    @Override
    public List<Location> findAllByAisle(String aisleCode, Long warehouseAreaId) {
        return locationPOTransfer.toDOs(locationPORepository.findByAisleCodeAndWarehouseAreaId(aisleCode, warehouseAreaId));
    }

    @Override
    public void deleteAll(List<Location> locations) {
        locationPORepository.deleteAll(locationPOTransfer.toPOs(locations));
    }

    @Override
    public List<Location> findAllByWarehouseAreaId(Long warehouseAreaId) {
        return locationPOTransfer.toDOs(locationPORepository.findByWarehouseAreaId(warehouseAreaId));
    }

    @Override
    public List<Location> findAllByWarehouseLogicId(Long warehouseLogicId) {
        return locationPOTransfer.toDOs(locationPORepository.findByWarehouseLogicId(warehouseLogicId));
    }

    @Override
    public List<Location> findAllByShelfCodes(Collection<String> shelfCodes) {
        return locationPOTransfer.toDOs(locationPORepository.findByShelfCodeIn(shelfCodes));
    }

    @Override
    public List<Location> findAllByLocationCodes(List<String> locationCodes, Long warehouseAreaId) {
        return locationPOTransfer.toDOs(locationPORepository.findAllByLocationCodeInAndWarehouseAreaId(locationCodes, warehouseAreaId));
    }
}
