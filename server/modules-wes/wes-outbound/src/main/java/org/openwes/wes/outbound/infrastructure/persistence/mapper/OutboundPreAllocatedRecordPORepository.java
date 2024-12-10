package org.openwes.wes.outbound.infrastructure.persistence.mapper;

import org.openwes.wes.outbound.infrastructure.persistence.po.OutboundPreAllocatedRecordPO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboundPreAllocatedRecordPORepository extends JpaRepository<OutboundPreAllocatedRecordPO, Long> {

    List<OutboundPreAllocatedRecordPO> findByOutboundPlanOrderIdIn(List<Long> outboundPlanOrderIds);
}
