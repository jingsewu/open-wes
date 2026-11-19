package org.openwes.domain.event;

import com.google.common.eventbus.Subscribe;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.id.Snowflake;
import org.openwes.common.utils.id.SnowflakeUtils;
import org.openwes.domain.event.api.DomainEvent;
import org.openwes.domain.event.api.DomainEventObserver;
import org.openwes.domain.event.domain.entity.DomainEventPO;
import org.openwes.domain.event.domain.repository.DomainEventPORepository;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DomainTransactionAspectTest {

    @Mock
    private DomainEventPORepository domainEventPORepository;

    @Mock
    private DomainEventExecutor domainEventExecutor;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @BeforeEach
    void setUp() {
        new SnowflakeUtils(new Snowflake(1, 1));
    }

    @Test
    void observerClassDoesNotTouchTheOutbox() throws Throwable {
        DomainTransactionAspect aspect = new DomainTransactionAspect(domainEventPORepository, domainEventExecutor);
        TestEvent event = new TestEvent();

        when(joinPoint.getArgs()).thenReturn(new Object[]{event});
        when(joinPoint.getTarget()).thenReturn(new ObserverSubscriber());
        when(joinPoint.proceed()).thenReturn("handled");

        Object result = aspect.updateDomainTransactionStatus(joinPoint);

        assertEquals("handled", result);
        verify(joinPoint).proceed();
        verifyNoInteractions(domainEventPORepository, domainEventExecutor);
    }

    @Test
    void observerMethodDoesNotTouchTheOutbox() throws Throwable {
        DomainTransactionAspect aspect = new DomainTransactionAspect(domainEventPORepository, domainEventExecutor);
        TestEvent event = new TestEvent();
        Method method = ObserverMethodSubscriber.class.getMethod("onEvent", TestEvent.class);

        when(joinPoint.getArgs()).thenReturn(new Object[]{event});
        when(joinPoint.getTarget()).thenReturn(new ObserverMethodSubscriber());
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.proceed()).thenReturn("handled");

        Object result = aspect.updateDomainTransactionStatus(joinPoint);

        assertEquals("handled", result);
        verify(joinPoint).proceed();
        verifyNoInteractions(domainEventPORepository, domainEventExecutor);
    }

    @Test
    void businessSubscriberUpdatesTheOutbox() throws Throwable {
        DomainTransactionAspect aspect = new DomainTransactionAspect(domainEventPORepository, domainEventExecutor);
        TestEvent event = new TestEvent();
        DomainEventPO eventPO = new DomainEventPO();
        eventPO.setId(event.getEventId());
        Method method = BusinessSubscriber.class.getMethod("onEvent", TestEvent.class);

        when(joinPoint.getArgs()).thenReturn(new Object[]{event});
        when(joinPoint.getTarget()).thenReturn(new BusinessSubscriber());
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(domainEventPORepository.findById(event.getEventId())).thenReturn(Optional.of(eventPO));
        when(domainEventExecutor.executeWithTransaction(joinPoint, eventPO)).thenReturn("handled");

        Object result = aspect.updateDomainTransactionStatus(joinPoint);

        assertEquals("handled", result);
        verify(domainEventExecutor).executeWithTransaction(joinPoint, eventPO);
    }

    static class TestEvent extends DomainEvent {
    }

    @DomainEventObserver
    static class ObserverSubscriber {

        @Subscribe
        public void onEvent(TestEvent event) {
        }
    }

    static class ObserverMethodSubscriber {

        @Subscribe
        @DomainEventObserver
        public void onEvent(TestEvent event) {
        }
    }

    static class BusinessSubscriber {

        @Subscribe
        public void onEvent(TestEvent event) {
        }
    }
}
