package org.openwes.script.plugin.runtime.host;

import org.openwes.script.plugin.api.host.ScriptHttpClient;

public class JdkScriptHttpClient implements ScriptHttpClient {

    @Override
    public String postJson(String url, String jsonBody, long timeoutMillis) {
        throw new UnsupportedOperationException("implemented in Task 5");
    }

    @Override
    public String get(String url, long timeoutMillis) {
        throw new UnsupportedOperationException("implemented in Task 5");
    }
}
