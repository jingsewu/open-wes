package org.openwes.wes.task.domain.service.impl;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.wes.api.task.dto.HandleTaskDTO;
import org.openwes.wes.api.task.dto.ReportAbnormalDTO;
import org.openwes.wes.task.domain.entity.OperationTask;
import org.openwes.wes.task.domain.entity.TransferContainerRecord;
import org.openwes.wes.task.domain.repository.OperationTaskRepository;
import org.openwes.wes.task.domain.service.OperationTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.openwes.common.utils.exception.code_enum.OperationTaskErrorDescEnum.CONTAINER_IS_DISPATCHED;

@Service
@RequiredArgsConstructor
public class OperationTaskServiceImpl implements OperationTaskService {

    private final OperationTaskRepository operationTaskRepository;

    public void handleTasks(List<OperationTask> operationTasks, HandleTaskDTO handleTaskDTO) {

        Map<Long, HandleTaskDTO.HandleTask> handleTaskMap = handleTaskDTO.getHandleTasks().stream()
                .collect(Collectors.toMap(HandleTaskDTO.HandleTask::getTaskId, v -> v));

        operationTasks.forEach(operationTask -> {
            HandleTaskDTO.HandleTask handleTask = handleTaskMap.get(operationTask.getId());
            operationTask.operate(handleTask.getOperatedQty(), handleTaskDTO.getHandleTaskType(),
                    handleTaskDTO.getTransferContainerCode(), handleTaskDTO.getTransferContainerRecordId());
        });

    }

    @Override
    public void handleAbnormalTasks(List<OperationTask> operationTasks, ReportAbnormalDTO handleTaskDTO) {
        Map<Long, ReportAbnormalDTO.HandleTask> handleTaskMap = handleTaskDTO.getHandleTasks().stream()
                .collect(Collectors.toMap(ReportAbnormalDTO.HandleTask::getTaskId, v -> v));

        operationTasks.forEach(operationTask -> {
            ReportAbnormalDTO.HandleTask handleTask = handleTaskMap.get(operationTask.getId());
            operationTask.reportAbnormal(handleTask.getAbnormalQty());
        });

    }

    @Override
    public void checkUnbindable(TransferContainerRecord transferContainerRecord) {
        long count = operationTaskRepository.countByTransferContainerRecordId(transferContainerRecord.getId());
        if (count > 0) {
            throw WmsException.throwWmsException(CONTAINER_IS_DISPATCHED, transferContainerRecord.getTransferContainerCode());
        }
    }
}
