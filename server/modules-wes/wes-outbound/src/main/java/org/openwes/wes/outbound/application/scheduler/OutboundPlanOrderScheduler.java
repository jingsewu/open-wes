package org.openwes.wes.outbound.application.scheduler;

import com.alibaba.ttl.TtlRunnable;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.api.outbound.event.NewOutboundPlanOrderEvent;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundPlanOrderScheduler {

    private final OutboundPlanOrderRepository outboundPlanOrderRepository;

    @Scheduled(cron = "${wms.schedule.config.handleShortWaitingOrders:0 0/5 * * * *}")
    public void handleShortWaitingOrders() {

        CompletableFuture.runAsync(Objects.requireNonNull(TtlRunnable.get(this::doHandleShortWaitingOrders)))
                .exceptionally(e -> {
                    log.error("handle short waiting orders error: ", e);
                    return null;
                });

    }

    public void doHandleShortWaitingOrders() {

        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByStatus(OutboundPlanOrderStatusEnum.SHORT_WAITING);
        if (ObjectUtils.isEmpty(outboundPlanOrders)) {
            return;
        }

        outboundPlanOrders.forEach(outboundPlanOrder ->
                DomainEventPublisher.sendAsyncDomainEvent(new NewOutboundPlanOrderEvent().setOrderNo(outboundPlanOrder.getOrderNo())));
    }

}
