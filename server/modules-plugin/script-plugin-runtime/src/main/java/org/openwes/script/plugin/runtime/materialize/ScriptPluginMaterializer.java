package org.openwes.script.plugin.runtime.materialize;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.host.ScriptHttpClient;
import org.openwes.script.plugin.runtime.host.DefaultScriptContext;
import org.openwes.script.plugin.runtime.invoke.ScriptInvocationExecutor;
import org.openwes.script.plugin.runtime.invoke.ScriptPluginInvocationException;
import org.springframework.stereotype.Component;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScriptPluginMaterializer {

    private final ScriptRevisionContextManager revisionContextManager;
    private final ScriptInvocationExecutor invocationExecutor;
    private final ScriptHttpClient scriptHttpClient;

    public MaterializedScriptPlugin materialize(ScriptPluginBundleDTO bundle) {
        ScriptRevisionContextManager.RevisionContext revisionContext = revisionContextManager.acquire(
                bundle.getPluginKey(), bundle.getRevisionId(), bundle.getScripts());

        List<MaterializedExtension> extensions = new ArrayList<>();
        MaterializedScriptPlugin plugin = new MaterializedScriptPlugin(bundle.getPluginKey(), bundle.getRevisionId(),
                extensions);

        for (ExtensionDeclarationDTO declaration : bundle.getDeclarations()) {
            PluginContractCatalog.ArchetypeDescriptor archetype = PluginContractCatalog.archetype(declaration.getType())
                    .orElseThrow(() -> new ScriptPluginInvocationException(bundle.getPluginKey(), declaration.getMethod(),
                            "unknown extension type " + declaration.getType()));
            Object scriptInstance = revisionContext.instances().get(declaration.getScript());
            if (scriptInstance == null) {
                throw new ScriptPluginInvocationException(bundle.getPluginKey(), declaration.getMethod(),
                        "script '" + declaration.getScript() + "' is not part of revision " + bundle.getRevisionId());
            }

            MaterializedExtension extension = new MaterializedExtension();
            extension.setDeclaration(declaration);
            extension.setScriptInstance(scriptInstance);
            extension.setScriptContext(new DefaultScriptContext(bundle.getPluginKey(), revisionContext.context(),
                    bundle.getConfig() == null ? Map.of() : bundle.getConfig(),
                    bundle.getConfigSchema() == null ? List.of() : bundle.getConfigSchema(), scriptHttpClient));

            if (archetype.extensionPointClassName() != null) {
                Class<?> extensionPoint = loadExtensionPoint(bundle, declaration, archetype);
                extension.setExtensionPointClass(extensionPoint);
                extension.setProxy(Proxy.newProxyInstance(extensionPoint.getClassLoader(),
                        new Class<?>[]{extensionPoint},
                        new ScriptExtensionInvocationHandler(plugin, extension, archetype.blocking(), invocationExecutor)));
            }
            extensions.add(extension);
        }
        return plugin;
    }

    public void closeRevisionsExcept(String pluginKey, Long keepRevisionId) {
        revisionContextManager.closeRevisionsExcept(pluginKey, keepRevisionId);
    }

    public void closePlugin(String pluginKey) {
        revisionContextManager.closePlugin(pluginKey);
    }

    public void closeAll() {
        revisionContextManager.closeAll();
    }

    private Class<?> loadExtensionPoint(ScriptPluginBundleDTO bundle, ExtensionDeclarationDTO declaration,
                                        PluginContractCatalog.ArchetypeDescriptor archetype) {
        try {
            return Class.forName(archetype.extensionPointClassName());
        } catch (ClassNotFoundException e) {
            log.error("extension point {} is not on this node's classpath", archetype.extensionPointClassName());
            throw new ScriptPluginInvocationException(bundle.getPluginKey(), declaration.getMethod(),
                    "extension point " + archetype.extensionPointClassName() + " is not available on this node");
        }
    }
}
