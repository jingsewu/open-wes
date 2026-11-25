package org.openwes.station.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.station.api.constants.ProcessStatusEnum;
import org.openwes.station.api.model.ArrivedContainerCache;
import org.openwes.station.api.model.OrderArea;
import org.openwes.station.api.model.SkuArea;
import org.openwes.station.api.constants.ChooseAreaEnum;
import org.openwes.station.infrastructure.remote.StocktakeService;
import org.openwes.wes.api.stocktake.dto.StocktakeTaskDTO;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.dto.OperationTaskVO;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Slf4j
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
        refreshOrderArea(stocktakeService, containerOperateTasks);

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

    /**
     * The station view has no stocktake order metadata of its own; the operator
     * needs the task number, counting method and type next to the queue. Populate
     * the shared OrderArea from the task that produced these records. Failure is
     * non-fatal — counting still works without the rail metadata.
     */
    private void refreshOrderArea(StocktakeService stocktakeService, List<OperationTaskVO> operationTaskVOS) {
        if (CollectionUtils.isEmpty(operationTaskVOS) || operationTaskVOS.get(0).getOperationTaskDTO() == null) {
            return;
        }

        Long stocktakeTaskId = operationTaskVOS.get(0).getOperationTaskDTO().getOrderId();
        try {
            List<StocktakeTaskDTO> tasks = stocktakeService.getStocktakeTasks(this.id);
            if (CollectionUtils.isEmpty(tasks)) {
                return;
            }

            StocktakeTaskDTO task = tasks.stream()
                    .filter(v -> Objects.equals(v.getId(), stocktakeTaskId))
                    .findFirst()
                    .orElse(tasks.get(0));

            OrderArea orderArea = new OrderArea();
            OrderArea.OrderVO order = new OrderArea.OrderVO();
            order.setOrderNo(task.getTaskNo());
            order.setStocktakeMethod(task.getStocktakeMethod() == null ? null : task.getStocktakeMethod().name());
            order.setStocktakeType(task.getStocktakeType() == null ? null : task.getStocktakeType().name());
            order.setStocktakeCreateMethod(task.getStocktakeCreateMethod() == null
                    ? null : task.getStocktakeCreateMethod().name());
            orderArea.setCurrentOrder(order);
            setOrderArea(orderArea);
        } catch (Exception e) {
            log.warn("work station: {} failed to load stocktake order metadata for task: {}",
                    this.id, stocktakeTaskId, e);
        }
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

    /**
     * STOCKTAKE_SUBMIT carries the stocktake record id, which the station exposes
     * as the operation task id. detailId is the stocktake task detail id and may
     * be shared by several records, so matching on it removed nothing (or too much).
     */
    public void removeOperationTask(Long recordId) {
        if (getSkuArea() == null || getSkuArea().getOperationViews() == null) return;
        getSkuArea().getOperationViews().forEach(info -> {
            if (info.getOperationTaskDTOs() != null) {
                info.getOperationTaskDTOs().removeIf(task -> recordId.equals(task.getId()));
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
