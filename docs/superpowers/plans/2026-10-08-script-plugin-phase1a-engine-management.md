# Script Plugin Phase 1a — Engine, Contract API & Management Plane Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the three management-plane modules of the script-plugin platform — a reusable Groovy `script-engine`, the `script-plugin-api` contract module (DTOs, Dubbo API, `PluginContractCatalog`), and `script-plugin-core` (JPA persistence, enable/disable/rollback service with validation orchestration, REST `/plugin-platform/scriptPlugin/**`, `@DubboService` impl, Liquibase tables) — so plugins can be authored, stored, validated and lifecycle-managed before the runtime (Plan 1b) materializes them.

**Architecture:** `script-engine` extracts the Groovy compilation from `api-platform`'s `JavaScriptUtils` into a cached, timeout-aware engine. `script-plugin-api` is the single source of contract truth: enums, DTOs, the Dubbo `IScriptPluginApi` (consumed by nodes to fetch enabled bundles), and `PluginContractCatalog` (machine-readable archetype signatures, per-app bean catalog, prompt fragment generator). `script-plugin-core` stores plugins as immutable revisions (`p_script_plugin*`), exposes the management REST API hosted by wes-server, and orchestrates enable-time validation: in-process when the target app matches the hosting node's validator, otherwise by publishing a validation request on the Redis `PLUGIN_LISTEN_PLUGIN_MANAGEMENT` channel and waiting for the target node's report (delivered via Dubbo `reportValidation`).

**Tech Stack:** Java 17, Spring Boot 3.2.2, Groovy 4.0.22 (`GroovyClassLoader`), JPA/Hibernate, Redisson (`RedisUtils` pub/sub), Dubbo 3.2.10, Liquibase, JUnit 5 + Mockito 4.6.0.

**Spec:** `docs/superpowers/specs/2026-10-08-script-plugin-ai-workbench-design.md` (see *Architecture → Module map, Data model, Status machine, Enable / refresh flow*, *AI Generation Pipeline → PluginContractCatalog*, *Runtime Contract → Enable-time validation*).

**Depends on:** `docs/superpowers/plans/2026-10-08-script-plugin-phase0-fixes.md` — in particular Task 5 creates `db.changelog-20261008.xml`, which Task 8 here **appends to**.

## Global Constraints

- Module homes: `server/modules-utils/script-engine/`, `server/modules-plugin/script-plugin-api/`, `server/modules-plugin/script-plugin-core/`.
- Java package roots: `org.openwes.script.engine`, `org.openwes.script.plugin.api(.dto/.enums)`, `org.openwes.script.plugin.core(.domain.entity/.domain.repository/.domain.service/.application/.controller)`.
- Gradle includes use the pattern `include 'modules-utils:script-engine'` etc. in `server/settings.gradle`; module dependencies use `implementation project(path: ":...", configuration: 'default')`.
- Entities use the repo conventions: `@EqualsAndHashCode(callSuper = true) @Data @Entity @EntityListeners(AuditingEntityListener.class) @Table(...) @DynamicUpdate`, id generator `@GeneratedValue(generator = "databaseIdGenerator")` + `@GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)`, `@Version Long version`, audit via `UpdateUserPO`.
- REST controllers: `@RestController @Validated @RequiredArgsConstructor @RequestMapping("plugin-platform/scriptPlugin") @Tag(...)`, return `org.openwes.common.utils.http.Response<T>` (`Response.success(data)`).
- Dubbo impls: `@Service @Validated @DubboService @RequiredArgsConstructor @Slf4j` (mirrors `PickingOrderApiImpl`). Both servers scan `org.openwes` and Dubbo scans `org.openwes`, so no extra configuration is needed.
- Timestamps are epoch millis via `System.currentTimeMillis()`; `createUser`/`updateUser` come from JPA auditing (`@CreatedBy`/`@LastModifiedBy`), do not set them manually.
- Enable-time validation uses the spec's stage names: `sdkVersion`, `declarations`, `compile`, `context`, `smoke`.
- Every commit message ends with: `Co-Authored-By: Claude Code <noreply@anthropic.com>`
- Run all Gradle commands from `server/`.

---

## File Map

| File | Action | Purpose |
|------|--------|---------|
| `server/settings.gradle` | Modify | include `script-engine`, `script-plugin-api`, `script-plugin-core` |
| `server/modules-utils/script-engine/build.gradle` | Create | engine module deps |
| `server/modules-utils/script-engine/src/main/java/org/openwes/script/engine/ScriptCompileException.java` | Create | compile failure |
| `server/modules-utils/script-engine/src/main/java/org/openwes/script/engine/ScriptExecutionException.java` | Create | invoke failure/timeout |
| `server/modules-utils/script-engine/src/main/java/org/openwes/script/engine/ScriptHashes.java` | Create | content-addressed cache keys |
| `server/modules-utils/script-engine/src/main/java/org/openwes/script/engine/GroovyScriptEngine.java` | Create | compile/cache/instantiate/invoke |
| `server/modules-utils/script-engine/src/main/java/org/openwes/script/engine/ScriptExecutionRuntime.java` | Create | timeout-enforced invocation pool |
| `server/modules-utils/script-engine/src/test/java/org/openwes/script/engine/GroovyScriptEngineTest.java` | Create | engine tests |
| `server/modules-utils/script-engine/src/test/java/org/openwes/script/engine/ScriptExecutionRuntimeTest.java` | Create | timeout tests |
| `server/modules-api-platform/api-platform/build.gradle` | Modify | depend on script-engine |
| `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/utils/JavaScriptUtils.java` | Modify | delegate `executeJava` to the engine |
| `server/modules-api-platform/api-platform/src/test/java/org/openwes/api/platform/utils/JavaScriptUtilsTest.java` | Create | delegation tests |
| `server/modules-plugin/script-plugin-api/build.gradle` | Create | contract module deps |
| `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/ScriptPluginApiConstants.java` | Create | contract version, supported apps |
| `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/enums/*.java` | Create | `ScriptPluginStatusEnum`, `RevisionOriginEnum`, `ExtensionType`, `PluginManagementMessageType` |
| `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/*.java` | Create | DTOs (see Task 2/3) |
| `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/IScriptPluginApi.java` | Create | Dubbo API |
| `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/host/ScriptContext.java` | Create | script-facing context contract |
| `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/host/ScriptHttpClient.java` | Create | timeout-enforced HTTP facade |
| `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/contract/PluginContractCatalog.java` | Create | single contract source |
| `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/ScriptPluginContentHasher.java` | Create | revision `content_hash` |
| `server/modules-plugin/script-plugin-api/src/test/java/.../contract/PluginContractCatalogTest.java` | Create | catalog integrity |
| `server/modules-plugin/script-plugin-api/src/test/java/.../ScriptPluginContentHasherTest.java` | Create | hash stability |
| `server/modules-plugin/script-plugin-core/build.gradle` | Create | core module deps |
| `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/entity/*.java` | Create | 4 entities |
| `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/repository/*.java` | Create | 4 repositories |
| `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginService.java` | Create | lifecycle + validation orchestration |
| `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginExecutionLogService.java` | Create | log ingestion + pruning |
| `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/controller/ScriptPluginController.java` | Create | management REST |
| `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/application/ScriptPluginApiImpl.java` | Create | `@DubboService` |
| `server/modules-plugin/script-plugin-core/src/test/java/.../domain/entity/ScriptPluginStateTest.java` | Create | state machine tests |
| `server/modules-plugin/script-plugin-core/src/test/java/.../domain/service/ScriptPluginServiceTest.java` | Create | enable/validate/rollback tests |
| `server/modules-plugin/script-plugin-core/src/test/java/.../domain/service/ScriptPluginExecutionLogServiceTest.java` | Create | pruning test |
| `server/modules-plugin/script-plugin-core/src/test/java/.../controller/ScriptPluginControllerTest.java` | Create | REST delegation test |
| `server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261008.xml` | Modify | append `p_script_plugin*` tables |
| `server/server/wes-server/build.gradle` | Modify | depend on `script-plugin-core` |
| `initdb.d/nacos_config.sql` | Modify | add `plugin-platform` gateway route |
| `server/server/wes-server/src/test/java/org/openwes/config/NacosGatewayRouteTest.java` | Modify | assert the new route |

---

## Task 1: `script-engine` module + `JavaScriptUtils` delegation

**Files:**
- Create: `server/modules-utils/script-engine/build.gradle`, the four main classes, two test classes
- Modify: `server/settings.gradle`, `server/modules-api-platform/api-platform/build.gradle`, `server/modules-api-platform/api-platform/.../utils/JavaScriptUtils.java`

**Interfaces:**
- Produces (relied on by Plan 1b): `GroovyScriptEngine.compile(String) → Class<?>`, `.newInstance(Class) → Object`, `.invoke(Object, String, Object...) → Object`; `ScriptExecutionRuntime(long timeoutMillis, int poolSize)`, `.execute(Supplier<T>) → T`, `AutoCloseable`; `ScriptHashes.sha256(String) → String`; `ScriptCompileException`, `ScriptExecutionException` (both `extends RuntimeException`).

- [ ] **Step 1: Register the module and create its build file**

Append to `server/settings.gradle` (in the `modules-utils` block, after `include 'modules-utils:distribute-scheduler'`):

```groovy
include 'modules-utils:script-engine'
```

Create `server/modules-utils/script-engine/build.gradle`:

```groovy
dependencies {
    implementation project(path: ":modules-utils:common-utils", configuration: 'default')
    implementation 'org.apache.groovy:groovy'
    implementation 'org.apache.commons:commons-lang3'
}
```

(Lombok and the JUnit/Mockito test deps come from the root `subprojects` block; groovy 4.0.22 is version-managed in `server/build.gradle`.)

- [ ] **Step 2: Write the failing engine tests**

`server/modules-utils/script-engine/src/test/java/org/openwes/script/engine/GroovyScriptEngineTest.java`:

```java
package org.openwes.script.engine;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroovyScriptEngineTest {

    private final GroovyScriptEngine engine = new GroovyScriptEngine();

    @Test
    void compile_isCachedByContent() {
        String source = "class Same { Object run(Object p) { return p } }";

        assertSame(engine.compile(source), engine.compile(source));
    }

    @Test
    void invoke_callsNamedMethodWithUntypedParameter() {
        Class<?> clazz = engine.compile("class Wave { Object wave(Object p) { return [1, 2] } }");
        Object instance = engine.newInstance(clazz);

        assertEquals(List.of(1, 2), engine.invoke(instance, "wave", null));
    }

    @Test
    void invoke_mutatesTypedMapParameter() {
        Map<String, Object> input = new HashMap<>();
        input.put("name", "World");
        Class<?> clazz = engine.compile(
                "class Greeter { Object greet(java.util.Map input) { return \"Hello, \" + input.get('name') } }");
        Object instance = engine.newInstance(clazz);

        assertEquals("Hello, World", engine.invoke(instance, "greet", input));
    }

    @Test
    void compile_invalidSource_throwsScriptCompileException() {
        assertThrows(ScriptCompileException.class, () -> engine.compile("class Broken {"));
    }

    @Test
    void compile_blankSource_throwsScriptCompileException() {
        assertThrows(ScriptCompileException.class, () -> engine.compile("   "));
    }

    @Test
    void invoke_missingMethod_throwsScriptExecutionException() {
        Class<?> clazz = engine.compile("class Only { Object x() { return 1 } }");
        Object instance = engine.newInstance(clazz);

        assertThrows(ScriptExecutionException.class, () -> engine.invoke(instance, "nope"));
    }

    @Test
    void invoke_methodThrows_propagatesAsScriptExecutionException() {
        Class<?> clazz = engine.compile("class Boom { Object run(Object p) { throw new IllegalStateException('boom') } }");
        Object instance = engine.newInstance(clazz);

        assertThrows(ScriptExecutionException.class, () -> engine.invoke(instance, "run", null));
    }
}
```

`server/modules-utils/script-engine/src/test/java/org/openwes/script/engine/ScriptExecutionRuntimeTest.java`:

```java
package org.openwes.script.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScriptExecutionRuntimeTest {

    @Test
    void execute_returnsSupplierValue() {
        try (ScriptExecutionRuntime runtime = new ScriptExecutionRuntime(1000, 2)) {
            assertEquals("ok", runtime.execute(() -> "ok"));
        }
    }

    @Test
    void execute_slowScript_timesOut() {
        try (ScriptExecutionRuntime runtime = new ScriptExecutionRuntime(200, 1)) {
            ScriptExecutionException e = assertThrows(ScriptExecutionException.class,
                    () -> runtime.execute(() -> sleepQuietly(5000)));
            assertEquals(true, e.getMessage().contains("timed out"));
        }
    }

    @Test
    void execute_scriptException_isWrapped() {
        try (ScriptExecutionRuntime runtime = new ScriptExecutionRuntime(1000, 1)) {
            ScriptExecutionException e = assertThrows(ScriptExecutionException.class,
                    () -> runtime.execute(() -> {
                        throw new ScriptExecutionException("inner failure");
                    }));
            assertEquals(true, e.getMessage().contains("inner failure"));
        }
    }

    /** Supplier cannot throw InterruptedException, so swallow it here and keep the wait observable. */
    private static Object sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return null;
    }
}
```

- [ ] **Step 3: Run the tests to verify they fail**

```bash
cd server && ./gradlew :modules-utils:script-engine:test
```

Expected: FAIL to compile — classes do not exist.

- [ ] **Step 4: Implement the engine**

`ScriptCompileException.java`:

```java
package org.openwes.script.engine;

/** Thrown when a Groovy script cannot be compiled. */
public class ScriptCompileException extends RuntimeException {

    public ScriptCompileException(String message) {
        super(message);
    }

    public ScriptCompileException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

`ScriptExecutionException.java`:

```java
package org.openwes.script.engine;

/** Thrown when a compiled script fails to instantiate, invoke, or exceeds its timeout. */
public class ScriptExecutionException extends RuntimeException {

    public ScriptExecutionException(String message) {
        super(message);
    }

    public ScriptExecutionException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

`ScriptHashes.java`:

```java
package org.openwes.script.engine;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ScriptHashes {

    private ScriptHashes() {
    }

    public static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
```

`GroovyScriptEngine.java`:

```java
package org.openwes.script.engine;

import groovy.lang.GroovyClassLoader;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.runtime.InvokerHelper;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Compiles and caches Groovy script classes. Cache keys are content hashes
 * ({@link ScriptHashes#sha256}), so identical sources across plugins and revisions share one class.
 */
@Slf4j
public class GroovyScriptEngine {

    private final Map<String, Class<?>> compiledClassCache = new ConcurrentHashMap<>();

    public Class<?> compile(String source) {
        if (source == null || source.isBlank()) {
            throw new ScriptCompileException("script source is empty");
        }
        return compiledClassCache.computeIfAbsent(ScriptHashes.sha256(source), hash -> {
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
     * method parameter with any (or no) type.
     */
    public Object invoke(Object instance, String methodName, Object... args) {
        try {
            return InvokerHelper.invokeMethod(instance, methodName, args);
        } catch (ScriptExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new ScriptExecutionException("script method '" + methodName + "' failed: " + e.getMessage(), e);
        }
    }
}
```

`ScriptExecutionRuntime.java`:

```java
package org.openwes.script.engine;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

/**
 * Runs script invocations on a dedicated pool with a wall-clock timeout. The underlying
 * thread is interrupted on timeout; a script that swallows interrupts may keep running —
 * the documented trust model accepts this (see the spec's security stance).
 */
public class ScriptExecutionRuntime implements AutoCloseable {

    private final ExecutorService executor;
    private final long timeoutMillis;

    public ScriptExecutionRuntime(long timeoutMillis, int poolSize) {
        this.timeoutMillis = timeoutMillis;
        this.executor = Executors.newFixedThreadPool(poolSize, r -> {
            Thread thread = new Thread(r, "script-engine");
            thread.setDaemon(true);
            return thread;
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T execute(Supplier<T> invocation) {
        Callable<T> callable = invocation::get;
        Future<T> future = executor.submit(callable);
        try {
            return future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ScriptExecutionException("script timed out after " + timeoutMillis + "ms", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ScriptExecutionException scriptExecutionException) {
                throw scriptExecutionException;
            }
            throw new ScriptExecutionException("script execution failed: " + cause.getMessage(), cause);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScriptExecutionException("script execution interrupted", e);
        }
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
```

- [ ] **Step 5: Run the engine tests to verify they pass**

```bash
cd server && ./gradlew :modules-utils:script-engine:test
```

Expected: PASS (all 7 engine tests + 3 runtime tests).

- [ ] **Step 6: Write the failing `JavaScriptUtils` delegation test**

`server/modules-api-platform/api-platform/src/test/java/org/openwes/api/platform/utils/JavaScriptUtilsTest.java`:

```java
package org.openwes.api.platform.utils;

import org.junit.jupiter.api.Test;
import org.openwes.common.utils.exception.CommonException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaScriptUtilsTest {

    @Test
    void executeJava_withMethodMarker_callsMarkedMethod() {
        String script = "//java:myMethod\n"
                + "class MyClass { Object myMethod(Object param) { return \"Hello, \" + param } }";

        assertEquals("Hello, World", JavaScriptUtils.executeJava(script, "World"));
    }

    @Test
    void executeJava_withoutMarker_defaultsToConvert() {
        String script = "class MyClass { Object convert(Object param) { return param } }";

        assertEquals("X", JavaScriptUtils.executeJava(script, "X"));
    }

    @Test
    void executeJava_mapParameter_works() {
        String script = "//java:convert\n"
                + "class MyClass { Object convert(Object param) { return param.get('skuCode') } }";
        Map<String, Object> params = new HashMap<>();
        params.put("skuCode", "SKU-1");

        assertEquals("SKU-1", JavaScriptUtils.executeJava(script, params));
    }

    @Test
    void executeJava_invalidScript_throwsCommonExceptionInsteadOfNpe() {
        CommonException e = assertThrows(CommonException.class, () -> JavaScriptUtils.executeJava("class Broken {", "x"));

        assertTrue(e.getMessage().contains("script execution failed"));
    }
}
```

- [ ] **Step 7: Run the test to verify it fails**

```bash
cd server && ./gradlew :modules-api-platform:api-platform:test --tests "org.openwes.api.platform.utils.JavaScriptUtilsTest"
```

Expected: FAIL to compile — `org.openwes.script.engine` is not on the classpath yet; add the dependency in the next step, then the last test fails with NPE (old behavior).

- [ ] **Step 8: Delegate `executeJava` to the engine**

In `server/modules-api-platform/api-platform/build.gradle`, add after the `:modules-utils:common-utils` line:

```groovy
    implementation project(path: ":modules-utils:script-engine", configuration: 'default')
```

In `JavaScriptUtils.java`:

1. Delete the field `private static final ConcurrentHashMap<String, SoftReference<Class<?>>> javaCompiledClassMap = new ConcurrentHashMap<>();` and the now-unused imports (`java.lang.ref.SoftReference`, `java.util.concurrent.ConcurrentHashMap`, `java.lang.reflect.InvocationTargetException`, `java.lang.reflect.Method`, `java.lang.reflect.Modifier`).
2. Add imports: `org.openwes.common.utils.exception.CommonException`, `org.openwes.script.engine.GroovyScriptEngine`, `org.openwes.script.engine.ScriptCompileException`, `org.openwes.script.engine.ScriptExecutionException`.
3. Add the shared engine instance next to `GROOVY_PATTERN`:

```java
    private static final GroovyScriptEngine GROOVY_SCRIPT_ENGINE = new GroovyScriptEngine();
```

4. Replace the body of `executeJava` after the marker extraction with:

```java
    public static Object executeJava(@NonNull String script, Object param) {

        String methodName = "convert";

        Matcher matcher = GROOVY_PATTERN.matcher(script);
        if (matcher.find()) {
            methodName = matcher.group(1);
            script = script.substring(0, matcher.start()) + script.substring(matcher.end()); // More efficient string removal
        }

        try {
            Class<?> clazz = GROOVY_SCRIPT_ENGINE.compile(script);
            return GROOVY_SCRIPT_ENGINE.invoke(GROOVY_SCRIPT_ENGINE.newInstance(clazz), methodName, param);
        } catch (ScriptCompileException | ScriptExecutionException e) {
            log.error("Error executing Java script: {}", script, e);
            throw new CommonException("script execution failed: " + e.getMessage());
        }
    }
```

Keep `executeJs` (GraalVM) and `main` as they are.

- [ ] **Step 9: Run the tests to verify they pass**

```bash
cd server && ./gradlew :modules-api-platform:api-platform:test --tests "org.openwes.api.platform.utils.JavaScriptUtilsTest"
```

Expected: PASS.

- [ ] **Step 10: Commit**

```bash
git add server/settings.gradle server/modules-utils/script-engine server/modules-api-platform/api-platform/build.gradle server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/utils/JavaScriptUtils.java server/modules-api-platform/api-platform/src/test/java/org/openwes/api/platform/utils/JavaScriptUtilsTest.java
git commit -m "feat(script-engine): extract cached Groovy engine with timeout support

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 2: `script-plugin-api` — module, enums, DTOs

**Files:**
- Create: `server/modules-plugin/script-plugin-api/build.gradle`, enums, DTOs
- Modify: `server/settings.gradle`

**Interfaces:**
- Produces: the enum and DTO vocabulary used by every later task (exact class/field names below).

- [ ] **Step 1: Register the module and create its build file**

Append to `server/settings.gradle` (in the `modules-plugin` block, after `include 'modules-plugin:plugin-extension'`):

```groovy
include 'modules-plugin:script-plugin-api'
```

Create `server/modules-plugin/script-plugin-api/build.gradle`:

```groovy
dependencies {
    implementation project(path: ":modules-plugin:plugin-extension", configuration: 'default')
    implementation project(path: ":modules-wes:wes-api", configuration: 'default')
    implementation project(path: ":modules-station:station-api", configuration: 'default')
    implementation project(path: ":modules-api-platform:api-platform-api", configuration: 'default')
    implementation project(path: ":modules-utils:common-utils", configuration: 'default')
    implementation project(path: ":modules-utils:script-engine", configuration: 'default')

    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'com.google.guava:guava'
    implementation 'org.apache.commons:commons-lang3'
    implementation 'org.slf4j:slf4j-api'
}
```

- [ ] **Step 2: Create the constants and enums**

`org/openwes/script/plugin/api/ScriptPluginApiConstants.java`:

```java
package org.openwes.script.plugin.api;

import java.util.List;

public final class ScriptPluginApiConstants {

    private ScriptPluginApiConstants() {
    }

    /** Platform contract version; a plugin's sdk_version must equal this or enabling is refused. */
    public static final String CONTRACT_VERSION = "1.0";

    public static final String APP_WES = "wes";
    public static final String APP_STATION = "station";

    /** Target apps that may host script plugins. A single plugin never spans both (spec: no cross-app plugins in v1). */
    public static final List<String> SUPPORTED_APPS = List.of(APP_WES, APP_STATION);
}
```

`org/openwes/script/plugin/api/enums/ScriptPluginStatusEnum.java`:

```java
package org.openwes.script.plugin.api.enums;

public enum ScriptPluginStatusEnum {
    DRAFT,
    ENABLED,
    DISABLED,
    ARCHIVED
}
```

`org/openwes/script/plugin/api/enums/RevisionOriginEnum.java`:

```java
package org.openwes.script.plugin.api.enums;

public enum RevisionOriginEnum {
    AI,
    MANUAL
}
```

`org/openwes/script/plugin/api/enums/ExtensionType.java`:

```java
package org.openwes.script.plugin.api.enums;

/**
 * The extensible archetypes. Each value maps to exactly one host extension interface and one
 * script method (see {@code PluginContractCatalog}).
 */
public enum ExtensionType {
    /** WES outbound wave grouping hook. */
    OUTBOUND_WAVE_WAVE_ACTION,
    /** WES outbound wave split-to-picking hook. */
    OUTBOUND_WAVE_SPLIT_ACTION,
    /** API platform request validation hook. */
    REQUEST_VALIDATE,
    /** API platform request enrichment hook. */
    REQUEST_SUPPLY,
    /** API platform post-invoke hook. */
    REQUEST_AFTER_INVOKE,
    /** API platform custom request plugin (replaces the built-in invoke). */
    CUSTOM_REQUEST_INVOKE,
    /** Station custom API executed from the workstation. */
    CUSTOM_API,
    /** Entity lifecycle listener (entityName declared per plugin). */
    LIFECYCLE_LISTENER,
    /** Self-provided HTTP endpoint under /plugin-platform/ext/{pluginKey}/**. */
    HTTP_ENDPOINT,
    /** Self-provided cron job. */
    CRON
}
```

`org/openwes/script/plugin/api/enums/PluginManagementMessageType.java`:

```java
package org.openwes.script.plugin.api.enums;

public enum PluginManagementMessageType {
    /** A plugin's status, revision pointer, or content changed; nodes must refresh their registry. */
    PLUGIN_CHANGED
}
```

- [ ] **Step 3: Create the DTOs**

`org/openwes/script/plugin/api/dto/ExtensionDeclarationDTO.java`:

```java
package org.openwes.script.plugin.api.dto;

import lombok.Data;
import org.openwes.script.plugin.api.enums.ExtensionType;

/**
 * One declared extension point of a plugin. Field usage depends on `type`:
 * apiType (REQUEST_*), entityName (LIFECYCLE_LISTENER), customApiCode (CUSTOM_API),
 * httpMethod + path (HTTP_ENDPOINT), cron (CRON). `script` names a key in the revision's script map,
 * `method` is the script method to invoke.
 */
@Data
public class ExtensionDeclarationDTO {
    private ExtensionType type;
    private int priority;
    private String script;
    private String method;
    private String apiType;
    private String entityName;
    private String customApiCode;
    private String httpMethod;
    private String path;
    private String cron;
}
```

`org/openwes/script/plugin/api/dto/ConfigFieldDTO.java`:

```java
package org.openwes.script.plugin.api.dto;

import lombok.Data;

/** One field of the plugin's configuration schema; read inside scripts via ctx.config* . */
@Data
public class ConfigFieldDTO {
    private String field;
    private String type;
    private boolean required;
    private String defaultValue;
}
```

`org/openwes/script/plugin/api/dto/ScriptPluginDTO.java`:

```java
package org.openwes.script.plugin.api.dto;

import lombok.Data;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;

import java.util.List;

@Data
public class ScriptPluginDTO {
    private Long id;
    private String pluginKey;
    private String name;
    private String description;
    private String applySystem;
    private String applyModule;
    private ScriptPluginStatusEnum status;
    private List<ExtensionDeclarationDTO> extensionPoints;
    private List<ConfigFieldDTO> configSchema;
    private String sdkVersion;
    private Long currentRevisionId;
    private String lastValidationStatus;
    private String lastValidationError;
    private Long lastValidationAt;
    private Long updateTime;
}
```

`org/openwes/script/plugin/api/dto/ScriptPluginRevisionDTO.java`:

```java
package org.openwes.script.plugin.api.dto;

import lombok.Data;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;

import java.util.List;
import java.util.Map;

@Data
public class ScriptPluginRevisionDTO {
    private Long id;
    private Long pluginId;
    private Integer revisionNo;
    /** script file name -> groovy source */
    private Map<String, String> scripts;
    private List<ExtensionDeclarationDTO> extensionPoints;
    private List<ConfigFieldDTO> configSchema;
    private String contentHash;
    private String changeSummary;
    private RevisionOriginEnum origin;
    private String aiModel;
    private String promptVersion;
    private String createdBy;
    private Long createdAt;
}
```

`org/openwes/script/plugin/api/dto/ScriptPluginBundleDTO.java`:

```java
package org.openwes.script.plugin.api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Everything a target node needs to materialize one plugin revision: identity, declarations,
 * script sources and the current configuration values.
 */
@Data
public class ScriptPluginBundleDTO {
    private String pluginKey;
    private String name;
    private String applySystem;
    private String applyModule;
    private String sdkVersion;
    private Long revisionId;
    private List<ExtensionDeclarationDTO> declarations;
    private List<ConfigFieldDTO> configSchema;
    private Map<String, String> scripts;
    /** config field -> value, resolved from p_script_plugin_config and schema defaults */
    private Map<String, Object> config;
}
```

`org/openwes/script/plugin/api/dto/ValidationReportDTO.java`:

```java
package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationReportDTO {

    private String pluginKey;
    private Long revisionId;
    private String targetApp;
    private boolean passed;
    private List<ValidationFailureDTO> failures = new ArrayList<>();
    private Long finishedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationFailureDTO {
        private String stage;
        private String message;
    }

    public static ValidationReportDTO passed(String pluginKey, Long revisionId, String targetApp) {
        return new ValidationReportDTO(pluginKey, revisionId, targetApp, true, new ArrayList<>(),
                System.currentTimeMillis());
    }

    public static ValidationReportDTO failed(String pluginKey, Long revisionId, String targetApp, String stage, String message) {
        List<ValidationFailureDTO> failures = new ArrayList<>();
        failures.add(new ValidationFailureDTO(stage, message));
        return new ValidationReportDTO(pluginKey, revisionId, targetApp, false, failures, System.currentTimeMillis());
    }

    public String failureSummary() {
        StringBuilder summary = new StringBuilder();
        failures.forEach(f -> summary.append(f.getStage()).append(": ").append(f.getMessage()).append('\n'));
        return summary.toString();
    }
}
```

`org/openwes/script/plugin/api/dto/ExecutionLogDTO.java`:

```java
package org.openwes.script.plugin.api.dto;

import lombok.Data;
import org.openwes.script.plugin.api.enums.ExtensionType;

@Data
public class ExecutionLogDTO {
    private String pluginKey;
    private Long revisionId;
    private ExtensionType extensionType;
    private boolean success;
    private long durationMs;
    private String errorMessage;
    private Long createdAt;
}
```

`org/openwes/script/plugin/api/dto/PluginManagementMessage.java`:

```java
package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.script.plugin.api.enums.PluginManagementMessageType;

/** Payload of the Redis PLUGIN_LISTEN_PLUGIN_MANAGEMENT channel (Redisson JsonJacksonCodec). */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginManagementMessage {
    private String pluginKey;
    private PluginManagementMessageType type;
}
```

- [ ] **Step 4: Compile the module**

```bash
cd server && ./gradlew :modules-plugin:script-plugin-api:compileJava
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add server/settings.gradle server/modules-plugin/script-plugin-api
git commit -m "feat(script-plugin-api): module with contract enums and DTOs

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 3: `script-plugin-api` — Dubbo API, `ScriptContext`, contract catalog, content hasher

**Files:**
- Create: `IScriptPluginApi.java`, `host/ScriptContext.java`, `host/ScriptHttpClient.java`, `contract/PluginContractCatalog.java`, `ScriptPluginContentHasher.java`
- Test: `PluginContractCatalogTest.java`, `ScriptPluginContentHasherTest.java`

**Interfaces:**
- Consumes: all Task 2 DTOs/enums; extension interfaces from `plugin-extension`; host APIs `IOutboundPlanOrderApi` (wes-api), `IPickingOrderApi` (wes-api), `ICallbackApi` (api-platform-api), `IStationApi`/`IPtlApi` (station-api).
- Produces (relied on by Plans 1b/2): `IScriptPluginApi` (6 methods below), `ScriptContext` (methods below), `PluginContractCatalog.archetypes()/archetype(ExtensionType)/beanCatalog()/toPromptFragment(String, List<ExtensionType>)`, `ScriptPluginContentHasher.hash(...)`.

- [ ] **Step 1: Write the failing catalog and hasher tests**

`server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/contract/PluginContractCatalogTest.java`:

```java
package org.openwes.script.plugin.api.contract;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.enums.ExtensionType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginContractCatalogTest {

    @Test
    void everyExtensionType_hasAnArchetypeWithAScriptMethod() {
        for (ExtensionType type : ExtensionType.values()) {
            PluginContractCatalog.ArchetypeDescriptor archetype = PluginContractCatalog.archetype(type).orElseThrow();
            assertEquals(type, archetype.type());
            assertFalse(archetype.scriptMethod().isBlank());
        }
    }

    @Test
    void archetypeExtensionPointClasses_resolveAndDeclareTheScriptMethod() throws Exception {
        for (PluginContractCatalog.ArchetypeDescriptor archetype : PluginContractCatalog.archetypes()) {
            if (archetype.extensionPointClassName() == null) {
                continue; // dispatcher-backed archetypes (HTTP_ENDPOINT, CRON)
            }
            Class<?> extensionPoint = Class.forName(archetype.extensionPointClassName());
            // wave/split/validate/... must be declared somewhere in the interface hierarchy
            assertTrue(declaresMethod(extensionPoint, archetype.scriptMethod()),
                    extensionPoint.getName() + " must declare " + archetype.scriptMethod());
        }
    }

    private boolean declaresMethod(Class<?> clazz, String methodName) {
        Class<?> current = clazz;
        while (current != null) {
            for (java.lang.reflect.Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return true;
                }
            }
            current = current.getSuperclass();
        }
        return false;
    }

    @Test
    void beanCatalog_listsResolvableTypesPerApp() throws Exception {
        assertEquals(2, PluginContractCatalog.beanCatalog().size());
        for (var entry : PluginContractCatalog.beanCatalog().entrySet()) {
            assertFalse(entry.getValue().isEmpty(), entry.getKey() + " must ship a bean catalog");
            for (PluginContractCatalog.BeanDescriptor bean : entry.getValue()) {
                Class.forName(bean.type().getName());
            }
        }
    }

    @Test
    void promptFragment_containsOnlyRequestedArchetypes() {
        String fragment = PluginContractCatalog.toPromptFragment("wes",
                java.util.List.of(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION, ExtensionType.CRON));

        assertTrue(fragment.contains("OUTBOUND_WAVE_WAVE_ACTION"));
        assertTrue(fragment.contains("CRON"));
        assertFalse(fragment.contains("CUSTOM_API"));
        assertTrue(fragment.contains("ctx."));
    }
}
```

`server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/ScriptPluginContentHasherTest.java`:

```java
package org.openwes.script.plugin.api;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ScriptPluginContentHasherTest {

    private ExtensionDeclarationDTO declaration() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        declaration.setScript("Wave.groovy");
        declaration.setMethod("wave");
        declaration.setPriority(100);
        return declaration;
    }

    @Test
    void hash_isStableForSameContent() {
        String first = ScriptPluginContentHasher.hash(List.of(declaration()), Map.of("Wave.groovy", "src"));
        String second = ScriptPluginContentHasher.hash(List.of(declaration()), Map.of("Wave.groovy", "src"));

        assertEquals(first, second);
    }

    @Test
    void hash_changesWhenScriptChanges() {
        String first = ScriptPluginContentHasher.hash(List.of(declaration()), Map.of("Wave.groovy", "src"));
        String second = ScriptPluginContentHasher.hash(List.of(declaration()), Map.of("Wave.groovy", "src2"));

        assertNotEquals(first, second);
    }

    @Test
    void hash_changesWhenPriorityChanges() {
        ExtensionDeclarationDTO changed = declaration();
        changed.setPriority(1);
        assertNotEquals(
                ScriptPluginContentHasher.hash(List.of(declaration()), Map.of("Wave.groovy", "src")),
                ScriptPluginContentHasher.hash(List.of(changed), Map.of("Wave.groovy", "src")));
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

```bash
cd server && ./gradlew :modules-plugin:script-plugin-api:test
```

Expected: FAIL to compile — classes do not exist.

- [ ] **Step 3: Create the Dubbo API and script-facing host contracts**

`org/openwes/script/plugin/api/IScriptPluginApi.java`:

```java
package org.openwes.script.plugin.api;

import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;

import java.util.List;

/**
 * Management-plane API consumed by every node's script-plugin runtime.
 * Provided by the wes node (script-plugin-core is hosted there).
 */
public interface IScriptPluginApi {

    /** All ENABLED bundles for one app, ordered by pluginKey for deterministic materialization. */
    List<ScriptPluginBundleDTO> getEnabledPlugins(String applySystem);

    /** Monotonic registry stamp for one app (max plugin updateTime); used by the periodic safety poll. */
    long getRegistryVersion(String applySystem);

    /** The validation request waiting for this app, or null. Cleared when the node reports back. */
    ScriptPluginBundleDTO getPendingValidation(String applySystem);

    void reportValidation(ValidationReportDTO report);

    void reportExecution(ExecutionLogDTO executionLog);

    /** Runtime-triggered disable (e.g. cron auto-disable after repeated failures). */
    void disable(String pluginKey, String reason);
}
```

`org/openwes/script/plugin/api/host/ScriptHttpClient.java`:

```java
package org.openwes.script.plugin.api.host;

/** ctx.http — timeout-enforced outbound HTTP for scripts. */
public interface ScriptHttpClient {

    String postJson(String url, String jsonBody, long timeoutMillis);

    String get(String url, long timeoutMillis);
}
```

`org/openwes/script/plugin/api/host/ScriptContext.java`:

```java
package org.openwes.script.plugin.api.host;

import org.openwes.api.platform.api.ICallbackApi;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;
import org.openwes.station.api.IStationApi;
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi;
import org.openwes.wes.api.outbound.IPickingOrderApi;
import org.slf4j.Logger;

import java.util.List;

/**
 * Host context passed to every script method. Facade methods are sugar over bean lookup against
 * the target app's catalog (see PluginContractCatalog.beanCatalog); availability differs per app.
 * This is a usability contract, not a security boundary — scripts run with full JVM access.
 */
public interface ScriptContext {

    String pluginKey();

    /** Child (revision) context first, then the main application context. */
    <T> T bean(Class<T> type);

    Object bean(String name);

    Object configObject(String field);

    String configString(String field);

    Integer configInt(String field);

    Boolean configBool(String field);

    List<ConfigFieldDTO> configSchema();

    ScriptHttpClient http();

    Logger log();

    default IOutboundPlanOrderApi outbound() {
        return bean(IOutboundPlanOrderApi.class);
    }

    default IPickingOrderApi pickingOrder() {
        return bean(IPickingOrderApi.class);
    }

    default ICallbackApi callback() {
        return bean(ICallbackApi.class);
    }

    @SuppressWarnings("rawtypes")
    default IStationApi station() {
        return bean(IStationApi.class);
    }
}
```

- [ ] **Step 4: Create the contract catalog**

`org/openwes/script/plugin/api/contract/PluginContractCatalog.java`:

```java
package org.openwes.script.plugin.api.contract;

import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Single source of contract truth. The AI prompt, the generated documentation and the enable-time
 * static validation all read from here, so they cannot drift from the code.
 */
public final class PluginContractCatalog {

    private PluginContractCatalog() {
    }

    /**
     * @param type                   the archetype
     * @param extensionPointClassName host interface the proxy implements, or null for dispatcher-backed archetypes
     * @param scriptMethod           the method name a script must declare
     * @param blocking               whether an exception blocks the host operation
     */
    public record ArchetypeDescriptor(ExtensionType type, String extensionPointClassName, String scriptMethod,
                                      boolean blocking) {
    }

    /**
     * @param name catalog label (also accepted by ctx.bean(String)); resolution is type-first
     * @param type Spring bean type present in that app
     */
    public record BeanDescriptor(String name, Class<?> type) {
    }

    public static final List<ArchetypeDescriptor> ARCHETYPES = List.of(
            new ArchetypeDescriptor(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION,
                    "org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveWaveAction", "wave", true),
            new ArchetypeDescriptor(ExtensionType.OUTBOUND_WAVE_SPLIT_ACTION,
                    "org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveSplitAction", "split", true),
            new ArchetypeDescriptor(ExtensionType.REQUEST_VALIDATE,
                    "org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin", "validate", true),
            new ArchetypeDescriptor(ExtensionType.REQUEST_SUPPLY,
                    "org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin", "supply", false),
            new ArchetypeDescriptor(ExtensionType.REQUEST_AFTER_INVOKE,
                    "org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin", "afterInvoke", false),
            new ArchetypeDescriptor(ExtensionType.CUSTOM_REQUEST_INVOKE,
                    "org.openwes.plugin.extension.business.api.platform.request.ICustomRequestPlugin", "invoke", true),
            new ArchetypeDescriptor(ExtensionType.CUSTOM_API,
                    "org.openwes.plugin.extension.business.station.ICustomApiPlugin", "execute", true),
            new ArchetypeDescriptor(ExtensionType.LIFECYCLE_LISTENER,
                    "org.openwes.plugin.extension.business.IEntityLifecycleListener", "afterStatusChange", false),
            new ArchetypeDescriptor(ExtensionType.HTTP_ENDPOINT, null, "handle", true),
            new ArchetypeDescriptor(ExtensionType.CRON, null, "run", false));

    private static final Map<String, List<BeanDescriptor>> BEAN_CATALOG = buildBeanCatalog();

    public static List<ArchetypeDescriptor> archetypes() {
        return ARCHETYPES;
    }

    public static Optional<ArchetypeDescriptor> archetype(ExtensionType type) {
        return ARCHETYPES.stream().filter(a -> a.type() == type).findFirst();
    }

    public static Map<String, List<BeanDescriptor>> beanCatalog() {
        return BEAN_CATALOG;
    }

    /**
     * Human/AI-readable contract subset for one app and a set of archetypes — the AI prompt and the
     * docs rewrite embed exactly this text.
     */
    public static String toPromptFragment(String app, List<ExtensionType> types) {
        StringBuilder fragment = new StringBuilder();
        fragment.append("Contract version: ").append(ScriptPluginApiConstants.CONTRACT_VERSION).append('\n');
        fragment.append("Target app: ").append(app).append('\n');
        fragment.append("Archetypes:\n");
        for (ArchetypeDescriptor archetype : ARCHETYPES) {
            if (!types.contains(archetype.type())) {
                continue;
            }
            fragment.append("- ").append(archetype.type())
                    .append(": implement '")
                    .append(archetype.extensionPointClassName() == null ? "dispatcher" : archetype.extensionPointClassName())
                    .append("', script method '")
                    .append(archetype.scriptMethod()).append("(.., ctx)'")
                    .append(archetype.blocking() ? ", blocking (exception blocks the host operation)"
                            : ", non-blocking (exception is logged and execution continues)")
                    .append('\n');
        }
        fragment.append("Available beans (ctx.bean / facade methods):\n");
        for (BeanDescriptor bean : BEAN_CATALOG.getOrDefault(app, List.of())) {
            fragment.append("- ").append(bean.name()).append(" -> ").append(bean.type().getName()).append('\n');
        }
        fragment.append("Config access: ctx.configString/configInt/configBool(field); secrets only via ctx.config.\n");
        fragment.append("Logging: ctx.log(). No hardcoded pluginKey; use ctx.pluginKey().");
        return fragment.toString();
    }

    private static Map<String, List<BeanDescriptor>> buildBeanCatalog() {
        Map<String, List<BeanDescriptor>> catalog = new LinkedHashMap<>();
        catalog.put(ScriptPluginApiConstants.APP_WES, List.of(
                new BeanDescriptor("outbound", org.openwes.wes.api.outbound.IOutboundPlanOrderApi.class),
                new BeanDescriptor("pickingOrder", org.openwes.wes.api.outbound.IPickingOrderApi.class),
                new BeanDescriptor("callback", org.openwes.api.platform.api.ICallbackApi.class)));
        catalog.put(ScriptPluginApiConstants.APP_STATION, List.of(
                new BeanDescriptor("station", org.openwes.station.api.IStationApi.class),
                new BeanDescriptor("ptl", org.openwes.station.api.IPtlApi.class)));
        return catalog;
    }
}
```

- [ ] **Step 5: Create the content hasher**

`org/openwes/script/plugin/api/ScriptPluginContentHasher.java`:

```java
package org.openwes.script.plugin.api;

import org.openwes.script.engine.ScriptHashes;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Canonical content hash of a revision: declarations (sorted) + script sources + config schema.
 * Two revisions with the same hash are behaviorally identical.
 */
public final class ScriptPluginContentHasher {

    private ScriptPluginContentHasher() {
    }

    public static String hash(List<ExtensionDeclarationDTO> declarations, Map<String, String> scripts) {
        StringBuilder canonical = new StringBuilder();
        declarations.stream()
                .sorted(Comparator.comparing(d -> d.getType().name() + "|" + nullSafe(d.getScript())
                        + "|" + nullSafe(d.getMethod())))
                .forEach(d -> canonical.append(d.getType()).append('|').append(nullSafe(d.getPriority()))
                        .append('|').append(nullSafe(d.getScript())).append('|').append(nullSafe(d.getMethod()))
                        .append('|').append(nullSafe(d.getApiType())).append('|').append(nullSafe(d.getEntityName()))
                        .append('|').append(nullSafe(d.getCustomApiCode())).append('|')
                        .append(nullSafe(d.getHttpMethod())).append('|').append(nullSafe(d.getPath()))
                        .append('|').append(nullSafe(d.getCron())).append('\n'));
        scripts.keySet().stream().sorted().forEach(name ->
                canonical.append(name).append(':').append(scripts.get(name)).append('\n'));
        return ScriptHashes.sha256(canonical.toString());
    }

    private static String nullSafe(Object value) {
        return value == null ? "" : value.toString();
    }
}
```

- [ ] **Step 6: Run the tests to verify they pass**

```bash
cd server && ./gradlew :modules-plugin:script-plugin-api:test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add server/modules-plugin/script-plugin-api
git commit -m "feat(script-plugin-api): Dubbo API, script context contract and plugin contract catalog

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 4: `script-plugin-core` — module, entities, repositories

**Files:**
- Create: `server/modules-plugin/script-plugin-core/build.gradle`, 4 entities, 4 repositories, state-machine test
- Modify: `server/settings.gradle`

**Interfaces:**
- Produces: entity/repository classes used by Tasks 5–7 verbatim.

- [ ] **Step 1: Register the module and create its build file**

Append to `server/settings.gradle` (after the `script-plugin-api` include):

```groovy
include 'modules-plugin:script-plugin-core'
```

Create `server/modules-plugin/script-plugin-core/build.gradle`:

```groovy
dependencies {
    implementation project(path: ":modules-plugin:script-plugin-api", configuration: 'default')
    implementation project(path: ":modules-utils:common-utils", configuration: 'default')

    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    implementation 'org.apache.dubbo:dubbo-spring-boot-starter'

    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'com.google.guava:guava'
    implementation 'org.apache.commons:commons-lang3'
    implementation 'io.swagger.core.v3:swagger-annotations'
}
```

- [ ] **Step 2: Write the failing state-machine test**

`server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/entity/ScriptPluginStateTest.java`:

```java
package org.openwes.script.plugin.core.domain.entity;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScriptPluginStateTest {

    private ScriptPlugin plugin(ScriptPluginStatusEnum status) {
        ScriptPlugin plugin = new ScriptPlugin();
        plugin.setPluginKey("packing-wave");
        plugin.setStatus(status);
        return plugin;
    }

    @Test
    void enable_fromDraft_ok() {
        ScriptPlugin plugin = plugin(ScriptPluginStatusEnum.DRAFT);
        plugin.enable();
        assertEquals(ScriptPluginStatusEnum.ENABLED, plugin.getStatus());
    }

    @Test
    void enable_fromDisabled_ok() {
        ScriptPlugin plugin = plugin(ScriptPluginStatusEnum.DISABLED);
        plugin.enable();
        assertEquals(ScriptPluginStatusEnum.ENABLED, plugin.getStatus());
    }

    @Test
    void enable_fromEnabled_throws() {
        assertThrows(IllegalStateException.class, () -> plugin(ScriptPluginStatusEnum.ENABLED).enable());
    }

    @Test
    void disable_requiresEnabled() {
        ScriptPlugin plugin = plugin(ScriptPluginStatusEnum.ENABLED);
        plugin.disable();
        assertEquals(ScriptPluginStatusEnum.DISABLED, plugin.getStatus());
        assertThrows(IllegalStateException.class, () -> plugin(ScriptPluginStatusEnum.DRAFT).disable());
    }

    @Test
    void archive_fromAnyNonArchivedState_ok() {
        ScriptPlugin draft = plugin(ScriptPluginStatusEnum.DRAFT);
        draft.archive();
        assertEquals(ScriptPluginStatusEnum.ARCHIVED, draft.getStatus());
        ScriptPlugin enabled = plugin(ScriptPluginStatusEnum.ENABLED);
        enabled.archive();
        assertEquals(ScriptPluginStatusEnum.ARCHIVED, enabled.getStatus());
    }

    @Test
    void archive_isTerminal() {
        assertThrows(IllegalStateException.class, () -> plugin(ScriptPluginStatusEnum.ARCHIVED).archive());
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

```bash
cd server && ./gradlew :modules-plugin:script-plugin-core:test
```

Expected: FAIL to compile — entities do not exist.

- [ ] **Step 4: Create the four entities**

`org/openwes/script/plugin/core/domain/entity/ScriptPlugin.java`:

```java
package org.openwes.script.plugin.core.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "p_script_plugin", indexes = {
        @Index(unique = true, name = "uk_script_plugin_key", columnList = "pluginKey"),
        @Index(name = "idx_script_plugin_app_status", columnList = "applySystem,status")
})
@DynamicUpdate
public class ScriptPlugin extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    @Comment("插件key，声明式，全局唯一")
    private String pluginKey;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 512)
    private String description = "";

    /** Target app: wes / station — see ScriptPluginApiConstants. */
    @Column(nullable = false, length = 32)
    private String applySystem;

    @Column(length = 64)
    private String applyModule = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ScriptPluginStatusEnum status;

    /** JSON array of ExtensionDeclarationDTO. */
    @Column(nullable = false, columnDefinition = "json")
    private String extensionPoints;

    /** JSON array of ConfigFieldDTO, nullable. */
    @Column(columnDefinition = "json")
    private String configSchema;

    @Column(nullable = false, length = 16)
    private String sdkVersion;

    private Long currentRevisionId;

    @Column(length = 16)
    private String lastValidationStatus;

    @Column(columnDefinition = "text")
    private String lastValidationError;

    private Long lastValidationAt;

    @Version
    private Long version;

    public void enable() {
        if (status != ScriptPluginStatusEnum.DRAFT && status != ScriptPluginStatusEnum.DISABLED) {
            throw new IllegalStateException("only DRAFT or DISABLED plugins can be enabled");
        }
        this.status = ScriptPluginStatusEnum.ENABLED;
    }

    public void disable() {
        if (status != ScriptPluginStatusEnum.ENABLED) {
            throw new IllegalStateException("only ENABLED plugins can be disabled");
        }
        this.status = ScriptPluginStatusEnum.DISABLED;
    }

    public void archive() {
        if (status == ScriptPluginStatusEnum.ARCHIVED) {
            throw new IllegalStateException("plugin is already archived");
        }
        this.status = ScriptPluginStatusEnum.ARCHIVED;
    }
}
```

`org/openwes/script/plugin/core/domain/entity/ScriptPluginRevision.java` (no `@Version`, no `UpdateUserPO` — immutable):

```java
package org.openwes.script.plugin.core.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;

@Data
@Entity
@Table(name = "p_script_plugin_revision", indexes = {
        @Index(unique = true, name = "uk_script_revision_no", columnList = "pluginId,revisionNo")})
public class ScriptPluginRevision {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false)
    private Long pluginId;

    @Column(nullable = false)
    private Integer revisionNo;

    /** JSON object: script file name -> groovy source. */
    @Column(nullable = false, columnDefinition = "json")
    private String scripts;

    /** JSON array of ExtensionDeclarationDTO at creation time. */
    @Column(nullable = false, columnDefinition = "json")
    private String extensionPointsSnapshot;

    @Column(columnDefinition = "json")
    private String configSchemaSnapshot;

    @Column(nullable = false, length = 64)
    private String contentHash;

    @Column(nullable = false, length = 512)
    private String changeSummary = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RevisionOriginEnum origin;

    @Column(length = 64)
    private String aiModel;

    @Column(length = 32)
    private String promptVersion;

    @Column(nullable = false, length = 60)
    private String createdBy = "";

    @Column(nullable = false)
    private Long createdAt;

    @Comment("revisions are immutable; rollback only moves ScriptPlugin.currentRevisionId")
    public void markImmutable() {
        // intentionally empty: documents the invariant that no update path exists for revisions
    }
}
```

`org/openwes/script/plugin/core/domain/entity/ScriptPluginConfig.java`:

```java
package org.openwes.script.plugin.core.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "p_script_plugin_config", indexes = {
        @Index(unique = true, name = "uk_script_config_plugin", columnList = "pluginId")})
@DynamicUpdate
public class ScriptPluginConfig extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false)
    private Long pluginId;

    @Column(nullable = false, columnDefinition = "json")
    private String configJson;

    @Version
    private Long lockVersion;
}
```

`org/openwes/script/plugin/core/domain/entity/ScriptPluginExecutionLog.java`:

```java
package org.openwes.script.plugin.core.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.script.plugin.api.enums.ExtensionType;

@Data
@Entity
@Table(name = "p_script_plugin_execution_log", indexes = {
        @Index(name = "idx_script_exec_plugin", columnList = "pluginKey,id")})
public class ScriptPluginExecutionLog {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, length = 64)
    private String pluginKey;

    @Column(nullable = false)
    private Long revisionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ExtensionType extensionType;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private long durationMs;

    @Column(length = 1024)
    private String errorMessage;

    @Column(nullable = false)
    private Long createdAt;
}
```

- [ ] **Step 5: Create the four repositories**

`org/openwes/script/plugin/core/domain/repository/ScriptPluginRepository.java`:

```java
package org.openwes.script.plugin.core.domain.repository;

import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.entity.ScriptPlugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScriptPluginRepository extends JpaRepository<ScriptPlugin, Long> {

    Optional<ScriptPlugin> findByPluginKey(String pluginKey);

    List<ScriptPlugin> findByApplySystemAndStatusOrderByPluginKeyAsc(String applySystem, ScriptPluginStatusEnum status);

    // No status filter: the registry version must stay monotonic across disables, otherwise a
    // version-poll on a node whose copy still has the plugin enabled would never see the change.
    @Query("select coalesce(max(p.updateTime), 0) from ScriptPlugin p where p.applySystem = :applySystem")
    long findMaxUpdateTime(@Param("applySystem") String applySystem);
}
```

`ScriptPluginRevisionRepository.java`:

```java
package org.openwes.script.plugin.core.domain.repository;

import org.openwes.script.plugin.core.domain.entity.ScriptPluginRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScriptPluginRevisionRepository extends JpaRepository<ScriptPluginRevision, Long> {

    Optional<ScriptPluginRevision> findTopByPluginIdOrderByRevisionNoDesc(Long pluginId);

    List<ScriptPluginRevision> findAllByPluginIdOrderByRevisionNoDesc(Long pluginId);
}
```

`ScriptPluginConfigRepository.java`:

```java
package org.openwes.script.plugin.core.domain.repository;

import org.openwes.script.plugin.core.domain.entity.ScriptPluginConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScriptPluginConfigRepository extends JpaRepository<ScriptPluginConfig, Long> {

    Optional<ScriptPluginConfig> findByPluginId(Long pluginId);
}
```

`ScriptPluginExecutionLogRepository.java`:

```java
package org.openwes.script.plugin.core.domain.repository;

import org.openwes.script.plugin.core.domain.entity.ScriptPluginExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScriptPluginExecutionLogRepository extends JpaRepository<ScriptPluginExecutionLog, Long> {

    long countByPluginKey(String pluginKey);

    @Modifying
    @Query(value = """
            DELETE FROM p_script_plugin_execution_log
            WHERE plugin_key = :pluginKey
              AND id NOT IN (SELECT id FROM (
                    SELECT id FROM p_script_plugin_execution_log
                    WHERE plugin_key = :pluginKey
                    ORDER BY id DESC LIMIT :keep) newest)
            """, nativeQuery = true)
    int pruneToKeep(@Param("pluginKey") String pluginKey, @Param("keep") int keep);
}
```

- [ ] **Step 6: Run the tests to verify they pass**

```bash
cd server && ./gradlew :modules-plugin:script-plugin-core:test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add server/settings.gradle server/modules-plugin/script-plugin-core
git commit -m "feat(script-plugin-core): persistence entities and repositories

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 5: `ScriptPluginService` — lifecycle, bundles, validation orchestration

**Files:**
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginService.java`
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/ScriptPluginMaterializationValidator.java` (SPI implemented by the runtime in Plan 1b; the core only consumes it)
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginServiceTest.java`

**Interfaces:**
- Consumes: `IScriptPluginApi` bundle types, the four repositories from Task 4, `RedisUtils.publish(String, Object)` (`common-utils`), `RedisConstants.PLUGIN_LISTEN_PLUGIN_MANAGEMENT`.
- Produces (relied on by Tasks 6/7 and Plan 1b):
  - `ScriptPluginMaterializationValidator` — `String targetApp(); ValidationReportDTO validate(ScriptPluginBundleDTO bundle);`
  - `ScriptPluginService.createDraft(ScriptPluginDTO)`, `.saveRevision(String, ScriptPluginRevisionDTO)`, `.saveRevision(String, ScriptPluginRevisionDTO, RevisionOriginEnum, String, String)`, `.detail(String)`, `.list()`, `.revisions(String)`, `.enable(String)`, `.disable(String, String)`, `.archive(String)`, `.rollback(String, long)`, `.validate(String)`, `.getBundle(String)`, `.getEnabledBundles(String)`, `.getRegistryVersion(String)`, `.findPendingValidation(String)`, `.reportValidation(ValidationReportDTO)`

- [ ] **Step 1: Create the validator SPI**

`org/openwes/script/plugin/api/ScriptPluginMaterializationValidator.java`:

```java
package org.openwes.script.plugin.api;

import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;

/**
 * Implemented by the script-plugin runtime on each node (Plan 1b). The core orchestrator picks the
 * validator whose targetApp matches the plugin's applySystem and runs it in-process; for remote
 * apps the request travels over the Redis management channel and the node reports back via
 * IScriptPluginApi.reportValidation.
 */
public interface ScriptPluginMaterializationValidator {

    String targetApp();

    ValidationReportDTO validate(ScriptPluginBundleDTO bundle);
}
```

- [ ] **Step 2: Write the failing service test**

`server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginServiceTest.java`:

```java
package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.dto.PluginManagementMessage;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.entity.ScriptPlugin;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginRevision;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginConfigRepository;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginRepository;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginRevisionRepository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScriptPluginServiceTest {

    @Mock
    private ScriptPluginRepository pluginRepository;
    @Mock
    private ScriptPluginRevisionRepository revisionRepository;
    @Mock
    private ScriptPluginConfigRepository configRepository;
    @Mock
    private RedisUtils redisUtils;

    private ScriptPluginService service;

    @BeforeEach
    void setUp() {
        service = new ScriptPluginService(pluginRepository, revisionRepository, configRepository,
                List.of(), redisUtils);
    }

    private ScriptPlugin plugin(String pluginKey, String applySystem, ScriptPluginStatusEnum status) {
        ScriptPlugin plugin = new ScriptPlugin();
        plugin.setId(1L);
        plugin.setPluginKey(pluginKey);
        plugin.setName("Packing wave rule");
        plugin.setApplySystem(applySystem);
        plugin.setApplyModule("");
        plugin.setSdkVersion(ScriptPluginApiConstants.CONTRACT_VERSION);
        plugin.setStatus(status);
        plugin.setExtensionPoints("[]");
        plugin.setCurrentRevisionId(11L);
        return plugin;
    }

    private ScriptPluginRevision revision() {
        ScriptPluginRevision revision = new ScriptPluginRevision();
        revision.setId(11L);
        revision.setPluginId(1L);
        revision.setRevisionNo(1);
        revision.setScripts("{}");
        revision.setExtensionPointsSnapshot("[]");
        revision.setContentHash("hash");
        revision.setCreatedAt(System.currentTimeMillis());
        return revision;
    }

    @Test
    void enable_withPassingLocalValidator_enablesAndPublishes() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision()));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());
        ScriptPluginMaterializationValidator validator = mock(ScriptPluginMaterializationValidator.class);
        when(validator.targetApp()).thenReturn("wes");
        when(validator.validate(any())).thenReturn(
                ValidationReportDTO.passed("packing-wave", 11L, "wes"));
        service = new ScriptPluginService(pluginRepository, revisionRepository, configRepository,
                List.of(validator), redisUtils);

        ScriptPluginDTO dto = service.enable("packing-wave");

        assertEquals(ScriptPluginStatusEnum.ENABLED, dto.getStatus());
        assertEquals("PASSED", dto.getLastValidationStatus());
        verify(redisUtils).publish(eq(org.openwes.common.utils.constants.RedisConstants.PLUGIN_LISTEN_PLUGIN_MANAGEMENT),
                any(PluginManagementMessage.class));
    }

    @Test
    void enable_withFailingLocalValidator_refusesAndStaysDraft() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision()));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());
        ScriptPluginMaterializationValidator validator = mock(ScriptPluginMaterializationValidator.class);
        when(validator.targetApp()).thenReturn("wes");
        when(validator.validate(any())).thenReturn(
                ValidationReportDTO.failed("packing-wave", 11L, "wes", "compile", "line 3: unexpected token"));
        service = new ScriptPluginService(pluginRepository, revisionRepository, configRepository,
                List.of(validator), redisUtils);

        CommonException e = assertThrows(CommonException.class, () -> service.enable("packing-wave"));

        assertTrue(e.getMessage().contains("compile: line 3"));
        assertEquals(ScriptPluginStatusEnum.DRAFT, plugin.getStatus());
        assertEquals("FAILED", plugin.getLastValidationStatus());
        verify(redisUtils, never()).publish(any(), any());
    }

    @Test
    void enable_withWrongSdkVersion_isRefusedBeforeValidation() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        plugin.setSdkVersion("0.9");
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));

        assertThrows(CommonException.class, () -> service.enable("packing-wave"));
        verify(revisionRepository, never()).findById(any());
    }

    @Test
    void validate_forRemoteApp_waitsForNodeReport() throws Exception {
        ScriptPlugin plugin = plugin("station-api", "station", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("station-api")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision()));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<ValidationReportDTO> result = executor.submit(() -> service.validate("station-api"));
            for (int i = 0; i < 200 && service.findPendingValidation("station") == null; i++) {
                Thread.sleep(10);
            }
            assertNotNull(service.findPendingValidation("station"), "validation request must be pending for the station poller");

            service.reportValidation(ValidationReportDTO.passed("station-api", 11L, "station"));

            assertTrue(result.get(2, TimeUnit.SECONDS).isPassed());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void validate_forRemoteApp_timesOutWhenNodeNeverReports() {
        ScriptPlugin plugin = plugin("station-api", "station", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("station-api")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision()));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());
        service = new ScriptPluginService(pluginRepository, revisionRepository, configRepository,
                List.of(), redisUtils);
        org.springframework.test.util.ReflectionTestUtils.setField(service, "validationTimeoutMs", 100L);

        ValidationReportDTO report = service.validate("station-api");

        assertTrue(!report.isPassed());
        assertTrue(report.failureSummary().contains("did not report"));
    }

    @Test
    void rollback_movesThePointerAndPublishes() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.ENABLED);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        ScriptPluginRevision old = revision();
        old.setId(7L);
        old.setExtensionPointsSnapshot("[{\"type\":\"CRON\"}]");
        when(revisionRepository.findById(7L)).thenReturn(Optional.of(old));

        ScriptPluginDTO dto = service.rollback("packing-wave", 7L);

        assertEquals(7L, dto.getCurrentRevisionId());
        verify(redisUtils).publish(any(), any(PluginManagementMessage.class));
    }

    @Test
    void getEnabledBundles_returnsBundlesOrderedByPluginKey() {
        ScriptPlugin b = plugin("b-plugin", "wes", ScriptPluginStatusEnum.ENABLED);
        b.setId(2L);
        ScriptPlugin a = plugin("a-plugin", "wes", ScriptPluginStatusEnum.ENABLED);
        when(pluginRepository.findByApplySystemAndStatusOrderByPluginKeyAsc("wes", ScriptPluginStatusEnum.ENABLED))
                .thenReturn(List.of(a, b));
        ScriptPluginRevision revision = revision();
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision));
        when(configRepository.findByPluginId(any())).thenReturn(Optional.empty());

        List<ScriptPluginBundleDTO> bundles = service.getEnabledBundles("wes");

        assertEquals(List.of("a-plugin", "b-plugin"), bundles.stream().map(ScriptPluginBundleDTO::getPluginKey).toList());
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

```bash
cd server && ./gradlew :modules-plugin:script-plugin-core:test --tests "org.openwes.script.plugin.core.domain.service.ScriptPluginServiceTest"
```

Expected: FAIL to compile — `ScriptPluginService` does not exist.

- [ ] **Step 4: Implement `ScriptPluginService`**

`org/openwes/script/plugin/core/domain/service/ScriptPluginService.java`:

```java
package org.openwes.script.plugin.core.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.ScriptPluginContentHasher;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.dto.*;
import org.openwes.script.plugin.api.enums.PluginManagementMessageType;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.entity.ScriptPlugin;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginConfig;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginRevision;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginConfigRepository;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginRepository;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginRevisionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScriptPluginService {

    private final ScriptPluginRepository pluginRepository;
    private final ScriptPluginRevisionRepository revisionRepository;
    private final ScriptPluginConfigRepository configRepository;
    private final List<ScriptPluginMaterializationValidator> materializationValidators;
    private final RedisUtils redisUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${script-plugin.validation-timeout-ms:30000}")
    private long validationTimeoutMs = 30000;

    private record PendingValidation(String app, CompletableFuture<ValidationReportDTO> future) {
    }

    private final Map<String, PendingValidation> pendingValidations = new ConcurrentHashMap<>();

    public ScriptPluginDTO createDraft(ScriptPluginDTO dto) {
        if (StringUtils.isBlank(dto.getPluginKey()) || StringUtils.isBlank(dto.getName())
                || StringUtils.isBlank(dto.getApplySystem())) {
            throw new CommonException("pluginKey, name and applySystem are required");
        }
        if (!ScriptPluginApiConstants.SUPPORTED_APPS.contains(dto.getApplySystem())) {
            throw new CommonException("unsupported applySystem: " + dto.getApplySystem());
        }
        pluginRepository.findByPluginKey(dto.getPluginKey()).ifPresent(existing -> {
            throw new CommonException("script plugin already exists: " + dto.getPluginKey());
        });

        ScriptPlugin plugin = new ScriptPlugin();
        plugin.setPluginKey(dto.getPluginKey());
        plugin.setName(dto.getName());
        plugin.setDescription(dto.getDescription() == null ? "" : dto.getDescription());
        plugin.setApplySystem(dto.getApplySystem());
        plugin.setApplyModule(dto.getApplyModule() == null ? "" : dto.getApplyModule());
        plugin.setSdkVersion(ScriptPluginApiConstants.CONTRACT_VERSION);
        plugin.setExtensionPoints("[]");
        plugin.setStatus(ScriptPluginStatusEnum.DRAFT);
        return toDto(pluginRepository.save(plugin));
    }

    public ScriptPluginRevisionDTO saveRevision(String pluginKey, ScriptPluginRevisionDTO dto) {
        return saveRevision(pluginKey, dto, RevisionOriginEnum.MANUAL, null, null);
    }

    public ScriptPluginRevisionDTO saveRevision(String pluginKey, ScriptPluginRevisionDTO dto,
                                                RevisionOriginEnum origin, String aiModel, String promptVersion) {
        ScriptPlugin plugin = require(pluginKey);
        if (plugin.getStatus() == ScriptPluginStatusEnum.ARCHIVED) {
            throw new CommonException("archived plugins are read-only");
        }
        if (dto.getScripts() == null || dto.getScripts().isEmpty()) {
            throw new CommonException("a revision needs at least one script");
        }
        List<ExtensionDeclarationDTO> declarations = dto.getExtensionPoints() == null
                ? List.of() : dto.getExtensionPoints();

        int revisionNo = revisionRepository.findTopByPluginIdOrderByRevisionNoDesc(plugin.getId())
                .map(r -> r.getRevisionNo() + 1).orElse(1);

        ScriptPluginRevision revision = new ScriptPluginRevision();
        revision.setPluginId(plugin.getId());
        revision.setRevisionNo(revisionNo);
        revision.setScripts(writeJson(dto.getScripts()));
        revision.setExtensionPointsSnapshot(writeJson(declarations));
        revision.setConfigSchemaSnapshot(dto.getConfigSchema() == null ? null : writeJson(dto.getConfigSchema()));
        revision.setContentHash(ScriptPluginContentHasher.hash(declarations, dto.getScripts()));
        revision.setChangeSummary(dto.getChangeSummary() == null ? "" : dto.getChangeSummary());
        revision.setOrigin(origin);
        revision.setAiModel(aiModel);
        revision.setPromptVersion(promptVersion);
        revision.setCreatedAt(System.currentTimeMillis());
        revision = revisionRepository.save(revision);

        plugin.setCurrentRevisionId(revision.getId());
        plugin.setExtensionPoints(revision.getExtensionPointsSnapshot());
        plugin.setConfigSchema(revision.getConfigSchemaSnapshot());
        pluginRepository.save(plugin);
        publishManagementMessage(pluginKey);

        return toRevisionDto(revision);
    }

    public List<ScriptPluginDTO> list() {
        return pluginRepository.findAll().stream().map(this::toDto).toList();
    }

    public ScriptPluginDTO detail(String pluginKey) {
        return toDto(require(pluginKey));
    }

    public List<ScriptPluginRevisionDTO> revisions(String pluginKey) {
        ScriptPlugin plugin = require(pluginKey);
        return revisionRepository.findAllByPluginIdOrderByRevisionNoDesc(plugin.getId())
                .stream().map(this::toRevisionDto).toList();
    }

    /**
     * Enable-time validation (spec §Enable-time validation): runs on the node whose app matches the
     * plugin. The hosting node (wes) validates in-process; remote apps are asked through the
     * pending-validation channel and must report back within the timeout.
     */
    public ValidationReportDTO validate(String pluginKey) {
        ScriptPlugin plugin = require(pluginKey);
        ScriptPluginBundleDTO bundle = getBundle(pluginKey);
        if (bundle == null) {
            throw new CommonException("plugin has no revision to validate: " + pluginKey);
        }

        Optional<ScriptPluginMaterializationValidator> localValidator = materializationValidators.stream()
                .filter(v -> v.targetApp().equals(plugin.getApplySystem())).findFirst();
        if (localValidator.isPresent()) {
            return localValidator.get().validate(bundle);
        }

        CompletableFuture<ValidationReportDTO> future = new CompletableFuture<>();
        pendingValidations.put(pluginKey, new PendingValidation(plugin.getApplySystem(), future));
        publishManagementMessage(pluginKey);
        try {
            return future.get(validationTimeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.warn("validation for plugin {} on app {} did not report within {} ms",
                    pluginKey, plugin.getApplySystem(), validationTimeoutMs);
            return ValidationReportDTO.failed(pluginKey, bundle.getRevisionId(), plugin.getApplySystem(),
                    "remote", "target node " + plugin.getApplySystem() + " did not report within "
                            + validationTimeoutMs + " ms");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommonException("validation interrupted");
        } catch (ExecutionException e) {
            throw new CommonException("validation failed: " + e.getCause().getMessage());
        } finally {
            pendingValidations.remove(pluginKey);
        }
    }

    public ScriptPluginDTO enable(String pluginKey) {
        ScriptPlugin plugin = require(pluginKey);
        if (plugin.getStatus() != ScriptPluginStatusEnum.DRAFT && plugin.getStatus() != ScriptPluginStatusEnum.DISABLED) {
            throw new CommonException("only DRAFT or DISABLED plugins can be enabled");
        }
        if (plugin.getCurrentRevisionId() == null) {
            throw new CommonException("plugin has no revision to enable");
        }
        if (!ScriptPluginApiConstants.CONTRACT_VERSION.equals(plugin.getSdkVersion())) {
            throw new CommonException("plugin sdk_version " + plugin.getSdkVersion()
                    + " does not equal platform contract " + ScriptPluginApiConstants.CONTRACT_VERSION);
        }

        ValidationReportDTO report = validate(pluginKey);
        plugin.setLastValidationStatus(report.isPassed() ? "PASSED" : "FAILED");
        plugin.setLastValidationError(report.isPassed() ? null : report.failureSummary());
        plugin.setLastValidationAt(report.getFinishedAt());
        if (!report.isPassed()) {
            pluginRepository.save(plugin);
            throw new CommonException("enable refused, validation failed:\n" + plugin.getLastValidationError());
        }

        plugin.enable();
        pluginRepository.save(plugin);
        publishManagementMessage(pluginKey);
        return toDto(plugin);
    }

    public ScriptPluginDTO disable(String pluginKey, String reason) {
        ScriptPlugin plugin = require(pluginKey);
        if (plugin.getStatus() == ScriptPluginStatusEnum.ENABLED) {
            plugin.disable();
            pluginRepository.save(plugin);
            publishManagementMessage(pluginKey);
            log.warn("script plugin {} disabled: {}", pluginKey, reason);
        }
        return toDto(plugin);
    }

    public ScriptPluginDTO archive(String pluginKey) {
        ScriptPlugin plugin = require(pluginKey);
        plugin.archive();
        pluginRepository.save(plugin);
        publishManagementMessage(pluginKey);
        return toDto(plugin);
    }

    public ScriptPluginDTO rollback(String pluginKey, long revisionId) {
        ScriptPlugin plugin = require(pluginKey);
        if (plugin.getStatus() == ScriptPluginStatusEnum.ARCHIVED) {
            throw new CommonException("archived plugins cannot be rolled back");
        }
        ScriptPluginRevision revision = revisionRepository.findById(revisionId)
                .filter(r -> r.getPluginId().equals(plugin.getId()))
                .orElseThrow(() -> new CommonException("revision not found: " + revisionId));

        plugin.setCurrentRevisionId(revision.getId());
        plugin.setExtensionPoints(revision.getExtensionPointsSnapshot());
        plugin.setConfigSchema(revision.getConfigSchemaSnapshot());
        pluginRepository.save(plugin);
        publishManagementMessage(pluginKey);
        return toDto(plugin);
    }

    public ScriptPluginBundleDTO getBundle(String pluginKey) {
        ScriptPlugin plugin = pluginRepository.findByPluginKey(pluginKey).orElse(null);
        if (plugin == null || plugin.getCurrentRevisionId() == null) {
            return null;
        }
        ScriptPluginRevision revision = revisionRepository.findById(plugin.getCurrentRevisionId()).orElse(null);
        if (revision == null) {
            return null;
        }

        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey(plugin.getPluginKey());
        bundle.setName(plugin.getName());
        bundle.setApplySystem(plugin.getApplySystem());
        bundle.setApplyModule(plugin.getApplyModule());
        bundle.setSdkVersion(plugin.getSdkVersion());
        bundle.setRevisionId(revision.getId());
        bundle.setDeclarations(readDeclarations(revision.getExtensionPointsSnapshot()));
        bundle.setConfigSchema(revision.getConfigSchemaSnapshot() == null
                ? List.of() : readJson(revision.getConfigSchemaSnapshot(), new TypeReference<>() {
        }));
        bundle.setScripts(readJson(revision.getScripts(), new TypeReference<>() {
        }));
        bundle.setConfig(resolveConfig(plugin));
        return bundle;
    }

    public List<ScriptPluginBundleDTO> getEnabledBundles(String applySystem) {
        return pluginRepository.findByApplySystemAndStatusOrderByPluginKeyAsc(applySystem, ScriptPluginStatusEnum.ENABLED)
                .stream().map(ScriptPlugin::getPluginKey).map(this::getBundle).filter(Objects::nonNull).toList();
    }

    public long getRegistryVersion(String applySystem) {
        return pluginRepository.findMaxUpdateTime(applySystem);
    }

    public ScriptPluginBundleDTO findPendingValidation(String applySystem) {
        return pendingValidations.entrySet().stream()
                .filter(entry -> applySystem.equals(entry.getValue().app()))
                .map(entry -> getBundle(entry.getKey()))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    public void reportValidation(ValidationReportDTO report) {
        PendingValidation pending = pendingValidations.get(report.getPluginKey());
        if (pending != null) {
            pending.future().complete(report);
        }
        pluginRepository.findByPluginKey(report.getPluginKey()).ifPresent(plugin -> {
            plugin.setLastValidationStatus(report.isPassed() ? "PASSED" : "FAILED");
            plugin.setLastValidationError(report.isPassed() ? null : report.failureSummary());
            plugin.setLastValidationAt(report.getFinishedAt());
            pluginRepository.save(plugin);
        });
        log.info("validation report for {} (revision {}): passed={}", report.getPluginKey(),
                report.getRevisionId(), report.isPassed());
    }

    private ScriptPlugin require(String pluginKey) {
        return pluginRepository.findByPluginKey(pluginKey)
                .orElseThrow(() -> new CommonException("script plugin not found: " + pluginKey));
    }

    private void publishManagementMessage(String pluginKey) {
        redisUtils.publish(RedisConstants.PLUGIN_LISTEN_PLUGIN_MANAGEMENT,
                new PluginManagementMessage(pluginKey, PluginManagementMessageType.PLUGIN_CHANGED));
    }

    private Map<String, Object> resolveConfig(ScriptPlugin plugin) {
        Map<String, Object> config = new HashMap<>();
        configRepository.findByPluginId(plugin.getId()).ifPresent(row ->
                config.putAll(readJson(row.getConfigJson(), new TypeReference<>() {
                })));
        if (plugin.getConfigSchema() != null) {
            for (ConfigFieldDTO field : readJson(plugin.getConfigSchema(), new TypeReference<List<ConfigFieldDTO>>() {
            })) {
                if (!config.containsKey(field.getField()) && field.getDefaultValue() != null) {
                    config.put(field.getField(), field.getDefaultValue());
                }
            }
        }
        return config;
    }

    private List<ExtensionDeclarationDTO> readDeclarations(String json) {
        if (json == null) {
            return List.of();
        }
        return readJson(json, new TypeReference<>() {
        });
    }

    private <T> T readJson(String json, TypeReference<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (Exception e) {
            throw new CommonException("cannot parse stored JSON: " + e.getMessage());
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new CommonException("cannot serialize JSON: " + e.getMessage());
        }
    }

    private ScriptPluginDTO toDto(ScriptPlugin plugin) {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        dto.setId(plugin.getId());
        dto.setPluginKey(plugin.getPluginKey());
        dto.setName(plugin.getName());
        dto.setDescription(plugin.getDescription());
        dto.setApplySystem(plugin.getApplySystem());
        dto.setApplyModule(plugin.getApplyModule());
        dto.setStatus(plugin.getStatus());
        dto.setExtensionPoints(readDeclarations(plugin.getExtensionPoints()));
        dto.setConfigSchema(plugin.getConfigSchema() == null
                ? List.of() : readJson(plugin.getConfigSchema(), new TypeReference<>() {
        }));
        dto.setSdkVersion(plugin.getSdkVersion());
        dto.setCurrentRevisionId(plugin.getCurrentRevisionId());
        dto.setLastValidationStatus(plugin.getLastValidationStatus());
        dto.setLastValidationError(plugin.getLastValidationError());
        dto.setLastValidationAt(plugin.getLastValidationAt());
        dto.setUpdateTime(plugin.getUpdateTime());
        return dto;
    }

    private ScriptPluginRevisionDTO toRevisionDto(ScriptPluginRevision revision) {
        ScriptPluginRevisionDTO dto = new ScriptPluginRevisionDTO();
        dto.setId(revision.getId());
        dto.setPluginId(revision.getPluginId());
        dto.setRevisionNo(revision.getRevisionNo());
        dto.setScripts(readJson(revision.getScripts(), new TypeReference<>() {
        }));
        dto.setExtensionPoints(readDeclarations(revision.getExtensionPointsSnapshot()));
        dto.setConfigSchema(revision.getConfigSchemaSnapshot() == null
                ? List.of() : readJson(revision.getConfigSchemaSnapshot(), new TypeReference<>() {
        }));
        dto.setContentHash(revision.getContentHash());
        dto.setChangeSummary(revision.getChangeSummary());
        dto.setOrigin(revision.getOrigin());
        dto.setAiModel(revision.getAiModel());
        dto.setPromptVersion(revision.getPromptVersion());
        dto.setCreatedBy(revision.getCreatedBy());
        dto.setCreatedAt(revision.getCreatedAt());
        return dto;
    }
}
```

Implementation note for the executor: the `localValidator.get().validate(bundle)` call happens before any `pendingValidations` bookkeeping — the mock in the test does not need `configRepository` for the wrong-sdkVersion test, which is why that test only stubs `findByPluginKey`.

- [ ] **Step 5: Run the tests to verify they pass**

```bash
cd server && ./gradlew :modules-plugin:script-plugin-core:test --tests "org.openwes.script.plugin.core.domain.service.ScriptPluginServiceTest"
```

Expected: PASS (7 tests). If `ReflectionTestUtils` is missing, it is available through `spring-boot-starter-test` (already a global test dep).

- [ ] **Step 6: Commit**

```bash
git add server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/ScriptPluginMaterializationValidator.java server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginService.java server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginServiceTest.java
git commit -m "feat(script-plugin-core): lifecycle service with validation orchestration

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 6: Execution-log ingestion + REST controller

**Files:**
- Create: `.../domain/service/ScriptPluginExecutionLogService.java`
- Create: `.../controller/ScriptPluginController.java`
- Test: `.../domain/service/ScriptPluginExecutionLogServiceTest.java`, `.../controller/ScriptPluginControllerTest.java`

**Interfaces:**
- Consumes: `ScriptPluginService` (Task 5), `ScriptPluginExecutionLogRepository` (Task 4), `ExecutionLogDTO` (Task 2).
- Produces: `ScriptPluginExecutionLogService.report(ExecutionLogDTO)`; REST endpoints used by the workbench (Plan 3) and by manual verification.

- [ ] **Step 1: Write the failing tests**

`ScriptPluginExecutionLogServiceTest.java`:

```java
package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginExecutionLogRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginExecutionLogServiceTest {

    private final ScriptPluginExecutionLogRepository repository = mock(ScriptPluginExecutionLogRepository.class);
    private final ScriptPluginExecutionLogService service = new ScriptPluginExecutionLogService(repository);

    private ExecutionLogDTO log() {
        ExecutionLogDTO dto = new ExecutionLogDTO();
        dto.setPluginKey("packing-wave");
        dto.setRevisionId(11L);
        dto.setExtensionType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        dto.setSuccess(true);
        dto.setDurationMs(5);
        return dto;
    }

    @Test
    void report_savesTheRow() {
        when(repository.countByPluginKey("packing-wave")).thenReturn(10L);

        service.report(log());

        verify(repository).save(any());
        verify(repository, never()).pruneToKeep(any(), anyInt());
    }

    @Test
    void report_prunesWhenCapExceeded() {
        when(repository.countByPluginKey("packing-wave")).thenReturn(10_001L);

        service.report(log());

        verify(repository).pruneToKeep(eq("packing-wave"), eq(10_000));
    }
}
```

`ScriptPluginControllerTest.java`:

```java
package org.openwes.script.plugin.core.controller;

import org.junit.jupiter.api.Test;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.service.ScriptPluginService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginControllerTest {

    private final ScriptPluginService service = mock(ScriptPluginService.class);
    private final ScriptPluginController controller = new ScriptPluginController(service);

    @Test
    void list_wrapsServiceResultInSuccessResponse() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        dto.setPluginKey("packing-wave");
        dto.setStatus(ScriptPluginStatusEnum.DRAFT);
        when(service.list()).thenReturn(List.of(dto));

        Response<List<ScriptPluginDTO>> response = controller.list();

        assertEquals(Response.SUCCESS_CODE, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("packing-wave", response.getData().get(0).getPluginKey());
    }

    @Test
    void enable_delegatesToService() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        when(service.enable("packing-wave")).thenReturn(dto);

        Response<ScriptPluginDTO> response = controller.enable("packing-wave");

        assertEquals(Response.SUCCESS_CODE, response.getCode());
        verify(service).enable("packing-wave");
    }

    @Test
    void disable_passesUserReason() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        when(service.disable("packing-wave", "disabled by user")).thenReturn(dto);

        controller.disable("packing-wave");

        verify(service).disable("packing-wave", "disabled by user");
    }
}
```

- [ ] **Step 2: Run the tests to verify they fail**

```bash
cd server && ./gradlew :modules-plugin:script-plugin-core:test --tests "org.openwes.script.plugin.core.domain.service.ScriptPluginExecutionLogServiceTest" --tests "org.openwes.script.plugin.core.controller.ScriptPluginControllerTest"
```

Expected: FAIL to compile — both classes do not exist.

- [ ] **Step 3: Implement the log service**

```java
package org.openwes.script.plugin.core.domain.service;

import lombok.RequiredArgsConstructor;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginExecutionLog;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginExecutionLogRepository;
import org.springframework.stereotype.Service;

/**
 * Ingests execution-log reports from every node and enforces the capped-count retention
 * (spec §Data model: retention by capped count with periodic pruning).
 */
@Service
@RequiredArgsConstructor
public class ScriptPluginExecutionLogService {

    static final int RETENTION_CAP = 10_000;

    private final ScriptPluginExecutionLogRepository repository;

    public void report(ExecutionLogDTO dto) {
        ScriptPluginExecutionLog row = new ScriptPluginExecutionLog();
        row.setPluginKey(dto.getPluginKey());
        row.setRevisionId(dto.getRevisionId() == null ? 0L : dto.getRevisionId());
        row.setExtensionType(dto.getExtensionType());
        row.setSuccess(dto.isSuccess());
        row.setDurationMs(dto.getDurationMs());
        row.setErrorMessage(dto.getErrorMessage() == null ? null : dto.getErrorMessage().substring(0,
                Math.min(1024, dto.getErrorMessage().length())));
        row.setCreatedAt(dto.getCreatedAt() == null ? System.currentTimeMillis() : dto.getCreatedAt());
        repository.save(row);

        if (repository.countByPluginKey(dto.getPluginKey()) > RETENTION_CAP) {
            repository.pruneToKeep(dto.getPluginKey(), RETENTION_CAP);
        }
    }
}
```

- [ ] **Step 4: Implement the controller**

```java
package org.openwes.script.plugin.core.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.core.domain.service.ScriptPluginService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Management REST surface. Mapped under the reserved `plugin-platform` prefix; the gateway route
 * for /plugin-platform/** forwards without a RewritePath filter, so this prefix is stable both
 * through the gateway and on the direct service port.
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin-platform/scriptPlugin")
@Tag(name = "Script Plugin Platform Api")
public class ScriptPluginController {

    private final ScriptPluginService scriptPluginService;

    @GetMapping("/list")
    public Response<List<ScriptPluginDTO>> list() {
        return Response.success(scriptPluginService.list());
    }

    @GetMapping("/detail/{pluginKey}")
    public Response<ScriptPluginDTO> detail(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(scriptPluginService.detail(pluginKey));
    }

    @PostMapping("/createDraft")
    public Response<ScriptPluginDTO> createDraft(@RequestBody ScriptPluginDTO dto) {
        return Response.success(scriptPluginService.createDraft(dto));
    }

    @PostMapping("/{pluginKey}/revision")
    public Response<ScriptPluginRevisionDTO> saveRevision(@PathVariable("pluginKey") String pluginKey,
                                                          @RequestBody ScriptPluginRevisionDTO dto) {
        return Response.success(scriptPluginService.saveRevision(pluginKey, dto));
    }

    @GetMapping("/{pluginKey}/revisions")
    public Response<List<ScriptPluginRevisionDTO>> revisions(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(scriptPluginService.revisions(pluginKey));
    }

    @PostMapping("/{pluginKey}/validate")
    public Response<ValidationReportDTO> validate(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(scriptPluginService.validate(pluginKey));
    }

    @PostMapping("/{pluginKey}/enable")
    public Response<ScriptPluginDTO> enable(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(scriptPluginService.enable(pluginKey));
    }

    @PostMapping("/{pluginKey}/disable")
    public Response<ScriptPluginDTO> disable(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(scriptPluginService.disable(pluginKey, "disabled by user"));
    }

    @PostMapping("/{pluginKey}/archive")
    public Response<ScriptPluginDTO> archive(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(scriptPluginService.archive(pluginKey));
    }

    @PostMapping("/{pluginKey}/rollback/{revisionId}")
    public Response<ScriptPluginDTO> rollback(@PathVariable("pluginKey") String pluginKey,
                                              @PathVariable("revisionId") long revisionId) {
        return Response.success(scriptPluginService.rollback(pluginKey, revisionId));
    }
}
```

- [ ] **Step 5: Run the tests to verify they pass**

```bash
cd server && ./gradlew :modules-plugin:script-plugin-core:test
```

Expected: PASS (all core tests).

- [ ] **Step 6: Commit**

```bash
git add server/modules-plugin/script-plugin-core
git commit -m "feat(script-plugin-core): execution log ingestion and management REST

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 7: Dubbo `IScriptPluginApi` implementation

**Files:**
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/application/ScriptPluginApiImpl.java`

**Interfaces:**
- Consumes: `ScriptPluginService` (Task 5), `ScriptPluginExecutionLogService` (Task 6), `IScriptPluginApi` (Task 3).
- Produces: the Dubbo provider consumed by Plan 1b's node accessor (`getEnabledPlugins`, `getRegistryVersion`, `getPendingValidation`, `reportValidation`, `reportExecution`, `disable`).

- [ ] **Step 1: Implement the Dubbo service**

```java
package org.openwes.script.plugin.core.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.openwes.script.plugin.api.IScriptPluginApi;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.core.domain.service.ScriptPluginExecutionLogService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginService;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@DubboService
@RequiredArgsConstructor
@Slf4j
public class ScriptPluginApiImpl implements IScriptPluginApi {

    private final ScriptPluginService scriptPluginService;
    private final ScriptPluginExecutionLogService executionLogService;

    @Override
    public List<ScriptPluginBundleDTO> getEnabledPlugins(String applySystem) {
        return scriptPluginService.getEnabledBundles(applySystem);
    }

    @Override
    public long getRegistryVersion(String applySystem) {
        return scriptPluginService.getRegistryVersion(applySystem);
    }

    @Override
    public ScriptPluginBundleDTO getPendingValidation(String applySystem) {
        return scriptPluginService.findPendingValidation(applySystem);
    }

    @Override
    public void reportValidation(ValidationReportDTO report) {
        scriptPluginService.reportValidation(report);
    }

    @Override
    public void reportExecution(ExecutionLogDTO executionLog) {
        try {
            executionLogService.report(executionLog);
        } catch (Exception e) {
            // log shipping must never break the plugin invocation path on the reporting node
            log.error("failed to persist execution log for {}", executionLog.getPluginKey(), e);
        }
    }

    @Override
    public void disable(String pluginKey, String reason) {
        scriptPluginService.disable(pluginKey, reason);
    }
}
```

- [ ] **Step 2: Compile**

```bash
cd server && ./gradlew :modules-plugin:script-plugin-core:compileJava
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/application/ScriptPluginApiImpl.java
git commit -m "feat(script-plugin-core): Dubbo IScriptPluginApi provider

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 8: Liquibase tables, gateway route, wes-server wiring

**Files:**
- Modify: `server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261008.xml`
- Modify: `server/server/wes-server/build.gradle`
- Modify: `initdb.d/nacos_config.sql`
- Modify: `server/server/wes-server/src/test/java/org/openwes/config/NacosGatewayRouteTest.java`

**Interfaces:**
- Consumes: Phase-0 Task 5 created `db.changelog-20261008.xml` and its `create_p_plugin_table` changeSet; Phase-0 Task 6 created `NacosGatewayRouteTest`.
- Produces: a wes-server deployment where `/plugin-platform/scriptPlugin/**` is reachable and the four `p_script_plugin*` tables exist.

- [ ] **Step 1: Extend the gateway route test (failing first)**

Append this test method to `NacosGatewayRouteTest`:

```java
    @Test
    void pluginPlatformRoute_forwardsToWesWithoutRewrite() throws Exception {
        String sql = seedConfig();
        int start = sql.indexOf("- id: plugin-platform\\n");
        int end = sql.indexOf("- id: station\\n");
        assertTrue(start >= 0 && end > start, "plugin-platform route block not found in nacos_config.sql");
        String route = sql.substring(start, end);

        assertTrue(route.contains("uri: lb://wes"));
        assertTrue(route.contains("Path=/plugin-platform/**"));
        assertFalse(route.contains("RewritePath"),
                "controllers are mapped with the full plugin-platform prefix; the route must not strip it");
    }
```

- [ ] **Step 2: Run it to verify it fails**

```bash
cd server && ./gradlew :server:wes-server:test --tests "org.openwes.config.NacosGatewayRouteTest"
```

Expected: FAIL — `plugin-platform route block not found`.

- [ ] **Step 3: Add the route to the Nacos seed**

The routes YAML lives inside a single-line SQL INSERT where newlines are the **two literal characters** `\` `n`. Use the Edit tool with these exact strings (the `\n` sequences are literal backslash + n, reproduced from the file):

- old_string: `- id: station\n          uri: lb://station`
- new_string: `- id: plugin-platform\n          uri: lb://wes\n          predicates:\n            - Path=/plugin-platform/**\n        - id: station\n          uri: lb://station`

This inserts the new route directly before the station route, with the same `\n          ` indentation as its neighbors, and deliberately no `filters` block.

- [ ] **Step 4: Run the test to verify it passes**

```bash
cd server && ./gradlew :server:wes-server:test --tests "org.openwes.config.NacosGatewayRouteTest"
```

Expected: PASS.

- [ ] **Step 5: Append the script-plugin tables to the changelog**

In `db.changelog-20261008.xml`, add this changeSet after `create_p_plugin_table`:

```xml
    <changeSet id="create_p_script_plugin_tables" author="dev">
        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="p_script_plugin"/>
            </not>
        </preConditions>
        <sql>
            CREATE TABLE p_script_plugin (
                id                    BIGINT       NOT NULL,
                plugin_key            VARCHAR(64)  NOT NULL COMMENT '插件key，声明式，全局唯一',
                name                  VARCHAR(128) NOT NULL,
                description           VARCHAR(512) NOT NULL DEFAULT '',
                apply_system          VARCHAR(32)  NOT NULL COMMENT '目标应用: wes / station',
                apply_module          VARCHAR(64)  NOT NULL DEFAULT '',
                status                VARCHAR(16)  NOT NULL COMMENT 'DRAFT/ENABLED/DISABLED/ARCHIVED',
                extension_points      JSON         NOT NULL,
                config_schema         JSON         NULL,
                sdk_version           VARCHAR(16)  NOT NULL,
                current_revision_id   BIGINT       NULL,
                last_validation_status VARCHAR(16) NULL,
                last_validation_error TEXT         NULL,
                last_validation_at    BIGINT       NULL,
                version               BIGINT       NULL,
                create_time           BIGINT       NOT NULL DEFAULT 0,
                create_user           VARCHAR(60)  NOT NULL DEFAULT '',
                update_time           BIGINT       NOT NULL DEFAULT 0,
                update_user           VARCHAR(60)  NOT NULL DEFAULT '',
                PRIMARY KEY (id),
                UNIQUE KEY uk_script_plugin_key (plugin_key),
                KEY idx_script_plugin_app_status (apply_system, status)
            ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '脚本插件';

            CREATE TABLE p_script_plugin_revision (
                id                        BIGINT       NOT NULL,
                plugin_id                 BIGINT       NOT NULL,
                revision_no               INT          NOT NULL,
                scripts                   JSON         NOT NULL,
                extension_points_snapshot JSON         NOT NULL,
                config_schema_snapshot    JSON         NULL,
                content_hash              VARCHAR(64)  NOT NULL,
                change_summary            VARCHAR(512) NOT NULL DEFAULT '',
                origin                    VARCHAR(16)  NOT NULL COMMENT 'AI/MANUAL',
                ai_model                  VARCHAR(64)  NULL,
                prompt_version            VARCHAR(32)  NULL,
                created_by                VARCHAR(60)  NOT NULL DEFAULT '',
                created_at                BIGINT       NOT NULL DEFAULT 0,
                PRIMARY KEY (id),
                UNIQUE KEY uk_script_revision_no (plugin_id, revision_no)
            ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '脚本插件版本（不可变）';

            CREATE TABLE p_script_plugin_config (
                id            BIGINT      NOT NULL,
                plugin_id     BIGINT      NOT NULL,
                config_json   JSON        NOT NULL,
                lock_version  BIGINT      NOT NULL DEFAULT 0,
                create_time   BIGINT      NOT NULL DEFAULT 0,
                create_user   VARCHAR(60) NOT NULL DEFAULT '',
                update_time   BIGINT      NOT NULL DEFAULT 0,
                update_user   VARCHAR(60) NOT NULL DEFAULT '',
                PRIMARY KEY (id),
                UNIQUE KEY uk_script_config_plugin (plugin_id)
            ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '脚本插件配置';

            CREATE TABLE p_script_plugin_execution_log (
                id             BIGINT        NOT NULL,
                plugin_key     VARCHAR(64)   NOT NULL,
                revision_id    BIGINT        NOT NULL,
                extension_type VARCHAR(64)   NOT NULL,
                success        TINYINT(1)    NOT NULL,
                duration_ms    BIGINT        NOT NULL DEFAULT 0,
                error_message  VARCHAR(1024) NULL,
                created_at     BIGINT        NOT NULL DEFAULT 0,
                PRIMARY KEY (id),
                KEY idx_script_exec_plugin (plugin_key, id)
            ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '脚本插件执行日志（封顶保留）';
        </sql>
    </changeSet>
```

- [ ] **Step 6: Wire the module into wes-server**

In `server/server/wes-server/build.gradle`, add after the `plugin-sdk` line:

```groovy
    implementation project(path: ":modules-plugin:script-plugin-core", configuration: 'default')
```

- [ ] **Step 7: Compile and run all affected tests**

```bash
cd server && ./gradlew :modules-plugin:script-plugin-core:test :modules-plugin:script-plugin-api:test :modules-api-platform:api-platform:test :server:wes-server:test
```

Expected: PASS.

- [ ] **Step 8: Manual end-to-end verification (management plane)**

With a scratch MySQL + Redis (`docker compose up -d mysql redis`) and `cd server && ./gradlew :server:wes-server:bootRun`:

1. `curl -s http://localhost:9010/plugin-platform/scriptPlugin/list` → `{"code":"0","msg":"success","data":[]}` after Liquibase creates the tables.
2. Create a draft (no validator is registered yet — Plan 1b adds it; `validate`/`enable` for a wes plugin will fail with "no local validator" → use `curl` on `list`/`createDraft`/`revision` which need no validator):

```bash
curl -s -X POST http://localhost:9010/plugin-platform/scriptPlugin/createDraft \
  -H 'Content-Type: application/json' \
  -d '{"pluginKey":"packing-wave","name":"Packing wave rule","applySystem":"wes"}'
```

3. Save a revision and confirm `scripts`/`extensionPoints` persist and `content_hash` is returned:

```bash
curl -s -X POST http://localhost:9010/plugin-platform/scriptPlugin/packing-wave/revision \
  -H 'Content-Type: application/json' \
  -d '{"changeSummary":"first draft","extensionPoints":[{"type":"OUTBOUND_WAVE_WAVE_ACTION","priority":100,"script":"Wave.groovy","method":"wave"}],"scripts":{"Wave.groovy":"class Wave { Object wave(Object p) { return p } }"}}'
```

Expected: response `code = 0` with a revision containing a 64-char `contentHash`; `select * from p_script_plugin_revision` shows one row.

Record the observations in the commit message (step 9) or report them to the workspace owner — do not skip this step silently.

- [ ] **Step 9: Commit**

```bash
git add server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261008.xml server/server/wes-server/build.gradle initdb.d/nacos_config.sql server/server/wes-server/src/test/java/org/openwes/config/NacosGatewayRouteTest.java
git commit -m "feat(script-plugin-core): Liquibase tables, gateway route and wes-server wiring

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Acceptance for Phase 1a

- `cd server && ./gradlew test` is green.
- `script-engine`, `script-plugin-api`, `script-plugin-core` build; `JavaScriptUtils.executeJava` delegates to the engine with tests.
- `PluginContractCatalog` covers all ten `ExtensionType` values and resolves every declared interface method; `toPromptFragment("wes", ...)` filters correctly.
- REST: create draft → save revision → list/revisions/detail work; enable/disable/rollback/archive enforce the status machine; enable refuses on failed validation or sdk-version mismatch; a remote-app validation request is visible via `findPendingValidation` and completed by `reportValidation`.
- Dubbo: `ScriptPluginApiImpl` exposes the six `IScriptPluginApi` methods; execution logs are persisted with capped retention.
- Liquibase creates the four `p_script_plugin*` tables; the gateway seed routes `/plugin-platform/**` to wes without rewrite.
- Next: `docs/superpowers/plans/2026-10-08-script-plugin-phase1b-runtime-materialization.md`.
