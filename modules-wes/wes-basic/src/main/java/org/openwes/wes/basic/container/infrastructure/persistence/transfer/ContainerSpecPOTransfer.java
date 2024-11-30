package org.openwes.wes.basic.container.infrastructure.persistence.transfer;

import org.openwes.wes.basic.container.domain.entity.ContainerSpec;
import org.openwes.wes.basic.container.infrastructure.persistence.po.ContainerSpecPO;
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
public interface ContainerSpecPOTransfer {
    ContainerSpec toDO(ContainerSpecPO containerSpecPO);

    List<ContainerSpec> toDOs(List<ContainerSpecPO> containerSpecPOS);

    ContainerSpecPO toPO(ContainerSpec containerSpec);
}
