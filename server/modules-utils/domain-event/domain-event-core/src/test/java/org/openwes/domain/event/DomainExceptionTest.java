package org.openwes.domain.event;

import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.id.Snowflake;
import org.openwes.common.utils.id.SnowflakeUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestConfig.class)
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@Slf4j
public class DomainExceptionTest {

    @BeforeEach
    public void setUp() {
        new SnowflakeUtils(new Snowflake(1, 1));
    }

    @Test
    public void testExceptionHandler() {
        Assertions.assertThrows(CommonException.class, () -> DomainEventPublisher.sendSyncDomainEvent(new DomainSubscribeTester.TestSyncDomainEvent()));
    }
}
