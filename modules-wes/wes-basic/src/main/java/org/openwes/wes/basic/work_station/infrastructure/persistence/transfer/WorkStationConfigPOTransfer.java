package org.openwes.wes.basic.work_station.infrastructure.persistence.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.basic.work_station.domain.entity.WorkStationConfig;
import org.openwes.wes.basic.work_station.infrastructure.persistence.po.WorkStationConfigPO;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
    nullValueCheckStrategy = ALWAYS,
    nullValueMappingStrategy = RETURN_NULL,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WorkStationConfigPOTransfer {
    WorkStationConfigPO toPO(WorkStationConfig workStationConfig);

    WorkStationConfig toDO(WorkStationConfigPO workStationConfigPO);
}
