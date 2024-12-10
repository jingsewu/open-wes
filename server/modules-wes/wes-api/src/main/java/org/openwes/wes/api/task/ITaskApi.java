package org.openwes.wes.api.task;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.api.task.dto.*;

import java.util.Collection;
import java.util.List;

public interface ITaskApi {

    List<OperationTaskDTO> createOperationTasks(@Valid List<OperationTaskDTO> operationTaskDTOS);

    List<OperationTaskVO> getAndUpdateTasksWorkStation(@NotNull Long workStationId, @NotEmpty String containerCode, @NotNull String face,
                                                       @NotNull OperationTaskTypeEnum taskType);

    List<OperationTaskDTO> queryTasks(@NotEmpty Collection<Long> taskIds);

    List<OperationTaskDTO> queryOrderTasks(@NotEmpty Collection<Long> pickingOrderIds, int limit);

    void bindContainer(@Valid BindContainerDTO bindContainerDTO);

    void unbindContainer(@Valid UnBindContainerDTO unBindContainerDTO);

    void sealContainer(@Valid SealContainerDTO sealContainerDTO);

    void sealContainer(@NotNull Long pickingOrderId);

    List<OperationTaskDTO> getLimitCountUndoOperationTasksLimitDays(int days, String warehouseCode, OperationTaskTypeEnum taskType, int limitCount);

    void split(HandleTaskDTO handleTaskDTO);

    void complete(HandleTaskDTO handleTaskDTO);

    void reportAbnormal(ReportAbnormalDTO handleTaskDTO);

    List<OperationTaskDTO> queryByTransferContainerRecordIds(Collection<Long> transferContainerRecordIds);
}
