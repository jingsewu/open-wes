# Script Plugin Phase 4 — Closing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Close the script-plugin programme by making the backend docs, the contract catalog and a runnable Groovy sample mutually consistent and test-enforced.

**Architecture:** `PluginContractCatalog` gains a `toMarkdownTable()` renderer; `docs/standards/backend.md` embeds its output between marker comments and is checked by a JUnit test; the legacy `plugin-example` jar module becomes a non-built Groovy sample checked by the real static validator; `CLAUDE.md` gains a platform map and key file locations.

**Tech Stack:** Java 17, Gradle, JUnit 5, Jackson databind, Groovy 4, Markdown.

**Spec:** `docs/superpowers/specs/2026-10-27-script-plugin-phase4-closing-design.md`

## Global Constraints

- Work on `feature/script-plugin-ai-workbench` in place. Do **not** create a worktree.
- Java 17 is required; `JAVA_HOME` points at JDK 8 in this environment. Every Gradle command must prefix `JAVA_HOME="$HOME/.jdks/ms-17.0.20.1"`.
- Do **not** touch `client/`; Phase 4 has no client changes.
- Do **not** add `modules-plugin:plugin-example` back to `server/settings.gradle`.
- The block between `<!-- BEGIN GENERATED: script-plugin-archetypes -->` and `<!-- END GENERATED: script-plugin-archetypes -->` is generated. Never hand-edit inside it; change the catalog and paste `PluginContractCatalog.toMarkdownTable()`.
- The removed APIs `IContainerTaskGeneratorAction`, `getGeneratorPlugin` and `PluginUtils.getExtractObject` must not appear anywhere in `docs/standards/backend.md` (a test enforces this).
- The sample is validated statically only (contract, declarations, Groovy compilation). Do not claim runtime E2E, database, or application-context verification in docs or commit messages.
- Do **not** commit the untracked Phase 2/3A/3B plan documents. Stage explicit paths in every commit; never `git add docs/superpowers/plans/` as a directory.
- All docs, code comments and commit messages are in English.
- Commit messages end with:
  ```
  Co-Authored-By: Claude Code <noreply@anthropic.com>
  ```

---

### Task 1: Render the Archetype Table from the Contract Catalog

**Files:**
- Modify: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/contract/PluginContractCatalog.java`
- Test: `server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/contract/PluginContractCatalogTest.java`

**Interfaces:**
- Produces: `public static String toMarkdownTable()` — returns the full Markdown table (header, separator and one row per `ARCHETYPES` entry, each row terminated by `\n`).
- Produces (private): `hostExtensionPoint(PluginContractCatalog.ArchetypeDescriptor archetype)` — returns the host column text.
- Consumes: existing `ARCHETYPES`, `ArchetypeDescriptor.type()`, `.extensionPointClassName()`, `.scriptMethod()`, `.blocking()`, `ExtensionType.HTTP_ENDPOINT`.

- [ ] **Step 1: Write the failing test**

Append this test to `PluginContractCatalogTest`:

```java
@Test
void markdownTable_rendersEveryArchetypeWithHostPointAndBlocking() {
    String table = PluginContractCatalog.toMarkdownTable();

    assertEquals((long) PluginContractCatalog.archetypes().size() + 2, table.lines().count());
    for (PluginContractCatalog.ArchetypeDescriptor archetype : PluginContractCatalog.archetypes()) {
        assertTrue(table.contains("| `" + archetype.type() + "` |"),
                archetype.type() + " row is missing");
        assertTrue(table.contains("`" + archetype.scriptMethod() + "` |"),
                archetype.type() + " script method is missing");
    }
    assertTrue(table.contains("| `org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveWaveAction` | `wave` | yes |"));
    assertTrue(table.contains("| dispatcher (`/plugin-platform/ext/{pluginKey}/**`) | `handle` | yes |"));
    assertTrue(table.contains("| dispatcher (scheduler) | `run` | no |"));
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd server
JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew :modules-plugin:script-plugin-api:test \
  --tests '*PluginContractCatalogTest.markdownTable_rendersEveryArchetypeWithHostPointAndBlocking'
```

Expected: compilation FAIL with `cannot find symbol: method toMarkdownTable()`.

- [ ] **Step 3: Implement the renderer**

Add the following methods to `PluginContractCatalog`, after `archetypes()`:

```java
/** Renders the archetype contract as the Markdown table embedded in docs/standards/backend.md. */
public static String toMarkdownTable() {
    StringBuilder table = new StringBuilder();
    table.append("| Archetype | Host extension point | Script method | Blocking |\n");
    table.append("|-----------|----------------------|---------------|----------|\n");
    for (ArchetypeDescriptor archetype : ARCHETYPES) {
        table.append("| `").append(archetype.type()).append("` | ")
                .append(hostExtensionPoint(archetype)).append(" | `")
                .append(archetype.scriptMethod()).append("` | ")
                .append(archetype.blocking() ? "yes" : "no").append(" |\n");
    }
    return table.toString();
}

private static String hostExtensionPoint(ArchetypeDescriptor archetype) {
    if (archetype.extensionPointClassName() != null) {
        return "`" + archetype.extensionPointClassName() + "`";
    }
    return archetype.type() == ExtensionType.HTTP_ENDPOINT
            ? "dispatcher (`/plugin-platform/ext/{pluginKey}/**`)"
            : "dispatcher (scheduler)";
}
```

- [ ] **Step 4: Run the test to verify it passes**

```bash
cd server
JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew :modules-plugin:script-plugin-api:test \
  --tests '*PluginContractCatalogTest'
```

Expected: BUILD SUCCESSFUL, all `PluginContractCatalogTest` tests pass.

- [ ] **Step 5: Commit**

```bash
git add server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/contract/PluginContractCatalog.java \
        server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/contract/PluginContractCatalogTest.java
git commit -m "feat(script-plugin): render the archetype table from the contract catalog

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 2: Rewrite the Plugin Rules Chapter and Delete the Dead Interface

**Files:**
- Create: `server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/RepositoryRoot.java`
- Create: `server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/contract/PluginDocumentationConsistencyTest.java`
- Modify: `docs/standards/backend.md` (TOC lines 20–21; chapter lines 234–358)
- Delete: `server/modules-plugin/plugin-extension/src/main/java/org/openwes/plugin/extension/business/wes/ems/proxy/action/IContainerTaskGeneratorAction.java`

**Interfaces:**
- Produces: `RepositoryRoot.find()` → `Path` of the repository root, or `IllegalStateException` naming the starting path when `server/settings.gradle` is not found in any ancestor.
- Produces: `PluginDocumentationConsistencyTest` — asserts the generated block equals `PluginContractCatalog.toMarkdownTable().trim()` and that three removed API names are absent.
- Consumes: `PluginContractCatalog.toMarkdownTable()` from Task 1.

- [ ] **Step 1: Create the shared repository-root helper**

Create `RepositoryRoot.java`:

```java
package org.openwes.script.plugin.api;

import java.nio.file.Files;
import java.nio.file.Path;

/** Locates the repository root for tests that read docs and sample files. */
public final class RepositoryRoot {

    private RepositoryRoot() {
    }

    public static Path find() {
        Path start = Path.of("").toAbsolutePath().normalize();
        Path candidate = start;
        while (candidate != null) {
            if (Files.isRegularFile(candidate.resolve("server/settings.gradle"))) {
                return candidate;
            }
            candidate = candidate.getParent();
        }
        throw new IllegalStateException(
                "Cannot locate the repository root (server/settings.gradle) from " + start);
    }
}
```

- [ ] **Step 2: Write the failing docs-consistency test**

Create `PluginDocumentationConsistencyTest.java`:

```java
package org.openwes.script.plugin.api.contract;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.RepositoryRoot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginDocumentationConsistencyTest {

    private static final String BEGIN = "<!-- BEGIN GENERATED: script-plugin-archetypes -->";
    private static final String END = "<!-- END GENERATED: script-plugin-archetypes -->";

    @Test
    void generatedArchetypeTable_matchesTheContractCatalog() throws IOException {
        String docs = readDocs().replace("\r\n", "\n");
        int begin = docs.indexOf(BEGIN);
        int end = docs.indexOf(END);

        assertTrue(begin >= 0, "backend.md is missing " + BEGIN);
        assertTrue(end > begin, "backend.md is missing " + END + " after the begin marker");

        String documented = docs.substring(begin + BEGIN.length(), end).trim();
        assertEquals(PluginContractCatalog.toMarkdownTable().trim(), documented,
                "Regenerate the block from PluginContractCatalog.toMarkdownTable()");
    }

    @Test
    void backendDocumentation_doesNotReferenceRemovedApis() throws IOException {
        String docs = readDocs();

        assertFalse(docs.contains("IContainerTaskGeneratorAction"),
                "backend.md must not reference the deleted IContainerTaskGeneratorAction");
        assertFalse(docs.contains("getGeneratorPlugin"),
                "backend.md must not reference the deleted getGeneratorPlugin accessor");
        assertFalse(docs.contains("PluginUtils.getExtractObject"),
                "lookup is PluginSdkUtils.getExtractObject; PluginUtils.getExtractObject does not exist");
    }

    private String readDocs() throws IOException {
        return Files.readString(RepositoryRoot.find().resolve("docs/standards/backend.md"),
                StandardCharsets.UTF_8);
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

```bash
cd server
JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew :modules-plugin:script-plugin-api:test \
  --tests '*PluginDocumentationConsistencyTest'
```

Expected: FAIL with `backend.md is missing <!-- BEGIN GENERATED: script-plugin-archetypes -->`.

- [ ] **Step 4: Update the Table of Contents**

Replace lines 20–21 of `docs/standards/backend.md`:

```markdown
- [Plugin Rules](#plugin-rules)
  - [Script Plugin Rules](#script-plugin-rules)
  - [Jar Plugin Rules (Legacy)](#jar-plugin-rules-legacy)
```

- [ ] **Step 5: Replace the whole `## Plugin Rules` chapter**

Replace everything from `## Plugin Rules` through the end of current rule 12 (the line immediately before `## Unit Test Rules`) with the following. Do not use fixed line numbers: the TOC edit in the previous step shifted them. The four-backtick fence below is only a plan wrapper; write the content with normal triple-backtick fences.

````markdown
## Plugin Rules

Script plugins are the default for new plugin work: a plugin is a versioned bundle of Groovy scripts plus extension declarations, stored in the database and materialized into a running server at enable time. The PF4J jar-plugin path is kept for existing plugins and is frozen; do not add new jar plugins.

### Script Plugin Rules

1. **Contract Catalog Is the Single Source of Truth**: The allowed extension points, script methods and blocking semantics are defined once in `PluginContractCatalog.ARCHETYPES` (`org.openwes.script.plugin.api.contract`). The AI prompt, this documentation and the enable-time validation all read that catalog. The table below is generated by `PluginContractCatalog.toMarkdownTable()` and enforced by `PluginDocumentationConsistencyTest` — never edit it by hand; change the catalog and regenerate.

<!-- BEGIN GENERATED: script-plugin-archetypes -->
| Archetype | Host extension point | Script method | Blocking |
|-----------|----------------------|---------------|----------|
| `OUTBOUND_WAVE_WAVE_ACTION` | `org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveWaveAction` | `wave` | yes |
| `OUTBOUND_WAVE_SPLIT_ACTION` | `org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveSplitAction` | `split` | yes |
| `REQUEST_VALIDATE` | `org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin` | `validate` | yes |
| `REQUEST_SUPPLY` | `org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin` | `supply` | no |
| `REQUEST_AFTER_INVOKE` | `org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin` | `afterInvoke` | no |
| `CUSTOM_REQUEST_INVOKE` | `org.openwes.plugin.extension.business.api.platform.request.ICustomRequestPlugin` | `invoke` | yes |
| `CUSTOM_API` | `org.openwes.plugin.extension.business.station.ICustomApiPlugin` | `execute` | yes |
| `LIFECYCLE_LISTENER` | `org.openwes.plugin.extension.business.IEntityLifecycleListener` | `afterStatusChange` | no |
| `HTTP_ENDPOINT` | dispatcher (`/plugin-platform/ext/{pluginKey}/**`) | `handle` | yes |
| `CRON` | dispatcher (scheduler) | `run` | no |
<!-- END GENERATED: script-plugin-archetypes -->

2. **Script Method Signature: `ctx` Is Always the Last Parameter**: The runtime appends the extension's `ScriptContext` as the final argument of every invocation, after the host method arguments. Declare every script method with the host parameters first and `ctx` last; a method that does not accept the appended argument fails at invocation time (the smoke stage only checks that the declared method exists). Script instances are created with Spring's `createBean` inside a child context whose parent is the application context, so `@Autowired`, `@Resource`, `@Value` and `@PostConstruct` all work. Use `ctx.log()` for logging, `ctx.bean(...)` or the facade methods (`ctx.outbound()`, `ctx.pickingOrder()`, `ctx.callback()`, `ctx.station()`) for host beans, and `ctx.configString/configInt/configBool` for plugin config. Secrets come from `ctx.config` only — never hardcode them in a script.

```groovy
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi
import org.springframework.beans.factory.annotation.Autowired

class Wave {
    @Autowired
    IOutboundPlanOrderApi outbound

    Object wave(Object orders, Object ctx) {
        ctx.log().info('grouping {} orders for plugin {}', orders.size(), ctx.pluginKey())
        return orders.collect { [it] }
    }
}
```

3. **Script Plugin Failure Policy**: Failures never disappear silently, and only blocking archetypes propagate the failure to the host. Blocking means the `ScriptPluginInvocationException` reaches the host, which then decides how to react: the `OUTBOUND_WAVE_WAVE_ACTION` and `OUTBOUND_WAVE_SPLIT_ACTION` hosts catch any exception from the plugin (and accept a `null` return) and fall back to the built-in grouping/splitting, so a wave plugin never aborts wave creation.

| Situation | Behavior |
|-----------|----------|
| Blocking archetype throws or times out | `ScriptPluginInvocationException` is thrown to the host; unless the host defines a fallback (as the wave hosts do), the host operation aborts. |
| Non-blocking archetype throws or times out | The error is logged (`script plugin ... failed (non-blocking, continuing)`) and the default value for the return type is returned; the host continues. |
| `HTTP_ENDPOINT` throws or times out | The dispatcher logs the error and responds HTTP 500 with `Response.fail()`; the HTTP call fails. |
| `CRON` throws | The failure is logged and counted; after `script-plugin.cron.max-consecutive-failures` (default `10`) consecutive failures the plugin is disabled automatically through the management plane. |

Every invocation is reported to `p_script_plugin_execution_log` with plugin key, revision, extension type, success flag, duration and error message. Shipping the log must never break the invocation path; a failed shipment is only warned. Script execution is bounded by `script-plugin.execution.timeout-ms` (default `2000`).

4. **Priority and Built-in Fallback**: `IPlugin.priority()` returns `0` by default; the higher value wins when several plugins can serve the same extension point. `PluginSdkUtils.getExtractObject` and `LifecycleListenerRegistry` merge PF4J jar plugins with materialized script extensions and sort them by priority descending. Ties are broken by stable order: jar plugins are appended first, then script plugins in registry order (plugin key ascending). A script proxy answers `priority()` from its declaration's `priority` field. When no plugin for an extension point is enabled, the extracted list is empty and the calling code must keep its built-in behavior — a plugin never replaces the default unless it is enabled.

5. **Enable, Disable and Rollback Flow**: A plugin starts as a draft and receives immutable revisions, each with a content hash; `current_revision_id` points at the active revision. A revision can be saved only while the plugin is `DRAFT` or `DISABLED`; saving on an `ENABLED` plugin is rejected because the runtime would hot-deploy an unvalidated revision, and saving on an `ARCHIVED` plugin is rejected because archived plugins are read-only. To deploy a changed revision of an enabled plugin, disable it, save the new revision, then enable it again (validation runs). Enable is accepted only from `DRAFT` or `DISABLED`, only when a current revision exists, and only when the plugin's `sdk_version` equals the platform contract (`ScriptPluginApiConstants.CONTRACT_VERSION`, currently `1.0`). Validation runs on the node whose app matches `apply_system`: wes validates in-process; a remote app such as station is asked through the pending-validation channel — the management plane registers the pending request and publishes a management message, and the target node's `StationValidationPoller` polls `getPendingValidation` every `script-plugin.validation.poll-interval-ms` (default `5000` ms), dry-runs the bundle and reports the verdict back within the configured timeout. The Redis message itself wakes `ScriptPluginRegistry.refreshAll`; it is not what delivers the validation request. A failed validation leaves the plugin disabled and records the failing stage and message.

   The five validation stages are:
   1. `sdkVersion` — the plugin's `sdk_version` equals the platform contract version.
   2. `declarations` — static legality: known extension type, script and method present, declared method matches the archetype's script method, referenced script exists, `apiType` for request archetypes, `entityName` for lifecycle listeners, `customApiCode` for custom APIs, valid non-duplicate HTTP path/method, parseable cron expression.
   3. `compile` — every Groovy script compiles.
   4. `context` — dry-run materialization in a throwaway child context, including injection and `@PostConstruct`.
   5. `smoke` — every declared script actually declares the declared method.

   On success, `enable` publishes the management message; running nodes rematerialize without a restart (Redis message, with a 60 s registry-version poll as the safety net). A failed rematerialization keeps the previous revision running. `disable` only applies to an `ENABLED` plugin and makes every node drop the materialization, restoring the built-in behavior. `rollback` is a pointer switch to any revision that belongs to the plugin — earlier, current or later (archived plugins cannot be rolled back) — and republishes the bundle; revision history is never mutated. Rollback does not re-run the five validation stages, so operators should only roll back to a revision that already passed enable-time validation.

6. **Lifecycle Listeners Are Not `IPlugin`s**: `IEntityLifecycleListener` intentionally does **not** extend `IPlugin`. In the merge it therefore keeps priority `0` and stable order. Listeners are dispatched by `getEntityName()` matching the entity name through `LifecycleListenerRegistry`, one call per registered listener; a script `LIFECYCLE_LISTENER` declaration must set `entityName` and expose the `afterStatusChange` method.

7. **Runtime Wiring**: `wes-server` depends on both `script-plugin-core` (management plane) and `script-plugin-runtime`, and sets `script-plugin.registry.mode: local` so enable-time validation runs in-process. `station-server` depends on `script-plugin-runtime` only and sets `mode: remote`; it must also be annotated with `@EnableScheduling`, because registry polling, cron dispatch and the validation poller are `@Scheduled`. Both applications scan `org.openwes`, which covers `org.openwes.script.plugin.runtime`. Any other server embedding the runtime uses `mode: remote` and a Dubbo reference to the management plane.

   **v1 limitations:** (a) `apply_module` is stored and displayed but not enforced — a node materializes every enabled plugin for its `apply_system`, because the frozen `IScriptPluginApi.getEnabledBundles(String applySystem)` has no module argument; (b) compile/context dry-run and `@PostConstruct` are not time-bounded — only script invocations honour `script-plugin.execution.timeout-ms`; (c) self-provided HTTP endpoints are served by the app hosting the plugin — the gateway routes `/plugin-platform/**` to wes, so station HTTP endpoints are only reachable directly; (d) the workbench authorizes with server-side role checks against configurable role codes (`plugin.security.developer-role-codes`, default `pluginDeveloper`; `plugin.security.admin-role-codes`, default `pluginAdmin`; the existing super role is implicit). The configured codes are read at startup, so changing a code requires a wes-server restart. Every state-changing workbench action is appended to `p_script_plugin_operation_audit`; the gateway's `get:/plugin-platform/` and `post:/plugin-platform/` permissions only gate broad access, not individual actions; (e) a hot revision switch closes the previous child context, so an in-flight invocation on the old revision can fail during the switch (failure policies contain it).

8. **AI Draft Pipeline (`ai-plugin`)** — AI may only save revisions, never enable, disable, roll back or archive. Model output is untrusted: it is parsed into `AiDraftDocument`, checked against `PluginContractCatalog.typesForApp`, then run through the same static validation as enable-time; on wes it also gets the in-process materialization dry-run, while station drafts are static-only in v2 (preview handshake lands with the workbench). Every AI revision stores `RevisionOriginEnum.AI`, the model id and `PluginPromptTemplates.PROMPT_VERSION`. Never log requirements, prompts, model output or generated sources at info level. Generated scripts must read secrets through `ctx.config`, never hardcode them; prompts state the same rule. The SSE endpoint is `/plugin-platform/ai/draft/stream`; the gateway and any reverse proxy must disable response buffering for it. Every AI endpoint (`stream`, `generations`, `refreshCorpus`) requires the developer role through `PluginWorkbenchAccessService`, and each accepted AI revision is audited as `SAVE_REVISION` with actor, revision id and origin.

9. **Workbench Authorization and Audit** — `PluginWorkbenchAccessService` is the single authorization point for the workbench; controllers and domain services never read roles directly. Administrator implies developer, and the configured super role is implicitly both. Every management action (`createDraft`, `saveRevision`, `validate`, `enable`, `disable`, `archive`, `rollback`, `updateMetadata`, config save, test-run) goes through a workbench service (`ScriptPluginWorkbenchService`, `ScriptPluginConfigService`, or `ScriptPluginTestRunService`) and is appended to `p_script_plugin_operation_audit` with actor, action, plugin key, revision id, success and a short detail. Audit failures are logged and never change the business result. Denials are audited only for state-changing actions; read-only denials are logged at warn level. Audit details must never contain script bodies, prompts, config values, request bodies, test arguments or test results. `disableBySystem` is the only path that skips the role check, and only for internal callers such as the cron circuit breaker. Test-run executes the real script on the wes node inside a throwaway revision context, writes no execution log and is rejected for non-wes plugins; config saves use the `lockVersion` optimistic lock, and validation never echoes values.

10. **Example: Lazy Waving**: `server/modules-plugin/plugin-example/README.md` is a runnable sample, not a built module. It declares one `OUTBOUND_WAVE_WAVE_ACTION` whose `wave` method lives in `lazy-waving.groovy`, plus two string config fields: `packingStatusApiCode` (default `GET_PACKING_STATUS`) and `packingStatusOnlineValue` (default `1`). It is not seeded automatically: create the draft in the workbench (`/plugin-platform/plugin-workbench?mode=new`), save a revision, validate it and enable it as an administrator. `ctx.outbound()` and `ctx.callback()` are wes-only facade methods; read secrets through `ctx.config` only.

### Jar Plugin Rules (Legacy)

The jar-plugin path is frozen: it remains for plugins already packaged with PF4J, but new work must use a script plugin. These rules describe the real interfaces.

1. **One Domain Plugin Interface per Domain**: Every business domain has one extension-point interface that extends `IPlugin` and exposes that domain's lifecycle hooks. `IPlugin` extends PF4J's `ExtensionPoint` and provides default no-op `initialize()` and `destory()` methods and a `priority()` that defaults to `0` (higher wins).

```java
public interface IOutboundWavePlugin extends IPlugin, IEntityLifecycleListener<Long, String> {
    default String getEntityName() {
        return "OutboundWave";
    }
}
```

   `IEntityLifecycleListener<U, V>` supplies `afterStatusChange(U entityId, V entityOrderNo, String newStatus)` and requires `getEntityName()`. Implementations return the host entity name they listen to: `IOutboundPlanOrderPlugin` returns `OutboundPlanOrder`, `IOutboundWavePlugin` returns `OutboundWave`, and `IContainerTaskPlugin` returns `ContainerTask`.

2. **Action Interfaces Extend the Domain Interface**: Additional capabilities are separate action interfaces that extend the domain interface, so an implementation registered for the domain is also discoverable through the action. Methods default to `null`, and the caller treats `null` as "no plugin result, use built-in behavior".

```java
public interface IOutboundWaveWaveAction extends IOutboundWavePlugin {
    default List<List<OutboundPlanOrderDTO>> wave(List<OutboundPlanOrderDTO> orders) {
        return null;
    }
}

public interface IOutboundWaveSplitAction extends IOutboundWavePlugin {
    default List<PickingOrderDTO> split(OutboundWaveDTO wave) {
        return null;
    }
}
```

3. **Lookup, Priority and Fallback**: Business code looks up extensions through the injected `PluginSdkUtils.getExtractObject(Class<T>)`. It merges PF4J jar extensions with materialized script-plugin proxies, sorts by `priority()` descending, and keeps jar plugins first for equal priorities. When no plugin is enabled the returned list is empty; the caller must then run its built-in logic. A plugin result of `null` means "no result" as well.

```java
List<IOutboundWaveWaveAction> plugins = pluginSdkUtils.getExtractObject(IOutboundWaveWaveAction.class);
if (!plugins.isEmpty()) {
    List<List<OutboundPlanOrderDTO>> waves = plugins.get(0).wave(orders);
    if (waves != null) {
        return waves;
    }
}
// built-in wave grouping
```

4. **Jar Plugin Configuration and Packaging**: A jar plugin is packaged with a `plugin.properties` descriptor and implementations are annotated with PF4J's `@Extension`; the module processes `org.pf4j:pf4j` through `annotationProcessor`. Its configuration is read with `PluginUtils.getPluginConfig(pluginId, Class<T>)`, which resolves the stored config through `IPluginApi.getPluginConfig` and returns a fresh instance when no config is stored. The former jar sample inside `server/modules-plugin/plugin-example/` has been replaced by the script-plugin sample; the jar path receives no new samples.
````

- [ ] **Step 6: Delete the orphan interface**

```bash
git rm server/modules-plugin/plugin-extension/src/main/java/org/openwes/plugin/extension/business/wes/ems/proxy/action/IContainerTaskGeneratorAction.java
```

- [ ] **Step 7: Run the test to verify it passes**

```bash
cd server
JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew :modules-plugin:script-plugin-api:test \
  --tests '*PluginDocumentationConsistencyTest'
```

Expected: BUILD SUCCESSFUL, both tests pass.

- [ ] **Step 8: Verify no Java references to the deleted interface remain**

```bash
grep -rn "IContainerTaskGeneratorAction" server --include=*.java
```

Expected: no output.

- [ ] **Step 9: Commit**

```bash
git add docs/standards/backend.md \
        server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/RepositoryRoot.java \
        server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/contract/PluginDocumentationConsistencyTest.java \
        server/modules-plugin/plugin-extension/src/main/java/org/openwes/plugin/extension/business/wes/ems/proxy/action/IContainerTaskGeneratorAction.java
git commit -m "docs(script-plugin): rewrite the plugin rules against the real contract

Generate the archetype table from PluginContractCatalog, add a docs
consistency test, and delete the unreferenced
IContainerTaskGeneratorAction interface.

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 3: Rewrite `plugin-example` as a Script-Plugin Sample

**Files:**
- Create: `server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/example/ScriptPluginExampleValidationTest.java`
- Create: `server/modules-plugin/plugin-example/README.md`
- Create: `server/modules-plugin/plugin-example/plugin.json`
- Create: `server/modules-plugin/plugin-example/lazy-waving.groovy`
- Delete (tracked): `server/modules-plugin/plugin-example/build.gradle`
- Delete (tracked): `server/modules-plugin/plugin-example/src/` (8 Java files + `plugin.properties`)
- Modify: `server/settings.gradle:28` (remove the `include`)

**Interfaces:**
- Consumes: `RepositoryRoot.find()` from Task 2; `ScriptPluginStaticValidator.validate(ScriptPluginBundleDTO)` returning `List<ValidationReportDTO.ValidationFailureDTO>`; `PluginContractCatalog.typesForApp(String)`; `ScriptPluginBundleDTO` setters `setScripts(Map<String, String>)`.
- Produces: a `plugin.json` whose `pluginKey` is `lazy-waving`, one declaration of type `OUTBOUND_WAVE_WAVE_ACTION`, script `lazy-waving.groovy`, method `wave`, priority `100`, and two required `STRING` config fields.

- [ ] **Step 1: Write the failing sample-validation test**

Create `ScriptPluginExampleValidationTest.java`:

```java
package org.openwes.script.plugin.api.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.plugin.api.RepositoryRoot;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.api.validation.ScriptPluginStaticValidator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScriptPluginExampleValidationTest {

    @Test
    void sampleBundle_passesStaticValidation() throws IOException {
        List<ValidationReportDTO.ValidationFailureDTO> failures =
                new ScriptPluginStaticValidator(new GroovyScriptEngine()).validate(loadSampleBundle());

        assertTrue(failures.isEmpty(), () -> failures.stream()
                .map(failure -> failure.getStage() + ": " + failure.getMessage())
                .collect(Collectors.joining("\n")));
    }

    @Test
    void sampleDeclarations_areAllowedForTheTargetApp() throws IOException {
        ScriptPluginBundleDTO bundle = loadSampleBundle();
        List<ExtensionType> allowed = PluginContractCatalog.typesForApp(bundle.getApplySystem());

        assertFalse(bundle.getDeclarations().isEmpty(), "the sample must declare at least one extension");
        for (ExtensionDeclarationDTO declaration : bundle.getDeclarations()) {
            assertTrue(allowed.contains(declaration.getType()),
                    declaration.getType() + " is not allowed for applySystem " + bundle.getApplySystem());
        }
    }

    private ScriptPluginBundleDTO loadSampleBundle() throws IOException {
        Path sample = RepositoryRoot.find().resolve("server/modules-plugin/plugin-example");
        ScriptPluginBundleDTO bundle = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .readValue(Files.readString(sample.resolve("plugin.json"), StandardCharsets.UTF_8),
                        ScriptPluginBundleDTO.class);

        Map<String, String> scripts = new LinkedHashMap<>();
        try (Stream<Path> files = Files.list(sample)) {
            for (Path script : files.filter(file -> file.getFileName().toString().endsWith(".groovy")).toList()) {
                scripts.put(script.getFileName().toString(), Files.readString(script, StandardCharsets.UTF_8));
            }
        }
        bundle.setScripts(scripts);
        return bundle;
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
cd server
JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew :modules-plugin:script-plugin-api:test \
  --tests '*ScriptPluginExampleValidationTest'
```

Expected: FAIL with `java.nio.file.NoSuchFileException: .../plugin-example/plugin.json`.

- [ ] **Step 3: Remove the legacy jar module from the build and delete its sources**

Edit `server/settings.gradle` and delete line 28:

```groovy
include 'modules-plugin:plugin-example'
```

Then delete the tracked legacy module files:

```bash
git rm -r server/modules-plugin/plugin-example/src server/modules-plugin/plugin-example/build.gradle
```

Expected: `git rm` lists seven `.java` files and `src/main/resources/plugin.properties`. The untracked `plugin-example/build/` directory is left in place (it is gitignored and not part of the change set).

- [ ] **Step 4: Create `plugin.json`**

```json
{
  "pluginKey": "lazy-waving",
  "name": "Lazy Waving",
  "description": "Groups outbound plan orders into waves, waiting for completed parent orders and the packing station.",
  "applySystem": "wes",
  "applyModule": "outbound",
  "sdkVersion": "1.0",
  "declarations": [
    {
      "type": "OUTBOUND_WAVE_WAVE_ACTION",
      "priority": 100,
      "script": "lazy-waving.groovy",
      "method": "wave"
    }
  ],
  "configSchema": [
    {
      "field": "packingStatusApiCode",
      "type": "STRING",
      "required": true,
      "defaultValue": "GET_PACKING_STATUS"
    },
    {
      "field": "packingStatusOnlineValue",
      "type": "STRING",
      "required": true,
      "defaultValue": "1"
    }
  ]
}
```

- [ ] **Step 5: Create `lazy-waving.groovy`**

```groovy
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum
import org.openwes.common.utils.http.Response
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO

class LazyWaving {

    List<List<OutboundPlanOrderDTO>> wave(List<OutboundPlanOrderDTO> orders, Object ctx) {
        if (!orders) {
            return []
        }
        def apiCode = ctx.configString('packingStatusApiCode') ?: 'GET_PACKING_STATUS'
        def onlineValue = ctx.configString('packingStatusOnlineValue') ?: '1'
        ctx.log().info('lazy-waving: received {} orders for plugin {}', orders.size(), ctx.pluginKey())

        def waveNos = orders.findAll { it.customerWaveNo }.collect { it.customerWaveNo } as Set
        def related = waveNos ? ctx.outbound().getByCustomerWaveNos(waveNos) : []
        def relatedByWaveNo = related.groupBy { it.customerWaveNo }

        def pending = orders.findAll { order ->
            if (!order.customerWaveNo) {
                return true
            }
            def group = relatedByWaveNo[order.customerWaveNo] ?: []
            group.every {
                it.outboundPlanOrderStatus == OutboundPlanOrderStatusEnum.NEW ||
                    it.outboundPlanOrderStatus == OutboundPlanOrderStatusEnum.ASSIGNED
            }
        }

        def selected = pending
        if (pending.size() != orders.size()) {
            boolean online = packingStationOnline(apiCode, onlineValue, ctx)
            selected = online ? orders : pending
            ctx.log().info('lazy-waving: packing station online={}, selected {} of {} orders',
                online, selected.size(), orders.size())
        }
        return group(selected)
    }

    private boolean packingStationOnline(String apiCode, String onlineValue, Object ctx) {
        Response response = ctx.callback().callback(CallbackApiTypeEnum.COMMON_CALLBACK, apiCode, null)
        if (response == null || response.code != Response.SUCCESS_CODE) {
            ctx.log().warn('lazy-waving: packing-station status unavailable, response={}', response)
            return false
        }
        return response.data == onlineValue
    }

    private List<List<OutboundPlanOrderDTO>> group(List<OutboundPlanOrderDTO> orders) {
        List<List<OutboundPlanOrderDTO>> result = []
        result.addAll(orders.findAll { it.customerWaveNo }.groupBy { it.customerWaveNo }.values())
        result.addAll(orders.findAll { !it.customerWaveNo }.groupBy { it.customerOrderType ?: '' }.values())
        return result
    }
}
```

- [ ] **Step 6: Create `README.md`**

````markdown
# Lazy Waving — Script Plugin Sample

This sample is the script-plugin replacement for the legacy lazy-waving jar plugin. It declares one
`OUTBOUND_WAVE_WAVE_ACTION`: outbound plan orders are grouped into waves, but orders that share a
`customerWaveNo` with an already completed order are withheld until the packing station reports
online.

## Files

| File | Purpose |
|---|---|
| `plugin.json` | Bundle metadata: one declaration and two string config fields |
| `lazy-waving.groovy` | The script invoked by the host wave action |

## Declaration

| Field | Value |
|---|---|
| type | `OUTBOUND_WAVE_WAVE_ACTION` |
| script | `lazy-waving.groovy` |
| method | `wave` |
| priority | `100` |

## Configuration

| Field | Type | Required | Default |
|---|---|---|---|
| `packingStatusApiCode` | STRING | yes | `GET_PACKING_STATUS` |
| `packingStatusOnlineValue` | STRING | yes | `1` |

## Import and enable

The sample is not seeded automatically. Create it through the workbench:

1. Open `/plugin-platform/plugin-workbench?mode=new` as a user with the `pluginDeveloper` role.
2. Create a draft with the metadata from `plugin.json` and paste `lazy-waving.groovy` into the script editor.
3. Add the two config fields, save a revision and validate it.
4. Enable the plugin from `/plugin-platform/plugins` as a user with the `pluginAdmin` role.

## Runtime notes

- `ctx.outbound()` and `ctx.callback()` are available only on `wes`; the sample declares `applySystem: "wes"`.
- Read secrets through `ctx.config`, never hardcode them in the script.
- With no enabled wave plugin, wave creation keeps its built-in behavior.

## Validation

```bash
cd server
./gradlew :modules-plugin:script-plugin-api:test --tests '*ScriptPluginExampleValidationTest'
```

The test compiles the Groovy script and runs the same static validation stages as enable time
(`sdkVersion`, `declarations`, `compile`). It does not start an application context or touch a
database.

See `docs/standards/backend.md` §Script Plugin Rules for the full contract.
````

- [ ] **Step 7: Run the sample test to verify it passes**

```bash
cd server
JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew :modules-plugin:script-plugin-api:test \
  --tests '*ScriptPluginExampleValidationTest'
```

Expected: BUILD SUCCESSFUL, both tests pass.

- [ ] **Step 8: Run the whole `script-plugin-api` test suite**

```bash
cd server
JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew :modules-plugin:script-plugin-api:test
```

Expected: BUILD SUCCESSFUL; the new tests plus the pre-existing `PluginContractCatalogTest`, `ScriptPluginContentHasherTest` and `ScriptPluginStaticValidatorTest` pass.

- [ ] **Step 9: Commit**

```bash
git add server/settings.gradle \
        server/modules-plugin/plugin-example/README.md \
        server/modules-plugin/plugin-example/plugin.json \
        server/modules-plugin/plugin-example/lazy-waving.groovy \
        server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/example/ScriptPluginExampleValidationTest.java
git add -u server/modules-plugin/plugin-example
git commit -m "feat(script-plugin): replace the jar sample with a lazy-waving script sample

Remove plugin-example from the Gradle build and rewrite it as a
non-built Groovy bundle, validated by the real static validator.

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 4: Document the Platform in `CLAUDE.md`

**Files:**
- Modify: `CLAUDE.md` (Project Structure line 21; new section after Backend Conventions; Key File Locations table)

**Interfaces:**
- Consumes: module names and routes established in Tasks 1–3.
- Produces: a `## Script Plugin Platform` section and six Key File Locations rows. No rule text is duplicated; the section points at `docs/standards/backend.md` §Script Plugin Rules.

- [ ] **Step 1: Update the `modules-plugin/` line in Project Structure**

Replace line 21:

```markdown
modules-plugin/       # Plugin platform: script-plugin-api/core/runtime, legacy PF4J jar modules, plugin-example sample
```

- [ ] **Step 2: Add the platform section after Backend Conventions**

Insert after the last Backend Conventions bullet (`- **Plugins**: One IPlugin interface per domain with before/after lifecycle hooks`) and before `## Frontend Conventions`:

```markdown
## Script Plugin Platform

Script plugins are database-backed Groovy bundles — no jar packaging and no restart. The legacy PF4J jar-plugin path is frozen.

- **Modules**: `script-plugin-api` — contract catalog, DTOs and static validation; `script-plugin-core` — persistence, management services and `/plugin-platform/**`; `script-plugin-runtime` — materialization, invocation, dispatchers and registry; `plugin-api`/`plugin-core`/`plugin-sdk`/`plugin-extension` — legacy jar path; `plugin-example` — non-built sample (not in `settings.gradle`).
- **Hosts**: `wes-server` hosts management + runtime (`script-plugin.registry.mode: local`); `station-server` hosts runtime only (`mode: remote`).
- **Lookup**: `PluginSdkUtils.getExtractObject(Class<T>)` merges jar extensions with materialized script proxies, sorts by priority descending; an empty list means "use the built-in behavior".
- **Workbench**: `/plugin-platform/plugins`, `/plugin-platform/plugin-workbench?mode=new` (or `?pluginKey=<key>`), `/plugin-platform/audit`; client code lives in `client/src/pages/plugin_platform/`.
- **Tests**: `cd server && ./gradlew :modules-plugin:script-plugin-api:test`
- **Rules**: `docs/standards/backend.md` §Script Plugin Rules; the archetype table is generated from `PluginContractCatalog` and checked by `PluginDocumentationConsistencyTest`.
```

- [ ] **Step 3: Add Key File Locations rows**

Append these rows to the Key File Locations table (after the Script plugin AI pipeline row):

```markdown
| Script plugin contract catalog | `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/contract/PluginContractCatalog.java` |
| Script plugin management plane | `server/modules-plugin/script-plugin-core/` |
| Script plugin runtime | `server/modules-plugin/script-plugin-runtime/` |
| Script plugin sample | `server/modules-plugin/plugin-example/` |
| Script plugin workbench (client) | `client/src/pages/plugin_platform/` |
| Script plugin rules chapter | `docs/standards/backend.md` (§Script Plugin Rules) |
```

- [ ] **Step 4: Verify the section renders and links are correct**

```bash
grep -n "Script Plugin Platform\|Script plugin contract catalog\|modules-plugin/" CLAUDE.md
```

Expected: the new section heading, the updated Project Structure line, and the six new table rows are visible; no existing line was removed.

- [ ] **Step 5: Commit**

```bash
git add CLAUDE.md
git commit -m "docs: map the script plugin platform in CLAUDE.md

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 5: Final Verification

**Files:** none expected. If a check fails, fix the offending file and amend or add a commit with the matching task's scope.

- [ ] **Step 1: Run the full `script-plugin-api` test suite**

```bash
cd server
JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew :modules-plugin:script-plugin-api:test
```

Expected: BUILD SUCCESSFUL, including `ScriptPluginExampleValidationTest` and `PluginDocumentationConsistencyTest`.

- [ ] **Step 2: Run the full backend build without boot jars**

```bash
cd server
JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew build -x bootJar
```

Expected: BUILD SUCCESSFUL (the known-good Phase 2 command; plain `build` fails on pre-existing empty aggregator projects).

- [ ] **Step 3: Verify the sample is no longer a Gradle module**

```bash
grep -n "plugin-example" server/settings.gradle
```

Expected: no output.

- [ ] **Step 4: Verify the dead interface is gone**

```bash
grep -rn "IContainerTaskGeneratorAction" server --include=*.java
```

Expected: no output.

- [ ] **Step 5: Verify the change set**

```bash
git status --short
```

Expected: only the intended docs, sample, test, settings and `CLAUDE.md` files are modified/added/deleted. The three pre-existing untracked Phase 2/3A/3B plan documents and the new Phase 4 plan document may still show as untracked; they must not be committed. The stale untracked `plugin-example/build/` directory must not appear (it is gitignored).

- [ ] **Step 6: Report the result**

Report to the user: commands run, pass/fail counts, the two new test classes, the removed Gradle module, and the explicit statement that only static validation ran (no Spring context, database, or runtime E2E). Do not claim the manual acceptance walkthrough.

---

## Notes for the Executor

- `server/modules-plugin/script-plugin-api` already receives JUnit 5, Mockito, Spring Boot test and Jackson through the root `server/build.gradle` `subprojects` block; no `build.gradle` change is needed for the new tests.
- The test working directory under Gradle is the module directory; `RepositoryRoot.find()` walks up to the repository root from there and also works when an IDE starts tests at the repository root.
- `plugin.json` contains a top-level `description` field that `ScriptPluginBundleDTO` does not model; the test's `ObjectMapper` disables `FAIL_ON_UNKNOWN_PROPERTIES`. Do not add `description` to the DTO.
- The sample script is compiled, not executed, by `ScriptPluginExampleValidationTest`. The compile stage resolves its imports from the `script-plugin-api` test classpath (`wes-api`, `api-platform-api`, `common-utils`).
- The generated docs block must match `toMarkdownTable().trim()` byte-for-byte after CRLF normalization. If the test fails with a diff, paste the renderer's output between the markers rather than adjusting the renderer.
- Never stage `docs/superpowers/plans/` wholesale; the Phase 2/3A/3B plan documents are intentionally untracked.
