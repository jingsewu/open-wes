package org.openwes.script.plugin.runtime.invoke;

/** Thrown by blocking archetypes; the message always names the plugin that failed. */
public class ScriptPluginInvocationException extends RuntimeException {

    public ScriptPluginInvocationException(String pluginKey, String method, String message) {
        super("script plugin '" + pluginKey + "' method '" + method + "' failed: " + message);
    }
}
