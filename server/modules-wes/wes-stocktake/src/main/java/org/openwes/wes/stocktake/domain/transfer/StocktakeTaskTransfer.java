package org.openwes.wes.stocktake.domain.transfer;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openwes.wes.api.stocktake.dto.StocktakeTaskDTO;
import org.openwes.wes.api.stocktake.dto.StocktakeTaskDetailDTO;
import org.openwes.wes.stocktake.domain.entity.StocktakeTask;
import org.openwes.wes.stocktake.domain.entity.StocktakeTaskDetail;

import java.util.List;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StocktakeTaskTransfer {
    StocktakeTaskDTO toDTO(StocktakeTask task);

    List<StocktakeTaskDTO> toDTOS(List<StocktakeTask> taskList);

    StocktakeTaskDetailDTO toDetailDTO(StocktakeTaskDetail detail);

    List<StocktakeTaskDetailDTO> toDetailDTOS(List<StocktakeTaskDetail> details);
}
