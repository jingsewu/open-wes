package org.openwes.wes.stock.infrastructure.persistence.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.stock.domain.entity.StockAbnormalRecord;
import org.openwes.wes.stock.infrastructure.persistence.po.StockAbnormalRecordPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StockAbnormalRecordPOTransfer {

    List<StockAbnormalRecordPO> toPOs(List<StockAbnormalRecord> stockAbnormalRecords);

    List<StockAbnormalRecord> toDOs(List<StockAbnormalRecordPO> stockAbnormalRecordPOS);
}
