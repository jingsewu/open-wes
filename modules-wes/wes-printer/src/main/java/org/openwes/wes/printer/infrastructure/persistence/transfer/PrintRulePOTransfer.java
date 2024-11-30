package org.openwes.wes.printer.infrastructure.persistence.transfer;

import org.openwes.wes.printer.domain.entity.PrintRule;
import org.openwes.wes.printer.infrastructure.persistence.po.PrintRulePO;
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
public interface PrintRulePOTransfer {
    List<PrintRule> toDOs(List<PrintRulePO> printRulePOS);
}
