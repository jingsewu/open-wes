package org.openwes.wes.ems.proxy.infrastructure.persistence.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.ems.proxy.domain.entity.EmsLocationConfig;
import org.openwes.wes.ems.proxy.infrastructure.persistence.po.EmsLocationConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
    nullValueCheckStrategy = ALWAYS,
    nullValueMappingStrategy = RETURN_NULL,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EmsLocationConfigPOTransfer {

    EmsLocationConfigPO toPO(EmsLocationConfig workLocation);

    EmsLocationConfig toDO(EmsLocationConfigPO workLocationPO);
}
