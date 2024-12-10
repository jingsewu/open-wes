package org.openwes.wes.basic.main.data.domain.transfer;

import org.openwes.wes.api.main.data.dto.WarehouseMainDataDTO;
import org.openwes.wes.basic.main.data.domain.entity.WarehouseMainData;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WarehouseMainDataTransfer {

    WarehouseMainData toDO(WarehouseMainDataDTO warehouseDTO);

    WarehouseMainDataDTO toDTO(WarehouseMainData warehouseMainData);

    Collection<WarehouseMainDataDTO> toDOs(Collection<WarehouseMainData> warehouseMainDatas);
}
