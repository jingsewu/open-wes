package org.openwes.wes.outbound.domain.transfer;

import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
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
public interface OutboundPlanOrderTransfer {

    OutboundPlanOrder toDO(OutboundPlanOrderDTO outboundPlanOrderDTO);

    OutboundPlanOrderDTO toDTO(OutboundPlanOrder outboundPlanOrder);

    List<OutboundPlanOrderDTO> toDTOs(List<OutboundPlanOrder> outboundPlanOrders);

    List<OutboundPlanOrder> toDOs(List<OutboundPlanOrderDTO> outboundPlanOrderDTOs);

}
