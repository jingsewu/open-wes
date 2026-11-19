package org.openwes.domain.event.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Guava {@code @Subscribe} method (or its declaring class) as an observer of domain
 * events rather than a business consumer.
 *
 * <p>A domain event is persisted to the {@code d_domain_event} outbox and its status is updated
 * by the subscriber transaction. The event model supports only one business consumer per event.
 * Observer subscribers - metrics collectors, audit feeds and similar infrastructure - may listen
 * to the same event, but they must not take part in that outbox transaction. The domain
 * transaction aspect skips annotated subscribers so they can run concurrently with the business
 * consumer without racing on the event row's optimistic-lock version.</p>
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DomainEventObserver {
}
