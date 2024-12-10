package org.openwes.station.infrastructure.remote;

import org.openwes.wes.api.task.ITaskApi;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import lombok.Setter;
import org.apache.dubbo.config.annotation.DubboReference;
import org.openwes.wes.api.task.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Setter
@Service
public class TaskService {

    @DubboReference
    private ITaskApi taskApi;

    public List<OperationTaskVO> queryTasks(Long workStationId, String containerCode, String face, OperationTaskTypeEnum operationType) {
        return taskApi.getAndUpdateTasksWorkStation(workStationId, containerCode, face, operationType);
    }

    public void bindContainer(BindContainerDTO bindContainerDTO) {
        taskApi.bindContainer(bindContainerDTO);
    }

    public void unbindContainer(UnBindContainerDTO unBindContainerDTO) {
        taskApi.unbindContainer(unBindContainerDTO);
    }

    public void sealContainer(SealContainerDTO sealContainerDTO) {
        taskApi.sealContainer(sealContainerDTO);
    }

    public void split(HandleTaskDTO handleTaskDTO) {
        handleTaskDTO.setHandleTaskType(HandleTaskDTO.HandleTaskTypeEnum.SPLIT);
        taskApi.split(handleTaskDTO);
    }

    public void complete(HandleTaskDTO handleTaskDTO) {
        handleTaskDTO.setHandleTaskType(HandleTaskDTO.HandleTaskTypeEnum.COMPLETE);
        taskApi.complete(handleTaskDTO);
    }

    public void reportAbnormal(ReportAbnormalDTO handleTaskDTO) {
        taskApi.reportAbnormal(handleTaskDTO);
    }

}
