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

        operationTasks.forEach(operationTask -> {
            if (operationTask.getAssignedStationSlot() != null) {
                operationTask.getAssignedStationSlot().forEach((workStationId, putWallSlotCode) -> {
                    stationOperationTasks.computeIfAbsent(workStationId, k -> Sets.newHashSet()).add(operationTask);
                    stationOperationSkuBatches.computeIfAbsent(workStationId, k -> Sets.newHashSet()).add(operationTask.getSkuBatchStockId());
                    stationOperationContainers.computeIfAbsent(workStationId, k -> Sets.newHashSet()).add(operationTask.getSourceContainerCode());
                });
            }
        });

        containerStocks.forEach(containerStock -> stationOperationContainers
                .forEach((workStationId, containerCodes) -> {
                    if (containerCodes.contains(containerStock.getContainerCode())) {
                        stationContainerStocks.computeIfAbsent(workStationId, k -> Sets.newHashSet()).add(containerStock);
                    }
                }));

    }
}
