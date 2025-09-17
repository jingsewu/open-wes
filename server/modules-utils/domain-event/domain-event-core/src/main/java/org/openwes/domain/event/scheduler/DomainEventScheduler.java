package org.openwes.domain.event.scheduler;

import com.google.common.eventbus.EventBus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.distribute.scheduler.annotation.DistributedScheduled;
import org.openwes.domain.event.constants.DomainEventStatusEnum;
import org.openwes.domain.event.domain.entity.DomainEventPO;
import org.openwes.domain.event.domain.repository.DomainEventPORepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;
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

    @DistributedScheduled(cron = "0/30 * * * * *", name = "DomainEventScheduler#handleFailedDomainEvent")
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

        if (ObjectUtils.isEmpty(failedEvents)) {
            return;
        }

        // Track processed event signatures (type + content hash)
        Set<String> processedEventSignatures = new HashSet<>();

        failedEvents.forEach(e -> {
            try {
                // Create a unique signature for this event (type + content hash)
                String eventSignature = e.getEventType() + ":" + generateEventHash(e.getEvent());

                // Check if we've already processed an identical event
                if (processedEventSignatures.contains(eventSignature)) {
                    log.warn("Skipping duplicate event {} with signature {}", e.getId(), eventSignature);
                    e.succeed(); // Mark as success without processing
                    domainEventPORepository.save(e);
                    return;
                }

                // Process the event
                Object object = JsonUtils.string2Object(e.getEvent(), Class.forName(e.getEventType()));
                if (object != null) {
                    asyncEventBus.post(object);
                    processedEventSignatures.add(eventSignature); // Remember we processed this
                }
            } catch (ClassNotFoundException ex) {
                log.error("Retry failed - unknown event type: {}, event: {}", e.getEventType(), e);
            }
        });
    }

    @DistributedScheduled(cron = "0 0 1 * * *", name = "DomainEventScheduler#removeHistoryDomainEvent")
    public void removeHistoryDomainEvent() {

        long startTime = DateUtils.addDays(new Date(), -2).getTime();
        try {
            domainEventPORepository.deleteAllByCreateTimeLessThanAndStatus(startTime, DomainEventStatusEnum.SUCCESS);
        } catch (Exception e) {
            log.error("remove history domain event failed, startTime : {}", startTime);
        }
    }


    private int generateEventHash(String event) {
        try {
            // Convert string event to Map
            Map<String, Object> eventMap = JsonUtils.string2MapObject(event);

            // Create a copy without the eventId field
            Map<String, Object> copy = new HashMap<>(eventMap);
            copy.remove("eventId");

            return copy.hashCode();
        } catch (Exception e) {
            log.warn("Failed to generate event hash, using raw event string: {}", e.getMessage());
            return event.hashCode();
        }
    }
}
