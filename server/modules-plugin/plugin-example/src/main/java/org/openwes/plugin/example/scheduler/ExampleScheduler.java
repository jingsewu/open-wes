package org.openwes.plugin.example.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.openwes.distribute.scheduler.annotation.DistributedScheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ExampleScheduler {

    /**
     * add plugin id for the identity name of the task
     */
    @DistributedScheduled(cron = "0/1 * * * * ?", name = "Lazy-Waving-Plugin-0.0.1#ExampleScheduler#run")
    public void run() {
        log.info("ExampleScheduler run");
    }
}
