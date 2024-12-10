package org.openwes.wes.outbound.application.event;

import com.google.common.eventbus.Subscribe;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.basic.IPutWallApi;
import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.api.outbound.IPickingOrderApi;
import org.openwes.wes.api.outbound.constants.PickingOrderStatusEnum;
import org.openwes.wes.api.outbound.event.OutboundWaveCompleteEvent;
import org.openwes.wes.api.outbound.event.PickingOrderAbnormalEvent;
import org.openwes.wes.api.outbound.event.PickingOrderCompleteEvent;
import org.openwes.wes.api.task.ITaskApi;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.openwes.wes.api.task.dto.OperationTaskPickingDTO;
import org.openwes.wes.api.task.event.PickingOrderPickingEvent;
import org.openwes.wes.outbound.domain.aggregate.PickingOrderTaskAggregate;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PickingOrderSubscribe {

    private final IPickingOrderApi pickingOrderApi;
    private final IWarehouseAreaApi warehouseAreaApi;
    private final IPutWallApi putWallApi;
    private final ITaskApi taskApi;
    private final PickingOrderRepository pickingOrderRepository;
    private final PickingOrderTaskAggregate pickingOrderTaskAggregate;

    @Subscribe
    public void onPickingEvent(@Valid PickingOrderPickingEvent event) {

        List<OperationTaskPickingDTO> operationTasks = event.getOperationTasks()
                .stream().filter(v -> v.getTaskType() == OperationTaskTypeEnum.PICKING).toList();
        if (CollectionUtils.isEmpty(operationTasks)) {
            log.warn("receive new picking event: {} but all task types are wrong", event);
            return;
        }
        Set<Long> pickingOrderIds = operationTasks.stream().map(OperationTaskPickingDTO::getOrderId).collect(Collectors.toSet());

        List<PickingOrder> pickingOrders = pickingOrderRepository.findOrderAndDetailsByPickingOrderIds(pickingOrderIds);

        pickingOrderTaskAggregate.picking(operationTasks, pickingOrders);
    }

    @Subscribe
    public void onPickingAbnormalEvent(@Valid PickingOrderAbnormalEvent event) {

        List<PickingOrderAbnormalEvent.PickingOrderAbnormalDetail> details = event.getDetails();

        Set<Long> pickingOrderIds = details.stream()
                .map(PickingOrderAbnormalEvent.PickingOrderAbnormalDetail::getPickingOrderId).collect(Collectors.toSet());
        Set<Long> pickingOrderDetailIds = details.stream()
                .map(PickingOrderAbnormalEvent.PickingOrderAbnormalDetail::getPickingOrderDetailId).collect(Collectors.toSet());

        List<PickingOrder> pickingOrders = pickingOrderRepository.findOrderAndDetailsByPickingOrderIdsAndDetailIds(pickingOrderIds, pickingOrderDetailIds);
        Map<Long, PickingOrder> pickingOrderMap = pickingOrders.stream().collect(Collectors.toMap(PickingOrder::getId, Function.identity()));
        details.forEach(v -> {
            PickingOrder pickingOrder = pickingOrderMap.get(v.getPickingOrderId());
            pickingOrder.reportAbnormal(v.getAbnormalQty(), v.getPickingOrderDetailId());
        });

        pickingOrderRepository.saveOrderAndDetails(pickingOrders);

        pickingOrderApi.reallocate(event.getDetails().stream()
                .map(PickingOrderAbnormalEvent.PickingOrderAbnormalDetail::getPickingOrderDetailId).toList());
    }

    @Subscribe
    public void onCompleteEvent(@Valid PickingOrderCompleteEvent event) {
        List<PickingOrder> pickingOrders = pickingOrderRepository.findOrderByPickingOrderIds(event.getPickingOrderIds());

        handleWaves(pickingOrders);

        remindOrSealContainer(pickingOrders);
    }

    private void remindOrSealContainer(List<PickingOrder> pickingOrders) {
        List<PickingOrder> pickedPickingOrders = pickingOrders.stream()
                .filter(v -> v.getPickingOrderStatus() == PickingOrderStatusEnum.PICKED).toList();
        if (ObjectUtils.isEmpty(pickedPickingOrders)) {
            return;
        }

        Map<Long, List<PickingOrder>> pickedPickingOrderMap = pickedPickingOrders.stream()
                .collect(Collectors.groupingBy(PickingOrder::getWarehouseAreaId));

        Map<Long, WarehouseAreaDTO> warehouseAreaMap = warehouseAreaApi.getByIds(pickedPickingOrderMap.keySet())
                .stream().collect(Collectors.toMap(WarehouseAreaDTO::getId, Function.identity()));

        pickedPickingOrders.forEach(pickingOrder -> {
            WarehouseAreaDTO warehouseAreaDTO = warehouseAreaMap.get(pickingOrder.getWarehouseAreaId());
            if (WarehouseAreaWorkTypeEnum.ROBOT == warehouseAreaDTO.getWarehouseAreaWorkType()) {
                putWallApi.remindToSealContainer(pickingOrder.getId(), pickingOrder.getAssignedStationSlot());
            } else if (WarehouseAreaWorkTypeEnum.MANUAL == warehouseAreaDTO.getWarehouseAreaWorkType()) {
                taskApi.sealContainer(pickingOrder.getId());
            }
        });
    }

    private void handleWaves(List<PickingOrder> pickingOrders) {

        List<String> waveNos = pickingOrders.stream().filter(v -> v.getPickingOrderStatus() == PickingOrderStatusEnum.PICKED)
                .map(PickingOrder::getWaveNo).distinct().toList();

        List<PickingOrder> wavePickingOrders = pickingOrderRepository.findByWaveNos(waveNos);
        Map<String, List<PickingOrder>> waveMap = wavePickingOrders.stream().collect(Collectors.groupingBy(PickingOrder::getWaveNo));

        waveMap.forEach((waveNo, orders) -> {
            if (orders.stream().allMatch(v -> v.getPickingOrderStatus() == PickingOrderStatusEnum.PICKED
                    || v.getPickingOrderStatus() == PickingOrderStatusEnum.CANCELED)) {
                DomainEventPublisher.sendAsyncDomainEvent(new OutboundWaveCompleteEvent().setWaveNo(waveNo));
            }
        });
    }

}
