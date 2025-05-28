package org.openwes.plugin.sdk.domain.transfer;


import org.openwes.plugin.api.dto.PluginConfigDTO;
import org.openwes.plugin.sdk.domain.entity.ApplicationPluginConfig;
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
public interface ApplicationPluginConfigTransfer {

    PluginConfigDTO toDTO(ApplicationPluginConfig pluginConfig);

    ApplicationPluginConfig toDO(PluginConfigDTO pluginConfigDTO);
}
