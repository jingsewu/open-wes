package org.openwes.station.infrastructure.remote;

import org.openwes.wes.api.outbound.IPickingOrderApi;
import org.openwes.wes.api.outbound.constants.PickingOrderStatusEnum;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
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
    @DubboReference
    private IPickingOrderApi pickingOrderApi;

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
        PickingOrderDTO pickingOrderDTO = pickingOrderApi.getById(sealContainerDTO.getPickingOrderId());
        sealContainerDTO.setPickingOrderCompleted(pickingOrderDTO.getPickingOrderStatus() == PickingOrderStatusEnum.PICKED);
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
