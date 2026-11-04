package org.openwes.script.plugin.runtime.host;

import org.openwes.script.engine.ScriptExecutionException;
import org.openwes.script.plugin.api.host.ScriptHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** ctx.http implementation: JDK HttpClient with mandatory per-call timeouts. */
public class JdkScriptHttpClient implements ScriptHttpClient {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public String postJson(String url, String jsonBody, long timeoutMillis) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMillis))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody == null ? "" : jsonBody, StandardCharsets.UTF_8))
                .build();
        return send(request, "http post to " + url + " failed");
    }

    @Override
    public String get(String url, long timeoutMillis) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMillis))
                .GET()
                .build();
        return send(request, "http get to " + url + " failed");
    }

    private String send(HttpRequest request, String failureMessage) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException e) {
            throw new ScriptExecutionException(failureMessage + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScriptExecutionException(failureMessage + ": interrupted", e);
        }
    }
}
