package org.openwes.wes.stock.domain.transfer;

import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.openwes.wes.stock.domain.entity.SkuBatchAttribute;
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
public interface SkuBatchAttributeTransfer {

    List<SkuBatchAttributeDTO> toDTOs(List<SkuBatchAttribute> skuBatchAttributes);

    SkuBatchAttributeDTO toDTO(SkuBatchAttribute skuBatchAttribute);
}
