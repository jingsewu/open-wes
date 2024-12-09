package org.openwes.wes.task.domain.transfer;


import org.openwes.wes.api.task.dto.TransferContainerDTO;
import org.openwes.wes.task.domain.entity.TransferContainer;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransferContainerTransfer {

    TransferContainerDTO toDTO(TransferContainer transferContainer);

}
