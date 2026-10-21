package org.openwes.script.engine;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.time.Duration;

/**
 * Compiles and caches Groovy script classes. Cache keys are content hashes
 * ({@link ScriptHashes#sha256}), so identical sources across plugins and revisions share one class.
 * The cache is bounded so repeated AI-generated revisions cannot grow it without limit.
 */
@Slf4j
public class GroovyScriptEngine {

    private static final int MAX_CACHED_CLASSES = 500;

    private final Cache<String, Class<?>> compiledClassCache = Caffeine.newBuilder()
            .maximumSize(MAX_CACHED_CLASSES)
            .expireAfterAccess(Duration.ofHours(1))
            .build();

    public Class<?> compile(String source) {
        if (source == null || source.isBlank()) {
            throw new ScriptCompileException("script source is empty");
        }
        return compiledClassCache.get(ScriptHashes.sha256(source), hash -> {
            try (GroovyClassLoader loader = new GroovyClassLoader()) {
                return loader.parseClass(source);
            } catch (Exception e) {
                log.error("groovy compile failed: {}", e.getMessage());
                throw new ScriptCompileException("groovy compile failed: " + e.getMessage(), e);
            }
        });
    }

    public Object newInstance(Class<?> compiledClass) {
        try {
            return compiledClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new ScriptExecutionException("cannot instantiate script class " + compiledClass.getName(), e);
        }
    }

    /**
     * Invokes {@code methodName} with Groovy's dynamic dispatch, so scripts may declare the
     * method parameter with any (or no) type. Note that a null varargs array means one null
     * argument; callers wanting zero arguments omit them.
     */
    public Object invoke(Object instance, String methodName, Object... args) {
        if (args == null) {
            args = new Object[]{null};
        }
        try {
            return InvokerHelper.invokeMethod(instance, methodName, args);
        } catch (ScriptExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ScriptExecutionException("script method '" + methodName + "' failed: " + e.getMessage(), e);
        }
    }
}
