package org.openwes.wes.task.infrastructure.persistence.transfer;

import org.openwes.wes.task.domain.entity.TransferContainerRecord;
import org.openwes.wes.task.infrastructure.persistence.po.TransferContainerRecordPO;
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
public interface TransferContainerRecordPOTransfer {

    TransferContainerRecordPO toPO(TransferContainerRecord transferContainer);

    TransferContainerRecord toDO(TransferContainerRecordPO transferContainerPO);

    List<TransferContainerRecordPO> toPOs(List<TransferContainerRecord> transferContainers);

    List<TransferContainerRecord> toDOs(List<TransferContainerRecordPO> transferContainerPOS);
}
