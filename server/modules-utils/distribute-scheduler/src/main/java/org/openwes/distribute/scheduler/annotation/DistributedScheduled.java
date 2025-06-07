package org.openwes.distribute.scheduler.annotation;

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Scheduled(cron = "${task.default.cron}")
@SchedulerLock(
        name = "TASK_DEFAULT",
        lockAtLeastFor = "5m",
        lockAtMostFor = "10m"
)
public @interface DistributedScheduled {

    // Alias for @Scheduled.cron
    @AliasFor(annotation = Scheduled.class, attribute = "cron")
    String cron() default "";

    @AliasFor(annotation = Scheduled.class, attribute = "fixedDelayString")
    String fixedDelayString() default "";

    // Alias for @SchedulerLock.name
    @AliasFor(annotation = SchedulerLock.class, attribute = "name")
    String name();

    // Override lock durations if needed
    @AliasFor(annotation = SchedulerLock.class, attribute = "lockAtMostFor")
    String lockAtMostFor() default "10m";

    @AliasFor(annotation = SchedulerLock.class, attribute = "lockAtLeastFor")
    String lockAtLeastFor() default "5m";

    // Add this: Specify the scheduler bean name
    @AliasFor(annotation = Scheduled.class, attribute = "scheduler")
    String scheduler() default "defaultScheduler";
}
