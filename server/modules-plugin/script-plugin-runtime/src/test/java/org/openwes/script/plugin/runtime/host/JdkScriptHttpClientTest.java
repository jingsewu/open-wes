package org.openwes.script.plugin.runtime.host;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdkScriptHttpClientTest {

    private final JdkScriptHttpClient client = new JdkScriptHttpClient();
    private HttpServer server;
    private String baseUrl;

    private void startServer(String contextPath, long delayMillis) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(contextPath, exchange -> {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            byte[] response = ("echo:" + requestBody).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postJson_returnsTheResponseBody() throws IOException {
        startServer("/echo", 0);

        assertEquals("echo:{\"a\":1}", client.postJson(baseUrl + "/echo", "{\"a\":1}", 2000));
    }

    @Test
    void get_returnsTheResponseBody() throws IOException {
        startServer("/echo", 0);

        assertEquals("echo:", client.get(baseUrl + "/echo", 2000));
    }

    @Test
    void postJson_slowServer_throwsScriptExecutionException() throws IOException {
        startServer("/slow", 2000);

        RuntimeException e = assertThrows(RuntimeException.class,
                () -> client.postJson(baseUrl + "/slow", "{}", 200));
        assertTrue(e.getMessage().contains("http post"));
    }
}
