package org.openwes.domain.event.scheduler;

import com.google.common.eventbus.EventBus;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.domain.event.constants.DomainEventStatusEnum;
import org.openwes.domain.event.domain.entity.DomainEventPO;
import org.openwes.domain.event.domain.repository.DomainEventPORepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventScheduler {

    private final DomainEventPORepository domainEventPORepository;
    private final EventBus asyncEventBus;
    private final Executor asyncEventBusExecutor;

    private static final long DELAY_TIME_IN_MILLIS = 600 * 1000L;
    private static final int MAX_SIZE_PER_TIME = 100;

    @Scheduled(cron = "0 */2 * * * *")
    public void handleFailedDomainEvent() {
        CompletableFuture.runAsync(this::doHandleFailedDomainEvent, asyncEventBusExecutor)
                .exceptionally(e -> {
                    log.error("Failed to execute handle failed domain event due to: {}", e.getMessage(), e);
                    return null;
                })
                .thenRun(() -> log.debug("handle domain event successfully"));
    }

    public void doHandleFailedDomainEvent() {

        Long startTime = DateUtils.addDays(new Date(), -1).getTime();
        Long endTime = System.currentTimeMillis() - DELAY_TIME_IN_MILLIS;

        List<DomainEventPO> failedEvents = domainEventPORepository.findByStatusAndCreateTimeBetween(DomainEventStatusEnum.NEW, startTime, endTime, Pageable.ofSize(MAX_SIZE_PER_TIME));
        failedEvents.forEach(e -> {
            String eventType = e.getEventType();
            try {
                Object object = JsonUtils.string2Object(e.getEvent(), Class.forName(eventType));
                assert object != null;
                asyncEventBus.post(object);
            } catch (ClassNotFoundException ex) {
                log.error("retry fail event failed, event : {}", e);
            }
        });
    }

    @Scheduled(cron = "0 0 1 * * *")
    public void removeHistoryDomainEvent() {

        long startTime = DateUtils.addDays(new Date(), -2).getTime();
        try {
            domainEventPORepository.deleteAllByCreateTimeLessThanAndStatus(startTime, DomainEventStatusEnum.SUCCESS);
        } catch (Exception e) {
            log.error("remove history domain event failed, startTime : {}", startTime);
        }
    }
}
