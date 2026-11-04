# Script Plugin Phase 1b — Runtime Materialization & Execution Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build `script-plugin-runtime` — the node-side execution plane that fetches enabled bundles from the management plane, compiles their Groovy scripts, materializes one JDK proxy per declared extension point inside a per-revision child Spring context (so scripts get full `@Autowired`/`@Resource`/`@Value`/`@PostConstruct` support), merges those proxies with PF4J jar plugins at both extraction points in `plugin-sdk`, dispatches HTTP endpoints and cron jobs, and runs the enable-time materialization dry-run — so enabling a plugin takes effect on running nodes without a restart.

**Architecture:** `ScriptPluginRegistry` keeps a `ConcurrentSkipListMap` of materialized plugins for this app (`spring.application.name`), refreshing on a Redisson pub/sub message (`PLUGIN_LISTEN_PLUGIN_MANAGEMENT`) plus a 60 s version poll. `ScriptPluginMaterializer` compiles each revision's scripts with `GroovyScriptEngine` and creates instances via `context.getBeanFactory().createBean(scriptClass)` inside an `AnnotationConfigApplicationContext` whose parent is the main context (the same pattern as `PluginRegister`), then builds one JDK dynamic proxy per declaration; the proxy intercepts the archetype's script method (appending `ctx` as the last argument), answers identity methods (`priority`, `getApiType`, `getEntityName`, `customApiCode`), and applies the archetype's blocking/non-blocking failure policy. `ScriptExtensionProviderImpl` bridges the registry into `plugin-sdk`'s `ScriptExtensionProvider` SPI, so `PluginSdkUtils.getExtractObject` and `LifecycleListenerRegistry` merge script extensions with jar plugins, ordered by `IPlugin.priority()` descending. Dispatcher-backed archetypes (HTTP endpoint, cron) bypass proxies: `ExtHttpDispatcher` maps `plugin-platform/ext/{pluginKey}/**` and `ScriptCronDispatcher` scans due cron declarations under a `DistributeLock` with auto-disable after N consecutive failures.

**Tech Stack:** Java 17, Spring Boot 3.2.2 (`AnnotationConfigApplicationContext`, `CronExpression`, JDK `HttpClient`), Groovy 4.0.22, JDK dynamic proxies, Redisson pub/sub (`RedisUtils`), Dubbo (`@DubboReference`), JUnit 5 + Mockito 4.6.0, `com.sun.net.httpserver.HttpServer` (tests only).

**Spec:** `docs/superpowers/specs/2026-10-08-script-plugin-ai-workbench-design.md` (see *Architecture → Proxy materialization, Spring integration, Runtime contract, Enable-time validation*, *Archetype dispatch & failure policy*).

**Depends on:**
- `docs/superpowers/plans/2026-10-08-script-plugin-phase0-fixes.md` (provides `IPlugin.priority()` and the deterministic wave selection + fallback in `OutboundWaveServiceImpl`).
- `docs/superpowers/plans/2026-10-08-script-plugin-phase1a-engine-management.md` (provides `script-engine`, `script-plugin-api` incl. `ScriptPluginMaterializationValidator`, and `script-plugin-core`'s Dubbo provider).

## Global Constraints

- Module home: `server/modules-plugin/script-plugin-runtime/`; Gradle include pattern `include 'modules-plugin:script-plugin-runtime'`; project deps use `implementation project(path: ":...", configuration: 'default')`.
- Java package root: `org.openwes.script.plugin.runtime` with sub-packages `config`, `invoke`, `materialize`, `host`, `registry`, `dispatch`, `validation`.
- **The runtime must never depend on `script-plugin-core` or JPA.** Execution logs and registry data travel over `IScriptPluginApi` (local bean injection on wes, `@DubboReference` on station).
- Spring-bean injection into scripts uses `getBeanFactory().createBean(scriptClass)` inside a child `AnnotationConfigApplicationContext` with `setParent(mainContext)` — the same pattern as `org.openwes.plugin.sdk.register.PluginRegister`. `ctx.bean(Class)` resolution therefore hits the child context first, then the main context.
- Script method convention: **every script method's last parameter is `ctx`** (the `ScriptContext`); `ScriptInvocationExecutor` appends it automatically.
- Failure policy is driven by `PluginContractCatalog.ArchetypeDescriptor.blocking()`: blocking archetypes throw `ScriptPluginInvocationException` (message always contains the plugin key); non-blocking archetypes log and return the type-appropriate default. HTTP endpoints return `Response.fail()` with the plugin key and no stack trace; cron failures count toward auto-disable.
- Identity methods answered by every proxy: `priority()` → declared priority, `getApiType()`/`getEntityName()`/`customApiCode()` → declared value, falling back to the interface default method when the declaration leaves them empty.
- Logging is English-only. Every commit message ends with: `Co-Authored-By: Claude Code <noreply@anthropic.com>`.
- Run all Gradle commands from `server/`.

---

## File Map

| File | Action | Purpose |
|------|--------|---------|
| `server/settings.gradle` | Modify | include `script-plugin-runtime` |
| `server/modules-plugin/script-plugin-runtime/build.gradle` | Create | runtime module deps |
| `server/modules-utils/script-engine/src/main/java/org/openwes/script/engine/GroovyScriptEngine.java` | Modify | add `respondsTo` (Groovy stays isolated in this module) |
| `server/modules-utils/script-engine/src/test/java/org/openwes/script/engine/GroovyScriptEngineTest.java` | Modify | `respondsTo` tests |
| `.../runtime/config/RuntimeConfiguration.java` | Create | engine/runtime/http/clock beans |
| `.../runtime/registry/ScriptPluginApiAccessor.java` | Create | accessor abstraction |
| `.../runtime/registry/LocalScriptPluginApiAccessor.java` | Create | wes (local bean) |
| `.../runtime/registry/RemoteScriptPluginApiAccessor.java` | Create | station (`@DubboReference`) |
| `.../runtime/registry/ScriptPluginRegistry.java` | Create | materialized state, refresh, poll |
| `.../runtime/registry/ScriptExtensionProviderImpl.java` | Create | plugin-sdk SPI bridge |
| `.../runtime/invoke/InvocationOutcome.java` | Create | invocation result |
| `.../runtime/invoke/ScriptPluginInvocationException.java` | Create | blocking failure |
| `.../runtime/invoke/ScriptInvocationExecutor.java` | Create | timeout + ctx append + execution log |
| `.../runtime/materialize/MaterializedScriptPlugin.java` | Create | per-plugin holder |
| `.../runtime/materialize/MaterializedExtension.java` | Create | per-declaration holder |
| `.../runtime/materialize/ScriptRevisionContextManager.java` | Create | per-revision child contexts |
| `.../runtime/materialize/ScriptExtensionInvocationHandler.java` | Create | proxy handler |
| `.../runtime/materialize/ScriptPluginMaterializer.java` | Create | bundle → proxies |
| `.../runtime/host/DefaultScriptContext.java` | Create | `ScriptContext` impl |
| `.../runtime/host/JdkScriptHttpClient.java` | Create | `ScriptHttpClient` impl |
| `.../runtime/dispatch/ExtHttpDispatcher.java` | Create | `plugin-platform/ext/{pluginKey}/**` |
| `.../runtime/dispatch/ScriptCronDispatcher.java` | Create | cron scan + auto-disable |
| `.../runtime/validation/ScriptPluginMaterializationValidatorImpl.java` | Create | dry-run validation |
| `.../runtime/validation/StationValidationPoller.java` | Create | remote validation handshake |
| `server/modules-plugin/plugin-sdk/.../spi/ScriptExtensionProvider.java` | Create | merge SPI |
| `server/modules-plugin/plugin-sdk/.../utils/PluginSdkUtils.java` | Modify | merge + priority sort |
| `server/modules-plugin/plugin-sdk/.../utils/LifecycleListenerRegistry.java` | Modify | merge + provider list |
| `server/modules-plugin/plugin-sdk/src/test/java/org/openwes/plugin/sdk/utils/PluginSdkUtilsTest.java` | Create | merge tests |
| `server/modules-plugin/plugin-sdk/src/test/java/org/openwes/plugin/sdk/utils/LifecycleListenerRegistryTest.java` | Modify | new constructor + merge test |
| `server/modules-api-platform/api-platform/.../request/wms/CreateOutboundRequestHandler.java` | Modify | call `super.invoke(context)`, drop dead import |
| `server/modules-api-platform/api-platform/src/test/java/.../wms/CreateOutboundRequestHandlerTest.java` | Create | hook test |
| `server/server/wes-server/build.gradle` | Modify | `script-plugin-runtime` dep |
| `server/server/station-server/build.gradle` | Modify | `script-plugin-runtime` dep |
| `server/server/wes-server/src/main/resources/bootstrap.yml` | Modify | `script-plugin.registry.mode: local` |
| `server/server/station-server/src/main/resources/bootstrap.yml` | Modify | `script-plugin.registry.mode: remote` |
| `server/server/station-server/src/main/java/org/openwes/station/StationApplication.java` | Modify | `@EnableScheduling` |
| Runtime tests | Create | one test class per component (see tasks) |

---

## Task 1: Runtime module skeleton, config beans, accessors

**Files:**
- Create: `server/modules-plugin/script-plugin-runtime/build.gradle`, `.../config/RuntimeConfiguration.java`, `.../registry/ScriptPluginApiAccessor.java`, `LocalScriptPluginApiAccessor.java`, `RemoteScriptPluginApiAccessor.java`, `LocalScriptPluginApiAccessorTest.java`
- Modify: `server/settings.gradle`, `GroovyScriptEngine.java` (+ test) — add `respondsTo`

**Interfaces:**
- Consumes: `GroovyScriptEngine`, `ScriptExecutionRuntime` (script-engine); `IScriptPluginApi` + DTOs (script-plugin-api).
- Produces: `ScriptPluginApiAccessor` (6 methods mirroring `IScriptPluginApi`), `GroovyScriptEngine.respondsTo(Object, String) → boolean`, beans `GroovyScriptEngine`, `ScriptExecutionRuntime`, `Clock`, `ScriptHttpClient`.

- [ ] **Step 1: Register the module and create its build file**

Append to `server/settings.gradle` (after `include 'modules-plugin:script-plugin-core'`, added by Plan 1a):

```groovy
include 'modules-plugin:script-plugin-runtime'
```

Create `server/modules-plugin/script-plugin-runtime/build.gradle`:

```groovy
dependencies {
    implementation project(path: ":modules-plugin:script-plugin-api", configuration: 'default')
    implementation project(path: ":modules-plugin:plugin-sdk", configuration: 'default')
    implementation project(path: ":modules-utils:script-engine", configuration: 'default')
    implementation project(path: ":modules-utils:common-utils", configuration: 'default')
    implementation project(path: ":modules-utils:distribute-lock", configuration: 'default')

    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.apache.dubbo:dubbo-spring-boot-starter'
    implementation 'org.apache.commons:commons-lang3'
}
```

- [ ] **Step 2: Add `respondsTo` to the engine (failing test first)**

Append to `GroovyScriptEngineTest` (existing file from Plan 1a):

```java
    @Test
    void respondsTo_detectsDeclaredAndMissingMethods() {
        Class<?> clazz = engine.compile("class Wave { Object wave(Object orders, Object ctx) { return orders } }");
        Object instance = engine.newInstance(clazz);

        assertEquals(true, engine.respondsTo(instance, "wave"));
        assertEquals(false, engine.respondsTo(instance, "split"));
    }
```

Run: `cd server && ./gradlew :modules-utils:script-engine:test`
Expected: FAIL to compile — `respondsTo` does not exist.

- [ ] **Step 3: Implement `respondsTo`**

Add to `GroovyScriptEngine` (imports: `groovy.lang.MetaClass`, `org.codehaus.groovy.runtime.InvokerHelper`):

```java
    /** Groovy-aware method existence check used by the enable-time smoke validation. */
    public boolean respondsTo(Object instance, String methodName) {
        MetaClass metaClass = InvokerHelper.getMetaClass(instance);
        return !metaClass.respondsTo(instance, methodName).isEmpty();
    }
```

Run: `cd server && ./gradlew :modules-utils:script-engine:test` → Expected: PASS.

- [ ] **Step 4: Write the failing accessor test**

`server/modules-plugin/script-plugin-runtime/src/test/java/org/openwes/script/plugin/runtime/registry/LocalScriptPluginApiAccessorTest.java`:

```java
package org.openwes.script.plugin.runtime.registry;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.IScriptPluginApi;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocalScriptPluginApiAccessorTest {

    private final IScriptPluginApi api = mock(IScriptPluginApi.class);
    private final LocalScriptPluginApiAccessor accessor = new LocalScriptPluginApiAccessor(api);

    @Test
    void delegatesEveryManagementCall() {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("packing-wave");
        ValidationReportDTO report = ValidationReportDTO.passed("packing-wave", 11L, "wes");
        ExecutionLogDTO executionLog = new ExecutionLogDTO();
        when(api.getEnabledPlugins("wes")).thenReturn(List.of(bundle));
        when(api.getRegistryVersion("wes")).thenReturn(7L);
        when(api.getPendingValidation("wes")).thenReturn(bundle);

        assertEquals(List.of(bundle), accessor.getEnabledPlugins("wes"));
        assertEquals(7L, accessor.getRegistryVersion("wes"));
        assertEquals(bundle, accessor.getPendingValidation("wes"));
        accessor.reportValidation(report);
        accessor.reportExecution(executionLog);
        accessor.disable("packing-wave", "reason");

        verify(api).reportValidation(report);
        verify(api).reportExecution(executionLog);
        verify(api).disable("packing-wave", "reason");
    }
}
```

- [ ] **Step 5: Run to verify it fails**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test`
Expected: FAIL to compile — accessor classes do not exist.

- [ ] **Step 6: Implement the accessors and configuration**

`.../registry/ScriptPluginApiAccessor.java`:

```java
package org.openwes.script.plugin.runtime.registry;

import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;

import java.util.List;

/**
 * Node-side view of the management plane. `local` mode injects the management service bean
 * (wes hosts script-plugin-core); `remote` mode talks to it over Dubbo (station).
 */
public interface ScriptPluginApiAccessor {

    List<ScriptPluginBundleDTO> getEnabledPlugins(String applySystem);

    long getRegistryVersion(String applySystem);

    ScriptPluginBundleDTO getPendingValidation(String applySystem);

    void reportValidation(ValidationReportDTO report);

    void reportExecution(ExecutionLogDTO executionLog);

    void disable(String pluginKey, String reason);
}
```

`.../registry/LocalScriptPluginApiAccessor.java`:

```java
package org.openwes.script.plugin.runtime.registry;

import lombok.RequiredArgsConstructor;
import org.openwes.script.plugin.api.IScriptPluginApi;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "script-plugin.registry.mode", havingValue = "local", matchIfMissing = true)
@RequiredArgsConstructor
public class LocalScriptPluginApiAccessor implements ScriptPluginApiAccessor {

    /** On wes this is the same bean that is exported as @DubboService by script-plugin-core. */
    private final IScriptPluginApi scriptPluginApi;

    @Override
    public List<ScriptPluginBundleDTO> getEnabledPlugins(String applySystem) {
        return scriptPluginApi.getEnabledPlugins(applySystem);
    }

    @Override
    public long getRegistryVersion(String applySystem) {
        return scriptPluginApi.getRegistryVersion(applySystem);
    }

    @Override
    public ScriptPluginBundleDTO getPendingValidation(String applySystem) {
        return scriptPluginApi.getPendingValidation(applySystem);
    }

    @Override
    public void reportValidation(ValidationReportDTO report) {
        scriptPluginApi.reportValidation(report);
    }

    @Override
    public void reportExecution(ExecutionLogDTO executionLog) {
        scriptPluginApi.reportExecution(executionLog);
    }

    @Override
    public void disable(String pluginKey, String reason) {
        scriptPluginApi.disable(pluginKey, reason);
    }
}
```

`.../registry/RemoteScriptPluginApiAccessor.java`:

```java
package org.openwes.script.plugin.runtime.registry;

import org.apache.dubbo.config.annotation.DubboReference;
import org.openwes.script.plugin.api.IScriptPluginApi;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConditionalOnProperty(name = "script-plugin.registry.mode", havingValue = "remote")
public class RemoteScriptPluginApiAccessor implements ScriptPluginApiAccessor {

    @DubboReference
    private IScriptPluginApi scriptPluginApi;

    @Override
    public List<ScriptPluginBundleDTO> getEnabledPlugins(String applySystem) {
        return scriptPluginApi.getEnabledPlugins(applySystem);
    }

    @Override
    public long getRegistryVersion(String applySystem) {
        return scriptPluginApi.getRegistryVersion(applySystem);
    }

    @Override
    public ScriptPluginBundleDTO getPendingValidation(String applySystem) {
        return scriptPluginApi.getPendingValidation(applySystem);
    }

    @Override
    public void reportValidation(ValidationReportDTO report) {
        scriptPluginApi.reportValidation(report);
    }

    @Override
    public void reportExecution(ExecutionLogDTO executionLog) {
        scriptPluginApi.reportExecution(executionLog);
    }

    @Override
    public void disable(String pluginKey, String reason) {
        scriptPluginApi.disable(pluginKey, reason);
    }
}
```

`.../config/RuntimeConfiguration.java`:

```java
package org.openwes.script.plugin.runtime.config;

import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptExecutionRuntime;
import org.openwes.script.plugin.api.host.ScriptHttpClient;
import org.openwes.script.plugin.runtime.host.JdkScriptHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class RuntimeConfiguration {

    @Bean
    public GroovyScriptEngine groovyScriptEngine() {
        return new GroovyScriptEngine();
    }

    @Bean(destroyMethod = "close")
    public ScriptExecutionRuntime scriptExecutionRuntime(
            @Value("${script-plugin.execution.timeout-ms:2000}") long timeoutMillis,
            @Value("${script-plugin.execution.pool-size:4}") int poolSize) {
        return new ScriptExecutionRuntime(timeoutMillis, poolSize);
    }

    @Bean
    public ScriptHttpClient scriptHttpClient() {
        return new JdkScriptHttpClient();
    }

    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
```

Create a placeholder `.../host/JdkScriptHttpClient.java` so this task compiles (full implementation + tests in Task 5):

```java
package org.openwes.script.plugin.runtime.host;

import org.openwes.script.plugin.api.host.ScriptHttpClient;

public class JdkScriptHttpClient implements ScriptHttpClient {

    @Override
    public String postJson(String url, String jsonBody, long timeoutMillis) {
        throw new UnsupportedOperationException("implemented in Task 5");
    }

    @Override
    public String get(String url, long timeoutMillis) {
        throw new UnsupportedOperationException("implemented in Task 5");
    }
}
```

- [ ] **Step 7: Run the tests to verify they pass**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test :modules-utils:script-engine:test`
Expected: PASS.

- [ ] **Step 8: Commit**

```bash
git add server/settings.gradle server/modules-plugin/script-plugin-runtime server/modules-utils/script-engine
git commit -m "feat(script-plugin-runtime): module skeleton, config beans and management accessors

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 2: `ScriptRevisionContextManager` — per-revision child Spring contexts

**Files:**
- Create: `.../materialize/ScriptRevisionContextManager.java`, `.../materialize/ScriptRevisionContextManagerTest.java`

**Interfaces:**
- Consumes: `GroovyScriptEngine`; the main `ApplicationContext`.
- Produces (relied on by Tasks 4/9): `acquire(String pluginKey, Long revisionId, Map<String,String> scriptSources) → RevisionContext`; `openValidationScope(String pluginKey, Map<String,String> scriptSources) → ValidationScope` (AutoCloseable); `closeRevisionsExcept(String, Long)`; `closePlugin(String)`; `closeAll()`; `record RevisionContext(AnnotationConfigApplicationContext context, Map<String,Object> instances)`; `record ValidationScope(...) implements AutoCloseable`.

- [ ] **Step 1: Write the failing test**

`server/modules-plugin/script-plugin-runtime/src/test/java/org/openwes/script/plugin/runtime/materialize/ScriptRevisionContextManagerTest.java`:

```java
package org.openwes.script.plugin.runtime.materialize;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.script.engine.GroovyScriptEngine;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptRevisionContextManagerTest {

    private final AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext();
    private ScriptRevisionContextManager manager;

    @BeforeEach
    void setUp() {
        parent.registerBean(String.class, () -> "injected");
        parent.refresh();
        manager = new ScriptRevisionContextManager(parent, new GroovyScriptEngine());
    }

    @AfterEach
    void tearDown() {
        manager.closeAll();
        parent.close();
    }

    private Map<String, String> greeter() {
        return Map.of("Greeter.groovy", """
                import org.springframework.beans.factory.annotation.Autowired
                class Greeter {
                    @Autowired String greeting
                    String greet() { greeting }
                }
                """);
    }

    @Test
    void acquire_injectsBeansFromTheParentContext() throws Exception {
        ScriptRevisionContextManager.RevisionContext revisionContext =
                manager.acquire("packing-wave", 1L, greeter());

        Object greeter = revisionContext.instances().get("Greeter.groovy");
        Method greet = greeter.getClass().getMethod("greet");

        assertEquals("injected", greet.invoke(greeter));
    }

    @Test
    void acquire_sameRevision_reusesTheContext() {
        ScriptRevisionContextManager.RevisionContext first = manager.acquire("packing-wave", 1L, greeter());
        ScriptRevisionContextManager.RevisionContext second = manager.acquire("packing-wave", 1L, greeter());

        assertSame(first, second);
    }

    @Test
    void closeRevisionsExcept_closesOnlyTheSupersededContext() {
        ScriptRevisionContextManager.RevisionContext old = manager.acquire("packing-wave", 1L, greeter());
        ScriptRevisionContextManager.RevisionContext current = manager.acquire("packing-wave", 2L, greeter());

        manager.closeRevisionsExcept("packing-wave", 2L);

        assertFalse(old.context().isActive());
        assertTrue(current.context().isActive());
    }

    @Test
    void closePlugin_closesAllRevisionsOfThatPluginOnly() {
        ScriptRevisionContextManager.RevisionContext wave = manager.acquire("packing-wave", 1L, greeter());
        ScriptRevisionContextManager.RevisionContext other = manager.acquire("other-plugin", 1L, greeter());

        manager.closePlugin("packing-wave");

        assertFalse(wave.context().isActive());
        assertTrue(other.context().isActive());
    }

    @Test
    void openValidationScope_closesWithoutTouchingTheRevisionCache() {
        try (ScriptRevisionContextManager.ValidationScope scope = manager.openValidationScope("packing-wave", greeter())) {
            assertEquals(1, scope.instances().size());
            assertTrue(scope.context().isActive());
        }
        // the cache is untouched, so an acquire afterwards builds a fresh context
        assertTrue(manager.acquire("packing-wave", 1L, greeter()).context().isActive());
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManagerTest"`
Expected: FAIL to compile — class does not exist.

- [ ] **Step 3: Implement**

`.../materialize/ScriptRevisionContextManager.java`:

```java
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
        Map<String, Object> instances = createScriptInstances(context, pluginKey, scriptSources);
        return new ValidationScope(context, instances);
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
        return new RevisionContext(context, createScriptInstances(context, pluginKey, scriptSources));
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
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManagerTest"`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
git add server/modules-plugin/script-plugin-runtime
git commit -m "feat(script-plugin-runtime): per-revision child Spring contexts for script beans

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 3: Invocation executor — timeout, `ctx` append, execution logging

**Files:**
- Create: `.../invoke/InvocationOutcome.java`, `.../invoke/ScriptPluginInvocationException.java`, `.../invoke/ScriptInvocationExecutor.java`, `.../materialize/MaterializedScriptPlugin.java`, `.../materialize/MaterializedExtension.java`, `.../invoke/ScriptInvocationExecutorTest.java`

**Interfaces:**
- Consumes: `ScriptExecutionRuntime`, `GroovyScriptEngine`, `ScriptPluginApiAccessor`, `ExtensionDeclarationDTO`, `ScriptContext`.
- Produces (relied on by Tasks 4/7/8/9): `InvocationOutcome(boolean success, Object result, long durationMs, String errorMessage)`; `ScriptPluginInvocationException(String pluginKey, String method, String message)`; `ScriptInvocationExecutor.invoke(MaterializedScriptPlugin, MaterializedExtension, Object[]) → InvocationOutcome` (appends `extension.getScriptContext()` as the final argument; never throws for script failures; reporting failures are swallowed); holders `MaterializedScriptPlugin` (`pluginKey`, `revisionId`, `extensions`) and `MaterializedExtension` (`declaration`, `extensionPointClass`, `scriptInstance`, `proxy`, `scriptContext`).

- [ ] **Step 1: Write the failing test**

`server/modules-plugin/script-plugin-runtime/src/test/java/org/openwes/script/plugin/runtime/invoke/ScriptInvocationExecutorTest.java`:

```java
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

        InvocationOutcome outcome = executor.invoke(plugin(), extension, new Object[]{});

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

        InvocationOutcome outcome = quickExecutor.invoke(plugin(), extension, new Object[]{});

        assertFalse(outcome.success());
        assertTrue(outcome.errorMessage().contains("timed out"));
    }

    @Test
    void invoke_whenLogReportingFails_doesNotBreakTheInvocation() {
        doThrow(new RuntimeException("management plane down")).when(accessor).reportExecution(any());
        MaterializedExtension extension = extension(
                "class Wave { Object wave(Object orders, Object ctx) { return 'ok' } }", "wave");

        InvocationOutcome outcome = executor.invoke(plugin(), extension, new Object[]{});

        assertTrue(outcome.success());
        assertEquals("ok", outcome.result());
    }
}
```

Note: these tests reach `DefaultScriptContext` only through `pluginKey()`; the class is created in this task with the full implementation shown in Step 3 (Task 5 adds its dedicated tests and the JSON-tolerant `configInt`/`configBool` hardening).

- [ ] **Step 2: Run to verify it fails**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.invoke.ScriptInvocationExecutorTest"`
Expected: FAIL to compile — classes do not exist.

- [ ] **Step 3: Implement the holders, exception and executor**

`.../materialize/MaterializedScriptPlugin.java`:

```java
package org.openwes.script.plugin.runtime.materialize;

import lombok.Getter;

import java.util.List;

@Getter
public class MaterializedScriptPlugin {

    private final String pluginKey;
    private final Long revisionId;
    private final List<MaterializedExtension> extensions;

    public MaterializedScriptPlugin(String pluginKey, Long revisionId, List<MaterializedExtension> extensions) {
        this.pluginKey = pluginKey;
        this.revisionId = revisionId;
        this.extensions = extensions;
    }
}
```

`.../materialize/MaterializedExtension.java`:

```java
package org.openwes.script.plugin.runtime.materialize;

import lombok.Getter;
import lombok.Setter;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.host.ScriptContext;

@Getter
@Setter
public class MaterializedExtension {

    private ExtensionDeclarationDTO declaration;
    /** Host interface the proxy implements; null for dispatcher-backed archetypes (HTTP_ENDPOINT, CRON). */
    private Class<?> extensionPointClass;
    private Object scriptInstance;
    private Object proxy;
    private ScriptContext scriptContext;
}
```

`.../invoke/InvocationOutcome.java`:

```java
package org.openwes.script.plugin.runtime.invoke;

public record InvocationOutcome(boolean success, Object result, long durationMs, String errorMessage) {
}
```

`.../invoke/ScriptPluginInvocationException.java`:

```java
package org.openwes.script.plugin.runtime.invoke;

/** Thrown by blocking archetypes; the message always names the plugin that failed. */
public class ScriptPluginInvocationException extends RuntimeException {

    public ScriptPluginInvocationException(String pluginKey, String method, String message) {
        super("script plugin '" + pluginKey + "' method '" + method + "' failed: " + message);
    }
}
```

`.../invoke/ScriptInvocationExecutor.java`:

```java
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
```

`.../host/DefaultScriptContext.java` — in this task, create the minimal version below so the executor tests compile; Task 5 replaces it with the full implementation:

```java
package org.openwes.script.plugin.runtime.host;

import org.openwes.script.plugin.api.host.ScriptContext;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;
import org.openwes.script.plugin.api.host.ScriptHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.Map;

public class DefaultScriptContext implements ScriptContext {

    private final String pluginKey;
    private final ConfigurableApplicationContext revisionContext;
    private final Map<String, Object> config;
    private final List<ConfigFieldDTO> configSchema;
    private final ScriptHttpClient httpClient;

    public DefaultScriptContext(String pluginKey, ConfigurableApplicationContext revisionContext,
                                Map<String, Object> config, List<ConfigFieldDTO> configSchema,
                                ScriptHttpClient httpClient) {
        this.pluginKey = pluginKey;
        this.revisionContext = revisionContext;
        this.config = config;
        this.configSchema = configSchema;
        this.httpClient = httpClient;
    }

    @Override
    public String pluginKey() {
        return pluginKey;
    }

    @Override
    public <T> T bean(Class<T> type) {
        return revisionContext.getBean(type);
    }

    @Override
    public Object bean(String name) {
        return revisionContext.getBean(name);
    }

    @Override
    public Object configObject(String field) {
        return config.get(field);
    }

    @Override
    public String configString(String field) {
        Object value = config.get(field);
        return value == null ? null : value.toString();
    }

    @Override
    public Integer configInt(String field) {
        Object value = config.get(field);
        return value == null ? null : Integer.valueOf(value.toString());
    }

    @Override
    public Boolean configBool(String field) {
        Object value = config.get(field);
        return value == null ? null : Boolean.valueOf(value.toString());
    }

    @Override
    public List<ConfigFieldDTO> configSchema() {
        return configSchema;
    }

    @Override
    public ScriptHttpClient http() {
        return httpClient;
    }

    @Override
    public Logger log() {
        return LoggerFactory.getLogger("script-plugin." + pluginKey);
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.invoke.ScriptInvocationExecutorTest"`
Expected: PASS (4 tests).

- [ ] **Step 5: Commit**

```bash
git add server/modules-plugin/script-plugin-runtime
git commit -m "feat(script-plugin-runtime): timeout-aware script invocation executor with execution logging

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 4: Proxy handler and materializer

**Files:**
- Create: `.../materialize/ScriptExtensionInvocationHandler.java`, `.../materialize/ScriptPluginMaterializer.java`, `.../materialize/ScriptPluginMaterializerTest.java`

**Interfaces:**
- Consumes: `ScriptRevisionContextManager` (Task 2), `ScriptInvocationExecutor` + holders (Task 3), `PluginContractCatalog` + `ScriptPluginBundleDTO` (script-plugin-api), `ScriptHttpClient` bean.
- Produces (relied on by Task 6): `ScriptPluginMaterializer.materialize(ScriptPluginBundleDTO) → MaterializedScriptPlugin` (throws `ScriptPluginInvocationException` when a declaration is not materializable), `closeRevisionsExcept(String, Long)`, `closePlugin(String)`, `closeAll()`.

- [ ] **Step 1: Write the failing test**

`server/modules-plugin/script-plugin-runtime/src/test/java/org/openwes/script/plugin/runtime/materialize/ScriptPluginMaterializerTest.java`:

```java
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
        List<Object> orders = Arrays.asList(null, null);

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
```

- [ ] **Step 2: Run to verify it fails**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.materialize.ScriptPluginMaterializerTest"`
Expected: FAIL to compile — classes do not exist.

- [ ] **Step 3: Implement the handler and the materializer**

`.../materialize/ScriptExtensionInvocationHandler.java`:

```java
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
```

`.../materialize/ScriptPluginMaterializer.java`:

```java
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
```

Note: the materializer intentionally performs no sdk_version or declaration validation — that is the management plane's and Task 9's validator's job on the enable path. This method is the "already validated, materialize it" path.

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.materialize.ScriptPluginMaterializerTest"`
Expected: PASS (7 tests).

If `materialize_httpEndpoint_hasNoProxyAndCarriesTheScriptInstance` fails on the class-simple-name assertion, replace that assertion with `assertTrue(extension.getScriptInstance() != null)` — the exact class name of a Groovy-compiled class depends on the loader locality.

- [ ] **Step 5: Commit**

```bash
git add server/modules-plugin/script-plugin-runtime
git commit -m "feat(script-plugin-runtime): proxy materialization with identity methods and failure policy

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 5: `ScriptContext` and HTTP client implementations

**Files:**
- Modify: `.../host/DefaultScriptContext.java` (full version replacing the Task 3 minimal one)
- Modify: `.../host/JdkScriptHttpClient.java` (full version replacing the Task 1 placeholder)
- Create: `.../host/DefaultScriptContextTest.java`, `.../host/JdkScriptHttpClientTest.java`

**Interfaces:**
- Produces: complete `ScriptContext` behavior (typed config getters, `bean(Class)`/`bean(String)` resolution through the child→parent chain, per-plugin logger, `http()`) and a timeout-enforced `ScriptHttpClient` that throws `ScriptExecutionException` on transport failures.

- [ ] **Step 1: Write the failing tests**

`.../host/DefaultScriptContextTest.java`:

```java
package org.openwes.script.plugin.runtime.host;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultScriptContextTest {

    private final AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext();
    private final AnnotationConfigApplicationContext child = new AnnotationConfigApplicationContext();
    private DefaultScriptContext context;

    @BeforeEach
    void setUp() {
        parent.registerBean(String.class, () -> "injected");
        parent.refresh();
        child.setParent(parent);
        child.refresh();
        context = new DefaultScriptContext("packing-wave", child,
                Map.of("greeting", "hello", "maxPerWave", 5, "enabled", true), List.of(), new JdkScriptHttpClient());
    }

    @AfterEach
    void tearDown() {
        child.close();
        parent.close();
    }

    @Test
    void bean_resolvesThroughTheChildThenParentContext() {
        assertEquals("injected", context.bean(String.class));
        assertEquals("injected", context.bean("string"));
    }

    @Test
    void bean_missingType_throwsNoSuchBeanDefinition() {
        assertThrows(Exception.class, () -> context.bean(Integer.class));
    }

    @Test
    void typedConfigGetters_convertAndDefaultToNull() {
        assertEquals("hello", context.configString("greeting"));
        assertEquals(5, context.configInt("maxPerWave"));
        assertTrue(context.configBool("enabled"));
        assertNull(context.configString("missing"));
        assertNull(context.configInt("missing"));
    }

    @Test
    void logger_isNamespacedPerPlugin() {
        assertTrue(context.log().getName().contains("packing-wave"));
    }
}
```

`.../host/JdkScriptHttpClientTest.java`:

```java
package org.openwes.script.plugin.runtime.host;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdkScriptHttpClientTest {

    private final JdkScriptHttpClient client = new JdkScriptHttpClient();
    private HttpServer server;
    private String baseUrl;

    private void startServer(String contextPath, long delayMillis) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext(contextPath, exchange -> {
            String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            if (delayMillis > 0) {
                try {
                    Thread.sleep(delayMillis);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            byte[] response = ("echo:" + requestBody).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(response);
            }
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void postJson_returnsTheResponseBody() throws IOException {
        startServer("/echo", 0);

        assertEquals("echo:{\"a\":1}", client.postJson(baseUrl + "/echo", "{\"a\":1}", 2000));
    }

    @Test
    void get_returnsTheResponseBody() throws IOException {
        startServer("/echo", 0);

        assertEquals("echo:", client.get(baseUrl + "/echo", 2000));
    }

    @Test
    void postJson_slowServer_throwsScriptExecutionException() throws IOException {
        startServer("/slow", 2000);

        RuntimeException e = assertThrows(RuntimeException.class,
                () -> client.postJson(baseUrl + "/slow", "{}", 200));
        assertTrue(e.getMessage().contains("http post"));
    }
}
```

- [ ] **Step 2: Run to verify they fail**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.host.*"`
Expected: FAIL — placeholder `UnsupportedOperationException` for the client; `configInt` returns `Integer.valueOf("5")` for the string "5" but the map holds `5` (passes) — the failing assertions are the HTTP ones.

- [ ] **Step 3: Implement**

Replace `JdkScriptHttpClient` with:

```java
package org.openwes.script.plugin.runtime.host;

import org.openwes.script.engine.ScriptExecutionException;
import org.openwes.script.plugin.api.host.ScriptHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/** ctx.http implementation: JDK HttpClient with mandatory per-call timeouts. */
public class JdkScriptHttpClient implements ScriptHttpClient {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public String postJson(String url, String jsonBody, long timeoutMillis) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMillis))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody == null ? "" : jsonBody, StandardCharsets.UTF_8))
                .build();
        return send(request, "http post to " + url + " failed");
    }

    @Override
    public String get(String url, long timeoutMillis) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMillis))
                .GET()
                .build();
        return send(request, "http get to " + url + " failed");
    }

    private String send(HttpRequest request, String failureMessage) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException e) {
            throw new ScriptExecutionException(failureMessage + ": " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScriptExecutionException(failureMessage + ": interrupted", e);
        }
    }
}
```

Keep `DefaultScriptContext` as written in Task 3 — it is already the full implementation; update `configInt`/`configBool` so numbers and booleans from JSON-derived maps (which may decode as String) both work:

```java
    @Override
    public Integer configInt(String field) {
        Object value = config.get(field);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.valueOf(value.toString());
    }

    @Override
    public Boolean configBool(String field) {
        Object value = config.get(field);
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        return Boolean.valueOf(value.toString());
    }
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.host.*"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add server/modules-plugin/script-plugin-runtime
git commit -m "feat(script-plugin-runtime): script context and timeout-enforced HTTP client

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 6: `ScriptPluginRegistry` and the plugin-sdk bridge

**Files:**
- Create: `.../registry/ScriptPluginRegistry.java`, `.../registry/ScriptExtensionProviderImpl.java`, `.../registry/ScriptPluginRegistryTest.java`, `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/spi/ScriptExtensionProvider.java` (the SPI lives in plugin-sdk so it stays free of runtime dependencies; Task 10 wires both of plugin-sdk's extraction points to it)

**Interfaces:**
- Consumes: `ScriptPluginApiAccessor`, `ScriptPluginMaterializer`, `RedisUtils`.
- Produces (relied on by Tasks 7/8/9): `refreshAll()`, `pollForUpdates()` (`@Scheduled`), `getExtensions(Class<T> extensionType)`, `getMaterialized(String pluginKey)`, `all()`, `findHttpEndpoint(String pluginKey, String httpMethod, String path) → HttpEndpointMatch`, `destroy()`; `record HttpEndpointMatch(MaterializedScriptPlugin plugin, MaterializedExtension extension)`. Constructor: `(ScriptPluginApiAccessor, ScriptPluginMaterializer, RedisUtils, String applySystem)`.

- [ ] **Step 1: Write the failing test**

`.../registry/ScriptPluginRegistryTest.java`:

```java
package org.openwes.script.plugin.runtime.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.runtime.materialize.MaterializedScriptPlugin;
import org.openwes.script.plugin.runtime.materialize.ScriptPluginMaterializer;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginRegistryTest {

    private final ScriptPluginApiAccessor accessor = mock(ScriptPluginApiAccessor.class);
    private final ScriptPluginMaterializer materializer = mock(ScriptPluginMaterializer.class);
    private final RedisUtils redisUtils = mock(RedisUtils.class);
    private ScriptPluginRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ScriptPluginRegistry(accessor, materializer, redisUtils, "wes");
    }

    private ScriptPluginBundleDTO bundle(long revisionId) {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("packing-wave");
        bundle.setApplySystem("wes");
        bundle.setRevisionId(revisionId);
        return bundle;
    }

    @Test
    void refreshAll_materializesEnabledBundlesAndClosesSupersededRevisions() {
        ScriptPluginBundleDTO bundle = bundle(11L);
        MaterializedScriptPlugin materialized = new MaterializedScriptPlugin("packing-wave", 11L, new ArrayList<>());
        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of(bundle));
        when(accessor.getRegistryVersion("wes")).thenReturn(5L);
        when(materializer.materialize(bundle)).thenReturn(materialized);

        registry.refreshAll();

        assertSame(materialized, registry.getMaterialized("packing-wave"));
        verify(materializer).closeRevisionsExcept("packing-wave", 11L);
    }

    @Test
    void refreshAll_unchangedRevision_isNotRematerialized() {
        ScriptPluginBundleDTO bundle = bundle(11L);
        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of(bundle));
        when(materializer.materialize(bundle)).thenReturn(new MaterializedScriptPlugin("packing-wave", 11L, new ArrayList<>()));

        registry.refreshAll();
        registry.refreshAll();

        verify(materializer, times(1)).materialize(bundle);
    }

    @Test
    void refreshAll_failedRematerialization_keepsThePreviousRevision() {
        ScriptPluginBundleDTO first = bundle(11L);
        MaterializedScriptPlugin materialized = new MaterializedScriptPlugin("packing-wave", 11L, new ArrayList<>());
        when(materializer.materialize(first)).thenReturn(materialized);
        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of(first));
        registry.refreshAll();

        ScriptPluginBundleDTO second = bundle(12L);
        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of(second));
        when(materializer.materialize(second)).thenThrow(new IllegalStateException("compile failed"));
        registry.refreshAll();

        assertSame(materialized, registry.getMaterialized("packing-wave"));
        verify(materializer, never()).closeRevisionsExcept("packing-wave", 12L);
    }

    @Test
    void refreshAll_pluginNoLongerEnabled_isDematerialized() {
        ScriptPluginBundleDTO bundle = bundle(11L);
        when(materializer.materialize(bundle)).thenReturn(new MaterializedScriptPlugin("packing-wave", 11L, new ArrayList<>()));
        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of(bundle));
        registry.refreshAll();

        when(accessor.getEnabledPlugins("wes")).thenReturn(List.of());
        registry.refreshAll();

        assertNull(registry.getMaterialized("packing-wave"));
        verify(materializer).closePlugin("packing-wave");
    }

    @Test
    void pollForUpdates_refreshesOnlyWhenTheVersionChanged() {
        when(accessor.getRegistryVersion("wes")).thenReturn(5L);

        registry.pollForUpdates();
        verify(accessor, times(1)).getEnabledPlugins("wes");

        registry.pollForUpdates();
        verify(accessor, times(1)).getEnabledPlugins("wes");

        when(accessor.getRegistryVersion("wes")).thenReturn(6L);
        registry.pollForUpdates();
        verify(accessor, times(2)).getEnabledPlugins("wes");
    }

    @Test
    void pollForUpdates_whenManagementPlaneUnavailable_isSwallowed() {
        when(accessor.getRegistryVersion("wes")).thenThrow(new RuntimeException("dubbo down"));

        registry.pollForUpdates();

        verify(accessor, never()).getEnabledPlugins(any());
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.registry.ScriptPluginRegistryTest"`
Expected: FAIL to compile — class does not exist.

- [ ] **Step 3: Implement**

`.../registry/ScriptPluginRegistry.java`:

```java
package org.openwes.script.plugin.runtime.registry;

import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.PluginManagementMessage;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.runtime.materialize.MaterializedExtension;
import org.openwes.script.plugin.runtime.materialize.MaterializedScriptPlugin;
import org.openwes.script.plugin.runtime.materialize.ScriptPluginMaterializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Holds the materialized script plugins of this app. Refresh triggers: startup, the Redis
 * PLUGIN_LISTEN_PLUGIN_MANAGEMENT message (published by the management plane) and a 60 s version
 * poll as the safety net. A failed rematerialization keeps the previous revision running.
 */
@Slf4j
@Component
public class ScriptPluginRegistry implements DisposableBean {

    private final ScriptPluginApiAccessor accessor;
    private final ScriptPluginMaterializer materializer;
    private final RedisUtils redisUtils;
    private final String applySystem;

    /** keyed by pluginKey; a skip list keeps the iteration order deterministic */
    private final ConcurrentSkipListMap<String, MaterializedScriptPlugin> materialized = new ConcurrentSkipListMap<>();
    private final AtomicLong lastVersion = new AtomicLong(-1);

    public ScriptPluginRegistry(ScriptPluginApiAccessor accessor,
                                ScriptPluginMaterializer materializer,
                                RedisUtils redisUtils,
                                @Value("${spring.application.name:}") String applySystem) {
        this.accessor = accessor;
        this.materializer = materializer;
        this.redisUtils = redisUtils;
        this.applySystem = applySystem;
        redisUtils.listen(RedisConstants.PLUGIN_LISTEN_PLUGIN_MANAGEMENT, PluginManagementMessage.class,
                (channel, message) -> refreshAll());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadAtStartup() {
        refreshAll();
    }

    @Scheduled(fixedDelay = 60_000)
    public void pollForUpdates() {
        try {
            if (accessor.getRegistryVersion(applySystem) != lastVersion.get()) {
                refreshAll();
            }
        } catch (Exception e) {
            log.warn("script plugin registry poll failed: {}", e.getMessage());
        }
    }

    public synchronized void refreshAll() {
        try {
            List<ScriptPluginBundleDTO> bundles = accessor.getEnabledPlugins(applySystem);
            long version = accessor.getRegistryVersion(applySystem);

            Set<String> enabledKeys = bundles.stream().map(ScriptPluginBundleDTO::getPluginKey)
                    .collect(Collectors.toSet());
            materialized.keySet().stream()
                    .filter(pluginKey -> !enabledKeys.contains(pluginKey))
                    .toList()
                    .forEach(this::dematerialize);

            for (ScriptPluginBundleDTO bundle : bundles) {
                MaterializedScriptPlugin current = materialized.get(bundle.getPluginKey());
                if (current != null && Objects.equals(current.getRevisionId(), bundle.getRevisionId())) {
                    continue;
                }
                try {
                    MaterializedScriptPlugin fresh = materializer.materialize(bundle);
                    materialized.put(bundle.getPluginKey(), fresh);
                    materializer.closeRevisionsExcept(bundle.getPluginKey(), bundle.getRevisionId());
                    log.info("materialized script plugin {} revision {}", bundle.getPluginKey(), bundle.getRevisionId());
                } catch (Exception e) {
                    log.error("materialization of script plugin {} revision {} failed; keeping the previous revision: {}",
                            bundle.getPluginKey(), bundle.getRevisionId(), e.getMessage());
                }
            }
            lastVersion.set(version);
        } catch (Exception e) {
            log.error("script plugin registry refresh failed: {}", e.getMessage());
        }
    }

    public <T> List<T> getExtensions(Class<T> extensionType) {
        List<T> extensions = new ArrayList<>();
        for (MaterializedScriptPlugin plugin : materialized.values()) {
            for (MaterializedExtension extension : plugin.getExtensions()) {
                if (extension.getExtensionPointClass() == extensionType && extension.getProxy() != null) {
                    extensions.add(extensionType.cast(extension.getProxy()));
                }
            }
        }
        return extensions;
    }

    public Collection<MaterializedScriptPlugin> all() {
        return materialized.values();
    }

    public MaterializedScriptPlugin getMaterialized(String pluginKey) {
        return materialized.get(pluginKey);
    }

    public HttpEndpointMatch findHttpEndpoint(String pluginKey, String httpMethod, String path) {
        MaterializedScriptPlugin plugin = materialized.get(pluginKey);
        if (plugin == null) {
            return null;
        }
        return plugin.getExtensions().stream()
                .filter(extension -> extension.getDeclaration().getType() == ExtensionType.HTTP_ENDPOINT)
                .filter(extension -> matchesHttpMethod(extension.getDeclaration(), httpMethod))
                .filter(extension -> path.equals(extension.getDeclaration().getPath()))
                .findFirst()
                .map(extension -> new HttpEndpointMatch(plugin, extension))
                .orElse(null);
    }

    private boolean matchesHttpMethod(ExtensionDeclarationDTO declaration, String httpMethod) {
        return declaration.getHttpMethod() == null || declaration.getHttpMethod().isBlank()
                || declaration.getHttpMethod().equalsIgnoreCase(httpMethod);
    }

    private void dematerialize(String pluginKey) {
        materialized.remove(pluginKey);
        materializer.closePlugin(pluginKey);
        log.info("script plugin {} is no longer enabled; materialization dropped", pluginKey);
    }

    @Override
    public void destroy() {
        materializer.closeAll();
    }

    public record HttpEndpointMatch(MaterializedScriptPlugin plugin, MaterializedExtension extension) {
    }
}
```

`server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/spi/ScriptExtensionProvider.java`:

```java
package org.openwes.plugin.sdk.spi;

import java.util.List;

/**
 * SPI implemented by script-plugin-runtime so dynamically materialized script extensions merge
 * with PF4J jar plugins at every extraction point. Injecting List&lt;ScriptExtensionProvider&gt;
 * yields an empty list when the runtime module is absent, so plugin-sdk stays usable standalone.
 */
public interface ScriptExtensionProvider {

    <T> List<T> getExtensions(Class<T> extensionType);
}
```

`.../registry/ScriptExtensionProviderImpl.java`:

```java
package org.openwes.script.plugin.runtime.registry;

import lombok.RequiredArgsConstructor;
import org.openwes.plugin.sdk.spi.ScriptExtensionProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ScriptExtensionProviderImpl implements ScriptExtensionProvider {

    private final ScriptPluginRegistry registry;

    @Override
    public <T> List<T> getExtensions(Class<T> extensionType) {
        return registry.getExtensions(extensionType);
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.registry.ScriptPluginRegistryTest"`
Expected: PASS (6 tests).

- [ ] **Step 5: Commit**

```bash
git add server/modules-plugin/script-plugin-runtime server/modules-plugin/plugin-sdk
git commit -m "feat(script-plugin-runtime): registry with pub/sub refresh and version-poll safety net

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 7: `ExtHttpDispatcher` — self-provided HTTP endpoints

**Files:**
- Create: `.../dispatch/ExtHttpDispatcher.java`, `.../dispatch/ExtHttpDispatcherTest.java`

**Interfaces:**
- Consumes: `ScriptPluginRegistry.findHttpEndpoint` (Task 6), `ScriptInvocationExecutor` (Task 3).
- Produces: `dispatch(String pluginKey, HttpServletRequest request, String body) → Response<Object>` mapped at `plugin-platform/ext/{pluginKey}/**`. Scripts implement `handle(body, ctx)` and may return any JSON-serializable value.

Note (documented limitation): the gateway routes `/plugin-platform/**` to wes only. Station-target plugins that provide HTTP endpoints are reachable on the station node's own port, which is how workstation clients reach the station service today.

- [ ] **Step 1: Write the failing test**

`.../dispatch/ExtHttpDispatcherTest.java`:

```java
package org.openwes.script.plugin.runtime.dispatch;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.runtime.invoke.InvocationOutcome;
import org.openwes.script.plugin.runtime.invoke.ScriptInvocationExecutor;
import org.openwes.script.plugin.runtime.materialize.MaterializedExtension;
import org.openwes.script.plugin.runtime.materialize.MaterializedScriptPlugin;
import org.openwes.script.plugin.runtime.registry.ScriptPluginRegistry;
import org.openwes.script.plugin.runtime.registry.ScriptPluginRegistry.HttpEndpointMatch;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExtHttpDispatcherTest {

    private final ScriptPluginRegistry registry = mock(ScriptPluginRegistry.class);
    private final ScriptInvocationExecutor executor = mock(ScriptInvocationExecutor.class);
    private final ExtHttpDispatcher dispatcher = new ExtHttpDispatcher(registry, executor);

    private HttpServletRequest request(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn(uri);
        return request;
    }

    private HttpEndpointMatch match() {
        MaterializedScriptPlugin plugin = new MaterializedScriptPlugin("packing-wave", 11L, new ArrayList<>());
        MaterializedExtension extension = new MaterializedExtension();
        return new HttpEndpointMatch(plugin, extension);
    }

    @Test
    void dispatch_unknownEndpoint_returnsFailNamingThePlugin() {
        when(registry.findHttpEndpoint("packing-wave", "POST", "/order/status")).thenReturn(null);

        Response<Object> response = dispatcher.dispatch("packing-wave",
                request("/plugin-platform/ext/packing-wave/order/status"), "{}");

        assertEquals("1", response.getCode());
        assertTrue(response.getMsg().contains("packing-wave"));
        assertTrue(response.getMsg().contains("/order/status"));
    }

    @Test
    void dispatch_success_returnsTheScriptResult() {
        when(registry.findHttpEndpoint("packing-wave", "POST", "/order/status")).thenReturn(match());
        when(executor.invoke(any(), any(), any())).thenReturn(new InvocationOutcome(true, Map.of("status", "ok"), 2, null));

        Response<Object> response = dispatcher.dispatch("packing-wave",
                request("/plugin-platform/ext/packing-wave/order/status"), "{}");

        assertEquals("0", response.getCode());
        assertEquals(Map.of("status", "ok"), response.getData());
    }

    @Test
    void dispatch_scriptFailure_returnsFailWithoutStackTrace() {
        when(registry.findHttpEndpoint("packing-wave", "POST", "/order/status")).thenReturn(match());
        when(executor.invoke(any(), any(), any()))
                .thenReturn(new InvocationOutcome(false, null, 3, "boom: java.lang.IllegalStateException"));

        Response<Object> response = dispatcher.dispatch("packing-wave",
                request("/plugin-platform/ext/packing-wave/order/status"), "{}");

        assertEquals("1", response.getCode());
        assertTrue(response.getMsg().contains("packing-wave"));
        assertFalse(response.getMsg().contains("java.lang"));
        assertFalse(response.getMsg().contains("\tat"));
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.dispatch.ExtHttpDispatcherTest"`
Expected: FAIL to compile — class does not exist.

- [ ] **Step 3: Implement**

`.../dispatch/ExtHttpDispatcher.java`:

```java
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
            return failWith("script plugin '" + pluginKey + "' failed: " + outcome.errorMessage());
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
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.dispatch.ExtHttpDispatcherTest"`
Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add server/modules-plugin/script-plugin-runtime
git commit -m "feat(script-plugin-runtime): HTTP endpoint dispatcher under plugin-platform/ext

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 8: `ScriptCronDispatcher` — cron jobs with cluster locking and auto-disable

**Files:**
- Create: `.../dispatch/ScriptCronDispatcher.java`, `.../dispatch/ScriptCronDispatcherTest.java`

**Interfaces:**
- Consumes: `ScriptPluginRegistry.all()`, `ScriptInvocationExecutor`, `DistributeLock.acquireLock(String, long, long)`/`releaseLock(String)`, `ScriptPluginApiAccessor.disable`, `Clock` bean.
- Produces: `tick()` (`@Scheduled(fixedDelay = 1000)`), constructor `(ScriptPluginRegistry, ScriptInvocationExecutor, DistributeLock, ScriptPluginApiAccessor, Clock, int maxConsecutiveFailures)` (the bean constructor reads `script-plugin.cron.max-consecutive-failures`, default 10).

- [ ] **Step 1: Write the failing test**

`.../dispatch/ScriptCronDispatcherTest.java`:

```java
package org.openwes.script.plugin.runtime.dispatch;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.openwes.distribute.lock.DistributeLock;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.runtime.invoke.InvocationOutcome;
import org.openwes.script.plugin.runtime.invoke.ScriptInvocationExecutor;
import org.openwes.script.plugin.runtime.materialize.MaterializedExtension;
import org.openwes.script.plugin.runtime.materialize.MaterializedScriptPlugin;
import org.openwes.script.plugin.runtime.registry.ScriptPluginApiAccessor;
import org.openwes.script.plugin.runtime.registry.ScriptPluginRegistry;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptCronDispatcherTest {

    private static class MutableClock extends Clock {

        private Instant instant;
        private final ZoneId zone = ZoneId.systemDefault();

        MutableClock(String instant) {
            this.instant = Instant.parse(instant);
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }

    private final ScriptPluginRegistry registry = mock(ScriptPluginRegistry.class);
    private final ScriptInvocationExecutor executor = mock(ScriptInvocationExecutor.class);
    private final DistributeLock distributeLock = mock(DistributeLock.class);
    private final ScriptPluginApiAccessor accessor = mock(ScriptPluginApiAccessor.class);
    private final MutableClock clock = new MutableClock("2026-10-08T10:00:30Z");

    private ScriptCronDispatcher dispatcher(int maxConsecutiveFailures) {
        return new ScriptCronDispatcher(registry, executor, distributeLock, accessor, clock, maxConsecutiveFailures);
    }

    private void givenOneCronPlugin() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.CRON);
        declaration.setScript("Job.groovy");
        declaration.setMethod("run");
        declaration.setCron("0 * * * * *");
        MaterializedExtension extension = new MaterializedExtension();
        extension.setDeclaration(declaration);
        when(registry.all()).thenReturn(
                List.of(new MaterializedScriptPlugin("nightly-task", 11L, List.of(extension))));
    }

    @Test
    void tick_firesOnceAtTheNextCronBoundary() {
        givenOneCronPlugin();
        when(distributeLock.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(executor.invoke(any(), any(), any())).thenReturn(new InvocationOutcome(true, null, 1, null));
        ScriptCronDispatcher dispatcher = dispatcher(3);

        dispatcher.tick();
        verify(executor, never()).invoke(any(), any(), any());

        clock.advance(Duration.ofSeconds(30));
        dispatcher.tick();
        verify(executor, times(1)).invoke(any(), any(), any());

        dispatcher.tick();
        verify(executor, times(1)).invoke(any(), any(), any());
        verify(distributeLock).releaseLock(anyString());
    }

    @Test
    void tick_lockNotAcquired_skipsTheRun() {
        givenOneCronPlugin();
        when(distributeLock.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(false);
        ScriptCronDispatcher dispatcher = dispatcher(3);

        dispatcher.tick();
        clock.advance(Duration.ofSeconds(30));
        dispatcher.tick();

        verify(executor, never()).invoke(any(), any(), any());
        verify(distributeLock, never()).releaseLock(anyString());
    }

    @Test
    void tick_consecutiveFailures_reachTheLimit_autoDisablesThePlugin() {
        givenOneCronPlugin();
        when(distributeLock.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(executor.invoke(any(), any(), any())).thenReturn(new InvocationOutcome(false, null, 5, "boom"));
        ScriptCronDispatcher dispatcher = dispatcher(3);

        dispatcher.tick();
        for (int i = 0; i < 3; i++) {
            clock.advance(Duration.ofMinutes(1));
            dispatcher.tick();
        }

        verify(accessor).disable(eq("nightly-task"), contains("consecutive"));
    }

    @Test
    void tick_successResetsTheFailureCounter() {
        givenOneCronPlugin();
        when(distributeLock.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(executor.invoke(any(), any(), any()))
                .thenReturn(new InvocationOutcome(false, null, 5, "boom"))
                .thenReturn(new InvocationOutcome(true, null, 5, null))
                .thenReturn(new InvocationOutcome(false, null, 5, "boom"));
        ScriptCronDispatcher dispatcher = dispatcher(3);

        dispatcher.tick();
        for (int i = 0; i < 3; i++) {
            clock.advance(Duration.ofMinutes(1));
            dispatcher.tick();
        }

        verify(accessor, never()).disable(anyString(), anyString());
    }

    @Test
    void tick_pluginRemovedFromRegistry_purgesItsState() {
        givenOneCronPlugin();
        when(distributeLock.acquireLock(anyString(), anyLong(), anyLong())).thenReturn(true);
        when(executor.invoke(any(), any(), any())).thenReturn(new InvocationOutcome(true, null, 1, null));
        ScriptCronDispatcher dispatcher = dispatcher(3);
        dispatcher.tick();
        clock.advance(Duration.ofSeconds(30));
        dispatcher.tick();

        when(registry.all()).thenReturn(List.of());
        dispatcher.tick();

        givenOneCronPlugin();
        dispatcher.tick();
        verify(executor, times(1)).invoke(any(), any(), any());
        clock.advance(Duration.ofSeconds(30));
        dispatcher.tick();
        verify(executor, times(2)).invoke(any(), any(), any());
    }
}
```

Note `ArgumentMatchers` is imported only to satisfy IDE style — remove the import if unused (`contains` and `eq` are imported statically).

- [ ] **Step 2: Run to verify it fails**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.dispatch.ScriptCronDispatcherTest"`
Expected: FAIL to compile — class does not exist.

- [ ] **Step 3: Implement**

`.../dispatch/ScriptCronDispatcher.java`:

```java
package org.openwes.script.plugin.runtime.dispatch;

import lombok.extern.slf4j.Slf4j;
import org.openwes.distribute.lock.DistributeLock;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.runtime.invoke.InvocationOutcome;
import org.openwes.script.plugin.runtime.invoke.ScriptInvocationExecutor;
import org.openwes.script.plugin.runtime.materialize.MaterializedExtension;
import org.openwes.script.plugin.runtime.materialize.MaterializedScriptPlugin;
import org.openwes.script.plugin.runtime.registry.ScriptPluginApiAccessor;
import org.openwes.script.plugin.runtime.registry.ScriptPluginRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Fires due CRON declarations, at most once per occurrence per cluster (DistributeLock) and at
 * most once per occurrence per node (in-memory last-fired stamp). Consecutive failures leading to
 * auto-disable are reported through the management plane.
 */
@Component
@Slf4j
public class ScriptCronDispatcher {

    private static final long LOCK_LEASE_MILLIS = 30_000;

    private final ScriptPluginRegistry registry;
    private final ScriptInvocationExecutor executor;
    private final DistributeLock distributeLock;
    private final ScriptPluginApiAccessor accessor;
    private final Clock clock;
    private final int maxConsecutiveFailures;

    private final Map<String, Long> lastFiredAt = new ConcurrentHashMap<>();
    private final Map<String, CronExpression> parsedCron = new ConcurrentHashMap<>();
    private final Map<String, Integer> consecutiveFailures = new ConcurrentHashMap<>();
    private final Set<String> invalidCronKeys = ConcurrentHashMap.newKeySet();

    public ScriptCronDispatcher(ScriptPluginRegistry registry,
                                ScriptInvocationExecutor executor,
                                DistributeLock distributeLock,
                                ScriptPluginApiAccessor accessor,
                                Clock clock,
                                @Value("${script-plugin.cron.max-consecutive-failures:10}") int maxConsecutiveFailures) {
        this.registry = registry;
        this.executor = executor;
        this.distributeLock = distributeLock;
        this.accessor = accessor;
        this.clock = clock;
        this.maxConsecutiveFailures = maxConsecutiveFailures;
    }

    @Scheduled(fixedDelay = 1000)
    public void tick() {
        long now = clock.millis();
        Set<String> activeKeys = new HashSet<>();
        for (MaterializedScriptPlugin plugin : registry.all()) {
            for (MaterializedExtension extension : plugin.getExtensions()) {
                if (extension.getDeclaration().getType() != ExtensionType.CRON) {
                    continue;
                }
                String key = fireKey(plugin, extension);
                activeKeys.add(key);
                if (isDue(key, extension.getDeclaration().getCron(), now)) {
                    fire(plugin, extension, key);
                }
            }
        }
        lastFiredAt.keySet().retainAll(activeKeys);
        parsedCron.keySet().retainAll(activeKeys);
        consecutiveFailures.keySet().retainAll(activeKeys);
        invalidCronKeys.retainAll(activeKeys);
    }

    private boolean isDue(String key, String cron, long now) {
        CronExpression expression = resolveExpression(key, cron);
        if (expression == null) {
            return false;
        }
        Long lastFired = lastFiredAt.get(key);
        if (lastFired == null) {
            // start counting from activation: the first run happens at the next boundary, not immediately
            lastFiredAt.put(key, now);
            return false;
        }
        ZonedDateTime after = ZonedDateTime.ofInstant(Instant.ofEpochMilli(lastFired), clock.getZone());
        ZonedDateTime next = expression.next(after);
        if (next != null && !next.isAfter(ZonedDateTime.ofInstant(Instant.ofEpochMilli(now), clock.getZone()))) {
            lastFiredAt.put(key, now);
            return true;
        }
        return false;
    }

    private CronExpression resolveExpression(String key, String cron) {
        CronExpression cached = parsedCron.get(key);
        if (cached != null) {
            return cached;
        }
        if (invalidCronKeys.contains(key)) {
            return null;
        }
        try {
            CronExpression parsed = CronExpression.parse(cron);
            parsedCron.put(key, parsed);
            return parsed;
        } catch (IllegalArgumentException e) {
            invalidCronKeys.add(key);
            log.error("invalid cron expression '{}' for cron plugin {}: {}", cron, key, e.getMessage());
            return null;
        }
    }

    private void fire(MaterializedScriptPlugin plugin, MaterializedExtension extension, String key) {
        String lockKey = "script-plugin:cron:" + key;
        if (!distributeLock.acquireLock(lockKey, 0, LOCK_LEASE_MILLIS)) {
            return; // another node has already fired this occurrence
        }
        try {
            InvocationOutcome outcome = executor.invoke(plugin, extension, new Object[0]);
            if (outcome.success()) {
                consecutiveFailures.remove(key);
                return;
            }
            int failures = consecutiveFailures.merge(key, 1, Integer::sum);
            log.error("script cron {} failed ({}/{} consecutive): {}", key, failures, maxConsecutiveFailures,
                    outcome.errorMessage());
            if (failures >= maxConsecutiveFailures) {
                accessor.disable(plugin.getPluginKey(), "cron failed " + failures + " consecutive times");
            }
        } finally {
            distributeLock.releaseLock(lockKey);
        }
    }

    private String fireKey(MaterializedScriptPlugin plugin, MaterializedExtension extension) {
        return plugin.getPluginKey() + "#" + extension.getDeclaration().getMethod();
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.dispatch.ScriptCronDispatcherTest"`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
git add server/modules-plugin/script-plugin-runtime
git commit -m "feat(script-plugin-runtime): cron dispatcher with cluster lock and auto-disable

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 9: Enable-time materialization validator + station poller

**Files:**
- Create: `.../validation/ScriptPluginMaterializationValidatorImpl.java`, `.../validation/StationValidationPoller.java`, `.../validation/ScriptPluginMaterializationValidatorImplTest.java`, `.../validation/StationValidationPollerTest.java`

**Interfaces:**
- Consumes: `ScriptPluginMaterializationValidator` SPI (script-plugin-api), `GroovyScriptEngine` (`compile`, `respondsTo`), `ScriptRevisionContextManager.openValidationScope`, `ScriptPluginApiAccessor` (poller).
- Produces: the five-stage report (`sdkVersion`, `declarations`, `compile`, `context`, `smoke`) implementing the spec's enable-time validation ①–⑤; the poller that completes the station handshake every `script-plugin.validation.poll-interval-ms` (default 5000).

- [ ] **Step 1: Write the failing tests**

`.../validation/ScriptPluginMaterializationValidatorImplTest.java`:

```java
package org.openwes.script.plugin.runtime.validation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptPluginMaterializationValidatorImplTest {

    private final AnnotationConfigApplicationContext parent = new AnnotationConfigApplicationContext();
    private ScriptPluginMaterializationValidatorImpl validator;

    @BeforeEach
    void setUp() {
        parent.registerBean(String.class, () -> "injected");
        parent.refresh();
        validator = new ScriptPluginMaterializationValidatorImpl(new GroovyScriptEngine(),
                new ScriptRevisionContextManager(parent, new GroovyScriptEngine()), "wes");
    }

    @AfterEach
    void tearDown() {
        parent.close();
    }

    private ExtensionDeclarationDTO waveDeclaration() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        declaration.setScript("Wave.groovy");
        declaration.setMethod("wave");
        return declaration;
    }

    private ScriptPluginBundleDTO bundle(Map<String, String> scripts, ExtensionDeclarationDTO declaration) {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("packing-wave");
        bundle.setApplySystem("wes");
        bundle.setSdkVersion(ScriptPluginApiConstants.CONTRACT_VERSION);
        bundle.setRevisionId(11L);
        bundle.setScripts(scripts);
        bundle.setDeclarations(List.of(declaration));
        bundle.setConfig(Map.of());
        bundle.setConfigSchema(List.of());
        return bundle;
    }

    private boolean failedOn(ValidationReportDTO report, String stage) {
        return report.getFailures().stream().anyMatch(failure -> failure.getStage().equals(stage));
    }

    @Test
    void validate_withSpringInjectionAvailable_passes() {
        String source = """
                import org.springframework.beans.factory.annotation.Autowired
                class Wave {
                    @Autowired String greeting
                    Object wave(Object orders, Object ctx) { return [] }
                }
                """;

        ValidationReportDTO report = validator.validate(bundle(Map.of("Wave.groovy", source), waveDeclaration()));

        assertTrue(report.isPassed(), report.failureSummary());
        assertTrue(report.getTargetApp().equals("wes"));
    }

    @Test
    void validate_compileError_reportsCompileStage() {
        ValidationReportDTO report = validator.validate(bundle(Map.of("Wave.groovy", "class Wave {"), waveDeclaration()));

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "compile"));
    }

    @Test
    void validate_unsatisfiedInjection_reportsContextStage() {
        String source = """
                import org.springframework.beans.factory.annotation.Autowired
                class Wave {
                    @Autowired CharSequence missing
                    Object wave(Object orders, Object ctx) { return [] }
                }
                """;

        ValidationReportDTO report = validator.validate(bundle(Map.of("Wave.groovy", source), waveDeclaration()));

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "context"));
    }

    @Test
    void validate_missingScriptMethod_reportsSmokeStage() {
        String source = "class Wave { Object split(Object orders, Object ctx) { return [] } }";

        ValidationReportDTO report = validator.validate(bundle(Map.of("Wave.groovy", source), waveDeclaration()));

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "smoke"));
    }

    @Test
    void validate_wrongSdkVersion_reportsSdkVersionStage() {
        ScriptPluginBundleDTO bundle = bundle(Map.of("Wave.groovy", "class Wave { Object wave(Object o, Object ctx) { return [] } }"),
                waveDeclaration());
        bundle.setSdkVersion("0.9");

        ValidationReportDTO report = validator.validate(bundle);

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "sdkVersion"));
    }

    @Test
    void validate_declarationReferencingUnknownScript_reportsDeclarationsStage() {
        ScriptPluginBundleDTO bundle = bundle(Map.of("Other.groovy", "class Other { Object wave(Object o, Object ctx) { return [] } }"),
                waveDeclaration());

        ValidationReportDTO report = validator.validate(bundle);

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "declarations"));
    }

    @Test
    void validate_httpEndpointWithoutPath_reportsDeclarationsStage() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.HTTP_ENDPOINT);
        declaration.setScript("Http.groovy");
        declaration.setMethod("handle");
        String source = "class Http { Object handle(Object body, Object ctx) { return 'ok' } }";

        ValidationReportDTO report = validator.validate(bundle(Map.of("Http.groovy", source), declaration));

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "declarations"));
    }

    @Test
    void validate_invalidCron_reportsDeclarationsStage() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.CRON);
        declaration.setScript("Job.groovy");
        declaration.setMethod("run");
        declaration.setCron("not a cron");
        String source = "class Job { Object run(Object ctx) { return 1 } }";

        ValidationReportDTO report = validator.validate(bundle(Map.of("Job.groovy", source), declaration));

        assertFalse(report.isPassed());
        assertTrue(failedOn(report, "declarations"));
    }
}
```

`.../validation/StationValidationPollerTest.java`:

```java
package org.openwes.script.plugin.runtime.validation;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.runtime.registry.ScriptPluginApiAccessor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StationValidationPollerTest {

    private final ScriptPluginApiAccessor accessor = mock(ScriptPluginApiAccessor.class);
    private final ScriptPluginMaterializationValidator validator = mock(ScriptPluginMaterializationValidator.class);
    private final StationValidationPoller poller = new StationValidationPoller(accessor, validator);

    @Test
    void poll_withoutPendingRequest_doesNothing() {
        when(accessor.getPendingValidation("station")).thenReturn(null);

        poller.poll();

        verify(accessor, never()).reportValidation(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void poll_withPendingRequest_runsTheValidatorAndReportsBack() {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("station-api");
        bundle.setRevisionId(11L);
        ValidationReportDTO report = ValidationReportDTO.passed("station-api", 11L, "station");
        when(accessor.getPendingValidation("station")).thenReturn(bundle);
        when(validator.targetApp()).thenReturn("station");
        when(validator.validate(bundle)).thenReturn(report);

        poller.poll();

        verify(accessor).reportValidation(report);
    }

    @Test
    void poll_whenValidationThrows_reportsAFailedReport() {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("station-api");
        bundle.setRevisionId(11L);
        when(accessor.getPendingValidation("station")).thenReturn(bundle);
        when(validator.targetApp()).thenReturn("station");
        when(validator.validate(bundle)).thenThrow(new IllegalStateException("boom"));

        poller.poll();

        verify(accessor).reportValidation(org.mockito.ArgumentMatchers.argThat(
                report -> !report.isPassed() && report.getPluginKey().equals("station-api")));
    }
}
```

- [ ] **Step 2: Run to verify they fail**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.validation.*"`
Expected: FAIL to compile — classes do not exist.

- [ ] **Step 3: Implement**

`.../validation/ScriptPluginMaterializationValidatorImpl.java`:

```java
package org.openwes.script.plugin.runtime.validation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptCompileException;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Enable-time validation stage ①–⑤ (spec §Enable-time validation). Runs on the node whose app
 * matches the plugin: in-process on wes, and on station through the pending-validation handshake.
 * Stages: sdkVersion, declarations (static legality), compile, context (dry-run materialization
 * with injection and @PostConstruct), smoke (declared method existence).
 */
@Component
@Slf4j
public class ScriptPluginMaterializationValidatorImpl implements ScriptPluginMaterializationValidator {

    private final GroovyScriptEngine scriptEngine;
    private final ScriptRevisionContextManager revisionContextManager;
    private final String targetApp;

    public ScriptPluginMaterializationValidatorImpl(GroovyScriptEngine scriptEngine,
                                                    ScriptRevisionContextManager revisionContextManager,
                                                    @Value("${spring.application.name:}") String targetApp) {
        this.scriptEngine = scriptEngine;
        this.revisionContextManager = revisionContextManager;
        this.targetApp = targetApp;
    }

    @Override
    public String targetApp() {
        return targetApp;
    }

    @Override
    public ValidationReportDTO validate(ScriptPluginBundleDTO bundle) {
        List<ValidationReportDTO.ValidationFailureDTO> failures = new ArrayList<>();

        if (!ScriptPluginApiConstants.CONTRACT_VERSION.equals(bundle.getSdkVersion())) {
            failures.add(failure("sdkVersion", "plugin sdk_version " + bundle.getSdkVersion()
                    + " does not equal platform contract " + ScriptPluginApiConstants.CONTRACT_VERSION));
        }
        validateDeclarations(bundle, failures);

        if (failures.isEmpty()) {
            dryRun(bundle, failures);
        }

        return new ValidationReportDTO(bundle.getPluginKey(), bundle.getRevisionId(), targetApp,
                failures.isEmpty(), failures, System.currentTimeMillis());
    }

    private void validateDeclarations(ScriptPluginBundleDTO bundle,
                                      List<ValidationReportDTO.ValidationFailureDTO> failures) {
        Set<String> httpEndpoints = new HashSet<>();
        for (ExtensionDeclarationDTO declaration : bundle.getDeclarations()) {
            if (declaration.getType() == null
                    || PluginContractCatalog.archetype(declaration.getType()).isEmpty()) {
                failures.add(failure("declarations", "unknown extension type " + declaration.getType()));
                continue;
            }
            if (StringUtils.isBlank(declaration.getScript()) || StringUtils.isBlank(declaration.getMethod())) {
                failures.add(failure("declarations", declaration.getType() + " needs both script and method"));
                continue;
            }
            if (!bundle.getScripts().containsKey(declaration.getScript())) {
                failures.add(failure("declarations", declaration.getType() + " references unknown script '"
                        + declaration.getScript() + "'"));
                continue;
            }
            switch (declaration.getType()) {
                case REQUEST_VALIDATE, REQUEST_SUPPLY, REQUEST_AFTER_INVOKE, CUSTOM_REQUEST_INVOKE -> {
                    if (StringUtils.isBlank(declaration.getApiType())) {
                        failures.add(failure("declarations", declaration.getType() + " needs apiType"));
                    }
                }
                case LIFECYCLE_LISTENER -> {
                    if (StringUtils.isBlank(declaration.getEntityName())) {
                        failures.add(failure("declarations", "LIFECYCLE_LISTENER needs entityName"));
                    }
                }
                case CUSTOM_API -> {
                    if (StringUtils.isBlank(declaration.getCustomApiCode())) {
                        failures.add(failure("declarations", "CUSTOM_API needs customApiCode"));
                    }
                }
                case HTTP_ENDPOINT -> {
                    if (StringUtils.isBlank(declaration.getPath()) || !declaration.getPath().startsWith("/")
                            || declaration.getPath().endsWith("/")) {
                        failures.add(failure("declarations", "HTTP_ENDPOINT path must start with '/' and not end with '/'"));
                    } else if (!httpEndpoints.add(declaration.getHttpMethod() + " " + declaration.getPath())) {
                        failures.add(failure("declarations", "duplicate HTTP endpoint "
                                + declaration.getHttpMethod() + " " + declaration.getPath()));
                    }
                }
                case CRON -> {
                    if (StringUtils.isBlank(declaration.getCron())) {
                        failures.add(failure("declarations", "CRON needs a cron expression"));
                    } else {
                        try {
                            CronExpression.parse(declaration.getCron());
                        } catch (IllegalArgumentException e) {
                            failures.add(failure("declarations", "invalid cron '" + declaration.getCron()
                                    + "': " + e.getMessage()));
                        }
                    }
                }
                default -> {
                }
            }
        }
    }

    private void dryRun(ScriptPluginBundleDTO bundle, List<ValidationReportDTO.ValidationFailureDTO> failures) {
        try (ScriptRevisionContextManager.ValidationScope scope = revisionContextManager
                .openValidationScope(bundle.getPluginKey(), bundle.getScripts())) {
            for (ExtensionDeclarationDTO declaration : bundle.getDeclarations()) {
                Object instance = scope.instances().get(declaration.getScript());
                if (instance == null || !scriptEngine.respondsTo(instance, declaration.getMethod())) {
                    failures.add(failure("smoke", declaration.getScript() + " does not declare method '"
                            + declaration.getMethod() + "'"));
                }
            }
        } catch (ScriptCompileException e) {
            failures.add(failure("compile", e.getMessage()));
        } catch (Exception e) {
            String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            failures.add(failure("context", message));
        }
    }

    private ValidationReportDTO.ValidationFailureDTO failure(String stage, String message) {
        return new ValidationReportDTO.ValidationFailureDTO(stage, message);
    }
}
```

Note: the `compile` stage falls out of `dryRun` because `createScriptInstances` compiles; a bundle whose scripts fail to compile therefore reports `compile`, and injection problems report `context` — matching the tests.

`.../validation/StationValidationPoller.java`:

```java
package org.openwes.script.plugin.runtime.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.runtime.registry.ScriptPluginApiAccessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Completes the enable-time validation handshake on remote target apps: polls the management plane
 * for a validation request addressed to this app, runs the local materialization dry-run and
 * reports the verdict back.
 */
@Component
@ConditionalOnProperty(name = "script-plugin.registry.mode", havingValue = "remote")
@RequiredArgsConstructor
@Slf4j
public class StationValidationPoller {

    private final ScriptPluginApiAccessor accessor;
    private final ScriptPluginMaterializationValidator validator;

    @Scheduled(fixedDelayString = "${script-plugin.validation.poll-interval-ms:5000}")
    public void poll() {
        try {
            ScriptPluginBundleDTO pending = accessor.getPendingValidation(validator.targetApp());
            if (pending == null) {
                return;
            }
            ValidationReportDTO report;
            try {
                report = validator.validate(pending);
            } catch (Exception e) {
                report = ValidationReportDTO.failed(pending.getPluginKey(), pending.getRevisionId(),
                        validator.targetApp(), "remote", e.getMessage());
            }
            accessor.reportValidation(report);
            log.info("reported validation of plugin {} revision {}: passed={}", pending.getPluginKey(),
                    pending.getRevisionId(), report.isPassed());
        } catch (Exception e) {
            log.warn("script plugin validation poll failed: {}", e.getMessage());
        }
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd server && ./gradlew :modules-plugin:script-plugin-runtime:test --tests "org.openwes.script.plugin.runtime.validation.*"`
Expected: PASS (11 tests).

- [ ] **Step 5: Commit**

```bash
git add server/modules-plugin/script-plugin-runtime
git commit -m "feat(script-plugin-runtime): enable-time dry-run validator and station handshake poller

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 10: plugin-sdk merge — script extensions join jar plugins

**Files:**
- Create: `server/modules-plugin/plugin-sdk/src/test/java/org/openwes/plugin/sdk/utils/PluginSdkUtilsTest.java`
- Modify: `.../utils/PluginSdkUtils.java`, `.../utils/LifecycleListenerRegistry.java`, `.../utils/LifecycleListenerRegistryTest.java`
- Note: `org/openwes/plugin/sdk/spi/ScriptExtensionProvider.java` was created in Task 6 and must be present before this task's tests compile.

**Interfaces:**
- Consumes: `IPlugin.priority()` (Plan 0).
- Produces: `ScriptExtensionProvider.getExtensions(Class<T>) → List<T>` (implemented by `ScriptExtensionProviderImpl` in the runtime, Task 6); both merge points now include script extensions sorted by priority descending.

- [ ] **Step 1: Write the failing tests**

`PluginSdkUtilsTest.java`:

```java
package org.openwes.plugin.sdk.utils;

import org.junit.jupiter.api.Test;
import org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveWaveAction;
import org.openwes.plugin.sdk.spi.ScriptExtensionProvider;
import org.pf4j.PluginManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PluginSdkUtilsTest {

    private final PluginManager pluginManager = mock(PluginManager.class);

    @Test
    void getExtractObject_mergesScriptExtensionsAndSortsByPriorityDescending() {
        IOutboundWaveWaveAction jarPlugin = mock(IOutboundWaveWaveAction.class);
        when(jarPlugin.priority()).thenReturn(10);
        IOutboundWaveWaveAction scriptPlugin = mock(IOutboundWaveWaveAction.class);
        when(scriptPlugin.priority()).thenReturn(100);
        when(pluginManager.getExtensions(IOutboundWaveWaveAction.class)).thenReturn(List.of(jarPlugin));
        ScriptExtensionProvider provider = mock(ScriptExtensionProvider.class);
        when(provider.getExtensions(IOutboundWaveWaveAction.class)).thenReturn(List.of(scriptPlugin));

        PluginSdkUtils utils = new PluginSdkUtils(pluginManager, List.of(provider));

        assertEquals(List.of(scriptPlugin, jarPlugin), utils.getExtractObject(IOutboundWaveWaveAction.class));
    }

    @Test
    void getExtractObject_withoutScriptProviders_keepsJarPlugins() {
        IOutboundWaveWaveAction jarPlugin = mock(IOutboundWaveWaveAction.class);
        when(pluginManager.getExtensions(IOutboundWaveWaveAction.class)).thenReturn(List.of(jarPlugin));

        PluginSdkUtils utils = new PluginSdkUtils(pluginManager, List.of());

        assertEquals(List.of(jarPlugin), utils.getExtractObject(IOutboundWaveWaveAction.class));
    }

    @Test
    void getExtractObject_nothingRegistered_returnsEmptyList() {
        PluginSdkUtils utils = new PluginSdkUtils(pluginManager, List.of());

        assertTrue(utils.getExtractObject(IOutboundWaveWaveAction.class).isEmpty());
    }
}
```

Replace `LifecycleListenerRegistryTest` entirely:

```java
package org.openwes.plugin.sdk.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.plugin.extension.business.IEntityLifecycleListener;
import org.openwes.plugin.sdk.spi.ScriptExtensionProvider;
import org.pf4j.PluginManager;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LifecycleListenerRegistryTest {

    @Mock
    private PluginManager pluginManager;

    @Test
    void fireAfterStatusChange_withMatchingJarListener_callsAfterStatusChange() {
        IEntityLifecycleListener listener = mock(IEntityLifecycleListener.class);
        when(listener.getEntityName()).thenReturn("TestEntity");
        when(pluginManager.getExtensions(IEntityLifecycleListener.class)).thenReturn(List.of(listener));
        LifecycleListenerRegistry registry = new LifecycleListenerRegistry(pluginManager, List.of());

        registry.fireAfterStatusChange("TestEntity", 1L, "statusData", "NEW_STATUS");

        verify(listener).afterStatusChange(1L, "statusData", "NEW_STATUS");
    }

    @Test
    void fireAfterStatusChange_mergesScriptProvidedListeners() {
        IEntityLifecycleListener scriptListener = mock(IEntityLifecycleListener.class);
        when(scriptListener.getEntityName()).thenReturn("TestEntity");
        ScriptExtensionProvider provider = mock(ScriptExtensionProvider.class);
        when(provider.getExtensions(IEntityLifecycleListener.class)).thenReturn(List.of(scriptListener));
        LifecycleListenerRegistry registry = new LifecycleListenerRegistry(pluginManager, List.of(provider));

        registry.fireAfterStatusChange("TestEntity", 1L, "statusData", "NEW_STATUS");

        verify(scriptListener).afterStatusChange(1L, "statusData", "NEW_STATUS");
    }

    @Test
    void fireAfterStatusChange_ignoresListenersOfOtherEntities() {
        IEntityLifecycleListener listener = mock(IEntityLifecycleListener.class);
        when(listener.getEntityName()).thenReturn("OtherEntity");
        when(pluginManager.getExtensions(IEntityLifecycleListener.class)).thenReturn(List.of(listener));
        LifecycleListenerRegistry registry = new LifecycleListenerRegistry(pluginManager, List.of());

        registry.fireAfterStatusChange("TestEntity", 1L, "statusData", "NEW_STATUS");

        verify(listener, org.mockito.Mockito.never()).afterStatusChange(1L, "statusData", "NEW_STATUS");
    }
}
```

- [ ] **Step 2: Run to verify they fail**

Run: `cd server && ./gradlew :modules-plugin:plugin-sdk:test`
Expected: FAIL to compile — `ScriptExtensionProvider` does not exist; constructor arity mismatch.

- [ ] **Step 3: Implement**

(`ScriptExtensionProvider` already exists from Task 6.) Replace `PluginSdkUtils`:

```java
package org.openwes.plugin.sdk.utils;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.plugin.extension.IPlugin;
import org.openwes.plugin.sdk.spi.ScriptExtensionProvider;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PluginSdkUtils {

    private final PluginManager pluginManager;
    private final List<ScriptExtensionProvider> scriptExtensionProviders;

    public <T> List<T> getExtractObject(Class<T> tClass) {

        List<T> extractObjects = new ArrayList<>(pluginManager.getExtensions(tClass));
        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            extractObjects.addAll(provider.getExtensions(tClass));
        }

        if (CollectionUtils.isEmpty(extractObjects)) {
            return Collections.emptyList();
        }
        extractObjects.sort(Comparator.comparingInt(PluginSdkUtils::priorityOf).reversed());
        return extractObjects;
    }

    /** Extensions that do not extend IPlugin keep priority 0 and preserve their relative order (stable sort). */
    private static int priorityOf(Object extension) {
        return extension instanceof IPlugin plugin ? plugin.priority() : 0;
    }
}
```

Replace `LifecycleListenerRegistry`:

```java
package org.openwes.plugin.sdk.utils;

import lombok.RequiredArgsConstructor;
import org.openwes.plugin.extension.IPlugin;
import org.openwes.plugin.extension.business.IEntityLifecycleListener;
import org.openwes.plugin.sdk.spi.ScriptExtensionProvider;
import org.pf4j.PluginManager;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class LifecycleListenerRegistry {

    private final PluginManager pluginManager;
    private final List<ScriptExtensionProvider> scriptExtensionProviders;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <U, V> void fireAfterStatusChange(String entityName, U u, V v, String status) {

        List<IEntityLifecycleListener> entityListeners = new ArrayList<>();
        List<IEntityLifecycleListener> pluginListeners =
                pluginManager.getExtensions(IEntityLifecycleListener.class);
        if (pluginListeners != null) {
            entityListeners.addAll(pluginListeners);
        }
        for (ScriptExtensionProvider provider : scriptExtensionProviders) {
            entityListeners.addAll(provider.getExtensions(IEntityLifecycleListener.class));
        }

        entityListeners.sort(Comparator.comparingInt(LifecycleListenerRegistry::priorityOf).reversed());
        entityListeners.stream().filter(listener -> listener.getEntityName().equals(entityName))
                .forEach(listener -> listener.afterStatusChange(u, v, status));
    }

    private static int priorityOf(Object listener) {
        return listener instanceof IPlugin plugin ? plugin.priority() : 0;
    }
}
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd server && ./gradlew :modules-plugin:plugin-sdk:test`
Expected: PASS (3 + 3 tests).

- [ ] **Step 5: Commit**

```bash
git add server/modules-plugin/plugin-sdk
git commit -m "feat(plugin-sdk): merge script extensions with jar plugins at both extraction points

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 11: `CreateOutboundRequestHandler` runs the custom-request-plugin hook

**Files:**
- Modify: `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/application/service/handler/request/wms/CreateOutboundRequestHandler.java`
- Create: `server/modules-api-platform/api-platform/src/test/java/org/openwes/api/platform/application/service/handler/request/wms/CreateOutboundRequestHandlerTest.java`

**Interfaces:**
- Consumes: `AbstractRequestHandler.invoke(RequestHandleContext)` (the `ICustomRequestPlugin` hook, filtering by apiType), `PluginSdkUtils` (now merge-aware).
- Produces: the create-outbound request path invokes custom request plugins (script `CUSTOM_REQUEST_INVOKE` archetype included) before the built-in dispatch.

- [ ] **Step 1: Write the failing test**

`.../wms/CreateOutboundRequestHandlerTest.java`:

```java
package org.openwes.api.platform.application.service.handler.request.wms;

import org.junit.jupiter.api.Test;
import org.openwes.api.platform.api.constants.ApiTypeEnum;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.plugin.extension.business.api.platform.request.ICustomRequestPlugin;
import org.openwes.plugin.sdk.utils.PluginSdkUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateOutboundRequestHandlerTest {

    private final CreateOutboundRequestHandler handler = new CreateOutboundRequestHandler();

    private void withPlugin(ICustomRequestPlugin plugin) {
        PluginSdkUtils pluginUtils = mock(PluginSdkUtils.class);
        when(pluginUtils.getExtractObject(ICustomRequestPlugin.class)).thenReturn(List.of(plugin));
        ReflectionTestUtils.setField(handler, "pluginUtils", pluginUtils);
    }

    @Test
    void invoke_runsCustomRequestPluginsOfTheSameApiType() {
        ICustomRequestPlugin plugin = mock(ICustomRequestPlugin.class);
        when(plugin.getApiType()).thenReturn(ApiTypeEnum.ORDER_OUTBOUND_CREATE.name());
        withPlugin(plugin);
        RequestHandleContext context = new RequestHandleContext();

        handler.invoke(context);

        verify(plugin).invoke(context);
    }

    @Test
    void invoke_ignoresPluginsOfOtherApiTypes() {
        ICustomRequestPlugin plugin = mock(ICustomRequestPlugin.class);
        when(plugin.getApiType()).thenReturn("SOMETHING_ELSE");
        withPlugin(plugin);
        RequestHandleContext context = new RequestHandleContext();

        handler.invoke(context);

        verify(plugin, org.mockito.Mockito.never()).invoke(context);
    }

    @Test
    void invoke_pluginRejects_propagatesTheException() {
        ICustomRequestPlugin plugin = mock(ICustomRequestPlugin.class);
        when(plugin.getApiType()).thenReturn(ApiTypeEnum.ORDER_OUTBOUND_CREATE.name());
        org.mockito.Mockito.doThrow(new IllegalStateException("rejected by script plugin"))
                .when(plugin).invoke(org.mockito.ArgumentMatchers.any());
        withPlugin(plugin);

        assertThrows(IllegalStateException.class, () -> handler.invoke(new RequestHandleContext()));
    }
}
```

- [ ] **Step 2: Run to verify it fails**

Run: `cd server && ./gradlew :modules-api-platform:api-platform:test --tests "org.openwes.api.platform.application.service.handler.request.wms.CreateOutboundRequestHandlerTest"`
Expected: FAIL — `verify(plugin).invoke(context)` never happens (the override skips `super.invoke`).

- [ ] **Step 3: Fix the handler**

In `CreateOutboundRequestHandler`:
1. Delete the unused import `org.openwes.plugin.extension.business.api.platform.request.IInboundPlanOrderCreatePlugin` (line 8).
2. Replace the `invoke` method (with its comment):

```java
    @Override
    public void invoke(RequestHandleContext context) {

        // custom request plugins of this api type run first and may reject the request by throwing
        super.invoke(context);

        List<OutboundPlanOrderDTO> list = getTargetList(context, OutboundPlanOrderDTO.class);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        if (ConverterHelper.isAsyncApi(context.getApiType(), list.size())) {
            coreClientService.asyncCreateOutboundOrder(list);
        } else {
            coreClientService.createOutboundOrder(list);
        }
        context.setResponse(Response.builder().build());
    }
```

- [ ] **Step 4: Run the tests to verify they pass**

Run: `cd server && ./gradlew :modules-api-platform:api-platform:test --tests "org.openwes.api.platform.application.service.handler.request.wms.CreateOutboundRequestHandlerTest"`
Expected: PASS (3 tests).

- [ ] **Step 5: Commit**

```bash
git add server/modules-api-platform/api-platform
git commit -m "fix(api-platform): run custom request plugins on the create-outbound path

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 12: Server wiring and end-to-end acceptance

**Files:**
- Modify: `server/server/wes-server/build.gradle`, `server/server/station-server/build.gradle`, `server/server/wes-server/src/main/resources/bootstrap.yml`, `server/server/station-server/src/main/resources/bootstrap.yml`, `server/server/station-server/src/main/java/org/openwes/station/StationApplication.java`
- Modify: `docs/standards/backend.md` (plugin chapter — spec §Fix Work Package → Along-the-way)

**Interfaces:**
- Consumes: everything above.
- Produces: a deployment where wes runs the management plane + runtime and station runs the runtime against the remote registry.

- [ ] **Step 1: Wire the servers**

`server/server/wes-server/build.gradle` — after the `script-plugin-core` line (Plan 1a Task 8) add:

```groovy
    implementation project(path: ":modules-plugin:script-plugin-runtime", configuration: 'default')
```

`server/server/station-server/build.gradle` — add (adjust to sit with the existing module deps):

```groovy
    implementation project(path: ":modules-plugin:script-plugin-runtime", configuration: 'default')
```

`server/server/wes-server/src/main/resources/bootstrap.yml` — insert a new top-level block immediately before the `server:` line:

```yaml
script-plugin:
  registry:
    mode: local

```

(Edit anchor: replace `\nserver:\n  port: 9010` with the block above followed by `\nserver:\n  port: 9010`.)

`server/server/station-server/src/main/resources/bootstrap.yml` — insert immediately before its `server:` line:

```yaml
script-plugin:
  registry:
    mode: remote

```

`StationApplication.java` — cron dispatch, the registry poll and the validation poller all use `@Scheduled`, so station needs scheduling enabled (wes already has it):

```java
package org.openwes.station;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"org.openwes"}, exclude = {DataSourceAutoConfiguration.class})
public class StationApplication {

    public static void main(String[] args) {
        SpringApplication.run(StationApplication.class, args);
    }
}
```

- [ ] **Step 2: Full build and test run**

Run: `cd server && ./gradlew test --continue`
Expected: green. If pre-existing failures appear in unrelated modules, they must have been handled by Plan 0 Task 7's exemptions — do not add new exemptions for anything this plan touched.

- [ ] **Step 3: Manual end-to-end verification**

Bring up `mysql`, `redis` and `nacos` (docker compose), then `cd server && ./gradlew :server:wes-server:bootRun` and `:server:station-server:bootRun`. Perform, in order, recording each observed result:

1. **WES script plugin, hot enable.** Create a draft and a revision whose `OUTBOUND_WAVE_WAVE_ACTION` script returns one wave per order:

```groovy
class Wave {
    Object wave(Object orders, Object ctx) {
        ctx.log().info('packing-wave script invoked with {} orders', orders.size())
        return orders.collect { [it] }
    }
}
```

```bash
curl -s -X POST http://localhost:9010/plugin-platform/scriptPlugin/createDraft -H 'Content-Type: application/json' \
  -d '{"pluginKey":"packing-wave","name":"One order per wave","applySystem":"wes"}'
curl -s -X POST http://localhost:9010/plugin-platform/scriptPlugin/packing-wave/revision -H 'Content-Type: application/json' \
  -d '{"changeSummary":"one order per wave","extensionPoints":[{"type":"OUTBOUND_WAVE_WAVE_ACTION","priority":100,"script":"Wave.groovy","method":"wave"}],"scripts":{"Wave.groovy":"class Wave { Object wave(Object orders, Object ctx) { ctx.log().info('packing-wave invoked'); return orders.collect { [it] } } }"}}'
curl -s -X POST http://localhost:9010/plugin-platform/scriptPlugin/packing-wave/enable
```

Expected: `enable` returns `code = 0`, `lastValidationStatus = PASSED` (validated in-process on wes); the wes log shows `materialized script plugin packing-wave revision <id>`; creating outbound orders + waves via the normal flow now produces one wave per order, and `p_script_plugin_execution_log` receives rows.

2. **Disable reverts behavior.** `curl -s -X POST http://localhost:9010/plugin-platform/scriptPlugin/packing-wave/disable` → log shows the dematerialization, and wave creation returns to the built-in grouping.

3. **Station plugin via the handshake.** Create a draft with `"applySystem":"station"` and a `CUSTOM_API` declaration (`customApiCode` used by a workstation action), then `enable`. Expected: the request is answered by the station node's poller within ~5 s (wes log: `validation report for ... passed=true`), station log shows the dry-run, and the workstation action now executes the script (result/log line observable at the station).

4. **Self-provided HTTP endpoint.** Add a revision with `{"type":"HTTP_ENDPOINT","script":"Http.groovy","method":"handle","httpMethod":"GET","path":"/status"}`, enable, then:

```bash
curl -s http://localhost:9010/plugin-platform/ext/packing-wave/status
```

Expected: the script's return value wrapped in `{"code":"0",...}`.

5. **Cron.** Add a `{"type":"CRON","script":"Job.groovy","method":"run","cron":"0 * * * * *"}` declaration; expected: one log line per minute, no duplicate when both nodes are up (lock), and after forcing 10 consecutive script failures the plugin is disabled automatically (check `status` via `curl .../detail/packing-wave`).

6. **Injection smoke (the requirement "plugins must support Spring bean injection").** A script with `@Autowired IOutboundPlanOrderApi` (or any host bean) materializes and its `@PostConstruct` runs at enable time — verify via `p_script_plugin_execution_log` / logs.

7. **API-platform request-plugin archetype through the real handler.** Add a revision with `{"type":"REQUEST_SUPPLY","script":"Supply.groovy","method":"supply","apiType":"ORDER_OUTBOUND_CREATE"}` and a script that logs `ctx.pluginKey()`; enable; POST an outbound-create request to the API platform. Expected: the wes log shows the script line before the outbound order is created — this proves Task 11's `super.invoke(context)` reaches script-backed request plugins through `AbstractRequestHandler`, and with `CUSTOM_REQUEST_INVOKE` a script that throws rejects the request (HTTP error, no order created).

If any step fails, stop and report the deviation before continuing — do not mark the task complete on partial evidence.

- [ ] **Step 4: Rewrite the backend.md plugin chapter**

Extend the `## Plugin Rules` section of `docs/standards/backend.md` (after the existing items 1–3) with the script-plugin rules, taken from the code so the doc cannot drift: the archetype table from `PluginContractCatalog.ARCHETYPES` (name, script method, blocking or not), the "last parameter is always `ctx`" convention with a short Groovy example (including an `@Autowired` field), the failure policy table (blocking throws `ScriptPluginInvocationException`; non-blocking logs and continues; HTTP returns `Response.fail()`; cron auto-disables after N failures), the `IPlugin.priority()` contract (higher wins; selection falls back to built-in behavior when no plugin is enabled), and the enable/disable/rollback flow with its validation stages. Keep `IEntityLifecycleListener` documented as **not** an `IPlugin` subtype (priority 0, stable order). No secrets, no credentials in examples.

- [ ] **Step 5: Commit**

```bash
git add server/server/wes-server/build.gradle server/server/station-server/build.gradle server/server/wes-server/src/main/resources/bootstrap.yml server/server/station-server/src/main/resources/bootstrap.yml server/server/station-server/src/main/java/org/openwes/station/StationApplication.java docs/standards/backend.md
git commit -m "feat(script-plugin-runtime): wire runtime into servers and document plugin contracts

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Acceptance for Phase 1b

- `cd server && ./gradlew test` is green; every runtime class has focused tests (engine `respondsTo`, revision contexts with parent-context injection, executor timeout/logging, materializer proxying with identity methods + failure policy, script context + HTTP client, registry refresh/poll/dematerialization, HTTP dispatcher, cron dispatcher incl. auto-disable, validator five stages, station poller).
- `PluginSdkUtils.getExtractObject` and `LifecycleListenerRegistry` merge script extensions with jar plugins, sorted by priority descending; plugin-sdk still works without the runtime on the classpath (empty provider list).
- Enabling a script plugin on a running wes node materializes it and changes outbound wave behavior without restart; disabling reverts it; station-target plugins validate through the pending-validation handshake; cron jobs fire once per occurrence per cluster and auto-disable after the configured consecutive failures.
- `CreateOutboundRequestHandler` reaches custom request plugins.
- Phase 1b freezes the runtime-facing interfaces (`IScriptPluginApi`, `ScriptPluginBundleDTO`, `PluginContractCatalog`, `ScriptExtensionProvider`, `ScriptContext`) consumed by Plans 2 (AI pipeline) and 3 (workbench).
