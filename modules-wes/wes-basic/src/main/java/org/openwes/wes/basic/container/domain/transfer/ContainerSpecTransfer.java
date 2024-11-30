package org.openwes.wes.basic.container.domain.transfer;

import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import org.openwes.wes.basic.container.domain.entity.ContainerSpec;
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
public interface ContainerSpecTransfer {

    List<ContainerDTO.ContainerSlot> toContainerSlots(List<ContainerSpecDTO.ContainerSlotSpec> containerSlotSpecs);

    ContainerSpec toDO(ContainerSpecDTO containerSpecDTO);

    ContainerSpecDTO toDTO(ContainerSpec containerSpec);

    List<ContainerSpecDTO> toDTOs(List<ContainerSpec> containerSpecs);
}
