package org.openwes.wes.stock.infrastructure.persistence.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.stock.domain.entity.SkuBatchAttribute;
import org.openwes.wes.stock.infrastructure.persistence.po.SkuBatchAttributePO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SkuBatchAttributePOTransfer {

    SkuBatchAttributePO toPO(SkuBatchAttribute skuBatchAttribute);

    List<SkuBatchAttribute> toDOs(List<SkuBatchAttributePO> skuBatchAttributePOS);

    SkuBatchAttribute toDO(SkuBatchAttributePO skuBatchAttributePO);
}
