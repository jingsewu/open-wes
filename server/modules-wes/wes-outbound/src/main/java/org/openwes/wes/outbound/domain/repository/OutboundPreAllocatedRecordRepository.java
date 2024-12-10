package org.openwes.wes.outbound.domain.repository;

import org.openwes.wes.outbound.domain.entity.OutboundPreAllocatedRecord;

import java.util.List;

public interface OutboundPreAllocatedRecordRepository {

    void saveAll(List<OutboundPreAllocatedRecord> planPreAllocatedRecords);

    List<OutboundPreAllocatedRecord> findByOutboundPlanOrderIds(List<Long> outboundPlanOrderIds);
}
