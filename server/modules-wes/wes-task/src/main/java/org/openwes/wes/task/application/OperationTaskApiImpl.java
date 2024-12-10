package org.openwes.wes.task.application;

import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.stock.ISkuBatchAttributeApi;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.openwes.wes.api.task.ITaskApi;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.api.task.constants.TransferContainerRecordStatusEnum;
import org.openwes.wes.task.domain.aggregate.OperationTaskStockAggregate;
import org.openwes.wes.task.domain.aggregate.TransferContainerPutWallAggregate;
import org.openwes.wes.task.domain.entity.OperationTask;
import org.openwes.wes.task.domain.entity.TransferContainer;
import org.openwes.wes.task.domain.entity.TransferContainerRecord;
import org.openwes.wes.task.domain.repository.OperationTaskRepository;
import org.openwes.wes.task.domain.repository.TransferContainerRecordRepository;
import org.openwes.wes.task.domain.repository.TransferContainerRepository;
import org.openwes.wes.task.domain.service.OperationTaskService;
import org.openwes.wes.task.domain.service.TransferContainerService;
import org.openwes.wes.task.domain.transfer.OperationTaskTransfer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.openwes.wes.api.task.dto.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Primary
@Validated
@DubboService
@RequiredArgsConstructor
public class OperationTaskApiImpl implements ITaskApi {

    private final TransferContainerRecordRepository transferContainerRecordRepository;
    private final ISkuBatchAttributeApi skuBatchAttributeApi;
    private final ISkuMainDataApi skuMainDataApi;
    private final OperationTaskService operationTaskService;
    private final OperationTaskRepository operationTaskRepository;
    private final OperationTaskTransfer operationTaskTransfer;
    private final OperationTaskStockAggregate operationTaskStockAggregate;
    private final TransferContainerPutWallAggregate transferContainerPutWallAggregate;
    private final TransferContainerRepository transferContainerRepository;
    private final TransferContainerService transferContainerService;

    @Override
    public List<OperationTaskDTO> createOperationTasks(List<OperationTaskDTO> operationTaskDTOS) {
        List<OperationTask> operationTasks = operationTaskTransfer.toDOs(operationTaskDTOS);
        return operationTaskTransfer.toDTOs(operationTaskRepository.saveAll(operationTasks));
    }

    @Override
    public List<OperationTaskVO> getAndUpdateTasksWorkStation(Long workStationId, String containerCode, String face, OperationTaskTypeEnum taskType) {
        List<OperationTask> operationTasks = operationTaskRepository.findAllUndoTasksByStationAndContainer(workStationId, containerCode, face, taskType);

        if (CollectionUtils.isEmpty(operationTasks)) {
            return Collections.emptyList();
        }

        operationTasks.forEach(operationTask -> operationTask.setActualWorkStation(workStationId));
        operationTaskRepository.saveAll(operationTasks);

        List<OperationTaskDTO> operationTaskDTOS = operationTaskTransfer.toDTOs(operationTasks);

        Set<Long> skuMainDataIds = operationTaskDTOS.stream()
                .map(OperationTaskDTO::getSkuId).collect(Collectors.toSet());
        Map<Long, SkuMainDataDTO> skuMainDataDTOMap = skuMainDataApi.getByIds(skuMainDataIds)
                .stream().collect(Collectors.toMap(SkuMainDataDTO::getId, v -> v));

        Set<Long> skuBatchAttributedIds = operationTaskDTOS.stream().map(OperationTaskDTO::getSkuBatchAttributeId).collect(Collectors.toSet());
        Map<Long, SkuBatchAttributeDTO> skuBatchAttributeDTOMap = skuBatchAttributeApi.getBySkuBatchAttributeIds(skuBatchAttributedIds)
                .stream().collect(Collectors.toMap(SkuBatchAttributeDTO::getId, v -> v));

        return operationTaskDTOS.stream().map(v -> {
            SkuBatchAttributeDTO batchAttributeDTO = skuBatchAttributeDTOMap.get(v.getSkuBatchAttributeId());
            SkuMainDataDTO skuMainDataDTO = skuMainDataDTOMap.get(v.getSkuId());
            return new OperationTaskVO().setOperationTaskDTO(v)
                    .setSkuBatchAttributeDTO(batchAttributeDTO)
                    .setSkuMainDataDTO(skuMainDataDTO);
        }).toList();
    }

    @Override
    public List<OperationTaskDTO> queryTasks(Collection<Long> taskIds) {
        return operationTaskTransfer.toDTOs(operationTaskRepository.findAllByIds(taskIds));
    }

    @Override
    public List<OperationTaskDTO> queryOrderTasks(Collection<Long> pickingOrderIds, int limit) {
        return operationTaskTransfer.toDTOs(operationTaskRepository.findAllByLimitPickingOrderIds(pickingOrderIds, limit));
    }

    @Override
    public void bindContainer(BindContainerDTO bindContainerDTO) {
        TransferContainer transferContainer = transferContainerRepository
                .findByContainerCodeAndWarehouseCode(bindContainerDTO.getContainerCode(), bindContainerDTO.getWarehouseCode());
        transferContainerService.validateBindContainer(transferContainer);

        transferContainerPutWallAggregate.bindContainer(bindContainerDTO, transferContainer, bindContainerDTO.getPickingOrderId());
    }

    @Override
    public void unbindContainer(UnBindContainerDTO unBindContainerDTO) {

        TransferContainerRecord transferContainerRecord = transferContainerRecordRepository
                .findCurrentPickOrderTransferContainerRecord(unBindContainerDTO.getPickingOrderId(), unBindContainerDTO.getContainerCode());
        operationTaskService.checkUnbindable(transferContainerRecord);

        TransferContainer transferContainer = transferContainerRepository
                .findByContainerCodeAndWarehouseCode(unBindContainerDTO.getContainerCode(), unBindContainerDTO.getWarehouseCode());
        transferContainerPutWallAggregate.unBindContainer(unBindContainerDTO, transferContainer, transferContainerRecord);
    }

    @Override
    public List<OperationTaskDTO> getLimitCountUndoOperationTasksLimitDays(int days, String warehouseCode, OperationTaskTypeEnum taskType, int limitCount) {
        List<OperationTask> operationTasks = operationTaskRepository.findAllByLimitDaysAndWarehouseCodeAndCode(days, warehouseCode, taskType, limitCount);
        return operationTaskTransfer.toDTOs(operationTasks);
    }

    @Override
    public void split(HandleTaskDTO handleTaskDTO) {

        Map<Long, HandleTaskDTO.HandleTask> handleTaskMap = handleTaskDTO.getHandleTasks().stream()
                .collect(Collectors.toMap(HandleTaskDTO.HandleTask::getTaskId, v -> v));
        List<OperationTask> operationTasks = operationTaskRepository.findAllByIds(handleTaskMap.keySet());

        Set<Long> pickingOrderIds = operationTasks.stream().map(OperationTask::getOrderId).collect(Collectors.toSet());
        if (pickingOrderIds.size() > 1) {
            throw new IllegalArgumentException("only can split one picking order by once");
        }

        operationTaskStockAggregate.splitTasks(operationTasks, handleTaskDTO);
    }

    @Override
    public void complete(HandleTaskDTO handleTaskDTO) {
        Map<Long, HandleTaskDTO.HandleTask> handleTaskMap = handleTaskDTO.getHandleTasks().stream()
                .collect(Collectors.toMap(HandleTaskDTO.HandleTask::getTaskId, v -> v));
        List<OperationTask> operationTasks = operationTaskRepository.findAllByIds(handleTaskMap.keySet());

        operationTaskStockAggregate.complete(operationTasks, handleTaskDTO);
    }

    @Override
    public void reportAbnormal(ReportAbnormalDTO handleTaskDTO) {

        Map<Long, ReportAbnormalDTO.HandleTask> handleTaskMap = handleTaskDTO.getHandleTasks().stream()
                .collect(Collectors.toMap(ReportAbnormalDTO.HandleTask::getTaskId, v -> v));
        List<OperationTask> operationTasks = operationTaskRepository.findAllByIds(handleTaskMap.keySet());

        operationTaskStockAggregate.reportAbnormal(handleTaskDTO, operationTasks);
    }

    @Override
    public void sealContainer(SealContainerDTO sealContainerDTO) {

        TransferContainerRecord transferContainerRecord = transferContainerRecordRepository
                .findCurrentPickOrderTransferContainerRecord(sealContainerDTO.getPickingOrderId(), sealContainerDTO.getTransferContainerCode());

        TransferContainer transferContainer = transferContainerRepository
                .findByContainerCodeAndWarehouseCode(transferContainerRecord.getTransferContainerCode(), sealContainerDTO.getWarehouseCode());
        transferContainerPutWallAggregate.sealContainer(sealContainerDTO.isNeedHandlePutWallSlot(), transferContainerRecord, transferContainer);
    }

    @Override
    public void sealContainer(Long pickingOrderId) {
        List<TransferContainerRecord> transferContainerRecords = transferContainerRecordRepository.findAllByPickingOrderId(pickingOrderId)
                .stream().filter(v -> v.getTransferContainerStatus() == TransferContainerRecordStatusEnum.BOUNDED)
                .toList();

        if (ObjectUtils.isEmpty(transferContainerRecords)) {
            log.info("picking order: {} can't find andy transfer container records", pickingOrderId);
            return;
        }

        String warehouseCode = transferContainerRecords.iterator().next().getWarehouseCode();
        List<String> transferContainerCodes = transferContainerRecords.stream().map(TransferContainerRecord::getTransferContainerCode).toList();
        List<TransferContainer> transferContainers = transferContainerRepository
                .findAllByWarehouseCodeAndContainerCodeIn(warehouseCode, transferContainerCodes);

        transferContainerPutWallAggregate.sealContainer(false, transferContainerRecords, transferContainers);
    }

    @Override
    public List<OperationTaskDTO> queryByTransferContainerRecordIds(Collection<Long> transferContainerRecordIds) {
        List<OperationTask> operationTasks = operationTaskRepository.findAllByTransferContainerRecordIds(transferContainerRecordIds);
        return operationTaskTransfer.toDTOs(operationTasks);
    }
}
