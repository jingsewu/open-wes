package org.openwes.wes.inbound.infrastructure.persistence.transfer;

import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrder;
import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrderDetail;
import org.openwes.wes.inbound.infrastructure.persistence.po.EmptyContainerInboundOrderDetailPO;
import org.openwes.wes.inbound.infrastructure.persistence.po.EmptyContainerInboundOrderPO;
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
public interface EmptyContainerInboundOrderPOTransfer {
    EmptyContainerInboundOrderPO toPO(EmptyContainerInboundOrder emptyContainerInboundOrder);

    List<EmptyContainerInboundOrderPO> toPOs(List<EmptyContainerInboundOrder> emptyContainerInboundOrders);

    List<EmptyContainerInboundOrderDetailPO> toDetailPOs(List<EmptyContainerInboundOrderDetail> emptyContainerInboundOrderDetails);

    List<EmptyContainerInboundOrder> toDOs(List<EmptyContainerInboundOrderPO> emptyContainerInboundOrderPOS);

    List<EmptyContainerInboundOrderDetail> toDetailDOs(List<EmptyContainerInboundOrderDetailPO> emptyContainerInboundOrderDetailPOS);

}
