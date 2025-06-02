package org.openwes.api.platform.domain.transfer;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openwes.api.platform.api.dto.api.ApiConfigDTO;
import org.openwes.api.platform.domain.entity.ApiConfigPO;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ApiConfigTransfer {

    ApiConfigDTO toDTO(ApiConfigPO apiConfigPO);

    ApiConfigPO toDO(ApiConfigDTO apiConfig);
}
