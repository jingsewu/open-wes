package org.openwes.wes.stock.domain.transfer;

import org.openwes.wes.api.stock.dto.StockAbnormalRecordDTO;
import org.openwes.wes.stock.domain.entity.StockAbnormalRecord;
import org.openwes.wes.stock.domain.entity.StockAdjustmentDetail;
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
public interface StockAbnormalRecordTransfer {
    List<StockAbnormalRecord> toDOs(List<StockAbnormalRecordDTO> stockAbnormalRecordDTOS);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "id", target = "stockAbnormalRecordId")
    @Mapping(source = "qtyAbnormal", target = "qtyAdjustment")
    StockAdjustmentDetail toStockAdjustmentDetail(StockAbnormalRecord stockAbnormalRecord);

    List<StockAbnormalRecordDTO> toDTOs(List<StockAbnormalRecord> stockAbnormalRecords);
}
