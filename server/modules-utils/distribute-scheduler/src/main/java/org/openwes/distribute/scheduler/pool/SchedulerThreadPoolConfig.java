package org.openwes.distribute.scheduler.pool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
public class SchedulerThreadPoolConfig {

    @Bean(name = "defaultScheduler")
    public TaskScheduler defaultScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("scheduler-default-");
        scheduler.initialize();
        return scheduler;
    }

}
