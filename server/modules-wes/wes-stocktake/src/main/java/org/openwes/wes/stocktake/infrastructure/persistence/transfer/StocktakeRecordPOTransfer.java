package org.openwes.wes.stocktake.infrastructure.persistence.transfer;

import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.stocktake.domain.entity.StocktakeRecord;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeRecordPO;
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
public interface StocktakeRecordPOTransfer {
    @Mapping(source = "id", target = "id", ignore = true)
    @Mapping(source = "totalQty", target = "qtyOriginal")
    @Mapping(source = "id", target = "stockId")
    StocktakeRecord toDO(ContainerStockDTO containerStockDTO);

    StocktakeRecord toDO(StocktakeRecordPO stocktakeRecordPO);

    @Mapping(target = "containerId", expression = "java(-1L)")
    @Mapping(target = "skuId", expression = "java(-1L)")
    @Mapping(target = "stockId", expression = "java(-1L)")
    @Mapping(target = "skuBatchAttributeId", expression = "java(-1L)")
    @Mapping(target = "skuBatchStockId", expression = "java(-1L)")
    @Mapping(target = "qtyOriginal", expression = "java(0)")
    StocktakeRecord toEmptySlotDO(String warehouseCode, String containerCode, String containerFace, String containerSlotCode);

    List<StocktakeRecord> toDOS(List<StocktakeRecordPO> recordPOList);

    StocktakeRecordPO toPO(StocktakeRecord stocktakeRecord);

    List<StocktakeRecordPO> toPOS(List<StocktakeRecord> recordPOList);
}
