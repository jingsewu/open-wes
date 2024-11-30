package org.openwes.wes.outbound.domain.transfer;

import org.openwes.wes.api.outbound.dto.OutboundWaveDTO;
import org.openwes.wes.outbound.domain.aggregate.OutboundWaveAggregate;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
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
public interface OutboundWaveTransfer {
    OutboundWave toDO(OutboundWaveAggregate outboundWaveAggregate);

    List<OutboundWave> toDOs(List<OutboundWaveDTO> outboundWaveDTOS);

    OutboundWaveDTO toDTO(OutboundWave outboundWave, List<OutboundPlanOrder> outboundPlanOrders);

    List<OutboundWaveDTO> toDTOs(List<OutboundWave> outboundWaves);
}
