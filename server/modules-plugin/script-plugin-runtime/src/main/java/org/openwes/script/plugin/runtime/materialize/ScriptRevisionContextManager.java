package org.openwes.script.plugin.runtime.materialize;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.script.engine.GroovyScriptEngine;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Owns one child Spring context per (plugin, revision) and the script instances created inside it.
 * Script instances are built with createBean so that @Autowired/@Resource/@Value and @PostConstruct
 * run with the child context as their context and the main context as parent — the same pattern as
 * org.openwes.plugin.sdk.register.PluginRegister. Closing the child context tears the revision down.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScriptRevisionContextManager {

    private final ApplicationContext applicationContext;
    private final GroovyScriptEngine scriptEngine;

    private final Map<String, RevisionContext> revisionContexts = new ConcurrentHashMap<>();

    public RevisionContext acquire(String pluginKey, Long revisionId, Map<String, String> scriptSources) {
        return revisionContexts.computeIfAbsent(contextKey(pluginKey, revisionId),
                key -> create(key, pluginKey, scriptSources));
    }

    /** Throwaway context for enable-time dry-run validation; never cached. */
    public ValidationScope openValidationScope(String pluginKey, Map<String, String> scriptSources) {
        AnnotationConfigApplicationContext context = newChildContext("validation-" + pluginKey);
        try {
            Map<String, Object> instances = createScriptInstances(context, pluginKey, scriptSources);
            return new ValidationScope(context, instances);
        } catch (RuntimeException | Error e) {
            closeAfterFailure(context, "validation-" + pluginKey, e);
            throw e;
        }
    }

    public void closeRevisionsExcept(String pluginKey, Long keepRevisionId) {
        String keepKey = contextKey(pluginKey, keepRevisionId);
        revisionContexts.keySet().stream()
                .filter(key -> key.startsWith(pluginKey + "#") && !key.equals(keepKey))
                .toList()
                .forEach(this::close);
    }

    public void closePlugin(String pluginKey) {
        revisionContexts.keySet().stream()
                .filter(key -> key.startsWith(pluginKey + "#"))
                .toList()
                .forEach(this::close);
    }

    public void closeAll() {
        new ArrayList<>(revisionContexts.keySet()).forEach(this::close);
    }

    private void close(String key) {
        RevisionContext revisionContext = revisionContexts.remove(key);
        if (revisionContext != null) {
            revisionContext.context().close();
            log.info("closed script revision context {}", key);
        }
    }

    private RevisionContext create(String key, String pluginKey, Map<String, String> scriptSources) {
        AnnotationConfigApplicationContext context = newChildContext(key);
        try {
            return new RevisionContext(context, createScriptInstances(context, pluginKey, scriptSources));
        } catch (RuntimeException | Error e) {
            // computeIfAbsent does not cache failed mappings, so nobody else owns this context:
            // close it here or its beans (and classloader) leak on every failed compile/injection.
            closeAfterFailure(context, key, e);
            throw e;
        }
    }

    private void closeAfterFailure(AnnotationConfigApplicationContext context, String idSuffix, Throwable cause) {
        try {
            context.close();
            log.warn("closed script child context {} after failed script materialization: {}",
                    idSuffix, cause.getMessage());
        } catch (RuntimeException | Error closeFailure) {
            cause.addSuppressed(closeFailure);
            log.warn("failed to close script child context {} after failed script materialization",
                    idSuffix, closeFailure);
        }
    }

    private AnnotationConfigApplicationContext newChildContext(String idSuffix) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setId("script-plugin-" + idSuffix);
        context.setParent(applicationContext);
        context.refresh();
        return context;
    }

    private Map<String, Object> createScriptInstances(AnnotationConfigApplicationContext context, String pluginKey,
                                                      Map<String, String> scriptSources) {
        Map<String, Object> instances = new LinkedHashMap<>();
        for (Map.Entry<String, String> script : scriptSources.entrySet()) {
            Class<?> scriptClass = scriptEngine.compile(script.getValue());
            Object instance = context.getBeanFactory().createBean(scriptClass);
            context.getBeanFactory().registerSingleton(pluginKey + "#" + script.getKey(), instance);
            instances.put(script.getKey(), instance);
        }
        return instances;
    }

    static String contextKey(String pluginKey, Long revisionId) {
        return pluginKey + "#" + revisionId;
    }

    public record RevisionContext(AnnotationConfigApplicationContext context, Map<String, Object> instances) {
    }

    public record ValidationScope(AnnotationConfigApplicationContext context, Map<String, Object> instances)
            implements AutoCloseable {

        @Override
        public void close() {
            context.close();
        }
    }
}
