package org.openwes.wes.inbound.infrastructure.persistence.mapper;

import org.openwes.wes.inbound.infrastructure.persistence.po.AcceptOrderDetailPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AcceptOrderDetailPORepository extends JpaRepository<AcceptOrderDetailPO, Long> {

    List<AcceptOrderDetailPO> findByAcceptOrderId(Long acceptOrderId);

    List<AcceptOrderDetailPO> findByAcceptOrderIdIn(Collection<Long> acceptOrderIds);

    List<AcceptOrderDetailPO> findAllByWorkStationId(Long workStationId);

    List<AcceptOrderDetailPO> findAllByInboundPlanOrderIdIn(Collection<Long> inboundPlanOrderIds);
}
