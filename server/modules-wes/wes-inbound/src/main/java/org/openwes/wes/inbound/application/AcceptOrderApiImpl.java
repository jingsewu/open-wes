package org.openwes.wes.inbound.application;

import org.openwes.wes.api.inbound.IAcceptOrderApi;
import org.openwes.wes.inbound.domain.aggregate.InboundAcceptAggregate;
import org.openwes.wes.inbound.domain.entity.AcceptOrder;
import org.openwes.wes.inbound.domain.entity.AcceptOrderDetail;
import org.openwes.wes.inbound.domain.entity.InboundPlanOrder;
import org.openwes.wes.inbound.domain.repository.AcceptOrderRepository;
import org.openwes.wes.inbound.domain.repository.InboundPlanOrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@Service
@DubboService
@RequiredArgsConstructor
public class AcceptOrderApiImpl implements IAcceptOrderApi {

    private final AcceptOrderRepository acceptOrderRepository;
    private final InboundPlanOrderRepository inboundPlanOrderRepository;
    private final InboundAcceptAggregate inboundAcceptAggregate;

    @Override
    public void complete(Long acceptOrderId) {
        AcceptOrder acceptOrder = acceptOrderRepository.findById(acceptOrderId);
        List<Long> inboundPlanOrderIds = acceptOrder.getDetails().stream().map(AcceptOrderDetail::getInboundPlanOrderId).toList();
        List<InboundPlanOrder> inboundPlanOrders = inboundPlanOrderRepository.findAllByIds(inboundPlanOrderIds);
        inboundAcceptAggregate.complete(acceptOrder, inboundPlanOrders);
    }

    @Override
    public void cancel(Long acceptOrderId, Long acceptOrderDetailId) {
        AcceptOrder acceptOrder = acceptOrderRepository.findById(acceptOrderId);

        List<Long> inboundPlanOrderIds = acceptOrder.getDetails()
                .stream().filter(v -> acceptOrderDetailId == null || v.getId().equals(acceptOrderDetailId))
                .map(AcceptOrderDetail::getInboundPlanOrderId).toList();

        List<InboundPlanOrder> inboundPlanOrders = inboundPlanOrderRepository.findAllByIds(inboundPlanOrderIds);

        inboundAcceptAggregate.cancelAccept(acceptOrder, acceptOrderDetailId, inboundPlanOrders);
    }

    @Override
    public void complete(String containerCode) {
        AcceptOrder acceptOrder = acceptOrderRepository.findNewStatusAcceptOrder(containerCode);
        if (acceptOrder == null) {
            throw new IllegalArgumentException("can not find any accept order");
        }

        List<Long> inboundPlanOrderIds = acceptOrder.getDetails().stream().map(AcceptOrderDetail::getInboundPlanOrderId).toList();
        List<InboundPlanOrder> inboundPlanOrders = inboundPlanOrderRepository.findAllByIds(inboundPlanOrderIds);
        inboundAcceptAggregate.complete(acceptOrder, inboundPlanOrders);
    }

}
