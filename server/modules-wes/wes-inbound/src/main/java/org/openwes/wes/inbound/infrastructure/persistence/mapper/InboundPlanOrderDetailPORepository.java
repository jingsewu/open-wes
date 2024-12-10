package org.openwes.wes.inbound.infrastructure.persistence.mapper;

import org.openwes.wes.inbound.infrastructure.persistence.po.InboundPlanOrderDetailPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface InboundPlanOrderDetailPORepository extends JpaRepository<InboundPlanOrderDetailPO, Long> {

    List<InboundPlanOrderDetailPO> findByInboundPlanOrderId(Long inboundPlanOrderId);

    List<InboundPlanOrderDetailPO> findByInboundPlanOrderIdIn(Collection<Long> inboundPlanOrderIds);

    List<InboundPlanOrderDetailPO> findByBoxNoIn(Collection<String> boxNos);

}
