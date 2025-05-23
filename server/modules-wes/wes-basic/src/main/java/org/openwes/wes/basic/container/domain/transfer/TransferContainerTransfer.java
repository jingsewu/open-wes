package org.openwes.wes.basic.container.domain.transfer;


import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openwes.wes.api.task.dto.TransferContainerDTO;
import org.openwes.wes.basic.container.domain.entity.TransferContainer;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransferContainerTransfer {

    TransferContainerDTO toDTO(TransferContainer transferContainer);

    List<TransferContainerDTO> toDTOs(List<TransferContainer> transferContainers);
}
