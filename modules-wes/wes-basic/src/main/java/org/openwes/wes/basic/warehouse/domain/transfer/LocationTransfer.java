package org.openwes.wes.basic.warehouse.domain.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.api.basic.dto.AisleDTO;
import org.openwes.wes.api.basic.dto.LocationDTO;
import org.openwes.wes.basic.warehouse.domain.entity.Aisle;
import org.openwes.wes.basic.warehouse.domain.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
    nullValueCheckStrategy = ALWAYS,
    nullValueMappingStrategy = RETURN_NULL,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LocationTransfer {

    List<Location> toDOs(List<LocationDTO> locationDTOS);

    List<Aisle> toAisles(List<AisleDTO> aisleDTOS);

    List<LocationDTO> toDTOs(List<Location> locations);

    LocationDTO toDTO(Location location);

}
