package org.openwes.wes.basic.warehouse.application;

import com.google.common.collect.Lists;
import org.openwes.wes.api.basic.ILocationApi;
import org.openwes.wes.api.basic.dto.AisleDTO;
import org.openwes.wes.api.basic.dto.LocationDTO;
import org.openwes.wes.api.basic.dto.LocationUpdateDTO;
import org.openwes.wes.basic.warehouse.domain.entity.Location;
import org.openwes.wes.basic.warehouse.domain.repository.LocationRepository;
import org.openwes.wes.basic.warehouse.domain.transfer.LocationTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.Collection;
import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class LocationApiImpl implements ILocationApi {

    private final LocationRepository locationRepository;
    private final LocationTransfer locationTransfer;

    @Override
    public void createLocations(List<AisleDTO> aisleDTOS, List<LocationDTO> locationDTOS) {
        locationRepository.save(locationTransfer.toAisles(aisleDTOS), locationTransfer.toDOs(locationDTOS));
    }

    @Override
    public void update(LocationUpdateDTO locationUpdateDTO) {
        List<Location> locations = locationRepository.findAllByIds(locationUpdateDTO.getLocationIds());

        locations.forEach(v -> {
            v.setHeat(locationUpdateDTO.getHeat());
            v.setWarehouseLogicId(locationUpdateDTO.getWarehouseLogicId());
            v.setLocationStatus(locationUpdateDTO.getLocationStatus());
        });

        locationRepository.saveAll(locations);
    }

    @Override
    public void delete(Long id) {
        Location location = locationRepository.findById(id);
        location.delete();
        locationRepository.deleteAll(Lists.newArrayList(location));
    }

    @Override
    public List<LocationDTO> getByAisle(String aisleCode, Long warehouseAreaId) {
        return locationTransfer.toDTOs(locationRepository.findAllByAisle(aisleCode, warehouseAreaId));
    }

    @Override
    public List<LocationDTO> getByShelfCodes(Collection<String> shelfCodes) {
        return locationTransfer.toDTOs(locationRepository.findAllByShelfCodes(shelfCodes));
    }

    @Override
    public void deleteByAisle(String aisleCode, Long warehouseAreaId) {
        List<Location> locations = locationRepository.findAllByAisle(aisleCode, warehouseAreaId);
        locations.forEach(Location::delete);
        locationRepository.deleteAll(locations);
    }
}
