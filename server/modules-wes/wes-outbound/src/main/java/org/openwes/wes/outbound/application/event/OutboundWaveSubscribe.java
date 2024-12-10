package org.openwes.wes.outbound.application.event;

import com.google.common.eventbus.Subscribe;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.api.outbound.constants.OutboundWaveStatusEnum;
import org.openwes.wes.api.outbound.event.NewOutboundWaveEvent;
import org.openwes.wes.api.outbound.event.OutboundPlanOrderShortCompleteEvent;
import org.openwes.wes.api.outbound.event.OutboundWaveCompleteEvent;
import org.openwes.wes.outbound.domain.aggregate.PickingOrderWaveAggregate;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboundWaveSubscribe {

    private final OutboundWaveRepository outboundWaveRepository;
    private final PickingOrderWaveAggregate pickingOrderWaveAggregate;
    private final OutboundPlanOrderRepository outboundPlanOrderRepository;

    @Subscribe
    public void onCreateEvent(@Valid NewOutboundWaveEvent event) {
        OutboundWave outboundWave = outboundWaveRepository.findByWaveNo(event.getWaveNo());
        if (outboundWave.getWaveStatus() != OutboundWaveStatusEnum.NEW) {
            return;
        }
        pickingOrderWaveAggregate.split(outboundWave);
    }

    @Subscribe
    public void onCompleteEvent(@Valid OutboundWaveCompleteEvent event) {

        OutboundWave outboundWave = outboundWaveRepository.findByWaveNo(event.getWaveNo());
        outboundWave.complete();
        outboundWaveRepository.save(outboundWave);

        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(outboundWave.getOutboundPlanOrderIds());

        List<Long> outboundPlainOrderIds = outboundPlanOrders.stream().filter(v ->
                        !OutboundPlanOrderStatusEnum.isFinalStatues(v.getOutboundPlanOrderStatus()))
                .map(OutboundPlanOrder::getId).toList();

        if (CollectionUtils.isNotEmpty(outboundPlainOrderIds)) {
            log.info("outbound plan order ids: {} short completed", outboundPlainOrderIds);
            DomainEventPublisher.sendAsyncDomainEvent(new OutboundPlanOrderShortCompleteEvent().setOutboundPlanOrderIds(outboundPlainOrderIds));
        }
    }
}
