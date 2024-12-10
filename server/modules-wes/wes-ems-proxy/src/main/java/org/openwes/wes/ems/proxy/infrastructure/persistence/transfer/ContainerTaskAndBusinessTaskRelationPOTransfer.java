package org.openwes.wes.ems.proxy.infrastructure.persistence.transfer;

import org.openwes.wes.ems.proxy.domain.repository.ContainerTaskAndBusinessTaskRelation;
import org.openwes.wes.ems.proxy.infrastructure.persistence.po.ContainerTaskAndBusinessTaskRelationPO;
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
public interface ContainerTaskAndBusinessTaskRelationPOTransfer {

    List<ContainerTaskAndBusinessTaskRelationPO> toPOs(List<ContainerTaskAndBusinessTaskRelation> relations);

    List<ContainerTaskAndBusinessTaskRelation> toDOs(List<ContainerTaskAndBusinessTaskRelationPO> relationPOS);
}
