package org.openwes.station.domain.entity;

import org.openwes.station.api.constants.ProcessStatusEnum;
import org.openwes.station.infrastructure.remote.TaskService;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import org.openwes.wes.api.task.dto.ReportAbnormalDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class OutboundWorkStationCache extends WorkStationCache {

    private String inputPutWallSlot;
    private String activePutWallCode;

    public void input(String input) {
        this.inputPutWallSlot = input;
    }

    public void clearInput() {
        log.info("work station: {} clear input.", super.id);
        this.chooseArea = null;
        this.inputPutWallSlot = null;
    }

    public void operate() {
        this.chooseArea = null;
        this.operateTasks.removeIf(v -> {
            OperationTaskDTO operationTaskDTO = v.getOperationTaskDTO();
            if (operationTaskDTO.getRequiredQty() - operationTaskDTO.getOperatedQty() - operationTaskDTO.getAbnormalQty() == 0) {
                log.info("work station: {} remove task: {}.", super.id, operationTaskDTO.getId());
                return true;
            }
            return false;
        });

        if (ObjectUtils.isNotEmpty(this.getProcessingOperationTasks())) {
            this.operateTasks.stream().map(OperationTaskVO::getSkuMainDataDTO).findFirst()
                    .ifPresent(task -> resetActivePutWall(task.getSkuCode()));
        }
    }

    @Override
    public void resetActivePutWall(String skuCode) {

        log.info("work station: {} reset active put wall by sku: {}.", super.id, skuCode);

        Set<String> processingPutWallSlotCodes = this.getProcessingOperationTasks().stream()
                .filter(v -> StringUtils.equals(v.getSkuMainDataDTO().getSkuCode(), skuCode))
                .map(v -> v.getOperationTaskDTO().getTargetLocationCode())
                .collect(Collectors.toSet());

        boolean match = this.putWallSlots.stream()
                .anyMatch(putWallSlot -> StringUtils.equals(this.activePutWallCode, putWallSlot.getPutWallCode())
                        && processingPutWallSlotCodes.contains(putWallSlot.getPutWallSlotCode()));

        if (!match) {
            this.activePutWallCode = putWallSlots.stream()
                    .filter(putWallSlot -> processingPutWallSlotCodes.contains(putWallSlot.getPutWallSlotCode()))
                    .map(PutWallSlotDTO::getPutWallCode)
                    .findAny().orElse(null);
        }
    }

    public Collection<ArrivedContainerCache> queryTasksAndReturnRemovedContainers(TaskService taskService) {

        List<ArrivedContainerCache> undoContainers = this.getUndoContainers();
        if (CollectionUtils.isNotEmpty(this.operateTasks) || CollectionUtils.isEmpty(this.getUndoContainers())) {
            return Collections.emptySet();
        }

        List<OperationTaskVO> containerOperateTasks = undoContainers.stream()
                .flatMap(undoContainer ->
                        taskService.queryTasks(this.id, undoContainer.getContainerCode(), undoContainer.getFace(), OperationTaskTypeEnum.PICKING).stream())
                .toList();

        this.addOperateTasks(containerOperateTasks);

        if (this.operateTasks == null) {
            undoContainers.forEach(undoContainer -> undoContainer.setProcessStatus(ProcessStatusEnum.PROCEED));
        } else {
            Map<String, List<OperationTaskVO>> containerOperationTaskMap =
                    this.operateTasks.stream().collect(Collectors.groupingBy(v -> v.getOperationTaskDTO().getSourceContainerCode()));
            undoContainers.forEach(undoContainer -> {
                List<OperationTaskVO> operationTaskDTOS = containerOperationTaskMap.get(undoContainer.getContainerCode());
                if (CollectionUtils.isEmpty(operationTaskDTOS)
                        || operationTaskDTOS.stream().allMatch(v -> v.getOperationTaskDTO().getTaskStatus() == OperationTaskStatusEnum.PROCESSED)) {
                    undoContainer.setProcessStatus(ProcessStatusEnum.PROCEED);
                }
            });
        }

        setUndoContainersProcessing(undoContainers.stream().filter(v -> v.getProcessStatus() == ProcessStatusEnum.UNDO).toList());

        return removeProceedContainers();
    }

    public List<OperationTaskVO> getProcessingOperationTasks() {
        return Optional.ofNullable(this.operateTasks)
                .stream()
                .flatMap(Collection::stream)
                .filter(k -> k.getOperationTaskDTO() != null && k.getOperationTaskDTO().getTaskStatus() == OperationTaskStatusEnum.PROCESSING)
                .toList();
    }

    public void reportAbnormal(List<ReportAbnormalDTO.HandleTask> handleTasks) {
        List<OperationTaskVO> processingOperationTasks = getProcessingOperationTasks();
        if (CollectionUtils.isEmpty(processingOperationTasks)) {
            return;
        }

        Map<Long, ReportAbnormalDTO.HandleTask> handleTaskMap = handleTasks.stream()
                .collect(Collectors.toMap(ReportAbnormalDTO.HandleTask::getTaskId, Function.identity()));
        processingOperationTasks.stream()
                .filter(v -> handleTaskMap.containsKey(v.getOperationTaskDTO().getId()))
                .forEach(task -> {
                    OperationTaskDTO operationTaskDTO = task.getOperationTaskDTO();
                    ReportAbnormalDTO.HandleTask handleTask = handleTaskMap.get(operationTaskDTO.getId());
                    operationTaskDTO.setAbnormalQty(handleTask.getAbnormalQty());
                });

        if (this.operateTasks == null) {
            return;
        }
        // remove zero pick tasks
        this.operateTasks.removeIf(task -> {
            OperationTaskDTO operationTaskDTO = task.getOperationTaskDTO();
            return operationTaskDTO.getRequiredQty().equals(operationTaskDTO.getAbnormalQty());
        });
    }

    public OperationTaskDTO getFirstOperationTaskDTO() {
        if (CollectionUtils.isEmpty(this.operateTasks)) {
            return null;
        }
        return this.operateTasks.stream().iterator().next().getOperationTaskDTO();
    }
}
