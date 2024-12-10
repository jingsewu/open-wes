package org.openwes.wes.config.domain.transfer;

import org.openwes.wes.api.config.dto.SystemConfigDTO;
import org.openwes.wes.config.domain.entity.SystemConfig;
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
public interface SystemConfigTransfer {
    SystemConfig toDO(SystemConfigDTO systemConfigDTO);

    SystemConfigDTO toDTO(SystemConfig systemConfig);
}
