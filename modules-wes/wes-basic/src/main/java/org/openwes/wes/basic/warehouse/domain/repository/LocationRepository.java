package org.openwes.wes.basic.warehouse.domain.repository;

import org.openwes.wes.basic.warehouse.domain.entity.Aisle;
import org.openwes.wes.basic.warehouse.domain.entity.Location;

import java.util.Collection;
import java.util.List;

public interface LocationRepository {

    void save(List<Aisle> toAisles, List<Location> toLocations);

    void save(Location location);

    void saveAll(List<Location> locations);

    void deleteAll(List<Location> locations);

    Location findById(Long id);

    List<Location> findAllByIds(List<Long> locationIds);

    List<Location> findAllByAisle(String aisleCode, Long warehouseAreaId);

    List<Location> findAllByWarehouseAreaId(Long warehouseAreaId);

    List<Location> findAllByWarehouseLogicId(Long warehouseLogicId);

    List<Location> findAllByShelfCodes(Collection<String> shelfCodes);

    List<Location> findAllByLocationCodes(List<String> locationCodes, Long warehouseAreaId);
}
