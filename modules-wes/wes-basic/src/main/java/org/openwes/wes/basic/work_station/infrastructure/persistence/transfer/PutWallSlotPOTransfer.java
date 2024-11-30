package org.openwes.wes.basic.work_station.infrastructure.persistence.transfer;

import org.openwes.wes.basic.work_station.domain.entity.PutWallSlot;
import org.openwes.wes.basic.work_station.infrastructure.persistence.po.PutWallSlotPO;
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
public interface PutWallSlotPOTransfer {

    PutWallSlotPO toPO(PutWallSlot putWallSlot);

    List<PutWallSlotPO> toPOs(List<PutWallSlot> putWallSlots);

    PutWallSlot toDO(PutWallSlotPO putWallSlotPO);

    List<PutWallSlot> toDOs(List<PutWallSlotPO> putWallSlotPOs);
}
