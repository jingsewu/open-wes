package org.openwes.domain.event;

import org.openwes.common.utils.id.Snowflake;
import org.openwes.common.utils.id.SnowflakeUtils;
import org.openwes.domain.event.constants.DomainEventStatusEnum;
import org.openwes.domain.event.domain.entity.DomainEventPO;
import org.openwes.domain.event.domain.repository.DomainEventPORepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@ContextConfiguration(classes = TestConfig.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Slf4j
class DomainEventTest {

    @Autowired
    private DomainSubscribeTester domainSubscribeTester;
    @Autowired
    private DomainEventPORepository domainEventPORepository;
    @Autowired
    private PlatformTransactionManager transactionManager;


    @BeforeEach
    public void setUp() {
        new SnowflakeUtils(new Snowflake(1, 1));
    }

    @Test
    void testSubscribe() {
        domainSubscribeTester.subscribe(new DomainSubscribeTester.TestDomainEvent1());
    }

    @Test
    void testPublish() throws InterruptedException {

        int count = 50;

        long start = System.currentTimeMillis();
        IntStream.range(0, count).forEach(v -> {
            CompletableFuture.runAsync(() -> {
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                TransactionStatus status = transactionManager.getTransaction(def);
                try {
                    log.info("publish: {} message, Current Thread: {} Transaction is active: {}", v + 1, Thread.currentThread().getId(), TransactionSynchronizationManager.isActualTransactionActive());
                    DomainEventPublisher.sendAsyncDomainEvent(new DomainSubscribeTester.TestDomainEvent1());
                    log.info("commit");
                    transactionManager.commit(status);
                } catch (Exception e) {
                    log.error("Error occurred during transaction: {}", e.getMessage());
                    transactionManager.rollback(status);
                }
            });
        });

        long end = System.currentTimeMillis();
        log.info("cost time: {}", (end - start) / 1000);

        Thread.sleep(count * 500);

        List<DomainEventPO> domainEventPOS = domainEventPORepository.findAll();
        Assertions.assertTrue(domainEventPOS.stream().allMatch(v -> v.getStatus() == DomainEventStatusEnum.SUCCESS));
    }
}
