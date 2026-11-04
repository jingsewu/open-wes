package org.openwes.script.plugin.runtime.dispatch;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.runtime.invoke.InvocationOutcome;
import org.openwes.script.plugin.runtime.invoke.ScriptInvocationExecutor;
import org.openwes.script.plugin.runtime.materialize.MaterializedExtension;
import org.openwes.script.plugin.runtime.materialize.MaterializedScriptPlugin;
import org.openwes.script.plugin.runtime.registry.ScriptPluginRegistry;
import org.openwes.script.plugin.runtime.registry.ScriptPluginRegistry.HttpEndpointMatch;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExtHttpDispatcherTest {

    private final ScriptPluginRegistry registry = mock(ScriptPluginRegistry.class);
    private final ScriptInvocationExecutor executor = mock(ScriptInvocationExecutor.class);
    private final ExtHttpDispatcher dispatcher = new ExtHttpDispatcher(registry, executor);

    private HttpServletRequest request(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }

    private HttpEndpointMatch match() {
        MaterializedScriptPlugin plugin = new MaterializedScriptPlugin("packing-wave", 11L, new ArrayList<>());
        MaterializedExtension extension = new MaterializedExtension();
        return new HttpEndpointMatch(plugin, extension);
    }

    @Test
    void dispatch_unknownEndpoint_returns404AndFailNamingThePlugin() {
        when(registry.findHttpEndpoint("packing-wave", "POST", "/order/status")).thenReturn(null);

        ResponseEntity<Response<Object>> entity = dispatcher.dispatch("packing-wave",
                request("/plugin-platform/ext/packing-wave/order/status"), "{}");

        assertEquals(404, entity.getStatusCode().value());
        Response<Object> response = entity.getBody();
        assertNotNull(response);
        assertEquals("1", response.getCode());
        assertTrue(response.getMsg().contains("packing-wave"));
        assertTrue(response.getMsg().contains("POST"));
        assertTrue(response.getMsg().contains("/order/status"));
    }

    @Test
    void dispatch_success_returns200WithTheScriptResult() {
        when(registry.findHttpEndpoint("packing-wave", "POST", "/order/status")).thenReturn(match());
        when(executor.invoke(any(), any(), any())).thenReturn(new InvocationOutcome(true, Map.of("status", "ok"), 2, null));

        ResponseEntity<Response<Object>> entity = dispatcher.dispatch("packing-wave",
                request("/plugin-platform/ext/packing-wave/order/status"), "{}");

        assertEquals(200, entity.getStatusCode().value());
        Response<Object> response = entity.getBody();
        assertNotNull(response);
        assertEquals("0", response.getCode());
        assertEquals(Map.of("status", "ok"), response.getData());
    }

    @Test
    void dispatch_scriptFailure_returns500WithoutStackTrace() {
        when(registry.findHttpEndpoint("packing-wave", "POST", "/order/status")).thenReturn(match());
        when(executor.invoke(any(), any(), any()))
                .thenReturn(new InvocationOutcome(false, null, 3, "boom: java.lang.IllegalStateException"));

        ResponseEntity<Response<Object>> entity = dispatcher.dispatch("packing-wave",
                request("/plugin-platform/ext/packing-wave/order/status"), "{}");

        assertEquals(500, entity.getStatusCode().value());
        Response<Object> response = entity.getBody();
        assertNotNull(response);
        assertEquals("1", response.getCode());
        assertEquals("script plugin 'packing-wave' failed", response.getMsg());
        assertFalse(response.getMsg().contains("java.lang"));
        assertFalse(response.getMsg().contains("\tat"));
    }
}
