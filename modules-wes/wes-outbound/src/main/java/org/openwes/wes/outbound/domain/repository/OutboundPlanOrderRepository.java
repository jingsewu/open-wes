package org.openwes.wes.outbound.domain.repository;

import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;

import java.util.Collection;
import java.util.List;

public interface OutboundPlanOrderRepository {

    OutboundPlanOrder saveOutboundPlanOrder(OutboundPlanOrder outboundPlanOrder);

    void saveAll(List<OutboundPlanOrder> outboundPlanOrders);

    OutboundPlanOrder findByOrderNo(String orderNo);

    List<OutboundPlanOrder> findAllByIds(Collection<Long> orderIds);

    List<OutboundPlanOrder> findByCustomerOrderNo(String warehouseCode, String customerOrderNo);

    List<OutboundPlanOrder> findByCustomerOrderNos(String warehouseCode, Collection<String> customerOrderNos);

    List<OutboundPlanOrder> findByCustomerWaveNos(Collection<String> customerWaveNos);

    List<OutboundPlanOrder> findByWaveNos(Collection<String> waveNos);

    void saveOrderAndDetails(List<OutboundPlanOrder> outboundPlanOrders);

    List<OutboundPlanOrder> saveAllOrderAndDetails(List<OutboundPlanOrder> outboundPlanOrders);

    List<OutboundPlanOrder> findAllByStatus(OutboundPlanOrderStatusEnum outboundPlanOrderStatusEnum);
}
