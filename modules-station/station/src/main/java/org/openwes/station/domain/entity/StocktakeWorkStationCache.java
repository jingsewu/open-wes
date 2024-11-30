package org.openwes.station.domain.entity;

import org.openwes.station.api.constants.ProcessStatusEnum;
import org.openwes.station.infrastructure.remote.StocktakeService;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.dto.OperationTaskVO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class StocktakeWorkStationCache extends WorkStationCache {

    public Collection<ArrivedContainerCache> queryTasksAndReturnRemovedContainers(StocktakeService stocktakeService) {

        List<ArrivedContainerCache> undoContainers = this.getUndoContainers();
        if (CollectionUtils.isNotEmpty(this.operateTasks) || CollectionUtils.isEmpty(this.getUndoContainers())) {
            return Collections.emptySet();
        }

        List<OperationTaskVO> containerOperateTasks = undoContainers.stream()
                .flatMap(undoContainer ->
                        stocktakeService.generateStocktakeRecords(undoContainer.getContainerCode(), undoContainer.getFace(), this.id).stream())
                .toList();

        this.addOperateTasks(containerOperateTasks);

        Map<String, List<OperationTaskVO>> containerOperationTaskMap =
                this.operateTasks.stream().collect(Collectors.groupingBy(v -> v.getOperationTaskDTO().getSourceContainerCode()));

        undoContainers.forEach(undoContainer -> {
            List<OperationTaskVO> operationTaskDTOS = containerOperationTaskMap.get(undoContainer.getContainerCode());
            if (CollectionUtils.isEmpty(operationTaskDTOS)
                    || operationTaskDTOS.stream().allMatch(v -> v.getOperationTaskDTO().getTaskStatus() == OperationTaskStatusEnum.PROCESSED)) {
                undoContainer.setProcessStatus(ProcessStatusEnum.PROCEED);
            }
        });

        setUndoContainersProcessing(undoContainers.stream().filter(v -> v.getProcessStatus() == ProcessStatusEnum.UNDO).toList());

        return removeProceedContainers();
    }

    public void removeOperationTask(Long detailId) {
        this.operateTasks.removeIf(v -> v.getOperationTaskDTO().getDetailId().equals(detailId));
    }
}
