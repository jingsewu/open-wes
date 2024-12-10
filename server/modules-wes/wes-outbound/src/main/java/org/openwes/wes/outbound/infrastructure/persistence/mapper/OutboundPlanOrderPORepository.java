package org.openwes.wes.outbound.infrastructure.persistence.mapper;

import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.outbound.infrastructure.persistence.po.OutboundPlanOrderPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OutboundPlanOrderPORepository extends JpaRepository<OutboundPlanOrderPO, Long> {

    OutboundPlanOrderPO findByOrderNo(String orderNo);

    List<OutboundPlanOrderPO> findAllByWarehouseCodeAndCustomerOrderNo(String warehouseCode, String customerOrderNo);

    List<OutboundPlanOrderPO> findAllByWarehouseCodeAndCustomerOrderNoIn(String warehouseCode, Collection<String> customerOrderNo);

    List<OutboundPlanOrderPO> findAllByCustomerWaveNoIn(Collection<String> customerWaveNos);

    List<OutboundPlanOrderPO> findAllByWaveNoIn(Collection<String> waveNos);

    List<OutboundPlanOrderPO> findAllByOutboundPlanOrderStatus(OutboundPlanOrderStatusEnum outboundPlanOrderStatusEnum);
}
