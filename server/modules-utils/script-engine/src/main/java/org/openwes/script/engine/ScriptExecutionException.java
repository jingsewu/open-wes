package org.openwes.script.engine;

/** Thrown when a compiled script fails to instantiate, invoke, or exceeds its timeout. */
public class ScriptExecutionException extends RuntimeException {

    public ScriptExecutionException(String message) {
        super(message);
    }

    public ScriptExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
