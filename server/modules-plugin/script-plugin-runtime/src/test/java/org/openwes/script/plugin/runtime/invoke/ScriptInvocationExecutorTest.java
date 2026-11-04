package org.openwes.script.plugin.runtime.invoke;

import org.junit.jupiter.api.Test;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptExecutionRuntime;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.runtime.materialize.MaterializedExtension;
import org.openwes.script.plugin.runtime.materialize.MaterializedScriptPlugin;
import org.openwes.script.plugin.runtime.registry.ScriptPluginApiAccessor;
import org.openwes.script.plugin.runtime.host.DefaultScriptContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ScriptInvocationExecutorTest {

    private final GroovyScriptEngine engine = new GroovyScriptEngine();
    private final ScriptPluginApiAccessor accessor = mock(ScriptPluginApiAccessor.class);
    private final ScriptInvocationExecutor executor =
            new ScriptInvocationExecutor(new ScriptExecutionRuntime(2000, 2), engine, accessor);

    private MaterializedScriptPlugin plugin() {
        return new MaterializedScriptPlugin("packing-wave", 11L, new ArrayList<>());
    }

    private MaterializedExtension extension(String scriptSource, String method) {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        declaration.setScript("Wave.groovy");
        declaration.setMethod(method);
        MaterializedExtension extension = new MaterializedExtension();
        extension.setDeclaration(declaration);
        extension.setScriptInstance(engine.newInstance(engine.compile(scriptSource)));
        extension.setScriptContext(new DefaultScriptContext("packing-wave", null, Map.of(), List.of(), null));
        return extension;
    }

    @Test
    void invoke_appendsContextAsLastArgument() {
        MaterializedExtension extension = extension(
                "class Wave { Object wave(Object orders, Object ctx) { return ctx.pluginKey() } }", "wave");

        InvocationOutcome outcome = executor.invoke(plugin(), extension, new Object[]{List.of(1, 2)});

        assertTrue(outcome.success());
        assertEquals("packing-wave", outcome.result());
        verify(accessor).reportExecution(any(ExecutionLogDTO.class));
    }

    @Test
    void invoke_scriptThrows_returnsFailedOutcomeWithMessage() {
        MaterializedExtension extension = extension(
                "class Wave { Object wave(Object orders, Object ctx) { throw new IllegalStateException('boom') } }",
                "wave");

        InvocationOutcome outcome = executor.invoke(plugin(), extension, new Object[]{List.of(1, 2)});

        assertFalse(outcome.success());
        assertTrue(outcome.errorMessage().contains("boom"));
        assertNull(outcome.result());
    }

    @Test
    void invoke_scriptExceedsTimeout_returnsFailedOutcome() {
        ScriptInvocationExecutor quickExecutor =
                new ScriptInvocationExecutor(new ScriptExecutionRuntime(150, 1), engine, accessor);
        MaterializedExtension extension = extension(
                "class Wave { Object wave(Object orders, Object ctx) { Thread.sleep(5000); return null } }", "wave");

        InvocationOutcome outcome = quickExecutor.invoke(plugin(), extension, new Object[]{List.of(1, 2)});

        assertFalse(outcome.success());
        assertTrue(outcome.errorMessage().contains("timed out"));
    }

    @Test
    void invoke_whenLogReportingFails_doesNotBreakTheInvocation() {
        doThrow(new RuntimeException("management plane down")).when(accessor).reportExecution(any());
        MaterializedExtension extension = extension(
                "class Wave { Object wave(Object orders, Object ctx) { return 'ok' } }", "wave");

        InvocationOutcome outcome = executor.invoke(plugin(), extension, new Object[]{List.of(1, 2)});

        assertTrue(outcome.success());
        assertEquals("ok", outcome.result());
    }
}
