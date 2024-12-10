package org.openwes.wes.basic.container.domain.transfer;

import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.openwes.wes.basic.container.domain.entity.Container;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContainerTransfer {

    ContainerDTO toDTO(Container container);

    List<ContainerDTO> toDTOs(Collection<Container> containers);

    Container toDO(ContainerDTO containerDTO);

    List<Container> toDOs(Collection<ContainerDTO> containerDTO);

}
