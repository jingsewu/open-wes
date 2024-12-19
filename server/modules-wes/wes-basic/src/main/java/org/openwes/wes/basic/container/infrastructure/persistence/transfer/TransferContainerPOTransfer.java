package org.openwes.wes.basic.container.infrastructure.persistence.transfer;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openwes.wes.basic.container.domain.entity.TransferContainer;
import org.openwes.wes.basic.container.infrastructure.persistence.po.TransferContainerPO;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

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
