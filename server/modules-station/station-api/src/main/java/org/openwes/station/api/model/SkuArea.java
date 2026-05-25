package org.openwes.station.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.dto.OperationTaskDTO;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuArea {
    private String scanCode;
    private List<SkuTaskInfo> operationViews;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuTaskInfo {
        private SkuMainDataDTO skuMainDataDTO;
        private SkuBatchAttributeDTO skuBatchAttributeDTO;
        private List<OperationTaskDTO> operationTaskDTOs;
    }

    public void updateOperationViews(List<SkuTaskInfo> tasks) {
        if (this.operationViews == null) {
            this.operationViews = new ArrayList<>(tasks);
        } else {
            this.operationViews.addAll(tasks);
        }
    }

    public void markTasksProcessing(String skuCode, String containerCode, String face) {
        if (CollectionUtils.isEmpty(operationViews)) return;

        operationViews.stream()
                .filter(v -> v.getOperationTaskDTOs() != null)
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .forEach(task -> task.setTaskStatus(OperationTaskStatusEnum.NEW));

        operationViews.stream()
                .filter(v -> v.getSkuMainDataDTO() != null
                        && skuCode.equals(v.getSkuMainDataDTO().getSkuCode()))
                .filter(v -> v.getOperationTaskDTOs() != null)
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .filter(task -> Objects.equals(containerCode, task.getSourceContainerCode())
                        && (face == null || face.isEmpty() || Objects.equals(face, task.getSourceContainerFace())))
                .forEach(task -> task.setTaskStatus(OperationTaskStatusEnum.PROCESSING));
    }

    public void removeCompletedTasks() {
        if (operationViews == null) return;
        operationViews.forEach(view -> {
            if (view.getOperationTaskDTOs() != null) {
                view.getOperationTaskDTOs().removeIf(task ->
                        task.getRequiredQty() - task.getOperatedQty() - task.getAbnormalQty() == 0);
            }
        });
        operationViews.removeIf(view ->
                view.getOperationTaskDTOs() == null || view.getOperationTaskDTOs().isEmpty());
    }

    public void reportAbnormal(Map<Long, Integer> taskAbnormalQtyMap) {
        if (operationViews == null) return;
        operationViews.stream()
                .filter(v -> v.getOperationTaskDTOs() != null)
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .filter(task -> task.getTaskStatus() == OperationTaskStatusEnum.PROCESSING)
                .filter(task -> taskAbnormalQtyMap.containsKey(task.getId()))
                .forEach(task -> task.setAbnormalQty(taskAbnormalQtyMap.get(task.getId())));

        operationViews.forEach(view -> {
            if (view.getOperationTaskDTOs() != null) {
                view.getOperationTaskDTOs().removeIf(task ->
                        task.getRequiredQty().equals(task.getAbnormalQty()));
            }
        });
        operationViews.removeIf(view ->
                view.getOperationTaskDTOs() == null || view.getOperationTaskDTOs().isEmpty());
    }

    public boolean hasProcessingTasks() {
        return !getProcessingTasks().isEmpty();
    }

    public boolean hasTasks() {
        return CollectionUtils.isNotEmpty(operationViews)
                && operationViews.stream().anyMatch(v -> CollectionUtils.isNotEmpty(v.getOperationTaskDTOs()));
    }

    public boolean hasAbnormalTasks() {
        if (operationViews == null) return false;
        return operationViews.stream()
                .filter(v -> v.getOperationTaskDTOs() != null)
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .anyMatch(task -> task.getAbnormalQty() > 0);
    }

    public List<OperationTaskDTO> getProcessingTasks() {
        if (operationViews == null) return Collections.emptyList();
        return operationViews.stream()
                .filter(v -> v.getOperationTaskDTOs() != null)
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .filter(task -> task.getTaskStatus() == OperationTaskStatusEnum.PROCESSING)
                .toList();
    }

    public OperationTaskDTO getFirstProcessingTask() {
        return getProcessingTasks().stream().findFirst().orElse(null);
    }

    public OperationTaskDTO getFirstTask() {
        if (operationViews == null) return null;
        return operationViews.stream()
                .filter(v -> CollectionUtils.isNotEmpty(v.getOperationTaskDTOs()))
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .findFirst().orElse(null);
    }

    public List<OperationTaskDTO> getAllTasks() {
        if (operationViews == null) return Collections.emptyList();
        return operationViews.stream()
                .filter(v -> v.getOperationTaskDTOs() != null)
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .toList();
    }

    public void clear() {
        this.scanCode = null;
        if (this.operationViews != null) {
            this.operationViews.clear();
        }
    }
}
