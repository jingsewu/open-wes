package org.openwes.wes.outbound.application.event;

import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.api.outbound.dto.OutboundAllocateSkuBatchContext;
import org.openwes.wes.api.outbound.event.*;
import org.openwes.wes.common.facade.CallbackApiFacade;
import org.openwes.wes.outbound.domain.aggregate.OutboundPlanOrderPreAllocatedAggregate;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrderDetail;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.service.PickingOrderService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.openwes.common.utils.constants.RedisConstants.OUTBOUND_PLAN_ORDER_ASSIGNED_IDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundPlanOrderSubscribe {

    private final OutboundPlanOrderPreAllocatedAggregate outboundPlanOrderPreAllocatedAggregate;
    private final PickingOrderService pickingOrderService;
    private final OutboundPlanOrderRepository outboundPlanOrderRepository;
    private final CallbackApiFacade callbackApiFacade;
    private final RedisUtils redisUtils;

    @Subscribe
    public void onCreateEvent(@Valid NewOutboundPlanOrderEvent event) {
        log.info("Receive new outbound plan order pre allocate required, order no: {}", event.getOrderNo());

        OutboundPlanOrder outboundPlanOrder = outboundPlanOrderRepository.findByOrderNo(event.getOrderNo());
        if (outboundPlanOrder.getOutboundPlanOrderStatus() != OutboundPlanOrderStatusEnum.NEW) {
            log.error("outbound status must be NEW when preparing allocate stocks");
            return;
        }

        List<Long> skuIds = outboundPlanOrder.getDetails()
                .stream().map(OutboundPlanOrderDetail::getSkuId).toList();
        List<String> ownerCodes = outboundPlanOrder.getDetails()
                .stream().map(OutboundPlanOrderDetail::getOwnerCode).distinct().toList();

        OutboundAllocateSkuBatchContext preAllocateCache = pickingOrderService.prepareAllocateCache(skuIds, outboundPlanOrder.getWarehouseCode(), ownerCodes);

        log.info("pre allocate cache build success, start try allocate, order no: {}", outboundPlanOrder.getOrderNo());
        outboundPlanOrderPreAllocatedAggregate.preAllocate(outboundPlanOrder, preAllocateCache);
    }

    @Subscribe
    public void onAssignedEvent(@Valid OutboundPlanOrderAssignedEvent event) {
        String redisKey = OUTBOUND_PLAN_ORDER_ASSIGNED_IDS + event.getWarehouseCode();
        redisUtils.push(redisKey, event.getOutboundPlanOrderId());
    }

    @Subscribe
    public void onDispatchedEvent(@Valid OutboundPlanOrderDispatchedEvent event) {
        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(event.getOutboundPlanOrderIds());
        outboundPlanOrders.forEach(OutboundPlanOrder::dispatch);
        outboundPlanOrderRepository.saveAll(outboundPlanOrders);
    }

    @Subscribe
    public void onPickingEvent(@Valid OutboundPlanOrderPickingEvent event) {
        List<OutboundPlanOrderPickingEvent.PickingDetail> pickingDetails = event.getPickingDetails();
        List<Long> outboundPlanOrderIds = pickingDetails.stream().map(OutboundPlanOrderPickingEvent.PickingDetail::getOutboundOrderId).toList();
        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(outboundPlanOrderIds);

        Map<Long, OutboundPlanOrder> outboundPlanOrderMap = outboundPlanOrders.stream().collect(Collectors.toMap(OutboundPlanOrder::getId, v -> v));
        pickingDetails.forEach(pickingDetail -> {
            OutboundPlanOrder outboundPlanOrder = outboundPlanOrderMap.get(pickingDetail.getOutboundOrderId());
            outboundPlanOrder.picking(pickingDetail.getOperatedQty(), pickingDetail.getOutboundOrderDetailId());
        });

        List<Long> outboundPlanOrderDetailIds = pickingDetails.stream().map(OutboundPlanOrderPickingEvent.PickingDetail::getOutboundOrderDetailId).toList();
        outboundPlanOrders.forEach(outboundPlanOrder ->
                outboundPlanOrder.getDetails().removeIf(v -> !outboundPlanOrderDetailIds.contains(v.getId())));
        outboundPlanOrderRepository.saveOrderAndDetails(outboundPlanOrders);
    }

    @Subscribe
    public void onCompleteEvent(@Valid OutboundPlanOrderCompleteEvent event) {
        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(event.getOutboundPlanOrderIds());
        outboundPlanOrders.forEach(outboundPlanOrder ->
                callbackApiFacade.callback(CallbackApiTypeEnum.OUTBOUND_PLAN_ORDER_COMPLETE, outboundPlanOrder.getCustomerOrderType(), outboundPlanOrder));
    }

    @Subscribe
    public void onShortCompleteEvent(@Valid OutboundPlanOrderShortCompleteEvent event) {
        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(event.getOutboundPlanOrderIds());
        List<OutboundPlanOrder> shortOutboundPlanOrders = outboundPlanOrders.stream().filter(OutboundPlanOrder::shortComplete).toList();
        outboundPlanOrderRepository.saveOrderAndDetails(shortOutboundPlanOrders);
    }

}
