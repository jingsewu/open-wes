package org.openwes.wes.basic.main.data.infrastructure.persistence.transfer;

import org.openwes.wes.basic.main.data.domain.entity.SkuBarcodeData;
import org.openwes.wes.basic.main.data.infrastructure.persistence.po.SkuBarCodeDataPO;
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
public interface SkuBarcodeDataPOTransfer {

    SkuBarCodeDataPO toPO(SkuBarcodeData skuBarcodeData);

    SkuBarcodeData toDO(SkuBarCodeDataPO skuBarcodeDataPO);

    List<SkuBarcodeData> toDOs(List<SkuBarCodeDataPO> skuBarcodeDataPOs);

    List<SkuBarCodeDataPO> toPOs(List<SkuBarcodeData> skuMainDataList);
}
