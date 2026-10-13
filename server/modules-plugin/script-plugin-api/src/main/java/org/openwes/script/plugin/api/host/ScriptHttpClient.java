package org.openwes.script.plugin.api.host;

/** ctx.http — timeout-enforced outbound HTTP for scripts. */
public interface ScriptHttpClient {

    String postJson(String url, String jsonBody, long timeoutMillis);

    String get(String url, long timeoutMillis);
}
