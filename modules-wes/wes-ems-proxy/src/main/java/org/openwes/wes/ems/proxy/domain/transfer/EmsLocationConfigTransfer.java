package org.openwes.wes.ems.proxy.domain.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.ems.proxy.domain.entity.EmsLocationConfig;
import org.openwes.wes.api.ems.proxy.dto.EmsLocationConfigDTO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
    nullValueCheckStrategy = ALWAYS,
    nullValueMappingStrategy = RETURN_NULL,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmsLocationConfigTransfer {

    EmsLocationConfig toDO(EmsLocationConfigDTO emsLocationConfigDTO);
}
