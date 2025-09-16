package org.openwes.domain.event;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.domain.event.api.DomainEvent;

import java.util.Collections;
import java.util.List;

@Getter
public abstract class AggregatorRoot {

    private final List<DomainEvent> synchronousDomainEvents = Lists.newArrayList();
    private final List<DomainEvent> asynchronousDomainEvents = Lists.newArrayList();

    public void addSynchronizationEvents(DomainEvent... events) {
        if (events == null) {
            return;
        }
        Collections.addAll(synchronousDomainEvents, events);
    }

    public void addAsynchronousDomainEvents(DomainEvent... events) {
        if (events == null) {
            return;
        }
        Collections.addAll(asynchronousDomainEvents, events);
    }

    public void clearEvents() {
        synchronousDomainEvents.clear();
        asynchronousDomainEvents.clear();
    }

    public void sendEvents() {
        if (ObjectUtils.isNotEmpty(this.getAsynchronousDomainEvents())) {
            this.getAsynchronousDomainEvents().forEach(DomainEventPublisher::sendAsyncDomainEvent);
        }
        if (ObjectUtils.isNotEmpty(this.getSynchronousDomainEvents())) {
            this.getSynchronousDomainEvents().forEach(DomainEventPublisher::sendSyncDomainEvent);
        }
    }

    public void sendAndClearEvents() {
        try {
            if (ObjectUtils.isNotEmpty(this.getAsynchronousDomainEvents())) {
                this.getAsynchronousDomainEvents().forEach(DomainEventPublisher::sendAsyncDomainEvent);
            }
            if (ObjectUtils.isNotEmpty(this.getSynchronousDomainEvents())) {
                this.getSynchronousDomainEvents().forEach(DomainEventPublisher::sendSyncDomainEvent);
            }
        } finally {
            this.clearEvents();
        }
    }
}
