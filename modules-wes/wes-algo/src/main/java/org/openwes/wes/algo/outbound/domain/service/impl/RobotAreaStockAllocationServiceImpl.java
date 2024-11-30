package org.openwes.wes.algo.outbound.domain.service.impl;

import org.openwes.wes.algo.outbound.domain.service.StockAllocationService;
import org.openwes.wes.api.algo.dto.PickingOrderHandlerContext;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service("robotAreaStockAllocationService")
public class RobotAreaStockAllocationServiceImpl implements StockAllocationService {

    @Override
    public Stream<OperationTaskDTO> allocateSinglePickingOrder(PickingOrderDTO pickingOrder,
                                                               PickingOrderHandlerContext pickingOrderHandlerContext) {

        //1. priority to allocate the container stocks which coming to the station
        List<OperationTaskDTO> stationUndoOperationTasks = pickingOrderHandlerContext.getUndoOperationTasks().stream()
                .filter(v -> v.getAssignedStationSlot().keySet().containsAll(pickingOrder.getAssignedStationSlot().keySet())).toList();
        Set<String> containerSet = stationUndoOperationTasks.stream().map(OperationTaskDTO::getSourceContainerCode).collect(Collectors.toSet());
        List<ContainerStockDTO> comingContainerStocks = pickingOrderHandlerContext.getContainerStocks().stream()
                .filter(containerStockDTO -> containerSet.contains(containerStockDTO.getContainerCode()))
                .toList();

        Stream<OperationTaskDTO> allocate = allocate(pickingOrder, comingContainerStocks, pickingOrderHandlerContext.getStationAllocatedContainers());

        //2. allocate the container stocks which has been allocated in the same session
        if (MapUtils.isNotEmpty(pickingOrderHandlerContext.getStationAllocatedContainers())) {
            List<String> containerCodes = pickingOrder.getAssignedStationSlot().keySet().stream().flatMap(workStationId ->
                    pickingOrderHandlerContext.getStationAllocatedContainers().getOrDefault(workStationId, Collections.emptySet())
                            .stream()).distinct().toList();
            List<ContainerStockDTO> sessionAllocatedContainers = pickingOrderHandlerContext.getContainerStocks().stream()
                    .filter(containerStockDTO -> containerCodes.contains(containerStockDTO.getContainerSlotCode()))
                    .toList();
            allocate = Stream.concat(allocate(pickingOrder, sessionAllocatedContainers, pickingOrderHandlerContext.getStationAllocatedContainers()), allocate);
        }

        //TODO
        // 3. sort by the distance between the container location and the station
        List<ContainerStockDTO> leftContainerStocks = pickingOrderHandlerContext.getContainerStocks().stream().filter(v -> v.getAvailableQty() > 0).toList();
        return Stream.concat(allocate(pickingOrder, leftContainerStocks, pickingOrderHandlerContext.getStationAllocatedContainers()), allocate);
    }
}
