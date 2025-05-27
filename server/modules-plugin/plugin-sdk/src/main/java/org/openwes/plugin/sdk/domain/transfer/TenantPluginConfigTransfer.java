package org.openwes.plugin.sdk.domain.transfer;


import org.openwes.plugin.api.dto.TenantPluginConfigDTO;
import org.openwes.plugin.sdk.domain.entity.TenantPluginConfig;
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
public interface TenantPluginConfigTransfer {

    TenantPluginConfigDTO toDTO(TenantPluginConfig tenantPluginConfig);

    TenantPluginConfig toDO(TenantPluginConfigDTO tenantPluginConfigDTO);
}
