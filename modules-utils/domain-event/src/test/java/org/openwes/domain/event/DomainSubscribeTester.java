package org.openwes.domain.event;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DomainSubscribeTester {

    @Subscribe
    public void subscribe(TestDomainEvent1 domainEvent) {
        log.info("TestDomainEvent1: {}", domainEvent.getEventId());
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        DomainEventPublisher.sendAsyncDomainEvent(new TestDomainEvent2());
    }

    @Subscribe
    public void subscribe(TestDomainEvent2 domainEvent) {

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("TestDomainEvent2: {}", domainEvent.getEventId());

    }

    @Subscribe
    public void subscribe(TestSyncDomainEvent domainEvent) {
        throw new RuntimeException("test throw exception");
    }


    public static class TestDomainEvent1 extends DomainEvent {
    }

    public static class TestDomainEvent2 extends DomainEvent {
    }

    public static class TestSyncDomainEvent extends DomainEvent {
    }
}
