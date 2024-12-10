package org.openwes.wes.basic.warehouse.infrastructure.persistence.transfer;

import org.openwes.wes.basic.warehouse.domain.entity.Aisle;
import org.openwes.wes.basic.warehouse.domain.entity.Location;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.po.AislePO;
import org.openwes.wes.basic.warehouse.infrastructure.persistence.po.LocationPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LocationPOTransfer {

    List<AislePO> toAislePOs(List<Aisle> aisles);

    LocationPO toPO(Location location);

    List<LocationPO> toPOs(List<Location> locations);

    List<Location> toDOs(List<LocationPO> locationPOS);

    Location toDO(LocationPO orElseThrow);
}
