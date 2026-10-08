# Script Plugin Runtime & AI-Assisted Plugin Workbench Design

## Overview

This spec covers two deliverables requested together:

1. **Review** of the current plugin system (`server/modules-plugin/`): verified defects and design gaps, prioritized (see *Current State*).
2. **Design** of an AI-assisted plugin development feature: a script-plugin runtime (Groovy scripts stored in the database, materialized into the existing extension points) plus an in-app AI workbench that drafts plugins from natural language, validates them, and hands them to a human review gate.

### Confirmed product decisions

| Decision | Choice |
|---|---|
| Workbench form | In-app (client plugin platform), not offline tooling |
| Plugin visibility | Private/tenant-internal first; public store deferred |
| v1 archetype coverage | All four: WES business hooks, API-platform request plugins, station custom APIs, self-provided HTTP endpoints & cron jobs |
| Technical route | Script host + contract isomorphism: DB-stored Groovy scripts, runtime materialization, description layer isomorphic to a future standard Java plugin project |

### Core tenets

- A plugin = declarations (extension points, identity, config schema, priority) + Groovy scripts + metadata. Everything lives in the database; no plugin directory or jar packaging is involved.
- Runtime hook-in happens at a single choke point (`PluginSdkUtils.getExtractObject`); existing business call sites keep their current lookup/filter semantics.
- Scripts run with full JVM permissions — the same trust level as an uploaded jar. The safety model is a hard gate (AI drafts → human reviews → admin enables), plus audit logging and execution timeouts. No fake sandbox is claimed.
- AI never auto-enables anything; enabling is a separate, permissioned, audited action.

## Current State

### Existing architecture (as-is)

- `server/modules-plugin/` has five submodules: `plugin-api` (DTOs/enums), `plugin-core` (legacy management chain, never built into any server), `plugin-sdk` (PF4J wiring, management REST, plugin registration), `plugin-extension` (extension-point interfaces), `plugin-example` (sample jar plugin).
- PF4J 3.13.0 with vendored `org.pf4j.spring` classes inside plugin-sdk (the official `pf4j-spring` dependency is commented out in the root `build.gradle`).
- `PluginRegister` creates a per-plugin `AnnotationConfigApplicationContext` (parent = main context) scanning `plugin.scan-packages`; controllers and `@DistributedScheduled` jobs are registered dynamically; lifecycle events flow through Guava `@Subscribe` via `DomainEventProcessor`.
- Build consumers: `wes-server`, `api-platform`, `station`, `wes-ems-proxy`, `wes-outbound`. `plugin-core` and `plugin-example` have **zero** build consumers.

### Verified defects (P0 — must-fix work package)

| # | Defect | Evidence | Impact |
|---|--------|----------|--------|
| 1 | Lifecycle events emitted without `entityName`/`entityNo` | `wes-outbound/.../domain/entity/PickingOrder.java:83,98,109` | `LifecycleListenerRegistry` filters by `entityName`; picking-order listeners can never fire |
| 2 | Gateway route points at a non-existent service | `initdb.d/nacos_config.sql:54` routes `/plugin/**` to `lb://plugin`; only gateway/station/wes register | Every client call to `/plugin/**` fails |
| 3 | `PluginController.list()` reuses a single `HashMap` inside a stream | `plugin-sdk/.../controller/PluginController.java:32` | List API returns N references to the same map (all rows identical) |
| 4 | `SpringExtensionFactory` creates a new extension instance on every call | `plugin-sdk/.../org/pf4j/spring/SpringExtensionFactory.java:69` (documented in its own javadoc) | Extension state (`@Autowired` fields, caches) is lost between calls; singleton semantics broken |
| 5 | Multi-plugin selection is arbitrary | `wes-outbound/.../service/impl/OutboundWaveServiceImpl.java:39,61` (`iterator().next()`) | With more than one wave plugin, the winner is unspecified |
| 6 | No Liquibase changelog for `p_plugin` (`ddl-auto: none`) | no changelog found in repo | The legacy metadata table cannot exist in a real deployment |

### Other verified issues (P1 / mismatches)

- `CreateOutboundRequestHandler` overrides `invoke()` and skips the plugin hook, and imports the dead `IInboundPlanOrderCreatePlugin` (`api-platform/.../handler/request/wms/CreateOutboundRequestHandler.java:16`) — request plugins never see this API type.
- `plugin-core` is dead code: no build consumer; hand-rolled `plugin.properties` parsing (`line.split("=")` truncates values containing `=`); jar file leaked when validation fails; hardcoded `/local-storage/plugins/`.
- Config is stored in files (`plugins/config/{pluginUniqueKey}.json`) and double-JSON-encoded by `PluginService.config` — not multi-node safe, not transactional.
- Client `plugin_platform` pages call non-existent endpoints (`/plugin/pluginManage/addPlugin`, install/uninstall by numeric id) and use missing i18n keys (`pluginPlatform.*` routes in `client/src/routes/path2Compoment.tsx:605-625`, plus the typo `"pluginSystem.pluginStore.title,"`).
- `docs/standards/backend.md` Plugin Rules reference APIs that do not exist (`IContainerTaskGeneratorAction`, `getGeneratorPlugin()`).
- `@Extension` is misplaced on the `IPlugin` interface; no `@ExtensionPoint` annotations anywhere.
- Dynamic controller unregistration is a known TODO (`//TODO unRegister the controller not clear`).
- docker-compose mounts no `/plugins` volumes, so jar-installed plugins disappear on redeploy.
- Root `build.gradle` sets global `test { ignoreFailures = true }` — red tests never fail the build.

### Documented, intentionally not fixed

- **`plugin-core` (whole module)** — unpublished dead chain; deletion is the recommendation, not repair.
- **PF4J dynamic controller unregistration / annotation placement** — the new runtime sidesteps dynamic mapping entirely with a single fixed dispatcher.
- **Plugin directory volumes in docker-compose** — the script runtime stores plugins in the database, so no directory dependency exists for the new path.
- **`IContainerTaskGeneratorAction`** — dead interface referenced only by outdated docs; removed or deprecated during the docs rewrite.

## Architecture

### Design principle: contract isomorphism

A script plugin's *description layer* has the same shape as a future standard Java plugin project: identity + declarations (`plugin.properties` ↔ `p_script_plugin` row) and behavior (extension classes ↔ named scripts). A validated script plugin can later be re-issued as a Java project with the same declarations, and the workbench speaks the same vocabulary in both cases. This is also what makes AI generation tractable: the contract is machine-readable and single-sourced.

### Module map

New modules:

| Module | Location | Purpose |
|---|---|---|
| `script-engine` | `server/modules-utils/script-engine/` | Extract Groovy execution (from api-platform) into a reusable, cached, timeout-aware engine shared by script plugins and existing converter features |
| `script-plugin-api` | `server/modules-plugin/script-plugin-api/` | Single source of contract truth: DTOs, `IScriptPluginApi` (Dubbo), `PluginContractCatalog` (archetype signatures, facades, bean catalog), enums |
| `script-plugin-core` | `server/modules-plugin/script-plugin-core/` | Persistence (JPA), management REST (`/plugin-platform/**`), `@DubboService` impl, Liquibase changelogs |
| `script-plugin-runtime` | `server/modules-plugin/script-plugin-runtime/` | Proxy materialization, per-revision child Spring contexts, fixed HTTP/cron dispatchers, registry cache, execution logging, Redis refresh listener |
| `ai-plugin` | `server/modules-ai/ai-plugin/` | AI generation pipeline: prompt assembly, structured output, validation loop, SSE streaming |

Changes to existing code:

| Change | Where |
|---|---|
| Merge materialized script-plugin proxies into extension lookup | `PluginSdkUtils.getExtractObject` (`plugin-sdk/.../utils/PluginSdkUtils.java:18`) |
| Add module wiring to server builds | `wes-server` ← `script-plugin-core` + `script-plugin-runtime`; `station-server` ← `script-plugin-runtime` |
| Singleton extension semantics for the PF4J path | `SpringExtensionFactory` instance caching |
| Explicit plugin priority + deterministic selection | merged-list ordering; `OutboundWaveServiceImpl` picks highest priority |
| Lifecycle emitter fixes + startup validation of declared entityNames | emitters (PickingOrder et al.), `LifecycleListenerRegistry` |
| Gateway routes: fix `/plugin/**` → `lb://wes`; add `/plugin-platform/**` → `lb://wes` | `initdb.d/nacos_config.sql` |
| Workbench UI rebuild | `client/src/pages/plugin_platform/`, routes, i18n, menu seeds |
| Docs rewrite: `docs/standards/backend.md` plugin chapter + CLAUDE.md | docs |

Deployment: `script-plugin-core` and the management REST are hosted in **wes-server** (it owns the database). `script-plugin-runtime` is a plain Spring module added to **both wes-server and station-server** (station executes its own custom-API plugins; it fetches plugin definitions via Dubbo). `script-plugin-runtime` is deliberately **not** a PF4J plugin — see *Runtime Contract*.

### Component flow

```
[Workbench UI] --REST/SSE--> [ai-plugin] --draft--> [script-plugin-core] --DB--> p_script_plugin*
                                   |                        |
                                   v                        v  Redis pub/sub (version bump)
                        [PluginContractCatalog]      [script-plugin-runtime]  (per node)
                        (prompt + docs source)          - registry cache
                                                        - JDK proxy materialization
                                                        - per-revision Spring child context
                                                        - HTTP dispatcher / cron dispatcher
                                                            |
Business code --> PluginSdkUtils.getExtractObject() <-------+
              (PF4J extensions + materialized script proxies, priority-sorted)
```

### Data model

`p_script_plugin`
- `id`, `plugin_key` (unique, declared — mirrors the `plugin.id` convention), `name`, `description`
- `apply_system`, `apply_module` — target app; a node materializes the plugin only when it matches
- `status`: `DRAFT` / `ENABLED` / `DISABLED` / `ARCHIVED`
- `extension_points` (JSON), `config_schema` (JSON), `sdk_version`, `current_revision_id`
- standard audit columns

`p_script_plugin_revision` (immutable)
- `id`, `plugin_id`, `revision_no`, `scripts` (JSON: name → source), `extension_points_snapshot`, `config_schema_snapshot`
- `content_hash`, `change_summary`, `origin` (`AI` / `MANUAL`), `ai_model`, `prompt_version`, `created_by`, `created_at` (rollback does not create a revision; it is recorded in the operation audit)

`p_script_plugin_config`
- `id`, `plugin_id`, `config_json`, `lock_version` (optimistic), audit columns — replaces file-based config

`p_script_plugin_execution_log`
- `id`, `plugin_key`, `revision_id`, `extension_type`, `success`, `duration_ms`, `error_message`, `created_at`; retention by capped count with periodic pruning

### Status machine

`DRAFT → ENABLED` (enable) `→ DISABLED` (disable) `→ ENABLED` (re-enable); any non-archived state `→ ARCHIVED` (terminal in v1). Rollback = point `current_revision_id` at a historical revision; history is never mutated.

### Enable / refresh flow (hot effect)

1. Draft revision saved; enable-time validation runs on the target node (see *Runtime Contract*).
2. Status/revision written to DB; Redis publish on the existing `PLUGIN_LISTEN_PLUGIN_MANAGEMENT` channel (`plugin:listen:plugin:management`, `RedisConstants`) with plugin key + revision version.
3. Each node's runtime refreshes its registry and rebuilds the child context on first use — effect within seconds; no restart.

## Runtime Contract

### Access mechanism: proxy materialization

Business call sites resolve extensions via `PluginSdkUtils.getExtractObject(type)`, then filter by per-instance identity (`getApiType()`, `getEntityName()`, `customApiCode`, …). A single PF4J "host plugin" jar cannot satisfy this: PF4J instantiates one object per extension class, and one object can only carry one identity. Instead:

- `script-plugin-runtime` builds, per (script plugin × declared extension point), a JDK dynamic proxy implementing the extension interface. Identity methods return the plugin's declared values (e.g. its `apiType`, its `entityName`).
- `PluginSdkUtils.getExtractObject` returns PF4J extensions **plus** materialized proxies, sorted by declared priority (descending, then plugin key for determinism).
- Business call sites are untouched — they see ordinary interface instances. Every script invocation logs `pluginKey / revision / script` and writes an execution-log row for traceability.

Rejected alternative: a PF4J host jar implementing all four archetypes — fails the identity problem, requires plugin-directory deployment, and multiplies existing PF4J quirks.

### Extension archetype dispatch semantics

| Archetype | Identity | Script execution | Failure policy |
|---|---|---|---|
| `IRequestPlugin` (validate/supply/afterInvoke) | declared apiType | one script method per hook | validate: exception = reject request (message includes pluginKey); supply/afterInvoke: log + continue |
| `ICustomRequestPlugin.invoke` | declared apiType | `invoke(ctx)` | exception = platform error response (blocking) |
| `ICustomApiPlugin` (station) | declared customApiCode | `execute(param, ctx)` | exception = error surfaced to the workstation |
| WES hooks: wave / split | priority order | `wave(orders, ctx)` / `split(wave, ctx)` | null return or exception → built-in fallback + log |
| Lifecycle listeners | declared entityName | `afterStatusChange(id, no, status, ctx)` | log + continue (never affects the business transaction) |
| Self-provided HTTP endpoints | reserved prefix `/plugin-platform/ext/{pluginKey}/**` | routed by declaration (method + path) to a script | 500 with pluginKey in the body; no stack trace leaked |
| Self-provided cron jobs | declaration (`cron` + script) | one fixed runtime dispatcher + `DistributedLock` (single node fires) | logged; auto-disable after N consecutive failures (default 10, configurable) |

### Spring integration (dependency injection for scripts)

Mirrors `PluginRegister`'s approach for jar plugins:

- Per plugin revision, the runtime creates `AnnotationConfigApplicationContext(parent = main context)` and registers the compiled Groovy classes as beans.
- Therefore `@Autowired` / `@Resource` (by type or name), `@Value` (host `Environment`) and `@PostConstruct` all work inside script classes — the same semantics as Java plugins (contract isomorphism at the runtime level).
- `ctx.bean(name | Type.class)` resolves dynamically (child context first, then parent). Facades (`ctx.outbound`, `ctx.callback`, …) are documented sugar over the same lookup.
- Revision switch / rollback closes the old child context and drops the revision-scoped script class cache.

### Script API facades (v1)

`ctx.outbound`, `ctx.station`, `ctx.callback`, `ctx.http` (timeout-enforced), `ctx.config` (typed plugin config), `ctx.log`, `ctx.pluginKey` — plus `ctx.bean(...)`. Facades and the bean catalog are generated from `PluginContractCatalog`; a plugin's `sdk_version` must equal the platform contract version or enabling is refused. This list is a **discoverability/stability contract** for humans and AI — not a security boundary (Groovy has full JVM access, stated honestly in the docs).

### Enable-time validation (fail fast)

Runs on the target node: ① declaration legality (apiType exists, cron parses, route format, priority conflicts), ② Groovy compilation, ③ child-context build + injection + `@PostConstruct`, ④ auto-extraction of `@Autowired`/`@Resource` field types with existence check, ⑤ optional smoke invocation. Failure refuses enabling with a structured error (line number / missing bean name). Validation only runs on nodes matching `apply_system` / `apply_module` — wes and station have different bean catalogs by design.

## AI Generation Pipeline

### Contract single source: `PluginContractCatalog`

Machine-readable contract in `script-plugin-api`: archetype method signatures, facade list, bean catalog (per app), example skeletons, rules. The AI prompt **and** the rewritten documentation are generated from it — they cannot drift.

### Pipeline stages

1. **Structured brief**: free-text requirement → (target app, entities involved, expected behavior, config needs).
2. **Prompt assembly**: system rules (Groovy conventions, timeout/exception discipline, no hardcoded pluginKey, secrets only via `ctx.config`) + contract subset filtered by app × archetype (avoids context blowup and hallucinated beans) + few-shot examples + user brief.
3. **Structured output**: a draft document (below) — not naked code text.
4. **Static validation**: compilation + declaration legality.
5. **Materialization dry-run** (see above).
6. **Retry ≤ 3** with validator errors fed back — same pattern as the existing `AiPromptTemplate` SQL retry loop. After 3 failures, generation stops and surfaces a diagnosis to the human.
7. **Grounding**: plugin docs, bean catalog and examples added to the existing Pinecone corpus.

Existing AI infrastructure is reused: Spring AI + ZhipuAI GLM-4-air (temperature 0), streaming SSE consumption pattern from `generateCode`.

### Draft document

```json
{ "meta": { "name": "Packing status wave rule", "applySystem": "wes" },
  "extensions": [
    { "type": "OUTBOUND_WAVE_WAVE_ACTION", "priority": 100, "script": "WaveScript.wave" },
    { "type": "CUSTOM_API", "customApiCode": "GET_PACKING_STATUS", "script": "Packing.execute" },
    { "type": "HTTP_ENDPOINT", "method": "POST", "path": "/ext/packing/calc", "script": "Calc.handle" },
    { "type": "CRON", "cron": "0 */5 * * * ?", "script": "SyncJob.run" } ],
  "configSchema": [ { "field": "maxPerWave", "type": "NUMBER", "required": true } ],
  "scripts": { "WaveScript.groovy": "...", "Packing.groovy": "..." } }
```

Serializes 1:1 into `p_script_plugin_revision`.

### SSE protocol

`POST /plugin-platform/ai/draft/stream` — event types: `chunk` (streaming source), `stage` (`compiling` / `wiring` / `retry#2`), `result` (final draft), `error`. The client reuses the existing `generateCode` streaming consumption pattern.

### Guardrails

- **Roles**: developers create/edit drafts; admins enable/disable/archive. AI never auto-enables.
- **Quota**: per-user daily generation count + per-call token cap (ties into the AI rate-limiting item in `docs/optimization-plan.md` §3.3).
- **Reproducibility**: model ID and prompt-template version are stored on every AI-generated revision.
- **Secrets**: prompts carry structure, not secrets; generated scripts must read credentials via `ctx.config`.

## Workbench UX

### Information architecture (client plugin platform rebuild)

| Area | Content |
|---|---|
| My plugins | list with status, target app, extension summary, owner, last modified |
| New-plugin wizard | ① requirement ② AI generation ③ validation & test-run ④ submit for enable |
| Plugin detail | tabs: scripts (Monaco, hand-editable), config schema form, declarations, revisions (diff + rollback), test-run, execution stats/errors |
| Chat drawer | conversational iteration; accepted turns create new revisions |
| Execution observability | per-plugin invocations / errors / durations (v1 minimal; feeds auto-disable) |

### End-to-end flow

1. "New with AI" → pick target app → describe the requirement in natural language (can paste sample payloads).
2. AI proposes an extension-point combination; the user confirms identities and priorities.
3. Streamed generation with live validation badges (compile → injection → declaration legality).
4. Conversational iteration ("don't block on exception", "make the limit a config field"); manual edits are allowed and become the base for further AI turns.
5. **Test-run**: pick an extension point, provide sample input, real invocation, inspect result/exception/`ctx.log`. Labeled honestly: *"test-run executes for real against host services; use a test environment."*
6. **Submit → admin review → enable**: risk summary shows hooked points, priority, blocking/fallback semantics, and the bean list. Enabling takes effect within seconds.
7. **History**: every step is a diffable revision; rollback is a pointer switch. AI never enables anything.

## Fix Work Package (prerequisites)

### Must-fix (P0 table above, items 1–6)

Each fix ships with a test. Item 4 (singleton semantics) and item 5 (priority selection) align the legacy path with the semantics the script runtime will rely on.

### Along-the-way

- `CreateOutboundRequestHandler` calls the plugin hook (no-op until request scripts exist).
- Retire the broken client pages as the workbench replaces them (they currently call non-existent endpoints).
- Rewrite `docs/standards/backend.md` plugin chapter from `PluginContractCatalog`.

### Recorded, not fixed

- Items listed under *Documented, intentionally not fixed* (plugin-core, PF4J quirks, docker volumes, dead interfaces).

## Testing Strategy

- **Unit**: contract catalog generation and prompt subsetting; proxy identity for all four archetypes; script execution (compile, injection, timeout, isolation — table-driven per the failure-policy table); registry refresh; enable-time validation failure modes.
- **Integration**: wes slice + runtime module — a script plugin exercised through the real `OutboundWaveServiceImpl` / `AbstractRequestHandler` paths including fallback; station slice — custom API.
- **AI pipeline**: prompt snapshots; malformed structured output → retry; ≤3 retries with a mock ChatClient; SSE event sequence; fake-model end-to-end (fixed draft → passes static + materialization validation).
- **Test discipline**: per-module `test { ignoreFailures = false }` for all new/changed plugin modules; the global flag's removal is evaluated in Phase 0 after counting pre-existing failures.

## Phasing & Acceptance

| Phase | Deliverable | Acceptance |
|---|---|---|
| 0 Fixes | P0 items 1–6 + tests | each fix covered by a test; PickingOrder events match listeners |
| 1 Runtime | `script-engine`, `script-plugin-api`, `script-plugin-core`, `script-plugin-runtime` | a hand-written script plugin (inserted via DB) works in wes and station for all four archetypes; enable/disable/rollback are hot; failure policies match the table |
| 2 AI pipeline | `ai-plugin` | requirement → generated draft passes static + materialization validation; retry converges on malformed output; quota + generation audit in place |
| 3 Workbench | client rebuild + permissions/audit | end-to-end requirement→enable walkthrough; rollback works; enable requires admin role with audit trail |
| 4 Closing | docs rewrite, CLAUDE.md, one `plugin-example` scenario rewritten as a script plugin sample | documentation generated from the contract catalog stays consistent with code |

## Risks & Mitigations

- **GLM-4-air Groovy generation quality** → retry loop + few-shot examples + the human review gate; manual editing is always available as the fallback.
- **Multi-node refresh lag** → Redis pub/sub eventual consistency (seconds); the window is accepted; execution logs carry the revision for diagnosis.
- **wes/station contract differences** → `apply_system` isolation + target-node validation; a single plugin spanning both apps is explicitly unsupported in v1 (create one plugin per app).

## Non-Goals

- Public plugin store / marketplace distribution (the legacy jar chain is frozen as a future direction).
- JS scripts (re-evaluated in v1.5, JSON-transform use cases only).
- Real resource sandboxing / isolation for scripts (explicitly not attempted).
- Repairing `plugin-core` or preserving the legacy management pages.
- A generic mechanism to unregister dynamically-mapped controllers (the new runtime uses one fixed dispatcher instead).
- Cross-app single-plugin deployment.

## Appendix: Key Code Locations (verified at master `9d09311`)

- `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/utils/PluginSdkUtils.java:14,18`
- `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/utils/LifecycleListenerRegistry.java:12`
- `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/utils/ConfigMetadataExtractor.java:10`
- `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/register/PluginRegister.java:22`
- `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/configuration/PluginConfiguration.java:16`
- `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/service/PluginService.java:21`
- `server/modules-plugin/plugin-sdk/src/main/java/org/openwes/plugin/sdk/controller/PluginController.java:32`
- `server/modules-plugin/plugin-sdk/src/main/java/org/pf4j/spring/SpringExtensionFactory.java:69`
- `server/modules-plugin/plugin-core/src/main/java/org/openwes/plugin/core/controller/PluginManageController.java:30`
- `server/modules-plugin/plugin-api/src/main/java/org/openwes/plugin/api/dto/event/LifeCycleStatusChangeEvent.java:13`
- `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/application/service/handler/AbstractRequestHandler.java:29,59,71,83,95`
- `server/modules-api-platform/api-platform/src/main/java/org/openwes/api/platform/application/service/handler/request/wms/CreateOutboundRequestHandler.java:16`
- `server/modules-wes/wes-outbound/src/main/java/org/openwes/wes/outbound/domain/service/impl/OutboundWaveServiceImpl.java:27,39,61`
- `server/modules-wes/wes-outbound/src/main/java/org/openwes/wes/outbound/domain/entity/PickingOrder.java:23` (emit sites :83,98,109)
- `server/modules-station/station/src/main/java/org/openwes/station/application/business/handler/common/CustomApiHandler.java:20,26`
- `server/modules-utils/common-utils/src/main/java/org/openwes/common/utils/constants/RedisConstants.java:20` (`PLUGIN_LISTEN_PLUGIN_MANAGEMENT`)
- `initdb.d/nacos_config.sql:54` (`lb://plugin` route)
