package org.openwes.wes.stock.domain.transfer;

import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.stock.dto.StockCreateDTO;
import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.stock.domain.entity.ContainerStock;
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
public interface ContainerStockTransfer {

    @Mapping(source = "stockCreateDTO.transferQty", target = "totalQty")
    @Mapping(source = "stockCreateDTO.transferQty", target = "availableQty")
    @Mapping(source = "stockCreateDTO.targetContainerCode", target = "containerCode")
    @Mapping(source = "stockCreateDTO.targetContainerSlotCode", target = "containerSlotCode")
    @Mapping(source = "stockCreateDTO.targetContainerFace", target = "containerFace")
    @Mapping(target = "containerId", source = "stockCreateDTO.targetContainerId")
    ContainerStock fromCreateDTOtoDO(StockCreateDTO stockCreateDTO, Long skuBatchStockId);

    ContainerStockDTO do2DTO(ContainerStock containerStock);

    List<ContainerStock> toDOs(List<StockTransferDTO> stockTransferDTOS);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(source = "stockTransferDTO.boxStock", target = "boxStock")
    @Mapping(source = "stockTransferDTO.boxNo", target = "boxNo")
    @Mapping(source = "stockTransferDTO.transferQty", target = "totalQty")
    @Mapping(source = "stockTransferDTO.transferQty", target = "availableQty")
    @Mapping(source = "stockTransferDTO.targetContainerCode", target = "containerCode")
    @Mapping(source = "stockTransferDTO.targetContainerSlotCode", target = "containerSlotCode")
    @Mapping(source = "stockTransferDTO.targetContainerFace", target = "containerFace")
    @Mapping(source = "skuBatchStockId", target = "skuBatchStockId")
    ContainerStock toDO(StockTransferDTO stockTransferDTO, Long skuBatchStockId);

    List<ContainerStockDTO> toDTOs(List<ContainerStock> containerStocks);
}
