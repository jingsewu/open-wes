package org.openwes.wes.inbound.domain.transfer;

import org.openwes.wes.api.inbound.dto.EmptyContainerInboundRecordDTO;
import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrderDetail;
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
public interface EmptyContainerInboundOrderTransfer {

    List<EmptyContainerInboundOrderDetail> toDOs(List<EmptyContainerInboundRecordDTO> emptyContainerInboundRecordDTOS);
}
