package org.openwes.wes.inbound.infrastructure.persistence.transfer;

import org.openwes.common.utils.user.UserContext;
import org.openwes.wes.inbound.domain.entity.InboundPlanOrder;
import org.openwes.wes.inbound.domain.entity.InboundPlanOrderDetail;
import org.openwes.wes.inbound.infrastructure.persistence.po.InboundPlanOrderDetailPO;
import org.openwes.wes.inbound.infrastructure.persistence.po.InboundPlanOrderPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        imports = {UserContext.class})
public interface InboundPlanOrderPOTransfer {

    InboundPlanOrderPO toPO(InboundPlanOrder inboundPlanOrder);

    Collection<InboundPlanOrderPO> toPOs(Collection<InboundPlanOrder> inboundPlanOrders);

    List<InboundPlanOrderDetailPO> toDetailPOs(List<InboundPlanOrderDetail> inboundPlanOrderDetails);

    InboundPlanOrder toDO(InboundPlanOrderPO inboundPlanOrderPO, List<InboundPlanOrderDetailPO> details);

    InboundPlanOrder toDO(InboundPlanOrderPO inboundPlanOrderPO);

    Collection<InboundPlanOrder> toDOs(Collection<InboundPlanOrderPO> inboundPlanOrderPO);

    List<InboundPlanOrderDetail> toDetailDOs(Collection<InboundPlanOrderDetailPO> inboundPlanOrderDetailPO);

    InboundPlanOrderDetail toDetailDO(InboundPlanOrderDetailPO inboundPlanOrderDetailPO);

    InboundPlanOrderDetailPO toDetailPO(InboundPlanOrderDetail inboundPlanOrderDetail);
}
