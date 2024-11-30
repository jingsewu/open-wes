package org.openwes.wes.api.algo;

import org.openwes.wes.api.algo.dto.PickingOrderDispatchedResult;
import org.openwes.wes.api.algo.dto.PickingOrderHandlerContext;
import org.openwes.wes.api.algo.dto.PickingOrderReallocateContext;
import org.openwes.wes.api.algo.dto.PickingOrderReallocatedResult;
import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import jakarta.validation.Valid;

import java.util.List;

public interface IPickingOrderAlgoApi {

    WarehouseAreaWorkTypeEnum getWarehouseAreaWorkType();

    /**
     * dispatch picking orders to work stations and allocate container stocks for picking orders
     *
     * @param pickingOrderHandlerContext picking order handler context
     * @return
     */
    PickingOrderDispatchedResult dispatchOrders(@Valid PickingOrderHandlerContext pickingOrderHandlerContext);

    /**
     * allocate stocks for picking orders
     *
     * @param pickingOrderHandlerContext picking order handler context
     * @return
     */
    List<OperationTaskDTO> allocateStocks(@Valid PickingOrderHandlerContext pickingOrderHandlerContext);

    /**
     * reallocate stocks for picking orders
     *
     * @param pickingOrderReallocateContext picking order handler context
     * @return
     */
    PickingOrderReallocatedResult reallocateStocks(PickingOrderReallocateContext pickingOrderReallocateContext);
}
