package org.openwes.wes.basic.work_station.domain.transfer;

import org.openwes.wes.api.basic.dto.WorkStationConfigDTO;
import org.openwes.wes.basic.work_station.domain.entity.WorkStationConfig;
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
public interface WorkStationConfigTransfer {

    WorkStationConfigDTO toDTO(WorkStationConfig workStationConfig);

    WorkStationConfig toDO(WorkStationConfigDTO workStationConfigDTO);
}
