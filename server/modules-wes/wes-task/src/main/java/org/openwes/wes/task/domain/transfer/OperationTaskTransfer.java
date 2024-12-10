package org.openwes.wes.task.domain.transfer;

import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.task.domain.entity.OperationTask;
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
public interface OperationTaskTransfer {

    List<OperationTaskDTO> toDTOs(List<OperationTask> operationTasks);

    List<OperationTask> toDOs(List<OperationTaskDTO> operationTaskDTOS);

}
