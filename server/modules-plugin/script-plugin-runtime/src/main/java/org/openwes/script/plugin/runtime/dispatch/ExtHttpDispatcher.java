package org.openwes.script.plugin.runtime.dispatch;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.runtime.invoke.InvocationOutcome;
import org.openwes.script.plugin.runtime.invoke.ScriptInvocationExecutor;
import org.openwes.script.plugin.runtime.materialize.MaterializedExtension;
import org.openwes.script.plugin.runtime.materialize.MaterializedScriptPlugin;
import org.openwes.script.plugin.runtime.registry.ScriptPluginRegistry;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves self-provided script endpoints. The path prefix is reserved and stable inside and outside
 * the gateway (the /plugin-platform/** route forwards without a RewritePath filter), so plugin
 * authors declare paths relative to /plugin-platform/ext/{pluginKey}/.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ExtHttpDispatcher {

    private final ScriptPluginRegistry registry;
    private final ScriptInvocationExecutor executor;

    @RequestMapping("plugin-platform/ext/{pluginKey}/**")
    public Response<Object> dispatch(@PathVariable("pluginKey") String pluginKey,
                                     HttpServletRequest request,
                                     @RequestBody(required = false) String body) {
        String path = extractPath(request, pluginKey);
        ScriptPluginRegistry.HttpEndpointMatch match =
                registry.findHttpEndpoint(pluginKey, request.getMethod(), path);
        if (match == null) {
            return failWith("no script plugin endpoint " + pluginKey + " " + request.getMethod() + " " + path);
        }

        MaterializedScriptPlugin plugin = match.plugin();
        MaterializedExtension extension = match.extension();
        InvocationOutcome outcome = executor.invoke(plugin, extension, new Object[]{body});
        if (!outcome.success()) {
            log.error("script plugin endpoint {} {} failed: {}", pluginKey, path, outcome.errorMessage());
            return failWith("script plugin '" + pluginKey + "' failed");
        }
        return Response.success(outcome.result());
    }

    private String extractPath(HttpServletRequest request, String pluginKey) {
        String marker = "/ext/" + pluginKey;
        String uri = request.getRequestURI();
        int index = uri.indexOf(marker);
        String rest = index < 0 ? "/" : uri.substring(index + marker.length());
        if (rest.isEmpty()) {
            rest = "/";
        }
        if (rest.length() > 1 && rest.endsWith("/")) {
            rest = rest.substring(0, rest.length() - 1);
        }
        return rest;
    }

    private Response<Object> failWith(String message) {
        Response<Object> response = Response.fail();
        response.setMsg(message);
        return response;
    }
}
