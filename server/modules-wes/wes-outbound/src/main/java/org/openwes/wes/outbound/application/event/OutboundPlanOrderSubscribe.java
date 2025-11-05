package org.openwes.wes.outbound.application.event;

import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.api.outbound.constants.PickingOrderStatusEnum;
import org.openwes.wes.api.outbound.dto.OutboundAllocateSkuBatchContext;
import org.openwes.wes.api.outbound.event.*;
import org.openwes.wes.common.facade.CallbackApiFacade;
import org.openwes.wes.outbound.domain.aggregate.OutboundPlanOrderPreAllocatedAggregate;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrderDetail;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.entity.PickingOrderDetail;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.openwes.wes.outbound.domain.service.PickingOrderService;
import org.openwes.wes.outbound.domain.transfer.OutboundPlanOrderTransfer;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.openwes.common.utils.constants.RedisConstants.OUTBOUND_PLAN_ORDER_ASSIGNED_IDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundPlanOrderSubscribe {

    private final PickingOrderRepository pickingOrderRepository;
    private final OutboundPlanOrderPreAllocatedAggregate outboundPlanOrderPreAllocatedAggregate;
    private final PickingOrderService pickingOrderService;
    private final OutboundPlanOrderRepository outboundPlanOrderRepository;
    private final CallbackApiFacade callbackApiFacade;
    private final RedisUtils redisUtils;
    private final OutboundPlanOrderTransfer outboundPlanOrderTransfer;

    @Subscribe
    public void onCreateEvent(@Valid OutboundPlanOrderCreatedEvent event) {
        log.info("Receive new outbound plan order pre allocate required, order no: {}", event.getOrderNo());

        OutboundPlanOrder outboundPlanOrder = outboundPlanOrderRepository.findById(event.getAggregatorId());
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
        redisUtils.push(redisKey, event.getAggregatorId());
    }

    @Subscribe
    public void onDispatchedEvent(@Valid PickingOrderDispatchedEvent event) {

        PickingOrder pickingOrder = pickingOrderRepository.findById(event.getAggregatorId());
        if (pickingOrder == null) {
            log.error("picking order not found, picking order id: {}", event.getAggregatorId());
            return;
        }
        List<Long> outboundPlanOrderIds = pickingOrder.getDetails().stream().map(PickingOrderDetail::getOutboundOrderPlanId)
                .distinct().toList();

        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(outboundPlanOrderIds);
        outboundPlanOrders.forEach(OutboundPlanOrder::dispatch);
        outboundPlanOrderRepository.saveAllOrders(outboundPlanOrders);
    }

    @Subscribe
    public void onPickingEvent(@Valid PickingOrderPickedEvent event) {
        PickingOrderPickedEvent.PickingDetail pickingDetail = event.getPickingDetail();
        OutboundPlanOrder outboundPlanOrder = outboundPlanOrderRepository.findById(pickingDetail.getOutboundOrderId());

        outboundPlanOrder.picking(pickingDetail.getOperatedQty(), pickingDetail.getOutboundOrderDetailId());
        outboundPlanOrderRepository.saveOrderAndDetail(outboundPlanOrder);
    }

    @Subscribe
    public void onOutboundWaveCompletionEvent(@Valid OutboundWaveCompletionEvent event) {
        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(event.getOutboundPlanOrderIds());
        outboundPlanOrders.stream().filter(v -> v.getOutboundPlanOrderStatus() != OutboundPlanOrderStatusEnum.PICKED)
                .forEach(OutboundPlanOrder::shortComplete);
        outboundPlanOrderRepository.saveAllOrderAndDetails(outboundPlanOrders);
    }

    @Subscribe
    public void onCompleteEvent(@Valid OutboundPlanOrderCompletionEvent event) {
        OutboundPlanOrder outboundPlanOrder = outboundPlanOrderRepository.findById(event.getAggregatorId());
        callbackApiFacade.callback(CallbackApiTypeEnum.OUTBOUND_PLAN_ORDER_COMPLETE, outboundPlanOrder.getCustomerOrderType(),
                outboundPlanOrderTransfer.toDTO(outboundPlanOrder));
    }

}
