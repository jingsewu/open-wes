package org.openwes.script.engine;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Runs script invocations on a dedicated pool with a wall-clock timeout. The underlying
 * thread is interrupted on timeout; a script that swallows interrupts may keep running —
 * the documented trust model accepts this (see the spec's security stance).
 */
public class ScriptExecutionRuntime implements AutoCloseable {

    private final ExecutorService executor;
    private final long timeoutMillis;

    public ScriptExecutionRuntime(long timeoutMillis, int poolSize) {
        this.timeoutMillis = timeoutMillis;
        this.executor = Executors.newFixedThreadPool(poolSize, r -> {
            Thread thread = new Thread(r, "script-engine");
            thread.setDaemon(true);
            return thread;
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(Supplier<T> invocation) {
        Callable<T> callable = invocation::get;
        Future<T> future = executor.submit(callable);
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ScriptExecutionException("script timed out after " + timeoutMillis + "ms", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ScriptExecutionException scriptExecutionException) {
                throw scriptExecutionException;
            }
            throw new ScriptExecutionException("script execution failed: " + cause.getMessage(), cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScriptExecutionException("script execution interrupted", e);
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
