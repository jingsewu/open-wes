package org.openwes.wes.outbound.domain.repository;

import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;

import java.util.Collection;
import java.util.List;

public interface OutboundPlanOrderRepository {

    void saveOrderAndDetail(OutboundPlanOrder outboundPlanOrder);

    void saveAllOrderAndDetails(List<OutboundPlanOrder> outboundPlanOrders);

    void saveAllOrders(List<OutboundPlanOrder> outboundPlanOrders);

    OutboundPlanOrder findByOrderNo(String orderNo);

    OutboundPlanOrder findById(Long orderId);

    List<OutboundPlanOrder> findAllByIds(Collection<Long> orderIds);

    List<OutboundPlanOrder> findByCustomerOrderNo(String warehouseCode, String customerOrderNo);

    List<OutboundPlanOrder> findByCustomerOrderNos(String warehouseCode, Collection<String> customerOrderNos);

    List<OutboundPlanOrder> findByCustomerWaveNos(Collection<String> customerWaveNos);

    List<OutboundPlanOrder> findByWaveNos(Collection<String> waveNos);

    List<OutboundPlanOrder> findAllByStatus(OutboundPlanOrderStatusEnum outboundPlanOrderStatusEnum);

    long countByCustomerOrderNos(String warehouseCode, Collection<String> customerOrderNos);

}
