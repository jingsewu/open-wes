package org.openwes.script.plugin.runtime.materialize;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin;
import org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveWaveAction;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptExecutionRuntime;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.runtime.host.JdkScriptHttpClient;
import org.openwes.script.plugin.runtime.invoke.ScriptInvocationExecutor;
import org.openwes.script.plugin.runtime.invoke.ScriptPluginInvocationException;
import org.openwes.script.plugin.runtime.registry.ScriptPluginApiAccessor;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ScriptPluginMaterializerTest {

    private final AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext();
    private ScriptPluginMaterializer materializer;

    @BeforeEach
    void setUp() {
        parent.registerBean(String.class, () -> "injected");
        parent.refresh();
        GroovyScriptEngine engine = new GroovyScriptEngine();
        ScriptRevisionContextManager contextManager = new ScriptRevisionContextManager(parent, engine);
        ScriptInvocationExecutor executor = new ScriptInvocationExecutor(new ScriptExecutionRuntime(2000, 2), engine,
                mock(ScriptPluginApiAccessor.class));
        materializer = new ScriptPluginMaterializer(contextManager, executor, new JdkScriptHttpClient());
    }

    @AfterEach
    void tearDown() {
        materializer.closeAll();
        parent.close();
    }

    private ExtensionDeclarationDTO declaration(ExtensionType type, String script, String method) {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(type);
        declaration.setScript(script);
        declaration.setMethod(method);
        return declaration;
    }

    private ScriptPluginBundleDTO bundle(Map<String, String> scripts, ExtensionDeclarationDTO... declarations) {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("packing-wave");
        bundle.setApplySystem("wes");
        bundle.setSdkVersion(ScriptPluginApiConstants.CONTRACT_VERSION);
        bundle.setRevisionId(11L);
        bundle.setScripts(scripts);
        bundle.setDeclarations(List.of(declarations));
        bundle.setConfig(Map.of("maxPerWave", 10));
        bundle.setConfigSchema(List.of());
        return bundle;
    }

    @Test
    void materialize_waveAction_createsProxyBackedByInjectedScriptBean() {
        ExtensionDeclarationDTO declaration = declaration(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION, "Wave.groovy", "wave");
        declaration.setPriority(100);
        String source = """
                import org.springframework.beans.factory.annotation.Autowired
                class Wave {
                    @Autowired String greeting
                    Object wave(Object orders, Object ctx) { return [greeting, ctx.pluginKey(), orders.size()] }
                }
                """;
        List<OutboundPlanOrderDTO> orders = Arrays.asList(null, null);

        MaterializedScriptPlugin plugin = materializer.materialize(bundle(Map.of("Wave.groovy", source), declaration));
        IOutboundWaveWaveAction proxy = (IOutboundWaveWaveAction) plugin.getExtensions().get(0).getProxy();

        assertEquals(List.of("injected", "packing-wave", 2), proxy.wave(orders));
    }

    @Test
    void materialize_identityMethods_fallBackToInterfaceDefaults() {
        ExtensionDeclarationDTO declaration = declaration(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION, "Wave.groovy", "wave");
        declaration.setPriority(100);
        String source = "class Wave { Object wave(Object orders, Object ctx) { return null } }";

        MaterializedScriptPlugin plugin = materializer.materialize(bundle(Map.of("Wave.groovy", source), declaration));
        IOutboundWaveWaveAction proxy = (IOutboundWaveWaveAction) plugin.getExtensions().get(0).getProxy();

        assertEquals(100, proxy.priority());
        assertEquals("OutboundWave", proxy.getEntityName()); // IOutboundWavePlugin default method
    }

    @Test
    void materialize_declaredIdentityValues_winOverDefaults() {
        ExtensionDeclarationDTO declaration = declaration(ExtensionType.REQUEST_SUPPLY, "Supply.groovy", "supply");
        declaration.setApiType("ORDER_OUTBOUND_CREATE");
        String source = "class Supply { Object supply(Object context, Object ctx) { return null } }";

        MaterializedScriptPlugin plugin = materializer.materialize(bundle(Map.of("Supply.groovy", source), declaration));
        IRequestPlugin proxy = (IRequestPlugin) plugin.getExtensions().get(0).getProxy();

        assertEquals("ORDER_OUTBOUND_CREATE", proxy.getApiType());
    }

    @Test
    void materialize_blockingArchetype_throwsWithPluginKey() {
        ExtensionDeclarationDTO declaration = declaration(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION, "Wave.groovy", "wave");
        String source = "class Wave { Object wave(Object orders, Object ctx) { throw new IllegalStateException('boom') } }";

        MaterializedScriptPlugin plugin = materializer.materialize(bundle(Map.of("Wave.groovy", source), declaration));
        IOutboundWaveWaveAction proxy = (IOutboundWaveWaveAction) plugin.getExtensions().get(0).getProxy();

        ScriptPluginInvocationException e = assertThrows(ScriptPluginInvocationException.class,
                () -> proxy.wave(List.of()));
        assertTrue(e.getMessage().contains("packing-wave"));
        assertTrue(e.getMessage().contains("boom"));
    }

    @Test
    void materialize_nonBlockingArchetype_swallowsScriptFailure() {
        ExtensionDeclarationDTO declaration = declaration(ExtensionType.REQUEST_SUPPLY, "Supply.groovy", "supply");
        declaration.setApiType("ORDER_OUTBOUND_CREATE");
        String source = "class Supply { Object supply(Object context, Object ctx) { throw new IllegalStateException('boom') } }";

        MaterializedScriptPlugin plugin = materializer.materialize(bundle(Map.of("Supply.groovy", source), declaration));
        IRequestPlugin proxy = (IRequestPlugin) plugin.getExtensions().get(0).getProxy();

        assertDoesNotThrow(() -> proxy.supply(new RequestHandleContext()));
    }

    @Test
    void materialize_httpEndpoint_hasNoProxyAndCarriesTheScriptInstance() {
        ExtensionDeclarationDTO declaration = declaration(ExtensionType.HTTP_ENDPOINT, "Http.groovy", "handle");
        declaration.setHttpMethod("GET");
        declaration.setPath("/status");
        String source = "class Http { Object handle(Object body, Object ctx) { return 'ok' } }";

        MaterializedScriptPlugin plugin = materializer.materialize(bundle(Map.of("Http.groovy", source), declaration));
        MaterializedExtension extension = plugin.getExtensions().get(0);

        assertNull(extension.getProxy());
        assertNull(extension.getExtensionPointClass());
        assertEquals("http", extension.getScriptInstance().getClass().getSimpleName().toLowerCase());
    }

    @Test
    void materialize_declarationReferencingUnknownScript_throws() {
        ExtensionDeclarationDTO declaration = declaration(ExtensionType.CRON, "Missing.groovy", "run");

        assertThrows(ScriptPluginInvocationException.class,
                () -> materializer.materialize(bundle(Map.of("Job.groovy", "class Job { Object run(Object ctx) { return 1 } }"),
                        declaration)));
    }
}
