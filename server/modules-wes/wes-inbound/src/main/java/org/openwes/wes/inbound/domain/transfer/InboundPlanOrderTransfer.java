package org.openwes.wes.inbound.domain.transfer;

import org.openwes.wes.api.inbound.dto.InboundPlanOrderDTO;
import org.openwes.wes.api.inbound.dto.InboundPlanOrderDetailDTO;
import org.openwes.wes.inbound.domain.entity.InboundPlanOrder;
import org.openwes.wes.inbound.domain.entity.InboundPlanOrderDetail;
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
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InboundPlanOrderTransfer {

    InboundPlanOrder toDO(InboundPlanOrderDTO inboundPlanOrderDTO);

    List<InboundPlanOrder> toDOs(List<InboundPlanOrderDTO> inboundPlanOrderDTOS);

    InboundPlanOrderDTO toDTO(InboundPlanOrder inboundPlanOrder);

    List<InboundPlanOrderDTO> toDTOs(Collection<InboundPlanOrder> inboundPlanOrder);

    InboundPlanOrderDetail toDetailDO(InboundPlanOrderDetailDTO detailDTO);

    InboundPlanOrderDetailDTO toDetailDTO(InboundPlanOrderDetail inboundPlanOrderDetail);


}
