package org.openwes.wes.stocktake.infrastructure.persistence.transfer;

import org.openwes.wes.stocktake.domain.entity.StocktakeOrder;
import org.openwes.wes.stocktake.domain.entity.StocktakeOrderDetail;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeOrderDetailPO;
import org.openwes.wes.stocktake.infrastructure.persistence.po.StocktakeOrderPO;
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
public interface StocktakeOrderPOTransfer {

    StocktakeOrderPO toPO(StocktakeOrder stocktakeOrder);

    List<StocktakeOrderPO> toPOS(List<StocktakeOrder> stocktakeOrders);

    List<StocktakeOrderDetailPO> toDetailPOS(List<StocktakeOrderDetail> details);

    List<StocktakeOrderDetail> toDetailDOS(List<StocktakeOrderDetailPO> details);

    StocktakeOrder toDO(StocktakeOrderPO stocktakeOrderPO);

    List<StocktakeOrder> toDOS(List<StocktakeOrderPO> stocktakeOrderPOS);

    StocktakeOrder toDO(StocktakeOrderPO stocktakeOrderPO, List<StocktakeOrderDetailPO> details);

}
