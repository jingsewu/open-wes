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
 * Fires due CRON declarations. An occurrence runs at most once per node (in-memory last-fired
 * stamp) and at most once per cluster: the occurrence-scoped DistributeLock lease is intentionally
 * never released, so peers keep seeing the occurrence claimed until it is safely in the past.
 * Consecutive failures leading to auto-disable are reported through the management plane.
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
    private final Map<String, CachedCron> parsedCron = new ConcurrentHashMap<>();
    private final Map<String, Integer> consecutiveFailures = new ConcurrentHashMap<>();
    private final Map<String, String> invalidCrons = new ConcurrentHashMap<>();

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
                try {
                    ZonedDateTime occurrence = isDue(key, extension.getDeclaration().getCron(), now);
                    if (occurrence != null) {
                        fire(plugin, extension, key, occurrence);
                    }
                } catch (Exception e) {
                    // a broker/executor/disable failure for one declaration must not abort the
                    // remaining plugins or the state purge
                    log.error("script cron {} failed to dispatch: {}", key, e.getMessage());
                }
            }
        }
        lastFiredAt.keySet().retainAll(activeKeys);
        parsedCron.keySet().retainAll(activeKeys);
        consecutiveFailures.keySet().retainAll(activeKeys);
        invalidCrons.keySet().retainAll(activeKeys);
    }

    /**
     * Returns the cron occurrence that became due since the last stamp, or null when nothing is
     * due. The fresh-key stamp starts counting from activation: the first run happens at the next
     * boundary, never immediately.
     */
    private ZonedDateTime isDue(String key, String cron, long now) {
        Long lastFired = lastFiredAt.get(key);
        if (lastFired == null) {
            lastFiredAt.put(key, now);
            lastFired = now;
        }
        CronExpression expression = resolveExpression(key, cron);
        if (expression == null) {
            return null;
        }
        ZonedDateTime after = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastFired), clock.getZone());
        ZonedDateTime next = expression.next(after);
        if (next != null && !next.isAfter(ZonedDateTime.ofInstant(Instant.ofEpochMilli(now), clock.getZone()))) {
            lastFiredAt.put(key, now);
            return next;
        }
        return null;
    }

    private CronExpression resolveExpression(String key, String cron) {
        CachedCron cached = parsedCron.get(key);
        if (cached != null && cached.cron().equals(cron)) {
            return cached.expression();
        }
        String invalidCron = invalidCrons.get(key);
        if (invalidCron != null) {
            if (invalidCron.equals(cron)) {
                return null;
            }
            // the declaration changed: retry, a fixed expression must take effect without a restart
            invalidCrons.remove(key);
        }
        try {
            CronExpression parsed = CronExpression.parse(cron);
            parsedCron.put(key, new CachedCron(cron, parsed));
            invalidCrons.remove(key);
            return parsed;
        } catch (IllegalArgumentException e) {
            invalidCrons.put(key, cron);
            parsedCron.remove(key);
            log.error("invalid cron expression '{}' for cron plugin {}: {}", cron, key, e.getMessage());
            return null;
        }
    }

    private void fire(MaterializedScriptPlugin plugin, MaterializedExtension extension, String key,
                      ZonedDateTime occurrence) {
        // The occurrence-scoped key plus a lease that is never released claims this occurrence for
        // the whole cluster: a peer ticking later within the same boundary cannot acquire it.
        String lockKey = "script-plugin:cron:" + key + "@" + occurrence.toEpochSecond();
        if (!distributeLock.acquireLock(lockKey, 0, LOCK_LEASE_MILLIS)) {
            return; // another node has already claimed this occurrence
        }
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
    }

    private String fireKey(MaterializedScriptPlugin plugin, MaterializedExtension extension) {
        return plugin.getPluginKey() + "#" + extension.getDeclaration().getScript() + "#"
                + extension.getDeclaration().getMethod();
    }

    private record CachedCron(String cron, CronExpression expression) {
    }
}
