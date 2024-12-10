package org.openwes.wes.config.domain.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.api.config.dto.BarcodeParseRuleDTO;
import org.openwes.wes.config.domain.entity.BarcodeParseRule;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BarcodeParseRuleTransfer {

    BarcodeParseRule toDO(BarcodeParseRuleDTO barcodeParseRuleDTO);

    BarcodeParseRuleDTO toDTO(BarcodeParseRule barcodeParseRule);
}
