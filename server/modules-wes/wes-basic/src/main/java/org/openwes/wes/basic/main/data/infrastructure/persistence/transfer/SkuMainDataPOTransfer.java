package org.openwes.wes.basic.main.data.infrastructure.persistence.transfer;

import org.openwes.wes.basic.main.data.domain.entity.SkuMainData;
import org.openwes.wes.basic.main.data.infrastructure.persistence.po.SkuMainDataPO;
import org.mapstruct.*;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SkuMainDataPOTransfer {

    @Mapping(target = ".", source = "weight")
    @Mapping(target = ".", source = "volumeDTO")
    @Mapping(target = ".", source = "skuAttribute")
    @Mapping(target = ".", source = "skuConfig")
    SkuMainDataPO toPO(SkuMainData skuMainData);

    @InheritInverseConfiguration
    SkuMainData toDO(SkuMainDataPO skuMainDataPO);

    List<SkuMainData> toDOS(List<SkuMainDataPO> skuMainDataPOS);

}
