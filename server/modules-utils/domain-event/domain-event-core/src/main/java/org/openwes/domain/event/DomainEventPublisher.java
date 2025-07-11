package org.openwes.domain.event;

import com.google.common.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.domain.event.api.DomainEvent;
import org.openwes.domain.event.config.DomainEventExceptionContext;
import org.openwes.domain.event.domain.entity.DomainEventPO;
import org.openwes.domain.event.domain.repository.DomainEventPORepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {
    private static DomainEventPublisher instance;

    private final EventBus asyncEventBus;
    private final EventBus syncEventBus;
    private final DomainEventPORepository domainEventPORepository;

    @PostConstruct
    public void init() {
        instance = this;
    }

    public static void sendAsyncDomainEvent(DomainEvent event) {
        instance.sendAsyncDomainEventInternal(event);
    }

    public static void sendSyncDomainEvent(DomainEvent event) {
        instance.sendSyncDomainEventInternal(event);
    }

    private void sendAsyncDomainEventInternal(DomainEvent event) {
        Objects.requireNonNull(event, "DomainEvent cannot be null");

        DomainEventPO domainEventPO = new DomainEventPO();
        domainEventPO.setId(event.getEventId());
        domainEventPO.setEvent(JsonUtils.obj2String(event));
        domainEventPO.setEventType(event.getClass().getName());
        domainEventPORepository.save(domainEventPO);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            log.debug("Posting async event after commit: {}", event.getEventId());
                            asyncEventBus.post(event);
                        }
                    });
        } else {
            log.debug("Posting async event immediately: {}", event.getEventId());
            asyncEventBus.post(event);
        }
    }

    private void sendSyncDomainEventInternal(DomainEvent event) {
        Objects.requireNonNull(event, "DomainEvent cannot be null");

        try {
            log.debug("Posting sync event: {}", event.getEventId());
            syncEventBus.post(event);

            if (DomainEventExceptionContext.hasException()) {
                Throwable e = DomainEventExceptionContext.getException();
                log.error("Error processing sync domain event {}", event.getEventId(), e);
                throw new CommonException(e.getMessage(), e);
            }
        } finally {
            DomainEventExceptionContext.clearException();
        }
    }
}
