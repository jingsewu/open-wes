package org.openwes.wes.outbound.infrastructure.persistence.mapper;

import org.openwes.wes.outbound.infrastructure.persistence.po.PickingOrderDetailPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface PickingOrderDetailPORepository extends JpaRepository<PickingOrderDetailPO, Long> {

    List<PickingOrderDetailPO> findByPickingOrderIdIn(Collection<Long> pickingOrderIds);

    List<PickingOrderDetailPO> findAllByOutboundOrderPlanId(Long outboundOrderPlanId);

    @Query("select distinct pickingOrderId from PickingOrderDetailPO where outboundOrderPlanId in ?1")
    List<Long> findPickingOrderIdsByOutboundOrderPlanIdIn(List<Long> outboundPlanOrderIds);

    @Query("select distinct pickingOrderId from PickingOrderDetailPO where outboundOrderPlanId = ?1")
    List<Long> findPickingOrderIdsByOutboundOrderPlanId(Long outboundPlanOrderId);
}
