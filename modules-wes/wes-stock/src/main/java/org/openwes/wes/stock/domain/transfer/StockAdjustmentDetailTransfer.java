package org.openwes.wes.stock.domain.transfer;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openwes.wes.api.stock.dto.*;
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
    @Mapping(source = "stockAdjustmentDetail.skuBatchAttributeId", target = "skuBatchAttributeId")
    @Mapping(source = "stockAdjustmentDetail.skuId", target = "skuId")
    @Mapping(source = "stockAdjustmentDetail.qtyAdjustment", target = "transferQty")
    @Mapping(source = "stockAdjustmentOrder.orderNo", target = "orderNo")
    @Mapping(source = "skuBatchStock.warehouseAreaId", target = "warehouseAreaId")
    @Mapping(source = "containerStock.containerCode", target = "sourceContainerCode")
    @Mapping(source = "containerStock.containerSlotCode", target = "sourceContainerSlotCode")
    @Mapping(source = "containerStock.containerId", target = "targetContainerId")
    @Mapping(source = "containerStock.containerCode", target = "targetContainerCode")
    @Mapping(source = "containerStock.containerFace", target = "targetContainerFace")
    @Mapping(source = "containerStock.containerSlotCode", target = "targetContainerSlotCode")
    StockCreateDTO toStockCreateDTO(StockAdjustmentDetail stockAdjustmentDetail,
                                    StockAdjustmentOrder stockAdjustmentOrder,
                                    ContainerStockDTO containerStock,
                                    SkuBatchStockDTO skuBatchStock);

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
    StockTransferDTO toStockTransferDTO(StockAdjustmentDetail stockAdjustmentDetail,
                                        StockAdjustmentOrder stockAdjustmentOrder);

    StockAdjustmentDetailDTO toDTO(StockAdjustmentDetail stockAdjustmentDetail);

    List<StockAdjustmentDetailDTO> toDTOs(List<StockAdjustmentDetail> stockAdjustmentDetails);
}
