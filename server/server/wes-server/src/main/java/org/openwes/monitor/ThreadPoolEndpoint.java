package org.openwes.monitor;

import com.alibaba.ttl.spi.TtlWrapper;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Endpoint(id = "threadpool")
@RequiredArgsConstructor
public class ThreadPoolEndpoint {

    private final Executor asyncEventBusExecutor;

    @ReadOperation
    public ThreadPoolStats threadPoolStats() {

        if (asyncEventBusExecutor instanceof TtlWrapper taskExecutor) {
            ThreadPoolTaskExecutor unwrap = (ThreadPoolTaskExecutor)taskExecutor.unwrap();
            ThreadPoolExecutor executor = unwrap.getThreadPoolExecutor();
            return ThreadPoolStats.builder()
                    .corePoolSize(executor.getCorePoolSize())
                    .maxPoolSize(executor.getMaximumPoolSize())
                    .poolSize(executor.getPoolSize())
                    .activeCount(executor.getActiveCount())
                    .taskCount(executor.getTaskCount())
                    .completedTaskCount(executor.getCompletedTaskCount())
                    .queueSize(executor.getQueue().size())
                    .queueRemainingCapacity(executor.getQueue().remainingCapacity())
                    .build();
        }
        return ThreadPoolStats.builder().build();


    }

    @Data
    @Builder
    public static class ThreadPoolStats {
        private int corePoolSize;
        private int maxPoolSize;
        private int poolSize;
        private int activeCount;
        private long taskCount;
        private long completedTaskCount;
        private int queueSize;
        private int queueRemainingCapacity;
    }
}
