package org.openwes.domain.event;

import org.openwes.common.utils.exception.CommonException;
import org.openwes.domain.event.domain.entity.DomainEventPO;
import org.openwes.domain.event.domain.repository.DomainEventPORepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

@Component
@Aspect
@ConditionalOnClass(JpaRepository.class)
@Slf4j
@RequiredArgsConstructor
public class DomainTransactionAspect {

    private final DomainEventPORepository domainEventPORepository;
    private final PlatformTransactionManager transactionManager;

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

        return executeWithTransaction(joinPoint, domainEventPO);
    }

    private Object executeWithTransaction(ProceedingJoinPoint joinPoint, DomainEventPO domainEventPO) throws Throwable {

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus status = transactionManager.getTransaction(def);
        try {

            Object result = joinPoint.proceed();

            domainEventPO.succeed();
            domainEventPORepository.save(domainEventPO);

            transactionManager.commit(status);

            return result;
        } catch (Exception e) {
            log.error("proceed error:", e);
            if (TransactionSynchronizationManager.isActualTransactionActive()) {
                transactionManager.rollback(status);
            }
            throw new CommonException(e.getMessage());
        }
    }
}
