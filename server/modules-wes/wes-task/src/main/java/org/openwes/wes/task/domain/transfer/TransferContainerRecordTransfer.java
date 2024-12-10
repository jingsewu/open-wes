package org.openwes.wes.task.domain.transfer;

import org.openwes.wes.api.task.dto.SealContainerDTO;
import org.openwes.wes.task.domain.entity.TransferContainerRecord;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransferContainerRecordTransfer {
    TransferContainerRecord toDO(SealContainerDTO sealContainerDTO);
}
