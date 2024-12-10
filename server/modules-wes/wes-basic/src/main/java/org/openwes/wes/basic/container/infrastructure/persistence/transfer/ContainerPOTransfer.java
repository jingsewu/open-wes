package org.openwes.wes.basic.container.infrastructure.persistence.transfer;

import org.openwes.wes.basic.container.domain.entity.Container;
import org.openwes.wes.basic.container.infrastructure.persistence.po.ContainerPO;
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
public interface ContainerPOTransfer {

    Container toDO(ContainerPO containerPO);

    List<Container> toDOs(Collection<ContainerPO> containerPO);

    ContainerPO toPO(Container container);

    List<ContainerPO> toPOs(List<Container> containers);
}
