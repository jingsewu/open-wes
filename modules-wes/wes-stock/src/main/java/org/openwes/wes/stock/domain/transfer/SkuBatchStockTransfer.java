package org.openwes.wes.stock.domain.transfer;

import org.openwes.wes.api.stock.dto.SkuBatchStockDTO;
import org.openwes.wes.api.stock.dto.StockCreateDTO;
import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.stock.domain.entity.SkuBatchStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
public interface SkuBatchStockTransfer {

    List<SkuBatchStock> toDOs(List<StockTransferDTO> stockTransferDTOS);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(source = "stockTransferDTO.transferQty", target = "totalQty")
    @Mapping(source = "stockTransferDTO.warehouseCode", target = "warehouseCode")
    @Mapping(source = "stockTransferDTO.warehouseAreaId", target = "warehouseAreaId")
    @Mapping(source = "stockTransferDTO.transferQty", target = "availableQty")
    SkuBatchStock toDO(StockTransferDTO stockTransferDTO);

    @Mapping(source = "transferQty", target = "totalQty")
    @Mapping(source = "transferQty", target = "availableQty")
    SkuBatchStock fromCreateDTOtoDO(StockCreateDTO stockCreateDTO);

    List<SkuBatchStockDTO> toDTOs(List<SkuBatchStock> stockStocks);

    SkuBatchStockDTO toDTO(SkuBatchStock skuBatchStock);
}
