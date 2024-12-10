package org.openwes.wes.stock.domain.transfer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openwes.wes.api.stock.dto.StockAdjustmentDetailDTO;
import org.openwes.wes.api.stock.dto.StockTransferDTO;
import org.openwes.wes.stock.domain.entity.StockAdjustmentDetail;
import org.openwes.wes.stock.domain.entity.StockAdjustmentOrder;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StockAdjustmentDetailTransfer {

    @Mapping(source = "stockAdjustmentOrder.warehouseCode", target = "warehouseCode")
    @Mapping(target = "lockType", expression = "java(org.openwes.wes.api.stock.constants.StockLockTypeEnum.ADJUSTMENT)")
    @Mapping(source = "stockAdjustmentDetail.containerStockId", target = "containerStockId")
    @Mapping(source = "stockAdjustmentDetail.skuBatchStockId", target = "skuBatchStockId")
    @Mapping(source = "stockAdjustmentDetail.skuBatchAttributeId", target = "skuBatchAttributeId")
    @Mapping(source = "stockAdjustmentDetail.id", target = "taskId")
    @Mapping(source = "stockAdjustmentDetail.skuId", target = "skuId")
    @Mapping(source = "stockAdjustmentOrder.orderNo", target = "targetContainerCode")
    @Mapping(target = "targetContainerFace", expression = "java(\"\")")
    @Mapping(target = "targetContainerSlotCode", expression = "java(\"\")")
    @Mapping(source = "stockAdjustmentDetail.qtyAdjustment", target = "transferQty")
    @Mapping(target = "warehouseAreaId", expression = "java(0L)")
    @Mapping(source = "stockAdjustmentOrder.orderNo", target = "orderNo")
    StockTransferDTO toStockTransferDTO(StockAdjustmentDetail stockAdjustmentDetail, StockAdjustmentOrder stockAdjustmentOrder);

    StockAdjustmentDetailDTO toDTO(StockAdjustmentDetail stockAdjustmentDetail);

    List<StockAdjustmentDetailDTO> toDTOs(List<StockAdjustmentDetail> stockAdjustmentDetails);
}
