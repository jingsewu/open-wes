package org.openwes.wes.task.domain.aggregate;

import com.google.common.collect.Lists;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.basic.IPutWallApi;
import org.openwes.wes.api.main.data.ISkuMainDataApi;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.event.PickingOrderAbnormalEvent;
import org.openwes.wes.api.stock.IStockAbnormalRecordApi;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.constants.StockAbnormalTypeEnum;
import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import org.openwes.wes.api.stock.dto.*;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.dto.HandleTaskDTO;
import org.openwes.wes.api.task.dto.ReportAbnormalDTO;
import org.openwes.wes.task.domain.entity.OperationTask;
import org.openwes.wes.task.domain.entity.TransferContainer;
import org.openwes.wes.task.domain.entity.TransferContainerRecord;
import org.openwes.wes.task.domain.repository.OperationTaskRepository;
import org.openwes.wes.task.domain.repository.TransferContainerRecordRepository;
import org.openwes.wes.task.domain.repository.TransferContainerRepository;
import org.openwes.wes.task.domain.service.OperationTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationTaskStockAggregate {

    private final IStockApi stockApi;
    private final IStockAbnormalRecordApi stockAbnormalRecordApi;
    private final ISkuMainDataApi skuMainDataFacade;
    private final IPutWallApi putWallApi;
    private final OperationTaskService operationTaskService;
    private final OperationTaskRepository operationTaskRepository;
    private final TransferContainerRecordRepository transferContainerRecordRepository;
    private final TransferContainerPutWallAggregate transferContainerPutWallAggregate;
    private final TransferContainerRepository transferContainerRepository;

    @Transactional(rollbackFor = Exception.class)
    public void complete(List<OperationTask> operationTasks, HandleTaskDTO handleTaskDTO) {
        operationTaskService.handleTasks(operationTasks, handleTaskDTO);
        operationTaskRepository.saveAll(operationTasks);
    }

    @Transactional(rollbackFor = Exception.class)
    public void splitTasks(List<OperationTask> operationTasks, HandleTaskDTO handleTaskDTO) {

        operationTaskService.handleTasks(operationTasks, handleTaskDTO);

        Map<Long, OperationTask> operationTaskMap = operationTasks.stream().collect(Collectors.toMap(OperationTask::getId, v -> v));
        List<OperationTask> newTasks = handleTaskDTO.getHandleTasks().stream()
                .filter(v -> !Objects.equals(v.getRequiredQty(), v.getOperatedQty()) && v.getOperatedQty() > 0)
                .map(v -> {
                    OperationTask operationTask = operationTaskMap.get(v.getTaskId());
                    if (OperationTaskStatusEnum.PROCESSED != operationTask.getTaskStatus()) {
                        OperationTask newTask = new OperationTask();
                        BeanUtils.copyProperties(operationTask, newTask, "id", "version");

                        newTask.setRequiredQty(v.getOperatedQty());
                        newTask.setOperatedQty(v.getOperatedQty());
                        newTask.setAbnormalQty(0);
                        newTask.setAbnormal(false);
                        newTask.setTaskStatus(OperationTaskStatusEnum.PROCESSED);
                        return newTask;
                    }
                    return null;
                }).filter(Objects::nonNull).toList();

        operationTasks.addAll(newTasks);
        operationTaskRepository.saveAll(operationTasks);

        OperationTask operationTask = operationTasks.iterator().next();
        String putWallSlotCode = operationTask.getAssignedStationSlot().get(handleTaskDTO.getWorkStationId());
        if (!handleTaskDTO.isNeededAutoSealContainer()) {
            putWallApi.remindToSealContainer(operationTask.getOrderId(), Map.of(handleTaskDTO.getWorkStationId(), putWallSlotCode));
            return;
        }

        TransferContainerRecord transferContainerRecord = transferContainerRecordRepository
                .findCurrentPickOrderTransferContainerRecord(operationTask.getOrderId(), operationTask.getTargetContainerCode());
        TransferContainer transferContainer = transferContainerRepository
                .findByContainerCodeAndWarehouseCode(operationTask.getTargetContainerCode(), operationTask.getWarehouseCode());
        transferContainerPutWallAggregate.sealContainer(false, transferContainerRecord, transferContainer);

    }

    @Transactional(rollbackFor = Exception.class)
    public void reportAbnormal(ReportAbnormalDTO reportAbnormalDTO, List<OperationTask> operationTasks) {
        operationTaskService.handleAbnormalTasks(operationTasks, reportAbnormalDTO);
        operationTaskRepository.saveAll(operationTasks);

        List<OperationTask> abnormalOperationTasks = operationTasks.stream().filter(v -> v.getAbnormalQty() > 0).toList();
        List<Long> containerStockIds = abnormalOperationTasks.stream().map(OperationTask::getContainerStockId).toList();
        Set<Long> skuIds = abnormalOperationTasks.stream().map(OperationTask::getSkuId).collect(Collectors.toSet());

        List<ContainerStockDTO> containerStockDTOS = stockApi.getContainerStocks(containerStockIds);
        Map<Long, ContainerStockDTO> containerStockDTOMap = containerStockDTOS.stream()
                .collect(Collectors.toMap(ContainerStockDTO::getId, Function.identity()));
        List<SkuBatchStockDTO> skuBatchStockDTOS = stockApi.getSkuBatchStocks(containerStockDTOS.stream().map(ContainerStockDTO::getSkuBatchStockId).collect(Collectors.toSet()));
        Map<Long, SkuBatchStockDTO> skuBatchStockDTOMap = skuBatchStockDTOS.stream()
                .collect(Collectors.toMap(SkuBatchStockDTO::getId, Function.identity()));
        Map<Long, SkuMainDataDTO> skuMainDataDTOMap = skuMainDataFacade.getByIds(skuIds).stream().collect(Collectors.toMap(SkuMainDataDTO::getId, Function.identity()));

        List<StockAbnormalRecordDTO> stockAbnormalRecordDTOS = Lists.newArrayList();
        List<ContainerStockLockDTO> containerStockLockDTOS = Lists.newArrayList();
        List<SkuBatchStockLockDTO> skuBatchStockLockDTOS = Lists.newArrayList();
        List<PickingOrderAbnormalEvent.PickingOrderAbnormalDetail> detailAbnormalDTOS = Lists.newArrayList();
        abnormalOperationTasks.forEach(v -> {

            //1. unlock unPicked qty
            containerStockLockDTOS.add(ContainerStockLockDTO.builder().containerStockId(v.getContainerStockId())
                    .taskId(v.getId())
                    .lockQty(-v.getAbnormalQty())
                    .lockType(StockLockTypeEnum.OUTBOUND).build());
            skuBatchStockLockDTOS.add(SkuBatchStockLockDTO.builder().skuBatchStockId(v.getSkuBatchStockId())
                    .orderDetailId(v.getDetailId())
                    .lockQty(-v.getAbnormalQty())
                    .lockType(StockLockTypeEnum.OUTBOUND).build());

            ContainerStockDTO containerStockDTO = containerStockDTOMap.get(v.getContainerStockId());
            SkuBatchStockDTO skuBatchStockDTO = skuBatchStockDTOMap.get(v.getSkuBatchStockId());

            //2. if sku batch stock available qty is smaller than container stock available qty
            // that may be some orders allocate sku batch stock but not allocate container stock yet.
            // then only lock sku batch available qty
            // lock unPicked qty + min available qty
            int actualAvailableQty = Math.min(skuBatchStockDTO.getAvailableQty(), containerStockDTO.getAvailableQty());

            SkuMainDataDTO skuMainDataDTO = skuMainDataDTOMap.get(v.getSkuId());
            //3. create stock abnormal record
            stockAbnormalRecordDTOS.add(new StockAbnormalRecordDTO()
                    .setOrderNo(v.getOrderNo())
                    .setWarehouseCode(v.getWarehouseCode())
                    .setOwnerCode(skuMainDataDTO.getOwnerCode())
                    .setContainerCode(v.getSourceContainerCode())
                    .setContainerSlotCode(v.getSourceContainerSlot())
                    .setContainerStockId(v.getContainerStockId())
                    .setSkuBatchStockId(v.getSkuBatchStockId())
                    .setSkuBatchAttributeId(v.getSkuBatchAttributeId())
                    .setSkuId(v.getSkuId())
                    .setSkuCode(skuMainDataDTO.getSkuCode())
                    .setStockAbnormalType(StockAbnormalTypeEnum.PICKING)
                    .setAbnormalReason(reportAbnormalDTO.getAbnormalReason())
                    .setReasonDesc(Optional.ofNullable(reportAbnormalDTO.getAbnormalReason()).map(String::valueOf).orElse(StringUtils.EMPTY))
                    .setQtyAbnormal(-v.getAbnormalQty() - actualAvailableQty));

            containerStockDTO.setAvailableQty(containerStockDTO.getAvailableQty() - actualAvailableQty);
            skuBatchStockDTO.setAvailableQty(skuBatchStockDTO.getAvailableQty() - actualAvailableQty);

            detailAbnormalDTOS.add(new PickingOrderAbnormalEvent.PickingOrderAbnormalDetail()
                    .setPickingOrderId(v.getOrderId())
                    .setPickingOrderDetailId(v.getDetailId())
                    .setAbnormalQty(v.getAbnormalQty()));
        });

        if (CollectionUtils.isNotEmpty(skuBatchStockLockDTOS)) {
            stockApi.lockSkuBatchStock(skuBatchStockLockDTOS);
        }

        if (CollectionUtils.isNotEmpty(containerStockLockDTOS)) {
            stockApi.lockContainerStock(containerStockLockDTOS);
        }

        if (CollectionUtils.isNotEmpty(stockAbnormalRecordDTOS)) {
            stockAbnormalRecordApi.createStockAbnormalRecords(stockAbnormalRecordDTOS);
        }

        if (CollectionUtils.isNotEmpty(detailAbnormalDTOS)) {
            DomainEventPublisher.sendSyncDomainEvent(new PickingOrderAbnormalEvent().setDetails(detailAbnormalDTOS));
        }
    }

}
