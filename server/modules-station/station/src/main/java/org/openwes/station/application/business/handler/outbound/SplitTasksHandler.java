package org.openwes.station.application.business.handler.outbound;

import com.google.common.base.Preconditions;
import org.openwes.station.api.IPtlApi;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.event.outbound.SplitTasksEvent;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.EquipmentService;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import org.openwes.station.infrastructure.remote.TaskService;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.ems.proxy.constants.ContainerOperationTypeEnum;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.api.task.dto.HandleTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class SplitTasksHandler implements IBusinessHandler<SplitTasksEvent> {

    private final RemoteWorkStationService remoteWorkStationService;
    private final WorkStationService<OutboundWorkStationCache> workStationService;
    private final TaskService taskService;
    private final EquipmentService equipmentService;
    private final WorkStationCacheRepository<OutboundWorkStationCache> workStationRepository;
    private final IPtlApi ptlApi;

    @Override
    public void execute(SplitTasksEvent splitTasksEvent, Long workStationId) {
        OutboundWorkStationCache workStationCache = workStationService.getOrThrow(workStationId);
        Preconditions.checkState(CollectionUtils.isNotEmpty(workStationCache.getOperateTasks()));

        List<OperationTaskDTO> operateTasks = workStationCache.getProcessingOperationTasks().stream()
                .map(OperationTaskVO::getOperationTaskDTO)
                .filter(operationTaskDTO ->
                        StringUtils.equals(splitTasksEvent.getPutWallSlotCode(), operationTaskDTO.getTargetLocationCode()))
                // calculate operated qty order by required qty descending
                .sorted(Comparator.comparing(OperationTaskDTO::getOperatedQty)).toList();

        AtomicInteger totalOperatedQty = new AtomicInteger(splitTasksEvent.getOperatedQty());
        List<HandleTaskDTO.HandleTask> handleTasks = operateTasks.stream().map(operationTaskDTO -> {
            int operatedQty = Math.min(operationTaskDTO.getRequiredQty(), totalOperatedQty.get());
            totalOperatedQty.set(Math.max(0, totalOperatedQty.get() - operationTaskDTO.getRequiredQty()));

            HandleTaskDTO.HandleTask handleTask = HandleTaskDTO.HandleTask.builder()
                    .taskId(operationTaskDTO.getId())
                    .operatedQty(operatedQty)
                    .requiredQty(operationTaskDTO.getRequiredQty())
                    .taskType(OperationTaskTypeEnum.PICKING)
                    .build();

            operationTaskDTO.setRequiredQty(operationTaskDTO.getRequiredQty() - operatedQty);
            return handleTask;
        }).toList();

        PutWallSlotDTO putWallSlot = remoteWorkStationService.queryPutWallSlot(workStationId, splitTasksEvent.getPutWallSlotCode());
        workStationService.validatePicking(putWallSlot);

        HandleTaskDTO handleTaskDTO = HandleTaskDTO.builder().workStationId(workStationId)
                .transferContainerCode(putWallSlot.getTransferContainerCode())
                .transferContainerRecordId(putWallSlot.getTransferContainerRecordId())
                .handleTasks(handleTasks).build();
        taskService.split(handleTaskDTO);

        ptlApi.reminderBind(workStationId, putWallSlot.getPtlTag());

        workStationCache.operate();

        Collection<ArrivedContainerCache> doneContainers = workStationCache.queryTasksAndReturnRemovedContainers(taskService);
        workStationRepository.save(workStationCache);

        if (CollectionUtils.isNotEmpty(doneContainers)) {
            equipmentService.containerLeave(doneContainers, ContainerOperationTypeEnum.LEAVE);
        }
    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.SPLIT_TASKS;
    }

    @Override
    public Class<SplitTasksEvent> getParameterClass() {
        return SplitTasksEvent.class;
    }
}
