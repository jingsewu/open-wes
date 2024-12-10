package org.openwes.wes.algo.outbound.domain.entity;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
public class OrderAssignStationModel {

    private final Map<Long, Set<OperationTaskDTO>> stationOperationTasks;

    private final Map<Long, Set<Long>> stationOperationSkuBatches;

    private final Map<Long, Set<String>> stationOperationContainers;

    private final Map<Long, Set<ContainerStockDTO>> stationContainerStocks;

    public OrderAssignStationModel(List<OperationTaskDTO> operationTasks, List<ContainerStockDTO> containerStocks) {
        stationOperationTasks = Maps.newHashMap();
        stationOperationSkuBatches = Maps.newHashMap();
        stationOperationContainers = Maps.newHashMap();
        stationContainerStocks = Maps.newHashMap();

        operationTasks.forEach(operationTask ->
            operationTask.getAssignedStationSlot().forEach((workStationId, putWallSlotCode) -> {
                if (stationOperationTasks.containsKey(workStationId)) {
                    stationOperationTasks.get(workStationId).add(operationTask);
                } else {
                    stationOperationTasks.put(workStationId, Sets.newHashSet(operationTask));
                }

                if (stationOperationSkuBatches.containsKey(workStationId)) {
                    stationOperationSkuBatches.get(workStationId).add(operationTask.getSkuBatchStockId());
                } else {
                    stationOperationSkuBatches.put(workStationId, Sets.newHashSet(operationTask.getSkuBatchStockId()));
                }

                if (stationOperationContainers.containsKey(workStationId)) {
                    stationOperationContainers.get(workStationId).add(operationTask.getSourceContainerCode());
                } else {
                    stationOperationContainers.put(workStationId, Sets.newHashSet(operationTask.getSourceContainerCode()));
                }
            }));

        containerStocks.forEach(containerStock -> stationOperationContainers.forEach((workStationId, containerCodes) -> {
            if (containerCodes.contains(containerStock.getContainerCode())) {
                if (stationContainerStocks.containsKey(workStationId)) {
                    stationContainerStocks.get(workStationId).add(containerStock);
                } else {
                    stationContainerStocks.put(workStationId, Sets.newHashSet(containerStock));
                }
            }
        }));

    }
}
