package org.openwes.station.application.business.handler.outbound;

import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.OperationTaskErrorDescEnum;
import org.openwes.station.api.constants.ApiCodeEnum;
import org.openwes.station.application.PtlApiImpl;
import org.openwes.station.application.business.handler.IBusinessHandler;
import org.openwes.station.application.business.handler.common.OperationTaskRefreshHandler;
import org.openwes.station.application.business.handler.event.OperationTaskRefreshEvent;
import org.openwes.station.application.business.handler.event.outbound.TapPutWallSlotEvent;
import org.openwes.station.domain.entity.ArrivedContainerCache;
import org.openwes.station.domain.entity.OutboundWorkStationCache;
import org.openwes.station.domain.repository.WorkStationCacheRepository;
import org.openwes.station.domain.service.WorkStationService;
import org.openwes.station.infrastructure.remote.RemoteWorkStationService;
import org.openwes.station.infrastructure.remote.TaskService;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.api.task.dto.HandleTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import org.openwes.wes.api.task.dto.SealContainerDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TapPutWallSlotHandler implements IBusinessHandler<TapPutWallSlotEvent> {

    private final WorkStationService<OutboundWorkStationCache> workStationService;
    private final WorkStationCacheRepository<OutboundWorkStationCache> workStationRepository;
    private final TaskService taskService;
    private final PtlApiImpl ptlService;
    private final RemoteWorkStationService remoteWorkStationService;
    private final OperationTaskRefreshHandler<OutboundWorkStationCache> containerTaskRefreshHandler;


    @Override
    public void execute(TapPutWallSlotEvent tapPutWallSlotEvent, Long workStationId) {
        OutboundWorkStationCache workStationCache = workStationService.getOrThrow(workStationId);

        List<OperationTaskDTO> operateTasks = workStationCache.getProcessingOperationTasks().stream()
                .map(OperationTaskVO::getOperationTaskDTO)
                .filter(operationTaskDTO ->
                        StringUtils.equals(tapPutWallSlotEvent.getPutWallSlotCode(), operationTaskDTO.getTargetLocationCode()))
                .toList();

        PutWallSlotDTO putWallSlot = remoteWorkStationService.queryPutWallSlot(workStationId, tapPutWallSlotEvent.getPutWallSlotCode());

        if (PutWallSlotStatusEnum.WAITING_SEAL == putWallSlot.getPutWallSlotStatus()) {
            doSealContainer(workStationCache, putWallSlot, operateTasks);
            return;
        }

        if (CollectionUtils.isEmpty(operateTasks)) {
            log.warn("cannot find processing operation tasks");
            return;
        }

        doCompletePicking(workStationId, operateTasks, workStationCache, putWallSlot);
    }


    private void doSealContainer(OutboundWorkStationCache workStationCache, PutWallSlotDTO putWallSlot, List<OperationTaskDTO> operateTasks) {
        taskService.sealContainer(new SealContainerDTO()
                .setPutWallSlotCode(putWallSlot.getPutWallSlotCode())
                .setTransferContainerCode(putWallSlot.getTransferContainerCode())
                .setPickingOrderId(putWallSlot.getPickingOrderId())
                .setWarehouseCode(workStationCache.getWarehouseCode())
                .setWorkStationId(workStationCache.getId()));

        ptlService.off(workStationCache.getId(), putWallSlot.getPtlTag());

    }

    private void doCompletePicking(Long workStationId, List<OperationTaskDTO> operateTasks,
                                   OutboundWorkStationCache workStationCache, PutWallSlotDTO putWallSlot) {

        workStationService.validatePicking(putWallSlot);

        List<HandleTaskDTO.HandleTask> handleTasks = operateTasks.stream().map(operationTaskDTO -> {
            HandleTaskDTO.HandleTask handleTask = HandleTaskDTO.HandleTask.builder()
                    .taskId(operationTaskDTO.getId())
                    .operatedQty(operationTaskDTO.getToBeOperatedQty())
                    .requiredQty(operationTaskDTO.getRequiredQty())
                    .abnormalQty(operationTaskDTO.getAbnormalQty())
                    .taskType(OperationTaskTypeEnum.PICKING)
                    .build();
            operationTaskDTO.setOperatedQty(operationTaskDTO.getToBeOperatedQty());
            return handleTask;
        }).toList();

        HandleTaskDTO handleTaskDTO = HandleTaskDTO.builder().workStationId(workStationId)
                .transferContainerCode(putWallSlot.getTransferContainerCode())
                .transferContainerRecordId(putWallSlot.getTransferContainerRecordId())
                .handleTasks(handleTasks).build();

        try {
            taskService.complete(handleTaskDTO);

            workStationCache.operate();
            workStationRepository.save(workStationCache);

            if (CollectionUtils.isEmpty(workStationCache.getOperateTasks()) && CollectionUtils.isNotEmpty(workStationCache.getUndoContainers())) {
                ArrivedContainerCache arrivedContainerCache = workStationCache.getUndoContainers().get(0);
                containerTaskRefreshHandler.execute(new OperationTaskRefreshEvent()
                        .setContainerCode(arrivedContainerCache.getContainerCode())
                        .setFace(arrivedContainerCache.getFace()), workStationCache.getId());
            }

            ptlService.off(workStationId, putWallSlot.getPtlTag());

        } catch (WmsException e) {
            log.error("complete task failed and let container ", e);
            if (OperationTaskErrorDescEnum.OPERATION_TASK_IS_PROCESSED.getCode().equals(e.getCode())) {
                ArrivedContainerCache arrivedContainerCache = workStationCache.getUndoContainers().get(0);
                containerTaskRefreshHandler.execute(new OperationTaskRefreshEvent()
                        .setContainerCode(arrivedContainerCache.getContainerCode())
                        .setFace(arrivedContainerCache.getFace()), workStationCache.getId());
            }
        }

    }

    @Override
    public ApiCodeEnum getApiCode() {
        return ApiCodeEnum.TAP_PUT_WALL_SLOT;
    }

    @Override
    public Class<TapPutWallSlotEvent> getParameterClass() {
        return TapPutWallSlotEvent.class;
    }
}
