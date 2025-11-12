package org.openwes.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.openwes.domain.event.domain.entity.DomainEventPO;
import org.openwes.domain.event.domain.repository.DomainEventPORepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DomainEventExecutor {

    private final DomainEventPORepository domainEventPORepository;

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Object executeWithTransaction(ProceedingJoinPoint joinPoint, DomainEventPO domainEventPO) throws Throwable {
        try {
            Object result = joinPoint.proceed();
            // Update status in the same transaction
            domainEventPO.succeed();
            domainEventPORepository.save(domainEventPO);
            return result;
        } catch (Exception e) {
            log.error("domain execute with transaction error: ", e);
            throw e;
        }
    }
}
