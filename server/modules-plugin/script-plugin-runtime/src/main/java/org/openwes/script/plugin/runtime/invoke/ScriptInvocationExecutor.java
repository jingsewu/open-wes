package org.openwes.script.plugin.runtime.invoke;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptExecutionRuntime;
import org.openwes.script.engine.ScriptExecutionException;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.runtime.materialize.MaterializedExtension;
import org.openwes.script.plugin.runtime.materialize.MaterializedScriptPlugin;
import org.openwes.script.plugin.runtime.registry.ScriptPluginApiAccessor;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Runs script methods under the shared timeout runtime and ships execution logs. The extension's
 * ScriptContext is always appended as the final argument, so every script method must declare ctx
 * as its last parameter.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScriptInvocationExecutor {

    private final ScriptExecutionRuntime executionRuntime;
    private final GroovyScriptEngine scriptEngine;
    private final ScriptPluginApiAccessor accessor;

    public InvocationOutcome invoke(MaterializedScriptPlugin plugin, MaterializedExtension extension, Object[] args) {
        Object[] invokeArgs = Arrays.copyOf(args == null ? new Object[0] : args,
                (args == null ? 0 : args.length) + 1);
        invokeArgs[invokeArgs.length - 1] = extension.getScriptContext();

        long start = System.currentTimeMillis();
        try {
            Object result = executionRuntime.execute(() -> scriptEngine.invoke(extension.getScriptInstance(),
                    extension.getDeclaration().getMethod(), invokeArgs));
            long duration = System.currentTimeMillis() - start;
            report(plugin, extension, true, duration, null);
            return new InvocationOutcome(true, result, duration, null);
        } catch (ScriptExecutionException e) {
            long duration = System.currentTimeMillis() - start;
            report(plugin, extension, false, duration, e.getMessage());
            return new InvocationOutcome(false, null, duration, e.getMessage());
        }
    }

    private void report(MaterializedScriptPlugin plugin, MaterializedExtension extension, boolean success,
                        long durationMs, String errorMessage) {
        try {
            ExecutionLogDTO executionLog = new ExecutionLogDTO();
            executionLog.setPluginKey(plugin.getPluginKey());
            executionLog.setRevisionId(plugin.getRevisionId());
            executionLog.setExtensionType(extension.getDeclaration().getType());
            executionLog.setSuccess(success);
            executionLog.setDurationMs(durationMs);
            executionLog.setErrorMessage(errorMessage);
            executionLog.setCreatedAt(System.currentTimeMillis());
            accessor.reportExecution(executionLog);
        } catch (Exception e) {
            // shipping must never break the invocation path on this node
            log.warn("failed to report execution log of plugin {}: {}", plugin.getPluginKey(), e.getMessage());
        }
    }
}
