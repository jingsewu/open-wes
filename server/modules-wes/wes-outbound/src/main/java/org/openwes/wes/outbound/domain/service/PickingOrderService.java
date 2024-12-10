package org.openwes.wes.outbound.domain.service;

import org.openwes.wes.api.algo.dto.PickingOrderDispatchedResult;
import org.openwes.wes.api.algo.dto.PickingOrderHandlerContext;
import org.openwes.wes.api.algo.dto.PickingOrderReallocateContext;
import org.openwes.wes.api.algo.dto.PickingOrderReallocatedResult;
import org.openwes.wes.api.outbound.dto.OutboundAllocateSkuBatchContext;
import org.openwes.wes.api.task.dto.OperationTaskDTO;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.entity.PickingOrder;

import java.util.List;

public interface PickingOrderService {


    PickingOrderHandlerContext prepareFullContext(String warehouseCode, List<PickingOrder> pickingOrders);

    PickingOrderHandlerContext prepareStockContext(String warehouseCode, List<PickingOrder> manualPickingOrders);

    PickingOrderReallocateContext prepareReallocateStockContext(String warehouseCode, PickingOrder pickingOrder);

    /**
     * split a wave into a list of picking orders
     *
     * @param outboundWave outboundWave
     * @return
     */
    List<PickingOrder> spiltWave(OutboundWave outboundWave);

    /**
     * allocate stocks for the picking orders
     *
     * @param pickingOrders pickingOrders
     * @return
     */
    List<OperationTaskDTO> allocateStocks(PickingOrderHandlerContext pickingOrders);


    /**
     * allocate stocks for the picking orders, but the stocks that may be from different warehouse areas causing short picking
     *
     * @param pickingOrderReallocateContext pickingOrderHandlerContext
     * @return
     */
    PickingOrderReallocatedResult reallocateStocks(PickingOrderReallocateContext pickingOrderReallocateContext);

    /**
     * assign picking orders to some work stations based on the requirements
     * and
     * allocate stocks for the picking orders
     *
     * @param pickingOrderHandlerContext pickingOrderHandlerContext
     * @return
     */
    PickingOrderDispatchedResult dispatchOrders(PickingOrderHandlerContext pickingOrderHandlerContext);


    OutboundAllocateSkuBatchContext prepareAllocateCache(List<Long> skuIds, String warehouseCode, List<String> ownerCodes);

}
