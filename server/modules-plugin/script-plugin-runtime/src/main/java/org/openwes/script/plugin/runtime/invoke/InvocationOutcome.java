package org.openwes.script.plugin.runtime.invoke;

public record InvocationOutcome(boolean success, Object result, long durationMs, String errorMessage) {
}
