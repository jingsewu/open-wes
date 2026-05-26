package org.openwes.wes.outbound.application.event;

import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.api.outbound.event.*;
import org.openwes.wes.common.facade.CallbackApiFacade;
import org.openwes.wes.outbound.application.usecase.PreAllocateOutboundOrderUseCase;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.entity.PickingOrderDetail;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.openwes.wes.outbound.domain.transfer.OutboundPlanOrderTransfer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.openwes.common.utils.constants.RedisConstants.OUTBOUND_PLAN_ORDER_ASSIGNED_IDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundPlanOrderSubscribe {

    private final PreAllocateOutboundOrderUseCase preAllocateUseCase;
    private final OutboundPlanOrderRepository outboundPlanOrderRepository;
    private final PickingOrderRepository pickingOrderRepository;
    private final CallbackApiFacade callbackApiFacade;
    private final RedisUtils redisUtils;
    private final OutboundPlanOrderTransfer outboundPlanOrderTransfer;

    @Subscribe
    public void onCreateEvent(@Valid OutboundPlanOrderCreatedEvent event) {
        log.info("Receive new outbound plan order pre allocate required, order no: {}", event.getOrderNo());
        preAllocateUseCase.execute(event.getAggregatorId());
    }

    @Subscribe
    public void onAssignedEvent(@Valid OutboundPlanOrderAssignedEvent event) {
        String redisKey = OUTBOUND_PLAN_ORDER_ASSIGNED_IDS + event.getWarehouseCode();
        redisUtils.push(redisKey, event.getAggregatorId());
    }

    @Subscribe
    @Transactional(rollbackFor = Exception.class)
    public void onDispatchedEvent(@Valid PickingOrderDispatchedEvent event) {
        PickingOrder pickingOrder = pickingOrderRepository.findById(event.getAggregatorId());
        if (pickingOrder == null) {
            log.error("picking order not found, picking order id: {}", event.getAggregatorId());
            return;
        }
        List<Long> outboundPlanOrderIds = pickingOrder.getDetails().stream()
                .map(PickingOrderDetail::getOutboundOrderPlanId).distinct().toList();

        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(outboundPlanOrderIds);
        outboundPlanOrders.forEach(OutboundPlanOrder::dispatch);
        outboundPlanOrderRepository.saveAllOrders(outboundPlanOrders);
    }

    @Subscribe
    @Transactional(rollbackFor = Exception.class)
    public void onPickingEvent(@Valid PickingOrderPickedEvent event) {
        PickingOrderPickedEvent.PickingDetail pickingDetail = event.getPickingDetail();
        OutboundPlanOrder outboundPlanOrder = outboundPlanOrderRepository.findById(pickingDetail.getOutboundOrderId());
        outboundPlanOrder.picking(pickingDetail.getOperatedQty(), pickingDetail.getOutboundOrderDetailId());
        outboundPlanOrderRepository.saveOrderAndDetail(outboundPlanOrder);
    }

    @Subscribe
    @Transactional(rollbackFor = Exception.class)
    public void onCompleteEvent(@Valid OutboundPlanOrderCompletionEvent event) {
        OutboundPlanOrder outboundPlanOrder = outboundPlanOrderRepository.findById(event.getAggregatorId());
        callbackApiFacade.callback(CallbackApiTypeEnum.OUTBOUND_PLAN_ORDER_COMPLETE,
                outboundPlanOrder.getCustomerOrderType(),
                outboundPlanOrderTransfer.toDTO(outboundPlanOrder));
    }

    @Subscribe
    @Transactional(rollbackFor = Exception.class)
    public void onOutboundWaveCompletionEvent(@Valid OutboundWaveCompletionEvent event) {
        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(event.getOutboundPlanOrderIds());
        outboundPlanOrders.stream()
                .filter(v -> v.getOutboundPlanOrderStatus() != OutboundPlanOrderStatusEnum.PICKED)
                .forEach(OutboundPlanOrder::shortComplete);
        outboundPlanOrderRepository.saveAllOrderAndDetails(outboundPlanOrders);
    }
}
