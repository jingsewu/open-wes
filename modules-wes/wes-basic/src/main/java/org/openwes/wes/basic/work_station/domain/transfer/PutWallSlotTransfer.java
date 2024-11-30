package org.openwes.wes.basic.work_station.domain.transfer;

import org.openwes.wes.api.basic.dto.ContainerSpecDTO;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.basic.work_station.domain.entity.PutWallSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
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
public interface PutWallSlotTransfer {

    List<PutWallSlot> toDOs(List<ContainerSpecDTO.ContainerSlotSpec> containerSlotSpecs);

    PutWallSlotDTO toDTO(PutWallSlot putWallSlot);

    List<PutWallSlotDTO> toDTOs(List<PutWallSlot> putWallSlots);

    @Mapping(source = "containerSlotSpecCode", target = "putWallSlotCode")
    PutWallSlot toDO(ContainerSpecDTO.ContainerSlotSpec containerSlotSpec);


}
