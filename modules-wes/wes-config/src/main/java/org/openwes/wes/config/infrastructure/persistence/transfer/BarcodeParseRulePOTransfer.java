package org.openwes.wes.config.infrastructure.persistence.transfer;

import org.openwes.wes.config.domain.entity.BarcodeParseRule;
import org.openwes.wes.config.infrastructure.persistence.po.BarcodeParseRulePO;
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
public interface BarcodeParseRulePOTransfer {
    BarcodeParseRulePO toPO(BarcodeParseRule barcodeParseRule);

    BarcodeParseRule toDO(BarcodeParseRulePO barcodeParseRulePO);

    List<BarcodeParseRule> toDOs(List<BarcodeParseRulePO> barcodeParseRulePOS);
}
