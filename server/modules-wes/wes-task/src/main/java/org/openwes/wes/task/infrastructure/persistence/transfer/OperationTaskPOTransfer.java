package org.openwes.wes.task.infrastructure.persistence.transfer;

import org.openwes.wes.task.domain.entity.OperationTask;
import org.openwes.wes.task.infrastructure.persistence.po.OperationTaskPO;
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
public interface OperationTaskPOTransfer {

    OperationTask toDO(OperationTaskPO operationTaskPO);

    List<OperationTask> toDOs(List<OperationTaskPO> selectBatchIds);

    List<OperationTaskPO> toPOs(List<OperationTask> operationTasks);
}
