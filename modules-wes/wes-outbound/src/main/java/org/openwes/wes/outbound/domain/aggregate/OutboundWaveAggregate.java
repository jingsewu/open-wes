package org.openwes.wes.outbound.domain.aggregate;

import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboundWaveAggregate {

    private final OutboundWaveRepository outboundWaveRepository;
    private final OutboundPlanOrderRepository outboundPlanOrderRepository;

    @Transactional(rollbackFor = Exception.class)
    public String waveOrders(List<OutboundPlanOrder> outboundPlanOrders) {
        String waveNo = OrderNoGenerator.generationOutboundWaveNo();

        Integer maxPriority = outboundPlanOrders.stream().map(OutboundPlanOrder::getPriority).reduce(Integer::max).orElse(0);
        outboundWaveRepository.save(new OutboundWave(waveNo, maxPriority, outboundPlanOrders));

        outboundPlanOrders.forEach(v -> v.setWaveNo(waveNo));
        outboundPlanOrderRepository.saveAll(outboundPlanOrders);
        return waveNo;
    }

}
