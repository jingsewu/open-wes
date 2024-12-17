package org.openwes.wes.outbound.infrastructure.persistence.transfer;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrder;
import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrderDetail;
import org.openwes.wes.outbound.infrastructure.persistence.po.EmptyContainerOutboundOrderDetailPO;
import org.openwes.wes.outbound.infrastructure.persistence.po.EmptyContainerOutboundOrderPO;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmptyContainerOutboundOrderPOTransfer {

    EmptyContainerOutboundOrderPO toPO(EmptyContainerOutboundOrder emptyContainerOutboundOrder);

    List<EmptyContainerOutboundOrderDetailPO> toDetailPOs(List<EmptyContainerOutboundOrderDetail> details);

    EmptyContainerOutboundOrder toDO(EmptyContainerOutboundOrderPO emptyContainerOutboundOrderPO, List<EmptyContainerOutboundOrderDetailPO> detailPOs);

    List<EmptyContainerOutboundOrderPO> toPOs(List<EmptyContainerOutboundOrder> emptyContainerOutboundOrders);
}
