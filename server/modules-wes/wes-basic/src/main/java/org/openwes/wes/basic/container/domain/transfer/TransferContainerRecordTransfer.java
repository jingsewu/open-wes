package org.openwes.wes.basic.container.domain.transfer;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openwes.wes.api.basic.dto.TransferContainerRecordDTO;
import org.openwes.wes.api.task.dto.SealContainerDTO;
import org.openwes.wes.basic.container.domain.entity.TransferContainerRecord;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransferContainerRecordTransfer {
    TransferContainerRecord toDO(SealContainerDTO sealContainerDTO);

    TransferContainerRecordDTO toDTO(TransferContainerRecord transferContainerRecord);

    List<TransferContainerRecordDTO> toDTOs(List<TransferContainerRecord> transferContainerRecords);
}
