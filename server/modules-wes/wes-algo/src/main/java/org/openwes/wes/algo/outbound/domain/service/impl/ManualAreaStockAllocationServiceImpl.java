package org.openwes.wes.algo.outbound.domain.service.impl;

import org.openwes.wes.algo.outbound.domain.service.StockAllocationService;
import org.openwes.wes.api.algo.dto.PickingOrderHandlerContext;
import org.openwes.wes.api.outbound.dto.PickingOrderDTO;
import org.openwes.wes.api.stock.dto.ContainerStockDTO;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service("manualAreaStockAllocationService")
public class ManualAreaStockAllocationServiceImpl implements StockAllocationService {

    @Override
    public Stream<OperationTaskDTO> allocateSinglePickingOrder(PickingOrderDTO pickingOrder, PickingOrderHandlerContext pickingOrderHandlerContext) {
        // calculate the path of human then sort by path
        List<ContainerStockDTO> leftContainerStocks = pickingOrderHandlerContext.getContainerStocks().stream().filter(v -> v.getAvailableQty() > 0).toList();
        return allocate(pickingOrder, leftContainerStocks, pickingOrderHandlerContext.getStationAllocatedContainers());
    }
}
