package org.openwes.wes.basic.warehouse.domain.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.api.basic.dto.WarehouseLogicDTO;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseLogic;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
    nullValueCheckStrategy = ALWAYS,
    nullValueMappingStrategy = RETURN_NULL,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WarehouseLogicTransfer {

    WarehouseLogic toDO(WarehouseLogicDTO warehouseLogicDTO);

    WarehouseLogicDTO toDTO(WarehouseLogic warehouseLogic);
}
