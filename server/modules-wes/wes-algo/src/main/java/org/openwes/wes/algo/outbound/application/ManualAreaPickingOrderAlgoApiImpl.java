package org.openwes.wes.algo.outbound.application;

import org.openwes.wes.algo.outbound.domain.service.StockAllocationService;
import org.openwes.wes.api.algo.IPickingOrderAlgoApi;
import org.openwes.wes.api.algo.dto.PickingOrderDispatchedResult;
import org.openwes.wes.api.algo.dto.PickingOrderHandlerContext;
import org.openwes.wes.api.algo.dto.PickingOrderReallocateContext;
import org.openwes.wes.api.algo.dto.PickingOrderReallocatedResult;
import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class ManualAreaPickingOrderAlgoApiImpl implements IPickingOrderAlgoApi {

    private final StockAllocationService manualAreaStockAllocationService;

    @Override
    public WarehouseAreaWorkTypeEnum getWarehouseAreaWorkType() {
        return WarehouseAreaWorkTypeEnum.MANUAL;
    }

    @Override
    public PickingOrderDispatchedResult dispatchOrders(PickingOrderHandlerContext pickingOrderHandlerContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<OperationTaskDTO> allocateStocks(PickingOrderHandlerContext pickingOrderHandlerContext) {

        return pickingOrderHandlerContext.getPickingOrders().stream()
                .flatMap(pickingOrder -> manualAreaStockAllocationService.allocateSinglePickingOrder(pickingOrder, pickingOrderHandlerContext))
                .toList();
    }

    @Override
    public PickingOrderReallocatedResult reallocateStocks(PickingOrderReallocateContext pickingOrderReallocateContext) {
        return manualAreaStockAllocationService.reallocateSinglePickingOrder(pickingOrderReallocateContext);
    }
}
