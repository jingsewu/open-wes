package org.openwes.station.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.station.api.constants.ProcessStatusEnum;
import org.openwes.station.api.model.ArrivedContainerCache;
import org.openwes.station.api.model.SkuArea;
import org.openwes.station.api.constants.ChooseAreaEnum;
import org.openwes.station.infrastructure.remote.StocktakeService;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.dto.OperationTaskVO;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class StocktakeWorkStationCache extends WorkStationCache {

    public List<ArrivedContainerCache> queryTasksAndReturnRemovedContainers(StocktakeService stocktakeService) {
        List<ArrivedContainerCache> undoContainers = getWorkLocationArea().getUndoContainers();
        if (getSkuArea().hasTasks() || CollectionUtils.isEmpty(undoContainers)) {
            return Collections.emptyList();
        }

        List<OperationTaskVO> containerOperateTasks = undoContainers.stream()
                .flatMap(undoContainer ->
                        stocktakeService.generateStocktakeRecords(undoContainer.getContainerCode(), undoContainer.getFace(), this.id).stream())
                .toList();

        // Convert OperationTaskVO to SkuArea.SkuTaskInfo
        addOperationTasksToSkuArea(containerOperateTasks);

        Map<String, List<OperationTaskVO>> containerOperationTaskMap =
                containerOperateTasks.stream().collect(Collectors.groupingBy(v -> v.getOperationTaskDTO().getSourceContainerCode()));

        undoContainers.forEach(undoContainer -> {
            List<OperationTaskVO> operationTaskDTOS = containerOperationTaskMap.get(undoContainer.getContainerCode());
            if (CollectionUtils.isEmpty(operationTaskDTOS)
                    || operationTaskDTOS.stream().allMatch(v -> v.getOperationTaskDTO().getTaskStatus() == OperationTaskStatusEnum.PROCESSED)) {
                undoContainer.proceed();
            }
        });

        boolean isOneStepRelocation = false; // stocktake is not one-step relocation
        getWorkLocationArea().setUndoContainersProcessing(isOneStepRelocation);

        return getWorkLocationArea().removeProceedContainers();
    }

    private void addOperationTasksToSkuArea(List<OperationTaskVO> operationTaskVOS) {
        Map<String, List<OperationTaskVO>> grouped = operationTaskVOS.stream()
                .filter(v -> v.getSkuMainDataDTO() != null)
                .collect(Collectors.groupingBy(v -> v.getSkuMainDataDTO().getSkuCode()));

        List<SkuArea.SkuTaskInfo> tasks = grouped.values().stream()
                .map(vos -> {
                    OperationTaskVO first = vos.get(0);
                    SkuArea.SkuTaskInfo info = new SkuArea.SkuTaskInfo();
                    info.setSkuMainDataDTO(first.getSkuMainDataDTO());
                    info.setSkuBatchAttributeDTO(first.getSkuBatchAttributeDTO());
                    info.setOperationTaskDTOs(vos.stream()
                            .map(OperationTaskVO::getOperationTaskDTO)
                            .collect(Collectors.toList()));
                    return info;
                }).toList();

        getSkuArea().updateOperationViews(tasks);
    }

    public void removeOperationTask(Long detailId) {
        if (getSkuArea() == null || getSkuArea().getOperationViews() == null) return;
        getSkuArea().getOperationViews().forEach(info -> {
            if (info.getOperationTaskDTOs() != null) {
                info.getOperationTaskDTOs().removeIf(task -> task.getDetailId().equals(detailId));
            }
        });
        getSkuArea().getOperationViews().removeIf(info ->
                info.getOperationTaskDTOs() == null || info.getOperationTaskDTOs().isEmpty());
    }

    @Override
    protected void recalculateChooseArea() {
        boolean hasTasks = getSkuArea() != null && getSkuArea().hasTasks();
        boolean hasContainers = getWorkLocationArea() != null && getWorkLocationArea().hasContainers();

        if (hasTasks && hasContainers) {
            chooseArea(ChooseAreaEnum.SKU_AREA);
        } else if (hasContainers) {
            chooseArea(ChooseAreaEnum.CONTAINER_AREA);
        }
    }
}
