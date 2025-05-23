package org.openwes.wes.stocktake.domain.transfer;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openwes.wes.api.stocktake.dto.StocktakeRecordDTO;
import org.openwes.wes.stocktake.domain.entity.StocktakeRecord;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StocktakeRecordTransfer {
    StocktakeRecordDTO toDTO(StocktakeRecord record);

    List<StocktakeRecordDTO> toDTOs(List<StocktakeRecord> recordList);
}
