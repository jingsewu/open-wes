package org.openwes.script.plugin.runtime.dispatch;

import org.junit.jupiter.api.Test;
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

    private void givenOneCronPlugin() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.CRON);
        declaration.setScript("Job.groovy");
        declaration.setMethod("run");
        declaration.setCron("0 * * * * *");
        MaterializedExtension extension = new MaterializedExtension();
        extension.setDeclaration(declaration);
        when(registry.all()).thenReturn(
                List.of(new MaterializedScriptPlugin("nightly-task", 11L, List.of(extension))));
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

        dispatcher.tick();
        verify(executor, times(1)).invoke(any(), any(), any());
        verify(distributeLock).releaseLock(anyString());
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

        when(registry.all()).thenReturn(List.of());
        dispatcher.tick();

        givenOneCronPlugin();
        dispatcher.tick();
        verify(executor, times(1)).invoke(any(), any(), any());
        clock.advance(Duration.ofMinutes(1));
        dispatcher.tick();
        verify(executor, times(2)).invoke(any(), any(), any());
    }
}
