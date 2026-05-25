package org.openwes.station.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.station.api.model.ArrivedContainerCache;
import org.openwes.station.api.model.Tip;
import org.openwes.station.api.vo.WorkStationVO;
import org.openwes.station.infrastructure.remote.TaskService;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import org.openwes.wes.api.task.dto.ReportAbnormalDTO;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Slf4j
public class OutboundWorkStationCache extends WorkStationCache {

    public void input(String input) {
        getPutWallArea().input(input);
    }

    public void clearInput() {
        log.info("work station: {} clear input.", super.id);
        this.chooseArea = null;
        getPutWallArea().clearInput();
    }

    public void operate() {
        this.chooseArea = null;
        getSkuArea().removeCompletedTasks();

        if (!getSkuArea().getProcessingTasks().isEmpty()) {
            Set<String> processingSlotCodes = getSkuArea().getProcessingTasks().stream()
                    .map(OperationTaskDTO::getTargetLocationCode)
                    .collect(Collectors.toSet());
            getPutWallArea().resetActivePutWall(processingSlotCodes);
        }
        recalculateChooseArea();
    }

    public void resetActivePutWall(String skuCode) {
        log.info("work station: {} reset active put wall by sku: {}.", super.id, skuCode);

        Set<String> processingPutWallSlotCodes = getSkuArea().getProcessingTasks().stream()
                .map(OperationTaskDTO::getTargetLocationCode)
                .collect(Collectors.toSet());

        getPutWallArea().resetActivePutWall(processingPutWallSlotCodes);
    }

    public List<ArrivedContainerCache> queryTasksAndReturnRemovedContainers(TaskService taskService) {
        List<ArrivedContainerCache> undoContainers = getWorkLocationArea().getUndoContainers();
        if (getSkuArea().hasTasks() || CollectionUtils.isEmpty(undoContainers)) {
            return Collections.emptyList();
        }

        for (ArrivedContainerCache arrivedContainerCache : undoContainers) {
            List<OperationTaskVO> operationTaskVOS = taskService.queryTasks(
                    this.id, arrivedContainerCache.getContainerCode(),
                    arrivedContainerCache.getFace(), OperationTaskTypeEnum.PICKING);

            if (ObjectUtils.isEmpty(operationTaskVOS)
                    || operationTaskVOS.stream().allMatch(v -> v.getOperationTaskDTO().getTaskStatus() == OperationTaskStatusEnum.PROCESSED)) {
                arrivedContainerCache.proceed();
                continue;
            }

            // Convert OperationTaskVO to SkuArea.SkuTaskInfo
            addOperationTasksToSkuArea(operationTaskVOS);
            arrivedContainerCache.processing();
            break;
        }

        return getWorkLocationArea().removeProceedContainers();
    }

    private void addOperationTasksToSkuArea(List<OperationTaskVO> operationTaskVOS) {
        Map<String, List<OperationTaskVO>> grouped = operationTaskVOS.stream()
                .filter(v -> v.getSkuMainDataDTO() != null)
                .collect(Collectors.groupingBy(v -> v.getSkuMainDataDTO().getSkuCode()));

        List<org.openwes.station.api.model.SkuArea.SkuTaskInfo> tasks = grouped.values().stream()
                .map(vos -> {
                    OperationTaskVO first = vos.get(0);
                    org.openwes.station.api.model.SkuArea.SkuTaskInfo info = new org.openwes.station.api.model.SkuArea.SkuTaskInfo();
                    info.setSkuMainDataDTO(first.getSkuMainDataDTO());
                    info.setSkuBatchAttributeDTO(first.getSkuBatchAttributeDTO());
                    info.setOperationTaskDTOs(vos.stream()
                            .map(OperationTaskVO::getOperationTaskDTO)
                            .collect(Collectors.toList()));
                    return info;
                }).toList();

        getSkuArea().updateOperationViews(tasks);
    }

    public List<OperationTaskDTO> getProcessingOperationTasks() {
        return getSkuArea().getProcessingTasks();
    }

    public void reportAbnormal(List<ReportAbnormalDTO.HandleTask> handleTasks) {
        Map<Long, Integer> taskAbnormalQtyMap = handleTasks.stream()
                .collect(Collectors.toMap(ReportAbnormalDTO.HandleTask::getTaskId, ReportAbnormalDTO.HandleTask::getAbnormalQty));
        getSkuArea().reportAbnormal(taskAbnormalQtyMap);
    }

    public OperationTaskDTO getFirstOperationTaskDTO() {
        return getSkuArea().getFirstTask();
    }

    public void processTasks(String skuCode) {
        log.info("work station: {} code: {} process sku: {} tasks", this.id, this.stationCode, skuCode);
        this.chooseArea = null;
        getSkuArea().setScanCode(skuCode);

        ArrivedContainerCache processingContainer = getWorkLocationArea().getProcessingContainers().stream()
                .findFirst().orElse(null);
        if (processingContainer == null) {
            log.error("work station: {} code: {} no processing containers", this.id, this.stationCode);
            return;
        }

        getSkuArea().markTasksProcessing(skuCode, processingContainer.getContainerCode(), processingContainer.getFace());
        resetActivePutWall(skuCode);
    }

    @Override
    protected void recalculateChooseArea() {
        boolean hasTasks = getSkuArea() != null && getSkuArea().hasTasks();
        boolean hasContainers = getWorkLocationArea() != null && getWorkLocationArea().hasContainers();
        boolean hasPutWall = getPutWallArea() != null && getPutWallArea().hasWaitingBindingSlots();
        boolean hasProcessingTasks = getSkuArea() != null && getSkuArea().hasProcessingTasks();

        if (hasProcessingTasks) {
            chooseArea(WorkStationVO.ChooseAreaEnum.PUT_WALL_AREA);
        } else if (hasTasks && hasContainers) {
            chooseArea(WorkStationVO.ChooseAreaEnum.SKU_AREA);
        } else if (hasPutWall) {
            chooseArea(WorkStationVO.ChooseAreaEnum.PUT_WALL_AREA);
        } else if (hasContainers) {
            chooseArea(WorkStationVO.ChooseAreaEnum.CONTAINER_AREA);
        }
    }

    @Override
    protected void recalculateToolbar() {
        if (toolbar == null) return;
        boolean hasTasks = getSkuArea() != null && getSkuArea().hasTasks();
        boolean hasAbnormal = getSkuArea() != null && getSkuArea().hasAbnormalTasks();
        toolbar.setEnableReportAbnormal(hasTasks && !hasAbnormal);
        toolbar.setEnableSplitContainer(hasTasks);
    }
}
