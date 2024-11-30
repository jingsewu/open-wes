package org.openwes.wes.basic.main.data.transfer;

import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.basic.main.data.domain.entity.SkuMainData;
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
public interface SkuMainDataDTOTransfer {

    SkuMainDataDTO toDTO(SkuMainData skuMainData);
}
