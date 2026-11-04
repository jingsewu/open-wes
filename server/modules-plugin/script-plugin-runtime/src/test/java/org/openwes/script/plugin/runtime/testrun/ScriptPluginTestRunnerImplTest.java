package org.openwes.script.plugin.runtime.testrun;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptExecutionException;
import org.openwes.script.engine.ScriptExecutionRuntime;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.api.host.ScriptHttpClient;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScriptPluginTestRunnerImplTest {

    @Mock
    private ScriptRevisionContextManager contextManager;
    @Mock
    private GroovyScriptEngine scriptEngine;
    @Mock
    private ScriptExecutionRuntime executionRuntime;
    @Mock
    private ScriptHttpClient httpClient;

    private ScriptPluginTestRunnerImpl runner;
    private AnnotationConfigApplicationContext context;
    private Object scriptInstance;

    @BeforeEach
    void setUp() {
        runner = new ScriptPluginTestRunnerImpl(contextManager, scriptEngine, executionRuntime, httpClient);
        context = mock(AnnotationConfigApplicationContext.class);
        scriptInstance = new Object();
    }

    private ScriptRevisionContextManager.ValidationScope scope(Map<String, Object> instances) {
        return new ScriptRevisionContextManager.ValidationScope(context, instances);
    }

    private ScriptPluginBundleDTO bundle() {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("packing-wave");
        bundle.setRevisionId(11L);
        bundle.setScripts(Map.of("Wave.groovy", "class Wave {}"));
        bundle.setConfig(Map.of());
        bundle.setConfigSchema(List.of());
        return bundle;
    }

    private TestRunRequestDTO request(String script, String method) {
        TestRunRequestDTO request = new TestRunRequestDTO();
        request.setExtensionType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        request.setScript(script);
        request.setMethod(method);
        request.setArguments(Map.of("orders", List.of()));
        return request;
    }

    @Test
    void successfulRun_serializesTheResultAndClosesTheScope() {
        when(contextManager.openValidationScope("packing-wave",
                Map.of("Wave.groovy", "class Wave {}")))
                .thenReturn(scope(Map.of("Wave.groovy", scriptInstance)));
        when(scriptEngine.respondsTo(scriptInstance, "wave")).thenReturn(true);
        when(scriptEngine.invoke(eq(scriptInstance), eq("wave"), any(), any()))
                .thenReturn(Map.of("ok", true));
        when(executionRuntime.execute(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        TestRunResultDTO result = runner.run("packing-wave", bundle(),
                request("Wave.groovy", "wave"));

        assertTrue(result.isSuccess());
        assertTrue(result.getResult().contains("\"ok\""));
        assertFalse(result.isResultTruncated());
        assertTrue(result.getDurationMs() >= 0);
        verify(context).close();
    }

    @Test
    void scriptException_returnsFailureAndStillClosesTheScope() {
        when(contextManager.openValidationScope("packing-wave",
                Map.of("Wave.groovy", "class Wave {}")))
                .thenReturn(scope(Map.of("Wave.groovy", scriptInstance)));
        when(scriptEngine.respondsTo(scriptInstance, "wave")).thenReturn(true);
        when(scriptEngine.invoke(eq(scriptInstance), eq("wave"), any(), any()))
                .thenThrow(new ScriptExecutionException("boom"));
        when(executionRuntime.execute(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        TestRunResultDTO result = runner.run("packing-wave", bundle(),
                request("Wave.groovy", "wave"));

        assertFalse(result.isSuccess());
        assertEquals("boom", result.getError());
        verify(context).close();
    }

    @Test
    void timeout_isReportedAsFailureAndStillClosesTheScope() {
        when(contextManager.openValidationScope("packing-wave",
                Map.of("Wave.groovy", "class Wave {}")))
                .thenReturn(scope(Map.of("Wave.groovy", scriptInstance)));
        when(scriptEngine.respondsTo(scriptInstance, "wave")).thenReturn(true);
        when(executionRuntime.execute(any())).thenThrow(
                new ScriptExecutionException("script timed out after 50ms"));

        TestRunResultDTO result = runner.run("packing-wave", bundle(),
                request("Wave.groovy", "wave"));

        assertFalse(result.isSuccess());
        assertEquals("script timed out after 50ms", result.getError());
        verify(context).close();
    }

    @Test
    void unknownScript_isReportedWithoutInvoking() {
        when(contextManager.openValidationScope("packing-wave",
                Map.of("Wave.groovy", "class Wave {}")))
                .thenReturn(scope(Map.of("Wave.groovy", scriptInstance)));

        TestRunResultDTO result = runner.run("packing-wave", bundle(),
                request("Missing.groovy", "wave"));

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("not part of the current revision"));
        verify(context).close();
    }

    @Test
    void largeResult_isTruncatedToEightKilobytes() {
        when(contextManager.openValidationScope("packing-wave",
                Map.of("Wave.groovy", "class Wave {}")))
                .thenReturn(scope(Map.of("Wave.groovy", scriptInstance)));
        when(scriptEngine.respondsTo(scriptInstance, "wave")).thenReturn(true);
        when(scriptEngine.invoke(eq(scriptInstance), eq("wave"), any(), any()))
                .thenReturn("x".repeat(9000));
        when(executionRuntime.execute(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        TestRunResultDTO result = runner.run("packing-wave", bundle(),
                request("Wave.groovy", "wave"));

        assertTrue(result.isSuccess());
        assertTrue(result.isResultTruncated());
        assertEquals(8 * 1024, result.getResult().length());
    }
}
