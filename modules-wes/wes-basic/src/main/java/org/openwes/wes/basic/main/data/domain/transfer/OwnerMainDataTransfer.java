package org.openwes.wes.basic.main.data.domain.transfer;

import org.openwes.wes.api.main.data.dto.OwnerMainDataDTO;
import org.openwes.wes.basic.main.data.domain.entity.OwnerMainData;
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
public interface OwnerMainDataTransfer {

    OwnerMainData toDO(OwnerMainDataDTO ownerMainDataDTO);

    OwnerMainDataDTO toDTO(OwnerMainData ownerMainData);

}
