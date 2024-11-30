package org.openwes.wes.basic.work_station.domain.transfer;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.openwes.wes.basic.work_station.domain.entity.PutWall;
import org.openwes.wes.basic.work_station.domain.entity.WorkStation;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
    nullValueCheckStrategy = ALWAYS,
    nullValueMappingStrategy = RETURN_NULL,
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WorkStationTransfer {
    WorkStation toDO(WorkStationDTO workStationDTO);

    WorkStationDTO toDTO(WorkStation workStation);

    List<WorkStationDTO> toDTOs(List<WorkStation> workStations);

    WorkStationDTO toDTO(WorkStation workStation, List<PutWall> putWalls);
}
