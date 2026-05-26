package org.openwes.wes.outbound.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOutboundWaveUseCase {

    private final OutboundWaveRepository outboundWaveRepository;
    private final OutboundPlanOrderRepository outboundPlanOrderRepository;

    @Transactional(rollbackFor = Exception.class)
    public void execute(List<OutboundPlanOrder> outboundPlanOrders) {
        String waveNo = OrderNoGenerator.generationOutboundWaveNo();
        Integer maxPriority = outboundPlanOrders.stream()
                .map(OutboundPlanOrder::getPriority).reduce(Integer::max).orElse(0);
        outboundWaveRepository.save(new OutboundWave(waveNo, maxPriority, outboundPlanOrders));

        outboundPlanOrders.forEach(v -> v.wave(waveNo));
        outboundPlanOrderRepository.saveAllOrders(outboundPlanOrders);
    }
}
