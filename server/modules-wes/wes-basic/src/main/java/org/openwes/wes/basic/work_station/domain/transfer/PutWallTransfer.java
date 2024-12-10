package org.openwes.wes.basic.work_station.domain.transfer;

import org.openwes.wes.api.basic.dto.CreatePutWallDTO;
import org.openwes.wes.api.basic.dto.PutWallDTO;
import org.openwes.wes.basic.work_station.domain.entity.PutWall;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
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
public interface PutWallTransfer {

    PutWall toDO(PutWallDTO putWallDTO);

    void updateTarget(CreatePutWallDTO createPutWallDTO, @MappingTarget PutWall putWall);

    List<PutWallDTO> toDTOs(List<PutWall> putWalls);

    PutWallDTO toDTO(PutWall putWall);
}
