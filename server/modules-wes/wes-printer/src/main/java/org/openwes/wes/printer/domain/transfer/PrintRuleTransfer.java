package org.openwes.wes.printer.domain.transfer;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openwes.wes.api.print.dto.PrintRuleDTO;
import org.openwes.wes.printer.domain.entity.PrintRule;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PrintRuleTransfer {
    PrintRule toDO(PrintRuleDTO printRuleDTO);

    PrintRuleDTO toDTO(PrintRule printRule);
}
