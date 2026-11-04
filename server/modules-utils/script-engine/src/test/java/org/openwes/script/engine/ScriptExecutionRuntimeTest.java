package org.openwes.script.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScriptExecutionRuntimeTest {

    @Test
    void execute_returnsSupplierValue() {
        try (ScriptExecutionRuntime runtime = new ScriptExecutionRuntime(1000, 2)) {
            assertEquals("ok", runtime.execute(() -> "ok"));
        }
    }

    @Test
    void execute_slowScript_timesOut() {
        try (ScriptExecutionRuntime runtime = new ScriptExecutionRuntime(200, 1)) {
            ScriptExecutionException e = assertThrows(ScriptExecutionException.class,
                    () -> runtime.execute(() -> sleepQuietly(5000)));
            assertEquals(true, e.getMessage().contains("timed out"));
        }
    }

    @Test
    void execute_scriptException_isWrapped() {
        try (ScriptExecutionRuntime runtime = new ScriptExecutionRuntime(1000, 1)) {
            ScriptExecutionException e = assertThrows(ScriptExecutionException.class,
                    () -> runtime.execute(() -> {
                        throw new ScriptExecutionException("inner failure");
                    }));
            assertEquals(true, e.getMessage().contains("inner failure"));
        }
    }

    /** Supplier cannot throw InterruptedException, so swallow it here and keep the wait observable. */
    private static Object sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
