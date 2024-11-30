package org.openwes.wes.basic.main.data.domain.transfer;

import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.basic.main.data.domain.entity.SkuMainData;
import org.mapstruct.*;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SkuMainDataTransfer {

    SkuMainData toDO(SkuMainDataDTO skuMainDataDTO);

    SkuMainDataDTO toDTO(SkuMainData skuMainData);

    List<SkuMainDataDTO> toDTOs(List<SkuMainData> skuMainDataList);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    void toDo(@MappingTarget SkuMainData skuMainData, SkuMainDataDTO skuMainDataDTO);
}
