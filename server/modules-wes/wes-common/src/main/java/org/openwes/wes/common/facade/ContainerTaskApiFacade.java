package org.openwes.wes.common.facade;

import org.openwes.wes.api.ems.proxy.IContainerTaskApi;
import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Setter
@RequiredArgsConstructor
public class ContainerTaskApiFacade {

    private final IContainerTaskApi containerTaskApi;

    public void createContainerTasks(List<OperationTaskDTO> operationTasks, ContainerTaskTypeEnum containerTaskTypeEnum) {
        List<CreateContainerTaskDTO> createContainerTaskDTOS = operationTasks.stream()
                .map(operationTask -> new CreateContainerTaskDTO()
                        .setTaskGroupCode(operationTask.getSourceContainerCode())
                        .setContainerCode(operationTask.getSourceContainerCode())
                        .setContainerFace(operationTask.getSourceContainerFace())
                        .setBusinessTaskType(BusinessTaskTypeEnum.valueOf(operationTask.getTaskType().name()))
                        .setDestinations(operationTask.getAssignedStationSlot().keySet().stream().map(String::valueOf).toList())
                        .setCustomerTaskId(operationTask.getId())
                        .setContainerTaskType(containerTaskTypeEnum)
                        .setTaskPriority(operationTask.getPriority())).toList();
        containerTaskApi.createContainerTasks(createContainerTaskDTOS);
    }

    public void createContainerTasks(List<CreateContainerTaskDTO> createContainerTaskDTOS) {
        containerTaskApi.createContainerTasks(createContainerTaskDTOS);
    }

    public void cancelTasks(List<Long> customerTaskIds) {
        containerTaskApi.cancel(customerTaskIds);
    }
}
