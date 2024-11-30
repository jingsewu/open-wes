package org.openwes.wes.task.infrastructure.persistence.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.task.domain.entity.TransferContainer;
import org.openwes.wes.task.infrastructure.persistence.po.TransferContainerPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransferContainerPOTransfer {

    TransferContainerPO toPO(TransferContainer transferContainer);

    TransferContainer toDO(TransferContainerPO transferContainerPO);

    List<TransferContainer> toDOs(List<TransferContainerPO> transferContainerPOS);

    List<TransferContainerPO> toPOs(List<TransferContainer> transferContainers);
}
