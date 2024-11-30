package org.openwes.wes.basic.warehouse.domain.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.api.basic.dto.WarehouseConfigDTO;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseConfig;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
    nullValueCheckStrategy = ALWAYS,
    nullValueMappingStrategy = RETURN_NULL,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WarehouseConfigTransfer {

    @InheritInverseConfiguration
    WarehouseConfig toDO(WarehouseConfigDTO warehouseConfigDTO);

    @Mapping(target = ".", source = "warehouseMainDataConfig")
    WarehouseConfigDTO toDTO(WarehouseConfig warehouseConfig);
}
