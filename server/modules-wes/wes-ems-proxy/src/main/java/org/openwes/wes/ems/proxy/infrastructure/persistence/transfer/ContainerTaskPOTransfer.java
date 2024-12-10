package org.openwes.wes.ems.proxy.infrastructure.persistence.transfer;

import org.openwes.wes.ems.proxy.domain.entity.ContainerTask;
import org.openwes.wes.ems.proxy.infrastructure.persistence.po.ContainerTaskAndBusinessTaskRelationPO;
import org.openwes.wes.ems.proxy.infrastructure.persistence.po.ContainerTaskPO;
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
public interface ContainerTaskPOTransfer {

    List<ContainerTaskPO> toPOs(List<ContainerTask> containerTasks);

    List<ContainerTask> toDOs(List<ContainerTaskPO> containerTaskPOS);

    ContainerTask toDO(ContainerTaskPO containerTaskPO, List<ContainerTaskAndBusinessTaskRelationPO> relations);
}
