package org.openwes.wes.stock.infrastructure.persistence.transfer;

import org.openwes.wes.stock.domain.entity.StockAdjustmentDetail;
import org.openwes.wes.stock.domain.entity.StockAdjustmentOrder;
import org.openwes.wes.stock.infrastructure.persistence.po.StockAdjustmentDetailPO;
import org.openwes.wes.stock.infrastructure.persistence.po.StockAdjustmentOrderPO;
import org.mapstruct.Mapper;
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
public interface StockAdjustmentPOTransfer {

    StockAdjustmentOrderPO toPO(StockAdjustmentOrder stockAdjustmentOrder);

    List<StockAdjustmentDetailPO> toDetailPOs(List<StockAdjustmentDetail> details);

    StockAdjustmentOrder toDO(StockAdjustmentOrderPO stockAdjustmentOrderPO, List<StockAdjustmentDetailPO> details);

    List<StockAdjustmentOrderPO> toPOs(List<StockAdjustmentOrder> stockAdjustmentOrders);

    List<StockAdjustmentDetail> toDetailDOs(List<StockAdjustmentDetailPO> details);
}
