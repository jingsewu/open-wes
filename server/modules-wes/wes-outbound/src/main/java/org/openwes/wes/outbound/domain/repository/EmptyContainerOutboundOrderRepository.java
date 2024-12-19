package org.openwes.wes.outbound.domain.repository;

import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrder;

import java.util.List;

public interface EmptyContainerOutboundOrderRepository {

    void save(EmptyContainerOutboundOrder order);

    List<EmptyContainerOutboundOrder> findAllByIds(List<Long> orderIds);

    void saveAll(List<EmptyContainerOutboundOrder> emptyContainerOutboundOrders);

    List<EmptyContainerOutboundOrder> findOrderByDetailIds(List<Long> emptyContainerOutboundOrderDetailIds);

    void saveAllOrderAndDetails(List<EmptyContainerOutboundOrder> emptyContainerOutboundOrders);
}
