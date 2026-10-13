package org.openwes.script.engine;

/** Thrown when a Groovy script cannot be compiled. */
public class ScriptCompileException extends RuntimeException {

    public ScriptCompileException(String message) {
        super(message);
    }

    public ScriptCompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
