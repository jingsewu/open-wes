package org.openwes.wes.stock.domain.transfer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openwes.wes.api.stock.dto.StockCreateDTO;
import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.stock.domain.entity.ContainerStockTransaction;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContainerStockTransactionTransfer {

    @Mapping(target = "containerCode", source = "stockCreateDTO.targetContainerCode")
    @Mapping(target = "containerSlotCode", source = "stockCreateDTO.targetContainerSlotCode")
    ContainerStockTransaction fromCreateDTOtoDO(StockCreateDTO stockCreateDTO, Long skuBatchStockId);

    ContainerStockTransaction toDO(StockTransferDTO stockTransferDTO);
}
