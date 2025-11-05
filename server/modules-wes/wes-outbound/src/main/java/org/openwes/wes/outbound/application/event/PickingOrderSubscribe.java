package org.openwes.wes.outbound.application.event;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.wes.api.basic.IPutWallApi;
import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.api.outbound.IPickingOrderApi;
import org.openwes.wes.api.outbound.event.OutboundPlanOrderImprovedPriorityEvent;
import org.openwes.wes.api.outbound.event.PickingOrderRemindSealContainerEvent;
import org.openwes.wes.api.task.ITaskApi;
import org.openwes.wes.api.task.dto.OperationTaskPickingDTO;
import org.openwes.wes.api.task.event.OperationTaskAbnormalEvent;
import org.openwes.wes.api.task.event.OperationTaskPickedEvent;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PickingOrderSubscribe {

    private final IPickingOrderApi pickingOrderApi;
    private final IWarehouseAreaApi warehouseAreaApi;
    private final IPutWallApi putWallApi;
    private final ITaskApi taskApi;
    private final PickingOrderRepository pickingOrderRepository;

    @Subscribe
    public void onOperationTaskPickedEvent(@Valid OperationTaskPickedEvent event) {

        OperationTaskPickingDTO operationTask = event.getOperationTaskPicking();

        PickingOrder pickingOrder = pickingOrderRepository.findById(operationTask.getOrderId());
        pickingOrder.picking(operationTask.getOperatedQty(), operationTask.getDetailId());
        pickingOrderRepository.saveOrderAndDetail(pickingOrder);
    }

    @Subscribe
    public void onOperationTaskAbnormalEvent(@Valid OperationTaskAbnormalEvent event) {

        PickingOrder pickingOrder = pickingOrderRepository.findById(event.getPickingOrderId());
        pickingOrder.reportAbnormal(event.getAbnormalQty(), event.getPickingOrderDetailId());

        pickingOrderRepository.saveOrderAndDetail(pickingOrder);

        pickingOrderApi.reallocate(Lists.newArrayList(event.getPickingOrderDetailId()));
    }

    @Subscribe
    public void onPickingOrderRemindSealContainerEvent(@Valid PickingOrderRemindSealContainerEvent event) {

        WarehouseAreaDTO warehouseArea = warehouseAreaApi.getById(event.getWarehouseAreaId());
        if (WarehouseAreaWorkTypeEnum.ROBOT == warehouseArea.getWarehouseAreaWorkType()) {
            putWallApi.remindToSealContainer(event.getAggregatorId(), event.getAssignedStationSlots());
        } else if (WarehouseAreaWorkTypeEnum.MANUAL == warehouseArea.getWarehouseAreaWorkType()) {
            taskApi.sealContainer(event.getAggregatorId());
        }
    }

    @Subscribe
    public void onImprovePriority(OutboundPlanOrderImprovedPriorityEvent event) {
        List<PickingOrder> pickingOrders = pickingOrderRepository.findAllByOutboundPlanOrderId(event.getAggregatorId());

        if (ObjectUtils.isEmpty(pickingOrders)) {
            return;
        }

        pickingOrders.forEach(pickingOrder -> pickingOrder.improvePriority(event.getPriority()));
        pickingOrderRepository.saveAllOrders(pickingOrders);
    }

}
