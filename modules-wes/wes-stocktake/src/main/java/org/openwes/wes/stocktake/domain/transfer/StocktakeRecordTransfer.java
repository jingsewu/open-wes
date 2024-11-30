package org.openwes.wes.stocktake.domain.transfer;

import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.openwes.wes.api.stock.dto.StockCreateDTO;
import org.openwes.wes.api.stocktake.dto.StocktakeRecordDTO;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.entity.StocktakeRecord;
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
public interface StocktakeRecordTransfer {
    StocktakeRecordDTO toDTO(StocktakeRecord record);

    List<StocktakeRecordDTO> toDTOS(List<StocktakeRecord> recordList);

    @Mapping(source = "containerDTO.warehouseCode", target = "warehouseCode")
    @Mapping(source = "skuBatchAttributeDTO.id", target = "skuBatchAttributeId")
    @Mapping(source = "skuBatchAttributeDTO.skuId", target = "skuId")
    @Mapping(source = "qty", target = "transferQty")
    @Mapping(source = "stocktakeOrder.orderNo", target = "orderNo")
    @Mapping(source = "containerDTO.warehouseAreaId", target = "warehouseAreaId")
    @Mapping(source = "containerDTO.containerCode", target = "sourceContainerCode")
    @Mapping(source = "containerSlotCode", target = "sourceContainerSlotCode")
    @Mapping(source = "containerDTO.id", target = "targetContainerId")
    @Mapping(source = "containerDTO.containerCode", target = "targetContainerCode")
    @Mapping(source = "containerFace", target = "targetContainerFace")
    @Mapping(source = "containerSlotCode", target = "targetContainerSlotCode")
    StockCreateDTO toStockCreateDTO(StocktakeOrder stocktakeOrder,
                                    ContainerDTO containerDTO,
                                    String containerFace,
                                    String containerSlotCode,
                                    SkuBatchAttributeDTO skuBatchAttributeDTO,
                                    Integer qty
    );
}
