package org.openwes.wes.ems.proxy.domain.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.ems.proxy.domain.entity.ContainerTask;
import org.openwes.wes.api.ems.proxy.dto.ContainerTaskDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
    nullValueCheckStrategy = ALWAYS,
    nullValueMappingStrategy = RETURN_NULL,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_DEFAULT)
public interface ContainerTaskTransfer {

    @Mapping(target = "nextContainerTasks", ignore = true)
    ContainerTaskDTO toDTO(ContainerTask containerTask);

    List<ContainerTaskDTO> toDTOs(List<ContainerTask> containerTasks);

    ContainerTask toDO(ContainerTaskDTO containerTaskDTO);

    List<ContainerTask> toDOs(List<ContainerTaskDTO> containerTaskDTOs);

}
