package org.openwes.wes.basic.work_station.infrastructure.persistence.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.basic.work_station.domain.entity.WorkStation;
import org.openwes.wes.basic.work_station.infrastructure.persistence.po.WorkStationPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
    nullValueCheckStrategy = ALWAYS,
    nullValueMappingStrategy = RETURN_NULL,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WorkStationPOTransfer {

    WorkStation toDO(WorkStationPO workStationPO);

    List<WorkStation> toDOs(List<WorkStationPO> workStationPOS);

    WorkStationPO toPO(WorkStation workStation);
}
