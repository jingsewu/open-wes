package org.openwes.domain.event;

import com.google.common.eventbus.EventBus;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.utils.JsonUtils;
import org.openwes.domain.event.config.DomainEventExceptionContext;
import org.openwes.domain.event.domain.entity.DomainEventPO;
import org.openwes.domain.event.domain.repository.DomainEventPORepository;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisher {

    private static EventBus asyncEventBus;
    private static EventBus syncEventBus;
    private static DomainEventPORepository domainEventPORepository;

    @Resource(name = "asyncEventBus")
    public void setAsyncEventBus(EventBus asyncEventBus) {
        DomainEventPublisher.asyncEventBus = asyncEventBus;
    }

    @Resource(name = "syncEventBus")
    public void setSyncEventBus(EventBus syncEventBus) {
        DomainEventPublisher.syncEventBus = syncEventBus;
    }

    @Autowired
    public DomainEventPublisher(DomainEventPORepository domainEventPORepository) {
        DomainEventPublisher.domainEventPORepository = domainEventPORepository;
    }

    public static void sendAsyncDomainEvent(DomainEvent event) {
        DomainEventPO domainEventPO = new DomainEventPO();
        domainEventPO.setId(event.getEventId());
        domainEventPO.setEvent(JsonUtils.obj2String(event));
        domainEventPO.setEventType(event.getClass().getName());
        domainEventPORepository.save(domainEventPO);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    asyncEventBus.post(event);
                }
            });
        } else {
            asyncEventBus.post(event);
        }
    }

    public static void sendSyncDomainEvent(Object event) {
        try {
            syncEventBus.post(event);
            if (DomainEventExceptionContext.hasException()) {
                throw new CommonException(DomainEventExceptionContext.getException().getMessage());
            }
        } finally {
            DomainEventExceptionContext.clearException();
        }
    }
}
