package org.openwes.wes.config.domain.transfer;

import org.mapstruct.*;
import org.openwes.wes.api.config.dto.SystemConfigDTO;
import org.openwes.wes.config.domain.entity.SystemConfig;

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

    @Mapping(ignore = true, source = "id", target = "id")
    void toDO(SystemConfigDTO systemConfigDTO, @MappingTarget SystemConfig systemConfig);
}
