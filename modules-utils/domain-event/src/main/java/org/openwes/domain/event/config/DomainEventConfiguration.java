package org.openwes.domain.event.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import org.openwes.common.utils.user.UserContext;
import org.openwes.common.utils.utils.JsonUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@Slf4j
public class DomainEventConfiguration {

    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2 + 1;

    @Bean
    public Executor asyncEventBusExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE * 4);
        executor.setQueueCapacity(512);
        executor.setThreadNamePrefix("async-event-bus-executor");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return TtlExecutors.getTtlExecutor(executor);
    }

    @Bean("asyncEventBus")
    public AsyncEventBus asyncEventBus() {
        return new AsyncEventBus(asyncEventBusExecutor(), new AsyncSubscriberExceptionHandler());
    }

    @Bean("syncEventBus")
    public EventBus syncEventBus() {
        return new EventBus(new SyncSubscriberExceptionHandler());
    }

    public static class AsyncSubscriberExceptionHandler implements SubscriberExceptionHandler {
        @Override
        public void handleException(Throwable exception, SubscriberExceptionContext context) {
            log.error("exception occurred while handling event: {} with method: {} with tenant: {} with user: {}.",
                    JsonUtils.obj2String(context.getEvent()), context.getSubscriberMethod().getParameterTypes(),
                    "", UserContext.getCurrentUser(), exception);
        }
    }

    public static class SyncSubscriberExceptionHandler implements SubscriberExceptionHandler {
        @SneakyThrows
        @Override
        public void handleException(Throwable exception, SubscriberExceptionContext context) {
            log.error("exception occurred while handling event: {} with method: {} with tenant: {} with user: {}.",
                    JsonUtils.obj2String(context.getEvent()), context.getSubscriberMethod().getParameterTypes(),
                    "", UserContext.getCurrentUser(), exception);
            DomainEventExceptionContext.setException(exception);
        }
    }
}
