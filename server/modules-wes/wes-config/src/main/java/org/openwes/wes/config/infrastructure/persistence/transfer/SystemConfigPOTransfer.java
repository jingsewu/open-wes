package org.openwes.wes.config.infrastructure.persistence.transfer;

import org.openwes.wes.config.domain.entity.SystemConfig;
import org.openwes.wes.config.infrastructure.persistence.po.SystemConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SystemConfigPOTransfer {

    SystemConfigPO toPO(SystemConfig systemConfig);

    SystemConfig toDO(SystemConfigPO systemConfigPO);
}
