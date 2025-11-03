package org.openwes.wes.outbound.application.event;

import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.api.outbound.constants.OutboundWaveStatusEnum;
import org.openwes.wes.api.outbound.constants.PickingOrderStatusEnum;
import org.openwes.wes.api.outbound.event.NewOutboundWaveEvent;
import org.openwes.wes.api.outbound.event.PickingOrderCompleteEvent;
import org.openwes.wes.outbound.domain.aggregate.PickingOrderWaveAggregate;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboundWaveSubscribe {

    private final OutboundWaveRepository outboundWaveRepository;
    private final PickingOrderWaveAggregate pickingOrderWaveAggregate;
    private final PickingOrderRepository pickingOrderRepository;

    @Subscribe
    public void onCreateEvent(@Valid NewOutboundWaveEvent event) {
        OutboundWave outboundWave = outboundWaveRepository.findByWaveNo(event.getWaveNo());
        if (outboundWave.getWaveStatus() != OutboundWaveStatusEnum.NEW) {
            return;
        }
        pickingOrderWaveAggregate.split(outboundWave);
    }

    @Subscribe
    public void onPickingOrderCompleteEvent(@Valid PickingOrderCompleteEvent event) {
        PickingOrder pickingOrder = pickingOrderRepository.findById(event.getAggregatorId());
        String waveNo = pickingOrder.getWaveNo();

        List<PickingOrder> pickingOrders = pickingOrderRepository.findByWaveNo(waveNo);

        if (pickingOrders.stream().allMatch(v -> v.getPickingOrderStatus() == PickingOrderStatusEnum.PICKED
                || v.getPickingOrderStatus() == PickingOrderStatusEnum.CANCELED)) {

            OutboundWave outboundWave = outboundWaveRepository.findByWaveNo(waveNo);
            outboundWave.complete();
            outboundWaveRepository.save(outboundWave);
        }
    }
}
