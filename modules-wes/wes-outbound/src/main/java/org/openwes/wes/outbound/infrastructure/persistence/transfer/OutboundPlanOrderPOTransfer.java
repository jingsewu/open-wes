package org.openwes.wes.outbound.infrastructure.persistence.transfer;

import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrderDetail;
import org.openwes.wes.outbound.infrastructure.persistence.po.OutboundPlanOrderDetailPO;
import org.openwes.wes.outbound.infrastructure.persistence.po.OutboundPlanOrderPO;
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
public interface OutboundPlanOrderPOTransfer {

    OutboundPlanOrderPO toPO(OutboundPlanOrder outboundPlanOrder);

    List<OutboundPlanOrderDetailPO> toDetailPOs(List<OutboundPlanOrderDetail> details);

    List<OutboundPlanOrderPO> toPOs(List<OutboundPlanOrder> outboundPlanOrders);

    OutboundPlanOrder toDO(OutboundPlanOrderPO outboundPlanOrderPO, List<OutboundPlanOrderDetailPO> details);

    List<OutboundPlanOrder> toDOs(List<OutboundPlanOrderPO> outboundPlanOrderPOS);
}
