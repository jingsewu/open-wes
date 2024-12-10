package org.openwes.wes.outbound.infrastructure.repository.impl;

import org.openwes.wes.outbound.domain.entity.OutboundPreAllocatedRecord;
import org.openwes.wes.outbound.domain.repository.OutboundPreAllocatedRecordRepository;
import org.openwes.wes.outbound.infrastructure.persistence.mapper.OutboundPreAllocatedRecordPORepository;
import org.openwes.wes.outbound.infrastructure.persistence.po.OutboundPreAllocatedRecordPO;
import org.openwes.wes.outbound.infrastructure.persistence.transfer.OutboundPreAllocatedRecordPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboundPreAllocatedRecordRepositoryImpl implements OutboundPreAllocatedRecordRepository {

    private final OutboundPreAllocatedRecordPORepository outboundPreAllocatedRecordPORepository;

    private final OutboundPreAllocatedRecordPOTransfer outboundPreAllocatedRecordPOTransfer;

    @Override
    public void saveAll(List<OutboundPreAllocatedRecord> planPreAllocatedRecords) {
        outboundPreAllocatedRecordPORepository.saveAll(outboundPreAllocatedRecordPOTransfer.toPOs(planPreAllocatedRecords));
    }

    @Override
    public List<OutboundPreAllocatedRecord> findByOutboundPlanOrderIds(List<Long> outboundPlanOrderIds) {
        List<OutboundPreAllocatedRecordPO> recordPOS = outboundPreAllocatedRecordPORepository.findByOutboundPlanOrderIdIn(outboundPlanOrderIds);
        return outboundPreAllocatedRecordPOTransfer.toDOs(recordPOS);
    }
}
