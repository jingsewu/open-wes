package org.openwes.wes.basic.main.data.domain.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.api.main.data.dto.SkuBarcodeDataDTO;
import org.openwes.wes.basic.main.data.domain.entity.SkuBarcodeData;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SkuBarcodeDataTransfer {

    SkuBarcodeData toDO(SkuBarcodeDataDTO skuBarcodeDataDTO);

    SkuBarcodeDataDTO toDTO(SkuBarcodeData skuBarcodeData);

    List<SkuBarcodeDataDTO> toDTOs(List<SkuBarcodeData> skuBarcodeDataList);

}
