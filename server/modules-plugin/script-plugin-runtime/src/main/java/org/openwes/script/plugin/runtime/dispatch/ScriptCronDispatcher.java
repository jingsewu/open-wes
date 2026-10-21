package org.openwes.script.plugin.runtime.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.openwes.distribute.lock.DistributeLock;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.runtime.invoke.InvocationOutcome;
import org.openwes.script.plugin.runtime.invoke.ScriptInvocationExecutor;
import org.openwes.script.plugin.runtime.materialize.MaterializedExtension;
import org.openwes.script.plugin.runtime.materialize.MaterializedScriptPlugin;
import org.openwes.script.plugin.runtime.registry.ScriptPluginApiAccessor;
import org.openwes.script.plugin.runtime.registry.ScriptPluginRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fires due CRON declarations, at most once per occurrence per cluster (DistributeLock) and at
 * most once per occurrence per node (in-memory last-fired stamp). Consecutive failures leading to
 * auto-disable are reported through the management plane.
 */
@Component
@Slf4j
public class ScriptCronDispatcher {

    private static final long LOCK_LEASE_MILLIS = 30_000;

    private final ScriptPluginRegistry registry;
    private final ScriptInvocationExecutor executor;
    private final DistributeLock distributeLock;
    private final ScriptPluginApiAccessor accessor;
    private final Clock clock;
    private final int maxConsecutiveFailures;

    private final Map<String, Long> lastFiredAt = new ConcurrentHashMap<>();
    private final Map<String, CronExpression> parsedCron = new ConcurrentHashMap<>();
    private final Map<String, Integer> consecutiveFailures = new ConcurrentHashMap<>();
    private final Set<String> invalidCronKeys = ConcurrentHashMap.newKeySet();

    public ScriptCronDispatcher(ScriptPluginRegistry registry,
                                ScriptInvocationExecutor executor,
                                DistributeLock distributeLock,
                                ScriptPluginApiAccessor accessor,
                                Clock clock,
                                @Value("${script-plugin.cron.max-consecutive-failures:10}") int maxConsecutiveFailures) {
        this.registry = registry;
        this.executor = executor;
        this.distributeLock = distributeLock;
        this.accessor = accessor;
        this.clock = clock;
        this.maxConsecutiveFailures = maxConsecutiveFailures;
    }

    @Scheduled(fixedDelay = 1000)
    public void tick() {
        long now = clock.millis();
        Set<String> activeKeys = new HashSet<>();
        for (MaterializedScriptPlugin plugin : registry.all()) {
            for (MaterializedExtension extension : plugin.getExtensions()) {
                if (extension.getDeclaration().getType() != ExtensionType.CRON) {
                    continue;
                }
                String key = fireKey(plugin, extension);
                activeKeys.add(key);
                if (isDue(key, extension.getDeclaration().getCron(), now)) {
                    fire(plugin, extension, key);
                }
            }
        }
        lastFiredAt.keySet().retainAll(activeKeys);
        parsedCron.keySet().retainAll(activeKeys);
        consecutiveFailures.keySet().retainAll(activeKeys);
        invalidCronKeys.retainAll(activeKeys);
    }

    private boolean isDue(String key, String cron, long now) {
        CronExpression expression = resolveExpression(key, cron);
        if (expression == null) {
            return false;
        }
        Long lastFired = lastFiredAt.get(key);
        if (lastFired == null) {
            // start counting from activation: the first run happens at the next boundary, not immediately
            lastFiredAt.put(key, now);
            return false;
        }
        ZonedDateTime after = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastFired), clock.getZone());
        ZonedDateTime next = expression.next(after);
        if (next != null && !next.isAfter(ZonedDateTime.ofInstant(Instant.ofEpochMilli(now), clock.getZone()))) {
            lastFiredAt.put(key, now);
            return true;
        }
        return false;
    }

    private CronExpression resolveExpression(String key, String cron) {
        CronExpression cached = parsedCron.get(key);
        if (cached != null) {
            return cached;
        }
        if (invalidCronKeys.contains(key)) {
            return null;
        }
        try {
            CronExpression parsed = CronExpression.parse(cron);
            parsedCron.put(key, parsed);
            return parsed;
        } catch (IllegalArgumentException e) {
            invalidCronKeys.add(key);
            log.error("invalid cron expression '{}' for cron plugin {}: {}", cron, key, e.getMessage());
            return null;
        }
    }

    private void fire(MaterializedScriptPlugin plugin, MaterializedExtension extension, String key) {
        String lockKey = "script-plugin:cron:" + key;
        if (!distributeLock.acquireLock(lockKey, 0, LOCK_LEASE_MILLIS)) {
            return; // another node has already fired this occurrence
        }
        try {
            InvocationOutcome outcome = executor.invoke(plugin, extension, new Object[0]);
            if (outcome.success()) {
                consecutiveFailures.remove(key);
                return;
            }
            int failures = consecutiveFailures.merge(key, 1, Integer::sum);
            log.error("script cron {} failed ({}/{} consecutive): {}", key, failures, maxConsecutiveFailures,
                    outcome.errorMessage());
            if (failures >= maxConsecutiveFailures) {
                accessor.disable(plugin.getPluginKey(), "cron failed " + failures + " consecutive times");
            }
        } finally {
            distributeLock.releaseLock(lockKey);
        }
    }

    private String fireKey(MaterializedScriptPlugin plugin, MaterializedExtension extension) {
        return plugin.getPluginKey() + "#" + extension.getDeclaration().getScript() + "#"
                + extension.getDeclaration().getMethod();
    }
}
