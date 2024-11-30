package org.openwes.wes.stock.domain.transfer;

import org.openwes.wes.api.stock.dto.StockCreateDTO;
import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.stock.domain.entity.ContainerStockTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContainerStockTransactionTransfer {

    ContainerStockTransaction toDO(StockTransferDTO stockTransferDTO);

    ContainerStockTransaction fromCreateDTOtoDO(StockCreateDTO stockCreateDTO, Long skuBatchStockId);
}
