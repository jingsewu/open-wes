# Script Plugin Phase 0 — Legacy Fixes Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix the six verified P0 defects of the existing plugin system and turn the test suite back into a real gate, so the script-plugin runtime (Plans 1a/1b) can rely on deterministic extension lookup, working gateway routes, and trustworthy tests.

**Architecture:** All changes are surgical edits to existing files. `PickingOrder` lifecycle emitters switch to the constructor form used by `ContainerTask`/`OutboundPlanOrder` so `LifecycleListenerRegistry` can match listeners by `entityName`. `PluginController.list()` stops sharing one map across the stream. `SpringExtensionFactory` gains an instance cache plus a null-plugin guard (PF4J still instantiates jar extensions; the new script runtime produces its own proxies). `IPlugin` gains a `priority()` default and `OutboundWaveServiceImpl` selects the highest-priority action deterministically and falls back to built-in logic on null/exception. Liquibase gets the missing `p_plugin` table, the Nacos gateway seed routes `/plugin/**` to `lb://wes`, and the global `ignoreFailures = true` is removed.

**Tech Stack:** Java 17, Spring Boot 3.2.2, PF4J 3.13.0, JUnit 5, Mockito 4.6.0, Liquibase (`db.changelog-*.xml`), Gradle multi-module (`server/`).

**Spec:** `docs/superpowers/specs/2026-10-08-script-plugin-ai-workbench-design.md` (see *Current State → Verified defects (P0)* and *Fix Work Package*).

## Global Constraints

- All server-side commands run from `server/`: `cd server && ./gradlew :modules-plugin:plugin-sdk:test` etc.
- Tests: JUnit 5 + Mockito 4.6.0 (already global test deps in `server/build.gradle`); do **not** add new test dependencies.
- Java 17, Lombok, no new modules in this plan.
- Every commit message ends with: `Co-Authored-By: Claude Code <noreply@anthropic.com>`
- The legacy `plugin-core` module is dead code — do not touch it; the `p_plugin` table is recreated because the legacy management page 500s without it (spec item 6).
- `initdb.d/nacos_config.sql` is a mysqldump: the gateway YAML lives inside a single-line `INSERT` string where newlines are the **two literal characters** `\` `n`. When editing it with the Edit tool, the `\n` sequences in the old/new strings below are those literal two-character sequences, copied verbatim from the file.
- Test discipline: all modules touched by this plan must have `test { ignoreFailures = false }` after Task 7.

---

## File Map

| File | Action | Purpose |
|------|--------|---------|
| `server/modules-wes/wes-outbound/src/main/java/org/openwes/wes/outbound/domain/entity/PickingOrder.java` | Modify | Emit `entityName`/`entityNo` on lifecycle events (:83, :98, :109) |
| `server/modules-wes/wes-outbound/src/test/java/org/openwes/wes/outbound/domain/entity/PickingOrderTest.java` | Create | Assert emitted lifecycle events carry `entityName` = `PickingOrder` |
| `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/controller/PluginController.java` | Modify | Build one map per stream element (:55-65) |
| `server/modules-plugin/plugin-sdk/src/test/java/org/openwes/plugin/sdk/controller/PluginControllerTest.java` | Create | Assert distinct maps / distinct rows |
| `server/modules-plugin/plugin-sdk/src/main/java/org/pf4j/spring/SpringExtensionFactory.java` | Modify | Instance cache + null-plugin guard |
| `server/modules-plugin/plugin-sdk/src/main/java/org/pf4j/spring/SpringPluginManager.java` | Modify | Expose the created factory for eviction |
| `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/listen/CustomPluginStateListener.java` | Modify | Evict cached extension instances on plugin state changes |
| `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/configuration/PluginConfiguration.java` | Modify | Wire the manager into the state listener |
| `server/modules-plugin/plugin-sdk/src/test/java/org/pf4j/spring/SpringExtensionFactoryTest.java` | Create | Cache / eviction / null-plugin tests |
| `server/modules-plugin/plugin-extension/src/main/java/org/openwes/plugin/extension/IPlugin.java` | Modify | Add `default int priority()` |
| `server/modules-wes/wes-outbound/src/main/java/org/openwes/wes/outbound/domain/service/impl/OutboundWaveServiceImpl.java` | Modify | Highest-priority selection + null/exception fallback (:39-43, :61-66) |
| `server/modules-wes/wes-outbound/src/test/java/org/openwes/wes/outbound/domain/service/impl/OutboundWaveServiceImplTest.java` | Create | Priority selection + fallback tests |
| `server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261008.xml` | Create | `p_plugin` table (reused by Plan 1a for script-plugin tables) |
| `server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml` | Modify | Include the new changelog |
| `server/server/wes-server/src/test/java/org/openwes/config/NacosGatewayRouteTest.java` | Create | Static assertions on the gateway seed routes |
| `initdb.d/nacos_config.sql` | Modify | `lb://plugin` → `lb://wes` |
| `server/build.gradle` | Modify | Global `ignoreFailures = false` |
| touched module `build.gradle` files | Modify | Per-module `test { ignoreFailures = false }` where needed |

---

## Task 1: PickingOrder lifecycle events carry entityName/entityNo

**Files:**
- Modify: `server/modules-wes/wes-outbound/src/main/java/org/openwes/wes/outbound/domain/entity/PickingOrder.java`
- Test: `server/modules-wes/wes-outbound/src/test/java/org/openwes/wes/outbound/domain/entity/PickingOrderTest.java`

**Interfaces:**
- Consumes: `LifeCycleStatusChangeEvent(String newStatus, Long entityId, String entityNo, String entityName)` (exists, `plugin-api`), `AggregatorRoot.getSynchronousDomainEvents()` (exists).
- Produces: nothing downstream — the fix makes `LifecycleListenerRegistry.fireAfterStatusChange(event.getEntityName(), ...)` match listeners registered for `"PickingOrder"`.

- [ ] **Step 1: Write the failing test**

```java
package org.openwes.wes.outbound.domain.entity;

import org.junit.jupiter.api.Test;
import org.openwes.domain.event.api.DomainEvent;
import org.openwes.plugin.api.dto.event.LifeCycleStatusChangeEvent;
import org.openwes.wes.api.outbound.constants.PickingOrderStatusEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PickingOrderTest {

    private List<LifeCycleStatusChangeEvent> lifecycleEvents(PickingOrder order) {
        return order.getSynchronousDomainEvents().stream()
                .filter(LifeCycleStatusChangeEvent.class::isInstance)
                .map(LifeCycleStatusChangeEvent.class::cast)
                .toList();
    }

    @Test
    void create_emitsLifecycleEventWithEntityNameAndNo() {
        PickingOrder order = PickingOrder.create(1, false, "WH", 1L, "W1", List.of(), false);

        List<LifeCycleStatusChangeEvent> events = lifecycleEvents(order);
        assertEquals(1, events.size());
        assertEquals("PickingOrder", events.get(0).getEntityName());
        assertEquals(PickingOrderStatusEnum.NEW.name(), events.get(0).getNewStatus());
        assertEquals(order.getPickingOrderNo(), events.get(0).getEntityNo());
    }

    @Test
    void dispatch_emitsLifecycleEventWithEntityNameAndNo() {
        PickingOrder order = PickingOrder.create(1, false, "WH", 1L, "W1", List.of(), false);
        order.clearEvents();

        order.dispatch(java.util.Map.of(1L, "SLOT-1"));

        List<LifeCycleStatusChangeEvent> events = lifecycleEvents(order);
        assertEquals(1, events.size());
        assertEquals("PickingOrder", events.get(0).getEntityName());
        assertEquals(PickingOrderStatusEnum.DISPATCHED.name(), events.get(0).getNewStatus());
        assertEquals(order.getPickingOrderNo(), events.get(0).getEntityNo());
    }

    @Test
    void cancel_emitsLifecycleEventWithEntityNameAndNo() {
        PickingOrder order = PickingOrder.create(1, false, "WH", 1L, "W1", List.of(), false);
        order.clearEvents();

        order.cancel();

        List<LifeCycleStatusChangeEvent> events = lifecycleEvents(order);
        assertEquals(1, events.size());
        assertEquals("PickingOrder", events.get(0).getEntityName());
        assertEquals(PickingOrderStatusEnum.CANCELED.name(), events.get(0).getNewStatus());
        assertTrue(order.getPickingOrderNo().equals(events.get(0).getEntityNo()));
    }
}
```

Note: `create()` uses `SnowflakeUtils.generateId()` / `OrderNoGenerator.generationPickingOrderNo()` — both are pure static utilities, no Spring context needed.

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd server && ./gradlew :modules-wes:wes-outbound:test --tests "org.openwes.wes.outbound.domain.entity.PickingOrderTest"
```

Expected: tests FAIL — `getEntityName()` is `null` (old emitters never set it).

- [ ] **Step 3: Fix the three emit sites**

In `PickingOrder.java`, replace the line inside `create()` (currently `:83`):

```java
        order.addLifecycleEvent(new LifeCycleStatusChangeEvent().setEntityId(order.getId()).setNewStatus(PickingOrderStatusEnum.NEW.name()));
```

with:

```java
        order.addLifecycleEvent(new LifeCycleStatusChangeEvent(PickingOrderStatusEnum.NEW.name(), order.getId(),
                order.getPickingOrderNo(), order.getClass().getSimpleName()));
```

Replace the line inside `dispatch()` (currently `:98`):

```java
        this.addLifecycleEvent(new LifeCycleStatusChangeEvent().setEntityId(this.id).setNewStatus(PickingOrderStatusEnum.DISPATCHED.name()));
```

with:

```java
        this.addLifecycleEvent(new LifeCycleStatusChangeEvent(PickingOrderStatusEnum.DISPATCHED.name(), this.id,
                this.pickingOrderNo, this.getClass().getSimpleName()));
```

Replace the line inside `cancel()` (currently `:109`):

```java
        addLifecycleEvent(new LifeCycleStatusChangeEvent().setEntityId(this.id).setNewStatus(PickingOrderStatusEnum.CANCELED.name()));
```

with:

```java
        addLifecycleEvent(new LifeCycleStatusChangeEvent(PickingOrderStatusEnum.CANCELED.name(), this.id,
                this.pickingOrderNo, this.getClass().getSimpleName()));
```

This matches the working sibling pattern (`ContainerTask.java:64` uses `getClass().getSimpleName()`).

- [ ] **Step 4: Run the test to verify it passes**

```bash
cd server && ./gradlew :modules-wes:wes-outbound:test --tests "org.openwes.wes.outbound.domain.entity.PickingOrderTest"
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add server/modules-wes/wes-outbound/src/main/java/org/openwes/wes/outbound/domain/entity/PickingOrder.java server/modules-wes/wes-outbound/src/test/java/org/openwes/wes/outbound/domain/entity/PickingOrderTest.java
git commit -m "fix(outbound): emit entityName/entityNo on PickingOrder lifecycle events

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 2: PluginController.list() returns one map per plugin

**Files:**
- Modify: `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/controller/PluginController.java:55-65`
- Test: `server/modules-plugin/plugin-sdk/src/test/java/org/openwes/plugin/sdk/controller/PluginControllerTest.java`

**Interfaces:**
- Consumes: `PluginController(PluginService, PluginManager)` — Lombok `@RequiredArgsConstructor`; `PluginManager.getPlugins()` → `List<PluginWrapper>`; `PluginWrapper.getPluginId()/getPluginPath()/getPluginState()`.
- Produces: `GET /plugin/list` returning one independent map per plugin (consumed by ops tooling and the legacy client page until Plan 3 retires it).

- [ ] **Step 1: Write the failing test**

```java
package org.openwes.plugin.sdk.controller;

import org.junit.jupiter.api.Test;
import org.pf4j.PluginManager;
import org.pf4j.PluginState;
import org.pf4j.PluginWrapper;
import org.openwes.plugin.sdk.service.PluginService;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PluginControllerTest {

    @Test
    void list_returnsDistinctMapsPerPlugin() {
        PluginManager pluginManager = mock(PluginManager.class);
        PluginWrapper p1 = mock(PluginWrapper.class);
        when(p1.getPluginId()).thenReturn("plugin-a");
        when(p1.getPluginPath()).thenReturn(Path.of("/plugins/wes/jar/a.jar"));
        when(p1.getPluginState()).thenReturn(PluginState.STARTED);
        PluginWrapper p2 = mock(PluginWrapper.class);
        when(p2.getPluginId()).thenReturn("plugin-b");
        when(p2.getPluginPath()).thenReturn(Path.of("/plugins/wes/jar/b.jar"));
        when(p2.getPluginState()).thenReturn(PluginState.STOPPED);
        when(pluginManager.getPlugins()).thenReturn(List.of(p1, p2));

        PluginController controller = new PluginController(mock(PluginService.class), pluginManager);

        List<Map<String, Object>> result = controller.list();

        assertEquals(2, result.size());
        assertNotSame(result.get(0), result.get(1));
        assertEquals("plugin-a", result.get(0).get("pluginId"));
        assertEquals("plugin-b", result.get(1).get("pluginId"));
        assertEquals(PluginState.STARTED, result.get(0).get("pluginStatus"));
        assertEquals(PluginState.STOPPED, result.get(1).get("pluginStatus"));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd server && ./gradlew :modules-plugin:plugin-sdk:test --tests "org.openwes.plugin.sdk.controller.PluginControllerTest"
```

Expected: FAIL — both entries carry `pluginId = "plugin-b"` because the single `result` map is mutated by every stream element.

- [ ] **Step 3: Fix `list()`**

Replace the whole method (currently `:55-65`):

```java
    @GetMapping(value = "/list")
    public List<Map<String, Object>> list() {
        Map<String, Object> result = Maps.newHashMap();
        List<PluginWrapper> plugins = pluginManager.getPlugins();
        return plugins.stream().map(v -> {
            result.put("pluginId", v.getPluginId());
            result.put("pluginPath", v.getPluginPath().toString());
            result.put("pluginStatus", v.getPluginState());
            return result;
        }).toList();
    }
```

with:

```java
    @GetMapping(value = "/list")
    public List<Map<String, Object>> list() {
        List<PluginWrapper> plugins = pluginManager.getPlugins();
        return plugins.stream().map(v -> {
            Map<String, Object> result = Maps.newHashMap();
            result.put("pluginId", v.getPluginId());
            result.put("pluginPath", v.getPluginPath().toString());
            result.put("pluginStatus", v.getPluginState());
            return result;
        }).toList();
    }
```

(The `com.google.common.collect.Maps` import stays.)

- [ ] **Step 4: Run the test to verify it passes**

```bash
cd server && ./gradlew :modules-plugin:plugin-sdk:test --tests "org.openwes.plugin.sdk.controller.PluginControllerTest"
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/controller/PluginController.java server/modules-plugin/plugin-sdk/src/test/java/org/openwes/plugin/sdk/controller/PluginControllerTest.java
git commit -m "fix(plugin-sdk): build one map per plugin in PluginController.list()

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 3: SpringExtensionFactory — singleton semantics + null-plugin guard + eviction

**Files:**
- Modify: `server/modules-plugin/plugin-sdk/src/main/java/org/pf4j/spring/SpringExtensionFactory.java`
- Modify: `server/modules-plugin/plugin-sdk/src/main/java/org/pf4j/spring/SpringPluginManager.java`
- Modify: `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/listen/CustomPluginStateListener.java`
- Modify: `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/configuration/PluginConfiguration.java`
- Test: `server/modules-plugin/plugin-sdk/src/test/java/org/pf4j/spring/SpringExtensionFactoryTest.java`

**Interfaces:**
- Consumes: `PluginManager.whichPlugin(Class)` (PF4J, may return `null` for classes not owned by a plugin), `SpringPluginManager.createExtensionFactory()`.
- Produces: `SpringExtensionFactory.create(Class)` returning a cached instance; `evict(Class)` / `evictAll()`; `SpringPluginManager.getSpringExtensionFactory()`. Plan 1b does **not** rely on these (its proxies are built by the script runtime), but deterministic singleton semantics are the semantic baseline.

- [ ] **Step 1: Write the failing test**

```java
package org.pf4j.spring;

import org.junit.jupiter.api.Test;
import org.pf4j.PluginManager;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class SpringExtensionFactoryTest {

    public static class SimpleExtension {
    }

    @Test
    void create_withAutowireDisabled_returnsSameInstanceForSameClass() {
        SpringExtensionFactory factory = new SpringExtensionFactory(mock(PluginManager.class), false);

        SimpleExtension first = factory.create(SimpleExtension.class);
        SimpleExtension second = factory.create(SimpleExtension.class);

        assertSame(first, second);
    }

    @Test
    void evict_forcesNewInstance() {
        SpringExtensionFactory factory = new SpringExtensionFactory(mock(PluginManager.class), false);
        SimpleExtension first = factory.create(SimpleExtension.class);

        factory.evict(SimpleExtension.class);

        assertNotSame(first, factory.create(SimpleExtension.class));
    }

    @Test
    void create_whenWhichPluginReturnsNull_doesNotThrowNpe() {
        PluginManager pluginManager = mock(PluginManager.class);
        SpringExtensionFactory factory = new SpringExtensionFactory(pluginManager, true);

        SimpleExtension extension = factory.create(SimpleExtension.class);

        assertNotNull(extension);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd server && ./gradlew :modules-plugin:plugin-sdk:test --tests "org.pf4j.spring.SpringExtensionFactoryTest"
```

Expected: `create_withAutowireDisabled_returnsSameInstanceForSameClass` FAILS (new instance each call); `create_whenWhichPluginReturnsNull_doesNotThrowNpe` FAILS with NPE at `SpringExtensionFactory.java:175`.

- [ ] **Step 3: Add the cache and the guard in `SpringExtensionFactory.java`**

Add the import block entry (after `import java.lang.reflect.InvocationTargetException;` add):

```java
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
```

Update the class javadoc line `Creates a new extension instance every time a request is done.` to:

```java
 * Creates a new extension instance per extension class and caches it, so extensions keep singleton semantics
 * (autowired state survives between calls). Use {@link #evict(Class)} / {@link #evictAll()} when a plugin
 * is reloaded or unloaded.
```

Add the cache field after `protected final boolean autowire;`:

```java
    /**
     * Cache of extension instances keyed by extension class, so a class is instantiated once.
     */
    private final Map<Class<?>, Object> instanceCache = new ConcurrentHashMap<>();
```

Replace the `create` method body:

```java
    @Override
    public <T> T create(final Class<T> extensionClass) {
        if (!this.autowire) {
            log.warn("Create instance of '" + nameOf(extensionClass) + "' without using springs possibilities as" +
                    " autowiring is disabled.");
            return createWithoutSpring(extensionClass);
        }

        return getApplicationContextBy(extensionClass)
                .map(applicationContext -> createWithSpring(extensionClass, applicationContext))
                .orElseGet(() -> createWithoutSpring(extensionClass));
    }
```

with:

```java
    @Override
    @SuppressWarnings("unchecked")
    public <T> T create(final Class<T> extensionClass) {
        return (T) instanceCache.computeIfAbsent(extensionClass, this::doCreate);
    }

    private Object doCreate(final Class<?> extensionClass) {
        if (!this.autowire) {
            log.warn("Create instance of '" + extensionClass.getName() + "' without using springs possibilities as" +
                    " autowiring is disabled.");
            return createWithoutSpring(extensionClass);
        }

        return getApplicationContextBy(extensionClass)
                .map(applicationContext -> createWithSpring(extensionClass, applicationContext))
                .orElseGet(() -> createWithoutSpring(extensionClass));
    }

    /**
     * Removes the cached instance of the given extension class; the next {@link #create(Class)} call
     * instantiates a fresh instance.
     */
    public void evict(final Class<?> extensionClass) {
        instanceCache.remove(extensionClass);
    }

    /**
     * Removes all cached extension instances. Call when plugins are loaded/unloaded so stale
     * instances (and their classloaders) are not retained.
     */
    public void evictAll() {
        instanceCache.clear();
    }
```

Note the generics: `doCreate` returns `Object` and `createWithoutSpring` is declared `<T> T createWithoutSpring(Class<T>)` — since `createWithoutSpring(extensionClass)` is called with `Class<?>`, the inference yields `capture-of ?` assignable to `Object`; `getApplicationContextBy(Class)` is `<T>` with `Class<?>` → `Optional<ApplicationContext>` (T only appears in the parameter), so `.map(applicationContext -> createWithSpring(extensionClass, applicationContext))` returns `Optional<Object>` only if `createWithSpring` is re-typed — it already is `protected <T> T createWithSpring(final Class<T> extensionClass, final ApplicationContext applicationContext)` returning `Object` for `Class<?>` input... **Adjust `createWithSpring`'s signature to `protected Object createWithSpring(final Class<?> extensionClass, final ApplicationContext applicationContext)`** (remove its `@SuppressWarnings("unchecked")` and `(T)` cast on the return — it can return the raw autowired object), and keep `getApplicationContextBy` unchanged. If the compiler still complains about the lambda's captured wildcard, wrap as:

```java
        return getApplicationContextBy(extensionClass)
                .map(applicationContext -> (Object) createWithSpring(extensionClass, applicationContext))
                .orElseGet(() -> createWithoutSpring(extensionClass));
```

- [ ] **Step 4: Fix the null-plugin NPE in `getApplicationContextBy`**

Replace (currently `:170-175`):

```java
    protected <T> Optional<ApplicationContext> getApplicationContextBy(final Class<T> extensionClass) {
        final Plugin plugin = Optional.ofNullable(this.pluginManager.whichPlugin(extensionClass))
                .map(PluginWrapper::getPlugin)
                .orElse(null);

        final ApplicationContext applicationContext = PluginRegister.getApplicationContext(plugin.getWrapper().getPluginId());
```

with:

```java
    protected <T> Optional<ApplicationContext> getApplicationContextBy(final Class<T> extensionClass) {
        final PluginWrapper pluginWrapper = this.pluginManager.whichPlugin(extensionClass);
        final Plugin plugin = pluginWrapper != null ? pluginWrapper.getPlugin() : null;

        final ApplicationContext applicationContext = plugin != null
                ? PluginRegister.getApplicationContext(pluginWrapper.getPluginId())
                : null;
```

- [ ] **Step 5: Expose the factory and evict on state changes**

In `SpringPluginManager.java`, replace `createExtensionFactory()`:

```java
    @Override
    protected ExtensionFactory createExtensionFactory() {
        return new SpringExtensionFactory(this);
    }
```

with:

```java
    private SpringExtensionFactory springExtensionFactory;

    @Override
    protected ExtensionFactory createExtensionFactory() {
        this.springExtensionFactory = new SpringExtensionFactory(this);
        return this.springExtensionFactory;
    }

    public SpringExtensionFactory getSpringExtensionFactory() {
        return springExtensionFactory;
    }
```

In `CustomPluginStateListener.java`, replace the whole file with:

```java
package org.openwes.plugin.sdk.listen;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.plugin.sdk.register.PluginRegister;
import org.pf4j.Plugin;
import org.pf4j.PluginState;
import org.pf4j.PluginStateEvent;
import org.pf4j.PluginStateListener;
import org.pf4j.spring.SpringPluginManager;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomPluginStateListener implements PluginStateListener {

    private final PluginRegister pluginRegister;
    private final SpringPluginManager springPluginManager;

    @Override
    public void pluginStateChanged(PluginStateEvent event) {
        log.debug("Plugin {} state changed to {}", event.getPlugin().getPluginId(), event.getPluginState());
        // Extension instances cached by SpringExtensionFactory may belong to the changed plugin;
        // drop them so the next lookup rebuilds against the current plugin set.
        springPluginManager.getSpringExtensionFactory().evictAll();

        if (event.getPluginState() == PluginState.STARTED) {
            Plugin plugin = event.getPlugin().getPlugin();
            pluginRegister.applyRegister(plugin);
        } else if (event.getPluginState() == PluginState.STOPPED) {
            pluginRegister.applyUnRegister(event.getPlugin().getPlugin());
        } else if (event.getPluginState() == PluginState.UNLOADED) {
            pluginRegister.applyUnRegister(event.getPlugin().getPlugin());
        } else if (event.getPluginState() == PluginState.DISABLED) {
            pluginRegister.applyUnRegister(event.getPlugin().getPlugin());
        }
    }
}
```

If `getSpringExtensionFactory()` can be `null` at the moment a `Started` event fires during `SpringPluginManager.init()` (factory is created before `loadPlugins()` runs, so it is set — but be defensive), guard with:

```java
        if (springPluginManager.getSpringExtensionFactory() != null) {
            springPluginManager.getSpringExtensionFactory().evictAll();
        }
```

In `PluginConfiguration.java`, replace the bean method:

```java
    @Bean
    public DefaultPluginManager pluginManager(PluginRegister pluginClassRegister) {
        SpringPluginManager springPluginManager = new SpringPluginManager(Paths.get(pluginDir));
        springPluginManager.addPluginStateListener(new CustomPluginStateListener(pluginClassRegister));
        return springPluginManager;
    }
```

with:

```java
    @Bean
    public DefaultPluginManager pluginManager(PluginRegister pluginClassRegister) {
        SpringPluginManager springPluginManager = new SpringPluginManager(Paths.get(pluginDir));
        springPluginManager.addPluginStateListener(new CustomPluginStateListener(pluginClassRegister, springPluginManager));
        return springPluginManager;
    }
```

- [ ] **Step 6: Run the tests to verify they pass**

```bash
cd server && ./gradlew :modules-plugin:plugin-sdk:test
```

Expected: PASS (the three new tests plus the pre-existing `LifecycleListenerRegistryTest`).

- [ ] **Step 7: Commit**

```bash
git add server/modules-plugin/plugin-sdk/src/main/java/org/pf4j/spring/SpringExtensionFactory.java server/modules-plugin/plugin-sdk/src/main/java/org/pf4j/spring/SpringPluginManager.java server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/listen/CustomPluginStateListener.java server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/configuration/PluginConfiguration.java server/modules-plugin/plugin-sdk/src/test/java/org/pf4j/spring/SpringExtensionFactoryTest.java
git commit -m "fix(plugin-sdk): cache extension instances, evict on plugin state change, guard null plugin

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 4: Explicit plugin priority + deterministic wave-action selection

**Files:**
- Modify: `server/modules-plugin/plugin-extension/src/main/java/org/openwes/plugin/extension/IPlugin.java`
- Modify: `server/modules-wes/wes-outbound/src/main/java/org/openwes/wes/outbound/domain/service/impl/OutboundWaveServiceImpl.java:36-66`
- Test: `server/modules-wes/wes-outbound/src/test/java/org/openwes/wes/outbound/domain/service/impl/OutboundWaveServiceImplTest.java`

**Interfaces:**
- Consumes: `PluginSdkUtils.getExtractObject(Class)` (`plugin-sdk`), `IOutboundWaveWaveAction.wave(List<OutboundPlanOrderDTO>)`, `IOutboundWaveSplitAction.split(OutboundWaveDTO)`, `OutboundPlanOrderTransfer.toDTOs/toDOs`.
- Produces: `IPlugin.priority()` (default `0`) — Plan 1b's merge sorts by this same value.

- [ ] **Step 1: Write the failing test**

```java
package org.openwes.wes.outbound.domain.service.impl;

import org.junit.jupiter.api.Test;
import org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveSplitAction;
import org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveWaveAction;
import org.openwes.plugin.sdk.utils.PluginSdkUtils;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.OutboundPreAllocatedRecordRepository;
import org.openwes.wes.outbound.domain.transfer.OutboundPlanOrderTransfer;
import org.openwes.wes.outbound.domain.transfer.OutboundWaveTransfer;
import org.openwes.wes.outbound.domain.transfer.PickingOrderTransfer;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboundWaveServiceImplTest {

    private final PluginSdkUtils pluginUtils = mock(PluginSdkUtils.class);
    private final OutboundPlanOrderRepository outboundPlanOrderRepository = mock(OutboundPlanOrderRepository.class);
    private final OutboundWaveTransfer outboundWaveTransfer = mock(OutboundWaveTransfer.class);
    private final PickingOrderTransfer pickingOrderTransfer = mock(PickingOrderTransfer.class);
    private final OutboundPlanOrderTransfer outboundPlanOrderTransfer = mock(OutboundPlanOrderTransfer.class);
    private final OutboundPreAllocatedRecordRepository outboundPreAllocatedRecordRepository = mock(OutboundPreAllocatedRecordRepository.class);

    private final OutboundWaveServiceImpl service = new OutboundWaveServiceImpl(pluginUtils,
            outboundPlanOrderRepository, outboundWaveTransfer, pickingOrderTransfer,
            outboundPlanOrderTransfer, outboundPreAllocatedRecordRepository);

    private static IOutboundWaveWaveAction waveAction(int priority, List<List<OutboundPlanOrderDTO>> result) {
        return new IOutboundWaveWaveAction() {
            @Override
            public int priority() {
                return priority;
            }

            @Override
            public List<List<OutboundPlanOrderDTO>> wave(List<OutboundPlanOrderDTO> outboundPlanOrderDTOs) {
                return result;
            }
        };
    }

    @Test
    void wavePickings_picksHighestPriorityAction() {
        OutboundPlanOrder order = new OutboundPlanOrder().setCustomerWaveNo("W1");
        when(outboundPlanOrderTransfer.toDTOs(any())).thenReturn(List.of(new OutboundPlanOrderDTO()));
        List<List<OutboundPlanOrder>> pluginResult = List.of(List.of(order));
        when(outboundPlanOrderTransfer.toDOs(any())).thenReturn(pluginResult);
        when(pluginUtils.getExtractObject(IOutboundWaveWaveAction.class)).thenReturn(List.of(
                waveAction(10, List.of(List.of(new OutboundPlanOrderDTO()))),
                waveAction(100, List.of(List.of(new OutboundPlanOrderDTO())))));

        Collection<List<OutboundPlanOrder>> result = service.wavePickings(List.of(order));

        assertEquals(pluginResult, result);
    }

    @Test
    void wavePickings_whenHighestPriorityReturnsNull_fallsBackToBuiltInGrouping() {
        OutboundPlanOrder orderA = new OutboundPlanOrder().setCustomerWaveNo("W1");
        OutboundPlanOrder orderB = new OutboundPlanOrder().setCustomerWaveNo("W1");
        OutboundPlanOrder orderC = new OutboundPlanOrder().setCustomerWaveNo(null);
        when(pluginUtils.getExtractObject(IOutboundWaveWaveAction.class)).thenReturn(List.of(
                waveAction(100, null)));

        Collection<List<OutboundPlanOrder>> result = service.wavePickings(List.of(orderA, orderB, orderC));

        // built-in: one group for W1 (two orders) + one single-element group for the empty wave no
        assertEquals(2, result.size());
        assertEquals(1, result.stream().filter(g -> g.size() == 2).count());
        assertEquals(1, result.stream().filter(g -> g.size() == 1).count());
    }

    @Test
    void wavePickings_whenHighestPriorityThrows_fallsBackToBuiltInGrouping() {
        OutboundPlanOrder order = new OutboundPlanOrder().setCustomerWaveNo("W1");
        when(pluginUtils.getExtractObject(IOutboundWaveWaveAction.class)).thenReturn(List.of(
                new IOutboundWaveWaveAction() {
                    @Override
                    public int priority() {
                        return 100;
                    }

                    @Override
                    public List<List<OutboundPlanOrderDTO>> wave(List<OutboundPlanOrderDTO> outboundPlanOrderDTOs) {
                        throw new IllegalStateException("boom");
                    }
                }));

        Collection<List<OutboundPlanOrder>> result = service.wavePickings(List.of(order));

        assertEquals(1, result.size());
        assertEquals(1, result.iterator().next().size());
    }

    @Test
    void spiltWave_whenNoPlugins_usesBuiltInPath() {
        when(pluginUtils.getExtractObject(IOutboundWaveSplitAction.class)).thenReturn(List.of());

        service.spiltWave(new org.openwes.wes.outbound.domain.entity.OutboundWave());

        verify(outboundPlanOrderRepository, never()).findAllByIds(any());
    }
}
```

Notes:
- `OutboundPlanOrder` is `@Data @Accessors(chain = true)` — `setCustomerWaveNo` is fine. Its no-arg constructor is the implicit default.
- `OutboundWave` must be constructible without args; if it has a private constructor, replace the last test's subject with `service.spiltWave(null)`… verify at implementation time; if `OutboundWave` cannot be instantiated, delete the last test (`spiltWave` shares the identical code shape as `wavePickings`, which is covered).

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd server && ./gradlew :modules-wes:wes-outbound:test --tests "org.openwes.wes.outbound.domain.service.impl.OutboundWaveServiceImplTest"
```

Expected: FAIL — `IPlugin.priority()` does not exist yet (compilation error), and after it exists the selection is not priority-based.

- [ ] **Step 3: Add `priority()` to IPlugin**

Replace `IPlugin.java` with:

```java
package org.openwes.plugin.extension;

import org.pf4j.Extension;
import org.pf4j.ExtensionPoint;

@Extension
public interface IPlugin extends ExtensionPoint {

    default void initialize() {
    }

    default void destory() {
    }

    /**
     * Higher value wins when several plugins can serve the same extension point. Ties are broken
     * by the registry order (script plugins are merged after jar plugins, sorted by plugin key).
     */
    default int priority() {
        return 0;
    }

}
```

- [ ] **Step 4: Make `OutboundWaveServiceImpl` deterministic and fallback-safe**

Replace the class header and the two plugin branches. Imports to add: `lombok.extern.slf4j.Slf4j`, `org.openwes.plugin.extension.IPlugin`, `java.util.Comparator`. Replace:

```java
@Service
@RequiredArgsConstructor
public class OutboundWaveServiceImpl implements OutboundWaveService {
```

with:

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class OutboundWaveServiceImpl implements OutboundWaveService {
```

Replace the `wavePickings` plugin branch (currently `:39-43`):

```java
        List<IOutboundWaveWaveAction> outboundWaveActions = pluginUtils.getExtractObject(IOutboundWaveWaveAction.class);
        if (CollectionUtils.isNotEmpty(outboundWaveActions)) {
            return outboundWaveActions.iterator().next().wave(outboundPlanOrderTransfer.toDTOs(outboundPlanOrders))
                    .stream().map(outboundPlanOrderTransfer::toDOs).toList();
        }
```

with:

```java
        List<IOutboundWaveWaveAction> outboundWaveActions = pluginUtils.getExtractObject(IOutboundWaveWaveAction.class);
        IOutboundWaveWaveAction waveAction = outboundWaveActions.stream()
                .max(Comparator.comparingInt(IPlugin::priority)).orElse(null);
        if (waveAction != null) {
            try {
                List<List<OutboundPlanOrderDTO>> waves = waveAction.wave(outboundPlanOrderTransfer.toDTOs(outboundPlanOrders));
                if (waves != null) {
                    return waves.stream().map(outboundPlanOrderTransfer::toDOs).toList();
                }
                log.warn("outbound wave plugin returned null, falling back to built-in grouping");
            } catch (Exception e) {
                log.error("outbound wave plugin failed, falling back to built-in grouping", e);
            }
        }
```

(Add `import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;`.)

Replace the `spiltWave` plugin branch (currently `:61-66`):

```java
        List<IOutboundWaveSplitAction> outboundWaveSplitActions = pluginUtils.getExtractObject(IOutboundWaveSplitAction.class);
        if (CollectionUtils.isNotEmpty(outboundWaveSplitActions)) {
            List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(outboundWave.getOutboundPlanOrderIds());
            List<PickingOrderDTO> pickingOrderDTOS = outboundWaveSplitActions.iterator().next().split(outboundWaveTransfer.toDTO(outboundWave, outboundPlanOrders));
            return pickingOrderTransfer.toDOs(pickingOrderDTOS);
        }
```

with:

```java
        List<IOutboundWaveSplitAction> outboundWaveSplitActions = pluginUtils.getExtractObject(IOutboundWaveSplitAction.class);
        IOutboundWaveSplitAction splitAction = outboundWaveSplitActions.stream()
                .max(Comparator.comparingInt(IPlugin::priority)).orElse(null);
        if (splitAction != null) {
            try {
                List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(outboundWave.getOutboundPlanOrderIds());
                List<PickingOrderDTO> pickingOrderDTOS = splitAction.split(outboundWaveTransfer.toDTO(outboundWave, outboundPlanOrders));
                if (pickingOrderDTOS != null) {
                    return pickingOrderTransfer.toDOs(pickingOrderDTOS);
                }
                log.warn("outbound wave split plugin returned null, falling back to built-in split");
            } catch (Exception e) {
                log.error("outbound wave split plugin failed, falling back to built-in split", e);
            }
        }
```

- [ ] **Step 5: Run the tests to verify they pass**

```bash
cd server && ./gradlew :modules-wes:wes-outbound:test --tests "org.openwes.wes.outbound.domain.service.impl.OutboundWaveServiceImplTest"
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add server/modules-plugin/plugin-extension/src/main/java/org/openwes/plugin/extension/IPlugin.java server/modules-wes/wes-outbound/src/main/java/org/openwes/wes/outbound/domain/service/impl/OutboundWaveServiceImpl.java server/modules-wes/wes-outbound/src/test/java/org/openwes/wes/outbound/domain/service/impl/OutboundWaveServiceImplTest.java
git commit -m "fix(outbound): deterministic plugin selection by priority with built-in fallback

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 5: Liquibase changelog for `p_plugin`

**Files:**
- Create: `server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261008.xml`
- Modify: `server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml`

**Interfaces:**
- Consumes: Liquibase conventions (`<preConditions onFail="MARK_RAN">`, raw `<sql>`), master include list.
- Produces: `db.changelog-20261008.xml` — **Plan 1a appends its `p_script_plugin*` changeSets to this same file**; do not rename it.

- [ ] **Step 1: Create the changelog**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <!-- Legacy plugin metadata table: the Plugin JPA entity has no changelog, so the
         plugin management page failed with a missing-table error on every real deployment. -->
    <changeSet id="create_p_plugin_table" author="dev">
        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="p_plugin"/>
            </not>
        </preConditions>
        <sql>
            CREATE TABLE p_plugin (
                id                BIGINT       NOT NULL,
                code              VARCHAR(64)  NOT NULL COMMENT '插件编号',
                plugin_unique_key VARCHAR(255) NOT NULL COMMENT '插件ID,用于校验加载插件',
                name              VARCHAR(128) NOT NULL COMMENT '插件名称',
                developer         VARCHAR(255) NOT NULL COMMENT '开发者',
                plugin_version    VARCHAR(64)  NOT NULL COMMENT '插件版本',
                description       VARCHAR(255) NOT NULL DEFAULT '' COMMENT '描述',
                jar_file_path     VARCHAR(255) NOT NULL COMMENT 'jar文件地址',
                source_code_url   VARCHAR(255) NULL,
                license           VARCHAR(255) NULL,
                plugin_status     VARCHAR(64)  NOT NULL,
                download_count    INT          NOT NULL DEFAULT 0,
                version           BIGINT       NULL,
                create_time       BIGINT       NOT NULL DEFAULT 0 COMMENT 'Creation time',
                create_user       VARCHAR(60)  NOT NULL DEFAULT '' COMMENT 'Create user',
                update_time       BIGINT       NOT NULL DEFAULT 0,
                update_user       VARCHAR(60)  NOT NULL DEFAULT '',
                PRIMARY KEY (id),
                UNIQUE KEY uk_plugin_unique_key (plugin_unique_key),
                UNIQUE KEY uk_plugin_code_version (code, plugin_version)
            ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '插件元数据';
        </sql>
    </changeSet>

</databaseChangeLog>
```

- [ ] **Step 2: Include it in the master changelog**

In `db.changelog-master.xml`, after the line:

```xml
    <include file="/db/changelog/db.changelog-20260523.xml"/>
```

add:

```xml
    <include file="/db/changelog/db.changelog-20261008.xml"/>
```

- [ ] **Step 3: Verify the XML is well-formed**

```bash
python -c "import xml.dom.minidom,sys; xml.dom.minidom.parse('server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261008.xml'); xml.dom.minidom.parse('server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml'); print('XML OK')"
```

Expected: `XML OK`.

- [ ] **Step 4: Verify Liquibase picks the changelog up**

Run wes-server once against a scratch database (`docker compose up -d mysql` then `cd server && ./gradlew :server:wes-server:bootRun`), then check:

```sql
SELECT id, author, filename FROM l_change_log WHERE id = 'create_p_plugin_table';
SELECT COUNT(*) FROM p_plugin;
```

Expected: one row in `l_change_log`; `p_plugin` exists and is empty. If no database is available in the working environment, record that this step was skipped in the commit message and rely on Step 3 plus CI.

- [ ] **Step 5: Commit**

```bash
git add server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261008.xml server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml
git commit -m "fix(db): add Liquibase changelog for legacy p_plugin table

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 6: Gateway route `/plugin/**` → `lb://wes`

**Files:**
- Modify: `initdb.d/nacos_config.sql`
- Test: `server/server/wes-server/src/test/java/org/openwes/config/NacosGatewayRouteTest.java`

**Interfaces:**
- Consumes: the gateway YAML embedded in the `config_info` INSERT (line 54); route ids `user`, `ai`, `mdm`, `wms`, `search`, `plugin`, `api-platform`, `station`.
- Produces: a seed config where `/plugin/**` resolves to the wes service. Plan 1a adds the `plugin-platform` route with the same test extended.

- [ ] **Step 1: Write the failing test**

```java
package org.openwes.config;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the gateway seed config so a route cannot silently point at a non-existent service. */
class NacosGatewayRouteTest {

    private static String seedConfig() throws Exception {
        Path path = Path.of("..", "initdb.d", "nacos_config.sql");
        if (!Files.exists(path)) {
            path = Path.of("..", "..", "initdb.d", "nacos_config.sql");
        }
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    @Test
    void pluginRoute_pointsAtTheWesService() throws Exception {
        String sql = seedConfig();
        int start = sql.indexOf("- id: plugin\\n");
        int end = sql.indexOf("- id: api-platform\\n");
        assertTrue(start >= 0 && end > start, "plugin route block not found in nacos_config.sql");
        String pluginRoute = sql.substring(start, end);

        assertFalse(pluginRoute.contains("lb://plugin"), "no service named 'plugin' exists");
        assertTrue(pluginRoute.contains("uri: lb://wes"));
        assertTrue(pluginRoute.contains("Path=/plugin/**"));
    }
}
```

Note: `- id: plugin\n` contains the two literal characters `\` `n`; in Java source that is written `"- id: plugin\\n"`. The test runs from the `server/` Gradle root, hence the relative path fallbacks (`:server:wes-server:test` runs with working directory `server/server/wes-server`, so `../../initdb.d/nacos_config.sql` is the second candidate).

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd server && ./gradlew :server:wes-server:test --tests "org.openwes.config.NacosGatewayRouteTest"
```

Expected: FAIL — `pluginRoute.contains("lb://plugin")` is true.

- [ ] **Step 3: Fix the route**

With the Edit tool, replace (inside the giant single-line INSERT — copy the exact text):

- old_string: `uri: lb://plugin`
- new_string: `uri: lb://wes`

- [ ] **Step 4: Run the test to verify it passes**

```bash
cd server && ./gradlew :server:wes-server:test --tests "org.openwes.config.NacosGatewayRouteTest"
```

Expected: PASS.

- [ ] **Step 5: Note the operational step**

Existing deployments do **not** re-run this SQL seed. Update the `gateway` config in the Nacos console with the same change (`lb://plugin` → `lb://wes`) and publish it. Add this line to the commit message body.

- [ ] **Step 6: Commit**

```bash
git add initdb.d/nacos_config.sql server/server/wes-server/src/test/java/org/openwes/config/NacosGatewayRouteTest.java
git commit -m "fix(gateway): route /plugin/** to lb://wes

Existing deployments must apply the same change to the gateway config in Nacos.

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Task 7: Test suite becomes a real gate

**Files:**
- Modify: `server/build.gradle:38-41`
- Modify: `server/modules-plugin/plugin-sdk/build.gradle`, `server/modules-wes/wes-outbound/build.gradle` (append test block)
- Modify (only if pre-existing failures appear): the failing modules' `build.gradle`

**Interfaces:**
- Consumes: global `subprojects { test { useJUnitPlatform(); ignoreFailures = true } }`.
- Produces: red tests fail the build for every module touched by this and later plans.

- [ ] **Step 1: Flip the global flag**

In `server/build.gradle`, replace:

```groovy
    test {
        useJUnitPlatform()
        ignoreFailures = true
    }
```

with:

```groovy
    test {
        useJUnitPlatform()
        ignoreFailures = false
    }
```

- [ ] **Step 2: Append the per-module guard to the plan's modules**

At the end of `server/modules-plugin/plugin-sdk/build.gradle` and `server/modules-wes/wes-outbound/build.gradle`, append:

```groovy

test {
    ignoreFailures = false
}
```

- [ ] **Step 3: Run the full suite and record the outcome**

```bash
cd server && ./gradlew test --continue 2>&1 | tee /tmp/test-report.txt
```

Expected: mostly green (this and earlier plans added tests). For every failing module not touched by this plan, choose exactly one:
1. Trivial fix (typo, stale mock signature) → fix it in this task.
2. Pre-existing, non-trivial → append to that module's `build.gradle`:

```groovy

test {
    // Pre-existing failures unrelated to the plugin work; tracked in docs/optimization-plan.md §2.2
    ignoreFailures = true
}
```

- [ ] **Step 4: Re-run to confirm a green build**

```bash
cd server && ./gradlew test
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add server/build.gradle server/modules-plugin/plugin-sdk/build.gradle server/modules-wes/wes-outbound/build.gradle
# add any module build.gradle files exempted in Step 3 as well
git commit -m "test: fail builds on red tests; exempt pre-existing failing modules individually

Exempted modules (if any): <list them here, or 'none'>

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Acceptance for Phase 0

- `cd server && ./gradlew test` is green and fails on any red test in plugin-sdk / wes-outbound / wes-server.
- `PickingOrder` lifecycle events carry `entityName = "PickingOrder"` and a non-null `entityNo`.
- `/plugin/plugin/list` returns distinct rows per installed plugin; the gateway seed resolves `/plugin/**` to wes.
- `IPlugin.priority()` exists and `OutboundWaveServiceImpl` selects by it with built-in fallback on null/exception.
- `p_plugin` is created by Liquibase on a fresh database.
- Next: `docs/superpowers/plans/2026-10-08-script-plugin-phase1a-engine-management.md`.
