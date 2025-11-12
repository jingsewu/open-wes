package org.openwes.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.openwes.domain.event.api.DomainEvent;
import org.openwes.domain.event.domain.entity.DomainEventPO;
import org.openwes.domain.event.domain.repository.DomainEventPORepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Aspect
@ConditionalOnClass(JpaRepository.class)
@Slf4j
@RequiredArgsConstructor
public class DomainTransactionAspect {

    private final DomainEventPORepository domainEventPORepository;
    private final DomainEventExecutor domainEventExecutor;

    @Around("@annotation(com.google.common.eventbus.Subscribe)")
    public Object updateDomainTransactionStatus(ProceedingJoinPoint joinPoint) throws Throwable {

        Object[] args = joinPoint.getArgs();
        if (args == null || args.length < 1) {
            return joinPoint.proceed();
        }

        Object arg = args[0];
        if (!(arg instanceof DomainEvent domainEvent)) {
            return joinPoint.proceed();
        }

        Optional<DomainEventPO> optional = domainEventPORepository.findById(domainEvent.getEventId());
        if (optional.isEmpty()) {
            log.debug("event id: {} is not exist ,may be the event is not async.", domainEvent.getEventId());
            return joinPoint.proceed();
        }

        DomainEventPO domainEventPO = optional.get();

        return domainEventExecutor.executeWithTransaction(joinPoint, domainEventPO);
    }

}
