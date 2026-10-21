package org.openwes.script.plugin.runtime.dispatch;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openwes.distribute.lock.DistributeLock;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.runtime.invoke.InvocationOutcome;
import org.openwes.script.plugin.runtime.invoke.ScriptInvocationExecutor;
import org.openwes.script.plugin.runtime.materialize.MaterializedExtension;
import org.openwes.script.plugin.runtime.materialize.MaterializedScriptPlugin;
import org.openwes.script.plugin.runtime.registry.ScriptPluginApiAccessor;
import org.openwes.script.plugin.runtime.registry.ScriptPluginRegistry;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptCronDispatcherTest {

    private static class MutableClock extends Clock {

        private Instant instant;
        private final ZoneId zone = ZoneId.systemDefault();

        MutableClock(String instant) {
            this.instant = Instant.parse(instant);
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    private final ScriptPluginRegistry registry = mock(ScriptPluginRegistry.class);
    private final ScriptInvocationExecutor executor = mock(ScriptInvocationExecutor.class);
    private final DistributeLock distributeLock = mock(DistributeLock.class);
    private final ScriptPluginApiAccessor accessor = mock(ScriptPluginApiAccessor.class);
    private final MutableClock clock = new MutableClock("2026-10-08T10:00:30Z");

    private ScriptCronDispatcher dispatcher(int maxConsecutiveFailures) {
        return new ScriptCronDispatcher(registry, executor, distributeLock, accessor, clock, maxConsecutiveFailures);
    }

    private MaterializedExtension cronExtension(String script, String method, String cron) {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.CRON);
        declaration.setScript(script);
        declaration.setMethod(method);
        declaration.setCron(cron);
        MaterializedExtension extension = new MaterializedExtension();
        extension.setDeclaration(declaration);
        return extension;
    }

    private MaterializedScriptPlugin plugin(String pluginKey, MaterializedExtension... extensions) {
        return new MaterializedScriptPlugin(pluginKey, 11L, List.of(extensions));
    }

    private void givenOneCronPlugin() {
        when(registry.all()).thenReturn(List.of(
                plugin("nightly-task", cronExtension("Job.groovy", "run", "0 * * * * *"))));
    }

    private String occurrenceLockKey(String script) {
        return "script-plugin:cron:nightly-task#" + script + "#run@"
                + Instant.parse("2026-10-08T10:01:00Z").getEpochSecond();
    }

    @Test
    void tick_firesOnceAtTheNextCronBoundary() {
        givenOneCronPlugin();
        when(distributeLock.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(executor.invoke(any(), any(), any())).thenReturn(new InvocationOutcome(true, null, 1, null));
        ScriptCronDispatcher dispatcher = dispatcher(3);

        dispatcher.tick();
        verify(executor, never()).invoke(any(), any(), any());

        clock.advance(Duration.ofSeconds(30));
        dispatcher.tick();
        verify(executor, times(1)).invoke(any(), any(), any());
        verify(distributeLock).acquireLock(eq(occurrenceLockKey("Job.groovy")), eq(0L), eq(30_000L));

        dispatcher.tick();
        verify(executor, times(1)).invoke(any(), any(), any());
    }

    @Test
    void tick_lockNotAcquired_skipsTheRun() {
        givenOneCronPlugin();
        when(distributeLock.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(false);
        ScriptCronDispatcher dispatcher = dispatcher(3);

        dispatcher.tick();
        clock.advance(Duration.ofSeconds(30));
        dispatcher.tick();

        verify(executor, never()).invoke(any(), any(), any());
        verify(distributeLock, times(1)).acquireLock(eq(occurrenceLockKey("Job.groovy")), eq(0L), eq(30_000L));
        verify(distributeLock, never()).releaseLock(anyString());
    }

    @Test
    void tick_consecutiveFailures_reachTheLimit_autoDisablesThePlugin() {
        givenOneCronPlugin();
        when(distributeLock.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(executor.invoke(any(), any(), any())).thenReturn(new InvocationOutcome(false, null, 5, "boom"));
        ScriptCronDispatcher dispatcher = dispatcher(3);

        dispatcher.tick();
        for (int i = 0; i < 3; i++) {
            clock.advance(Duration.ofMinutes(1));
            dispatcher.tick();
        }

        verify(accessor).disable(eq("nightly-task"), contains("consecutive"));
    }

    @Test
    void tick_successResetsTheFailureCounter() {
        givenOneCronPlugin();
        when(distributeLock.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(executor.invoke(any(), any(), any()))
                .thenReturn(new InvocationOutcome(false, null, 5, "boom"))
                .thenReturn(new InvocationOutcome(true, null, 5, null))
                .thenReturn(new InvocationOutcome(false, null, 5, "boom"));
        ScriptCronDispatcher dispatcher = dispatcher(3);

        dispatcher.tick();
        for (int i = 0; i < 3; i++) {
            clock.advance(Duration.ofMinutes(1));
            dispatcher.tick();
        }

        verify(accessor, never()).disable(anyString(), anyString());
    }

    @Test
    void tick_pluginRemovedFromRegistry_purgesItsState() {
        givenOneCronPlugin();
        when(distributeLock.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(executor.invoke(any(), any(), any())).thenReturn(new InvocationOutcome(true, null, 1, null));
        ScriptCronDispatcher dispatcher = dispatcher(3);
        dispatcher.tick();
        clock.advance(Duration.ofSeconds(30));
        dispatcher.tick();
        verify(executor, times(1)).invoke(any(), any(), any());

        // move to the next boundary before the purge, so a restored plugin must start from scratch
        clock.advance(Duration.ofMinutes(1));
        when(registry.all()).thenReturn(List.of());
        dispatcher.tick();

        givenOneCronPlugin();
        dispatcher.tick();
        verify(executor, times(1)).invoke(any(), any(), any());
        clock.advance(Duration.ofMinutes(1));
        dispatcher.tick();
        verify(executor, times(2)).invoke(any(), any(), any());
    }

    @Test
    void tick_twoCronDeclarationsSameMethod_fireUnderDistinctKeys() {
        when(registry.all()).thenReturn(List.of(plugin("nightly-task",
                cronExtension("A.groovy", "run", "0 * * * * *"),
                cronExtension("B.groovy", "run", "0 * * * * *"))));
        when(distributeLock.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(executor.invoke(any(), any(), any())).thenReturn(new InvocationOutcome(true, null, 1, null));
        ScriptCronDispatcher dispatcher = dispatcher(3);

        dispatcher.tick();
        clock.advance(Duration.ofSeconds(30));
        dispatcher.tick();

        verify(executor, times(2)).invoke(any(), any(), any());
        ArgumentCaptor<String> lockKeys = ArgumentCaptor.forClass(String.class);
        verify(distributeLock, times(2)).acquireLock(lockKeys.capture(), eq(0L), eq(30_000L));
        assertEquals(2, lockKeys.getAllValues().size());
        assertEquals(1L, lockKeys.getAllValues().stream().filter(key -> key.contains("#A.groovy#run@")).count());
        assertEquals(1L, lockKeys.getAllValues().stream().filter(key -> key.contains("#B.groovy#run@")).count());
    }

    @Test
    void tick_cronChangedOnSameScriptAndMethod_takesEffect() {
        MaterializedExtension extension = cronExtension("Job.groovy", "run", "0 * * * * *");
        when(registry.all()).thenReturn(List.of(plugin("nightly-task", extension)));
        when(distributeLock.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(executor.invoke(any(), any(), any())).thenReturn(new InvocationOutcome(true, null, 1, null));
        ScriptCronDispatcher dispatcher = dispatcher(3);

        dispatcher.tick();
        extension.getDeclaration().setCron("0 0 * * * *");
        clock.advance(Duration.ofMinutes(1));
        dispatcher.tick();

        verify(executor, never()).invoke(any(), any(), any());
    }

    @Test
    void tick_invalidCron_retriedAfterDeclarationChanges() {
        MaterializedExtension extension = cronExtension("Job.groovy", "run", "not a cron");
        when(registry.all()).thenReturn(List.of(plugin("nightly-task", extension)));
        when(distributeLock.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(executor.invoke(any(), any(), any())).thenReturn(new InvocationOutcome(true, null, 1, null));
        ScriptCronDispatcher dispatcher = dispatcher(3);

        dispatcher.tick();
        extension.getDeclaration().setCron("0 * * * * *");
        clock.advance(Duration.ofMinutes(1));
        dispatcher.tick();

        verify(executor, times(1)).invoke(any(), any(), any());
    }

    @Test
    void tick_onePluginDispatchThrows_otherPluginsStillRun() {
        MaterializedScriptPlugin first = plugin("first-plugin", cronExtension("A.groovy", "run", "0 * * * * *"));
        MaterializedScriptPlugin second = plugin("second-plugin", cronExtension("B.groovy", "run", "0 * * * * *"));
        when(registry.all()).thenReturn(List.of(first, second));
        when(distributeLock.acquireLock(argThat(key -> key != null && key.contains("#A.groovy#run@")),
                anyLong(), anyLong())).thenThrow(new IllegalStateException("redis down"));
        when(distributeLock.acquireLock(argThat(key -> key != null && !key.contains("#A.groovy#run@")),
                anyLong(), anyLong())).thenReturn(true);
        when(executor.invoke(any(), any(), any())).thenReturn(new InvocationOutcome(true, null, 1, null));
        ScriptCronDispatcher dispatcher = dispatcher(3);

        dispatcher.tick();
        clock.advance(Duration.ofSeconds(30));
        dispatcher.tick();

        verify(executor, never()).invoke(eq(first), any(), any());
        verify(executor, times(1)).invoke(eq(second), any(), any());
    }
}
