package org.openwes.wes.common.configuration;

import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@EnableAsync
@Configuration
public class OutboundAsyncExecutorConfig {

    @Bean("wavePickingExecutor")
    public Executor wavePickingExecutor() {
        return getExecutor("wave-picking-executor");
    }

    @Bean("pickingOrderHandleExecutor")
    public Executor pickingOrderHandleExecutor() {
        return getExecutor("picking-order-handle-executor");
    }

    @Bean("pickingOrderReallocateExecutor")
    public Executor pickingOrderReallocateExecutor() {
        return getExecutor("picking-order-reallocate-executor");
    }

    @Bean("unlockTransferContainerExecutor")
    public Executor unlockTransferContainerExecutor() {
        return getExecutor("unlock-transfer-container-executor");
    }

    private Executor getExecutor(String executorName) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(Runtime.getRuntime().availableProcessors() * 2 + 1);
        executor.setMaxPoolSize(executor.getCorePoolSize() * 2);
        executor.setQueueCapacity(256);
        executor.setThreadNamePrefix(executorName);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return TtlExecutors.getTtlExecutor(executor);
    }
}
