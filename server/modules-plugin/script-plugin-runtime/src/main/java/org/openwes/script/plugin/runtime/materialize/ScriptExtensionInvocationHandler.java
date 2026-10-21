package org.openwes.script.plugin.runtime.materialize;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.runtime.invoke.InvocationOutcome;
import org.openwes.script.plugin.runtime.invoke.ScriptInvocationExecutor;
import org.openwes.script.plugin.runtime.invoke.ScriptPluginInvocationException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Routes host-interface calls of a script extension to the compiled Groovy instance.
 * Identity methods (priority/getApiType/getEntityName/customApiCode) answer from the declaration,
 * falling back to the interface default; all other default methods keep their default behavior.
 */
@RequiredArgsConstructor
@Slf4j
public class ScriptExtensionInvocationHandler implements InvocationHandler {

    private final MaterializedScriptPlugin plugin;
    private final MaterializedExtension extension;
    private final boolean blocking;
    private final ScriptInvocationExecutor executor;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();

        switch (methodName) {
            case "equals" -> {
                return proxy == args[0];
            }
            case "hashCode" -> {
                return System.identityHashCode(proxy);
            }
            case "toString" -> {
                return "ScriptExtension[" + plugin.getPluginKey() + "#" + plugin.getRevisionId() + "]";
            }
        }

        ExtensionDeclarationDTO declaration = extension.getDeclaration();
        switch (methodName) {
            case "priority" -> {
                return declaration.getPriority();
            }
            case "getEntityName" -> {
                return identityOrDefault(proxy, method, declaration.getEntityName(), args);
            }
            case "getApiType" -> {
                return identityOrDefault(proxy, method, declaration.getApiType(), args);
            }
            case "customApiCode" -> {
                return identityOrDefault(proxy, method, declaration.getCustomApiCode(), args);
            }
        }

        if (!methodName.equals(declaration.getMethod())) {
            return method.isDefault() ? InvocationHandler.invokeDefault(proxy, method, args)
                    : defaultValue(method.getReturnType());
        }

        InvocationOutcome outcome = executor.invoke(plugin, extension, args);
        if (outcome.success()) {
            return outcome.result();
        }
        if (blocking) {
            throw new ScriptPluginInvocationException(plugin.getPluginKey(), methodName, outcome.errorMessage());
        }
        log.error("script plugin {} method {} failed (non-blocking, continuing): {}",
                plugin.getPluginKey(), methodName, outcome.errorMessage());
        return defaultValue(method.getReturnType());
    }

    private Object identityOrDefault(Object proxy, Method method, Object declared, Object[] args) throws Throwable {
        if (declared instanceof String string ? !string.isBlank() : declared != null) {
            return declared;
        }
        return method.isDefault() ? InvocationHandler.invokeDefault(proxy, method, args) : null;
    }

    private Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive() || returnType == void.class) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == double.class) {
            return 0d;
        }
        if (returnType == float.class) {
            return 0f;
        }
        if (returnType == char.class) {
            return (char) 0;
        }
        return 0;
    }
}
