package org.openwes.wes.basic.warehouse.domain.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.basic.warehouse.domain.entity.WarehouseArea;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
    nullValueCheckStrategy = ALWAYS,
    nullValueMappingStrategy = RETURN_NULL,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WarehouseAreaTransfer {

    WarehouseArea toDO(WarehouseAreaDTO warehouseAreaDTODTO);

    WarehouseAreaDTO toDTO(WarehouseArea warehouseArea);

    List<WarehouseAreaDTO> toDTOs(List<WarehouseArea> warehouseAreas);
}
