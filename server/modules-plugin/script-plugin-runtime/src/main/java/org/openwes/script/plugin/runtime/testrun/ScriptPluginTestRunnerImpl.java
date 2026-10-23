package org.openwes.script.plugin.runtime.testrun;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptExecutionRuntime;
import org.openwes.script.plugin.api.ScriptPluginTestRunner;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;
import org.openwes.script.plugin.api.host.ScriptHttpClient;
import org.openwes.script.plugin.runtime.host.DefaultScriptContext;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScriptPluginTestRunnerImpl implements ScriptPluginTestRunner {

    static final int MAX_RESULT_LENGTH = 8 * 1024;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ScriptRevisionContextManager revisionContextManager;
    private final GroovyScriptEngine scriptEngine;
    private final ScriptExecutionRuntime executionRuntime;
    private final ScriptHttpClient scriptHttpClient;

    @Override
    public TestRunResultDTO run(String pluginKey, ScriptPluginBundleDTO bundle, TestRunRequestDTO request) {
        TestRunResultDTO result = new TestRunResultDTO();
        long startedAt = System.nanoTime();
        try (ScriptRevisionContextManager.ValidationScope scope = revisionContextManager
                .openValidationScope(pluginKey, bundle.getScripts())) {
            Object instance = scope.instances().get(request.getScript());
            if (instance == null) {
                throw new CommonException("script '" + request.getScript()
                        + "' is not part of the current revision");
            }
            if (!scriptEngine.respondsTo(instance, request.getMethod())) {
                throw new CommonException("script '" + request.getScript()
                        + "' does not declare method '" + request.getMethod() + "'");
            }
            DefaultScriptContext context = new DefaultScriptContext(pluginKey, scope.context(),
                    bundle.getConfig() == null ? Map.of() : bundle.getConfig(),
                    bundle.getConfigSchema() == null ? List.of() : bundle.getConfigSchema(),
                    scriptHttpClient);
            Object[] arguments = TestRunArgumentBuilder.build(request.getExtensionType(),
                    request.getArguments());
            Object[] invocationArguments = Arrays.copyOf(arguments, arguments.length + 1);
            invocationArguments[arguments.length] = context;
            Object value = executionRuntime.execute(() ->
                    scriptEngine.invoke(instance, request.getMethod(), invocationArguments));
            String json = serialize(value);
            result.setSuccess(true);
            result.setResultTruncated(json != null && json.length() > MAX_RESULT_LENGTH);
            result.setResult(truncate(json));
        } catch (RuntimeException e) {
            result.setSuccess(false);
            result.setError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        } finally {
            result.setDurationMs((System.nanoTime() - startedAt) / 1_000_000);
        }
        return result;
    }

    private static String serialize(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return value.toString();
        }
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= MAX_RESULT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_RESULT_LENGTH);
    }
}
