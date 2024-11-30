package org.openwes.wes.outbound.infrastructure.persistence.transfer;

import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.infrastructure.persistence.po.PickingOrderDetailPO;
import org.openwes.wes.outbound.infrastructure.persistence.po.PickingOrderPO;
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
public interface PickingOrderPOTransfer {

    PickingOrderPO toPO(PickingOrder pickingOrder);

    List<PickingOrderPO> toPOs(List<PickingOrder> pickingOrders);

    PickingOrder toDO(PickingOrderPO pickingOrderPO);

    List<PickingOrder> toDOs(List<PickingOrderPO> pickingOrderPOS);

    PickingOrder toDO(PickingOrderPO pickingOrderPO, List<PickingOrderDetailPO> details);
}
