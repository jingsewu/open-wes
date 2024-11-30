package org.openwes.wes.inbound.domain.repository;

import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrder;
import org.openwes.wes.inbound.domain.entity.EmptyContainerInboundOrderDetail;

import java.util.Collection;
import java.util.List;

public interface EmptyContainerInboundRepository {

    List<EmptyContainerInboundOrderDetail> saveOrderAndDetails(EmptyContainerInboundOrder emptyContainerInboundOrder);

    List<EmptyContainerInboundOrder> findOrdersByDetailIds(Collection<Long> detailIds);

    void saveAllOrderAndDetails(List<EmptyContainerInboundOrder> orders);
}
