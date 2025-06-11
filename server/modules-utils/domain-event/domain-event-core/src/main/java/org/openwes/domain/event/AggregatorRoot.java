package org.openwes.domain.event;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.domain.event.api.DomainEvent;

import java.util.Collections;
import java.util.List;

public interface AggregatorRoot {

    List<DomainEvent> synchronousDomainEvents = Lists.newArrayList();

    List<DomainEvent> asynchronousDomainEvents = Lists.newArrayList();

    default void addSynchronizationEvents(DomainEvent... events) {
        if (events == null) {
            return;
        }
        Collections.addAll(synchronousDomainEvents, events);
    }

    default void addAsynchronousDomainEvents(DomainEvent... events) {
        if (events == null) {
            return;
        }
        Collections.addAll(asynchronousDomainEvents, events);
    }

    default void clearEvents() {
        synchronousDomainEvents.clear();
        asynchronousDomainEvents.clear();
    }

    default List<DomainEvent> getSynchronousDomainEvents() {
        return synchronousDomainEvents;
    }

    default List<DomainEvent> getAsynchronousDomainEvents() {
        return asynchronousDomainEvents;
    }

    default void sendEvents() {
        if (ObjectUtils.isNotEmpty(this.getAsynchronousDomainEvents())) {
            this.getAsynchronousDomainEvents().forEach(DomainEventPublisher::sendAsyncDomainEvent);
        }
        if (ObjectUtils.isNotEmpty(this.getSynchronousDomainEvents())) {
            this.getSynchronousDomainEvents().forEach(DomainEventPublisher::sendSyncDomainEvent);
        }
    }

    default void sendAndClearEvents() {
        if (ObjectUtils.isNotEmpty(this.getAsynchronousDomainEvents())) {
            this.getAsynchronousDomainEvents().forEach(DomainEventPublisher::sendAsyncDomainEvent);
        }
        if (ObjectUtils.isNotEmpty(this.getSynchronousDomainEvents())) {
            this.getSynchronousDomainEvents().forEach(DomainEventPublisher::sendSyncDomainEvent);
        }
        this.clearEvents();
    }
}
