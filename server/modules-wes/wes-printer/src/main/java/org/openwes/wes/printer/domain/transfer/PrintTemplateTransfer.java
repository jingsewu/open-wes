package org.openwes.wes.printer.domain.transfer;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openwes.wes.api.print.dto.PrintTemplateDTO;
import org.openwes.wes.printer.domain.entity.PrintTemplate;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PrintTemplateTransfer {
    PrintTemplate toDO(PrintTemplateDTO printTemplateDTO);

    PrintTemplateDTO toDTO(PrintTemplate printTemplate);
}
