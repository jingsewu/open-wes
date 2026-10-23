# Script Plugin Phase 3 — Workbench Governance & Client Design

**Status:** Approved design, awaiting implementation planning
**Date:** 2026-10-22
**Extends:** `docs/superpowers/specs/2026-10-08-script-plugin-ai-workbench-design.md` (§Phasing, Phase 3)
**Depends on:** Phase 1 runtime (merged), Phase 2 AI draft pipeline (committed on `feature/script-plugin-ai-workbench`)

## 1. Context and goal

Phase 1 delivered the script plugin runtime and management API; Phase 2 delivered the AI draft
pipeline. The management surface is still reachable by anyone who passes the gateway JWT, has no
per-action roles, no operation audit, no config-value API, no execution-log query, no metadata
update, and no real test-run. The client still contains two legacy pages that call endpoints that no
longer exist.

Phase 3 closes the gap defined by the original design's acceptance criteria:

> end-to-end requirement → enable walkthrough; rollback works; enable requires an admin role with
> an audit trail.

The phase is split into two independently reviewed and executed plans:

- **Plan 3A — backend governance and APIs.**
- **Plan 3B — client workbench.**

No implementation starts until the relevant plan is reviewed.

## 2. Decisions already approved

| Decision | Choice |
|---|---|
| Phase structure | Backend first (3A), then client (3B), each reviewed/executed separately |
| Authorization | Role codes checked server-side; gateway gates broad workbench access; seed roles and menus with Liquibase |
| 3A API scope | Config values get/save, execution log query + stats, metadata update, real test-run |
| Client shape | Hybrid: AMIS for list/audit pages, custom React + antd for wizard/detail/chat |

## 3. Scope

### In scope

- Server-side role checks for every workbench action.
- Append-only operation audit for all state-changing actions.
- Config-value read/write with schema validation and hot reload.
- Execution-log paged query and aggregated stats.
- Plugin metadata update.
- Real test-run of a declared extension method on the hosting node.
- Capabilities and contract read endpoints for the client.
- Liquibase seeds: roles, menu system, menus, permissions, role-menu grants.
- Client workbench: plugin list, new-plugin wizard, plugin detail, AI chat drawer, execution and
  audit tabs, permission-aware actions, i18n.
- Removal of the broken legacy plugin pages.

### Out of scope

- A separate approval state or review workflow. `DRAFT` is the submitted state; an admin enables it.
- Secret management or masked config display.
- Test-run on the station node (`CUSTOM_API`); Phase 3 builds the wes workbench only.
- Plugin marketplace/store, JS scripts, resource sandboxing.
- Changing the gateway from prefix permissions to per-endpoint permissions.
- Reworking the global tab/router model (see §6.1 routing constraint).

## 4. Backend (Plan 3A)

### 4.1 Module dependencies and new interfaces

Current dependency facts that constrain the design:

- `script-plugin-core` depends on `script-plugin-api`; it does **not** depend on
  `script-plugin-runtime`.
- `script-plugin-runtime` depends on `script-plugin-api`; it does **not** depend on
  `script-plugin-core`.
- `script-plugin-api` already depends on `plugin-extension`, `wes-api`, `station-api`, and
  `api-platform-api` (required by `PluginContractCatalog`). The runtime therefore already has all
  host argument types on its classpath.
- `ai-plugin` already depends on `script-plugin-core`.
- `modules-user:user-api` exposes constants and JWT utilities but no role query.

New interface in `user-api`:

```java
public interface IUserRoleApi {
    Set<String> findRoleCodes(String username);
}
```

`UserRoleApiImpl` lives in `modules-user/user` (`application/impl`), uses the existing
`UserMapper.findByAccount` → `UserRoleMapper.findByUserId` → `RoleMapper.findAllById` chain, and is
annotated `@Primary @Service`. No cache: workbench operations are low-frequency and role changes
must take effect immediately.

`script-plugin-core/build.gradle` adds `project(':modules-user:user-api')`. There is no cycle.

All new client-facing DTOs live in `script-plugin-api` under `org.openwes.script.plugin.api.dto`
(`PluginCapabilitiesDTO`, `ScriptPluginConfigDTO`, `PageResultDTO`, `ExecutionStatsDTO`,
`ScriptPluginMetadataDTO`, `PluginContractDTO`, `TestRunRequestDTO`, `TestRunResultDTO`). New enums
live in `org.openwes.script.plugin.api.enums` (`ScriptPluginAuditActionEnum`). Entities, services,
repositories, and controllers live in `script-plugin-core`, except the test-run implementation in
`script-plugin-runtime`.

Placement of the test-run contract:

- `ScriptPluginTestRunner`, `TestRunRequestDTO`, and `TestRunResultDTO` go in `script-plugin-api`.
- The implementation goes in `script-plugin-runtime`.
- The controller in `script-plugin-core` injects the interface; both modules are on the
  `wes-server` classpath, so Spring supplies the runtime implementation.

### 4.2 Authorization model

New `PluginWorkbenchAccessService` in `script-plugin-core`:

- Reads the operator from `UserContext` (username only).
- Reads configurable role codes:
  - `plugin.security.developer-role-codes`, default `pluginDeveloper`
  - `plugin.security.admin-role-codes`, default `pluginAdmin`
  - `@Value("${superRoleCode:superAdmin}")` for the existing super role; a super role is treated as
    developer + admin.
- Roles are hierarchical: a user with an admin role is also a developer. `canDevelop` is true when
  the user has a developer role, an admin role, or the super role.
- Exposes:
  - `PluginCapabilitiesDTO capabilities()` → `{canDevelop, canAdminister, roles}`
  - `requireDeveloper()`, `requireAdmin()`
  - `requireDeveloper(pluginKey, action)` / `requireAdmin(pluginKey, action)` for state-changing
    operations.
- A blank username is rejected.
- Denials of state-changing operations are recorded in the operation audit (`success=false`,
  `detail="missing role <code>"`) before the exception is thrown. Denials of read-only endpoints
  are logged at WARN; they do not produce audit rows because the audit action vocabulary describes
  changes.

New error enum `ScriptPluginWorkbenchErrorDescEnum` implementing `IBaseError`, with
`AppCodeEnum.PLUGIN_PLATFORM` as the app code. The `PL002` module prefix avoids the AI pipeline's
existing `PL001` codes:

| Code | Meaning |
|---|---|
| `PL002001` | developer role required |
| `PL002002` | administrator role required |
| `PL002003` | authenticated user required |

Errors are thrown as `WmsException`. The existing `ControllerExceptionHandler` returns HTTP 200
with the business `status` code; this project-wide behavior is intentionally not changed. The
client's `requestInterceptor` already treats a non-success business status as an error.

**Enforcement matrix (enforced in the service layer, not only the controller):**

| Operation | Required capability |
|---|---|
| `createDraft` | developer |
| manual `saveRevision` | developer |
| `validate` | developer |
| `test-run` | developer |
| config read and save | admin |
| metadata update | developer |
| `enable`, `disable`, `archive`, `rollback` | admin |
| list, detail, revisions, executions | developer |
| per-plugin audit | developer |
| global audit | admin |
| capabilities, contract | any authenticated user |
| AI draft stream and generation history | developer |

**Internal bypasses:**

1. **AI revision save.** Authorization happens once at the AI stream entry point, on the request
   thread. The worker calls an internal `saveRevision` overload with an explicit actor; it does not
   read `UserContext` from the worker thread.
2. **Scheduled auto-disable.** `disableBySystem(pluginKey, reason)` skips the role check and records
   the actor as `system`. The user-facing `disable` remains admin-only.

Nested calls must not re-check with a stricter role than the entry point. `enable` (admin) calls an
internal `validateInternal`; it does not call the developer-gated public `validate`.

### 4.3 Operation audit

New table `p_script_plugin_operation_audit`:

| Column | Type / rule |
|---|---|
| `id` | bigint primary key |
| `plugin_key` | varchar(64), not null |
| `action` | varchar(32), not null; plain Java enum stored with `@Enumerated(STRING)` |
| `revision_id` | bigint, nullable |
| `operator` | varchar(64), not null; `system` for scheduled work |
| `success` | boolean, not null |
| `detail` | varchar(1024), nullable; failure reason or short summary |
| `created_at` | bigint, not null |

Indexes: `(plugin_key, id)` and `(created_at)`.

`ScriptPluginAuditActionEnum` is a plain enum, **not** `IEnum`, so no dictionary changes are
required: `CREATE_DRAFT`, `SAVE_REVISION`, `VALIDATE`, `ENABLE`, `DISABLE`, `ARCHIVE`, `ROLLBACK`,
`CONFIG_SAVE`, `METADATA_UPDATE`, `TEST_RUN`.

Entity `ScriptPluginOperationAudit` is append-only: `@Getter`, private constructor, static
`record(...)` factory, no setters. `ScriptPluginOperationAuditRepository` exposes only `save` and
`find` methods.

`ScriptPluginOperationAuditService.record(...)`:

- Resolves the operator from an explicit actor or `UserContext`.
- Writes on success and on business failure.
- Never stores config values, script sources, prompts, request bodies, or test-run arguments.
- Truncates `detail` to 1024 characters.

Recording points: after each user-facing operation returns successfully; for `ENABLE`/`VALIDATE`
failures, record `success=false` with the failure summary before rethrowing. Denials are recorded by
the access service as described in §4.2.

`ScriptPluginRevision.createdBy` is fixed in the same phase:

- manual save: current username;
- AI save: the user who started the generation;
- system action: `system`.

The `saveRevision` overload gains an explicit `actor` parameter. The existing five-argument overload
delegates with the current username for manual saves.

### 4.4 Config values

The table `p_script_plugin_config`, entity `ScriptPluginConfig` (unique `plugin_id`, JSON
`config_json`, `@Version lock_version`) and repository already exist. `ScriptPluginService.resolveConfig`
already merges stored values with `ConfigFieldDTO.defaultValue`.

APIs:

- `GET /plugin-platform/scriptPlugin/{pluginKey}/config` → admin
  ```json
  { "values": { "maxPerWave": 20 }, "schema": [ ... ], "lockVersion": 3 }
  ```
- `POST /plugin-platform/scriptPlugin/{pluginKey}/config` → admin
  ```json
  { "values": { "maxPerWave": 25 }, "lockVersion": 3 }
  ```

Validation against the current revision's `configSchema`:

- every submitted key must exist in the schema;
- every `required` field must be present and non-null;
- values must convert to the declared type: `STRING`, `INT`/`INTEGER`, `NUMBER`, `BOOLEAN`;
- unknown types are rejected as a schema error;
- on `lockVersion` mismatch, reject with a conflict message and return the current version.

Save behavior:

- insert the row when absent, update it otherwise;
- publish `PLUGIN_CHANGED` through the existing Redis management channel so enabled plugins rebuild
  within seconds;
- audit `CONFIG_SAVE` with the saved field names only, never values.

Config values are stored in plaintext. Phase 3 does not add encryption or masking; this is an
explicit limitation (§10).

### 4.5 Execution logs and stats

The existing `p_script_plugin_execution_log` is capped at 10,000 rows per plugin by
`ScriptPluginExecutionLogService`.

Add queries to `ScriptPluginExecutionLogRepository`:

- `Page<ScriptPluginExecutionLog> findByPluginKeyOrderByIdDesc(String pluginKey, Pageable pageable)`
- filtered variants for `success` and `extensionType` (explicit JPQL or Specifications);
- an aggregate query grouped by `extensionType` over a `createdAt >= since` window, returning
  total, failed, average duration, and maximum duration.

APIs (developer):

- `GET /plugin-platform/scriptPlugin/{pluginKey}/executions?page=1&pageSize=20&success=&extensionType=`
  → `PageResultDTO<ExecutionLogDTO>`
- `GET /plugin-platform/scriptPlugin/{pluginKey}/executions/stats?hours=24`
  → `{ total, failed, failureRate, avgDurationMs, maxDurationMs, byType: [{extensionType, total, failed, avgDurationMs, maxDurationMs}] }`

`pageSize` is capped at 100; `hours` is clamped to `[1, 720]`. `ExecutionLogDTO` gains an optional
`id` field (populated by queries, ignored on ingest) so the UI has a stable React key.

Stats are computed from at most the retained 10,000 rows per plugin; this is documented in the UI.

### 4.6 Metadata update

`POST /plugin-platform/scriptPlugin/{pluginKey}/metadata` → developer

```json
{ "name": "Packing status rule", "description": "...", "applyModule": "outbound" }
```

Rules:

- `pluginKey` and `applySystem` are immutable and not accepted;
- `ARCHIVED` plugins reject metadata changes;
- name is required, max 128; description max 512; applyModule max 64;
- audit `METADATA_UPDATE` with the changed field names.

### 4.7 Real test-run

Contract in `script-plugin-api`:

```java
public interface ScriptPluginTestRunner {
    TestRunResultDTO run(String pluginKey, ScriptPluginBundleDTO bundle, TestRunRequestDTO request);
}
```

```java
public class TestRunRequestDTO {
    @NotNull private ExtensionType extensionType;
    @NotBlank private String script;
    @NotBlank private String method;
    private Map<String, Object> arguments;
}

public class TestRunResultDTO {
    private boolean success;
    private String result;          // JSON text, truncated to 8 KB
    private boolean resultTruncated;
    private String error;
    private long durationMs;
}
```

Implementation `ScriptPluginTestRunnerImpl` in `script-plugin-runtime`:

1. Open a throwaway scope with
   `ScriptRevisionContextManager.openValidationScope(pluginKey, bundle.getScripts())`.
2. Resolve the script instance by `request.script`.
3. Build `DefaultScriptContext(pluginKey, scope.context(), bundle.config, bundle.configSchema,
   scriptHttpClient)`.
4. Build ordered arguments with `TestRunArgumentBuilder`, append the context, and invoke through
   `ScriptExecutionRuntime` (the existing execution timeout) and `GroovyScriptEngine.invoke`.
5. Serialize the return value with Jackson; on serialization failure use `toString`; truncate to
   8 KB.
6. Always close the validation scope.

`TestRunArgumentBuilder` maps named JSON arguments to the host signature. It constructs typed values
with Jackson, using the API module's host types that are already on the classpath:

| Archetype | Argument | Host type |
|---|---|---|
| `HTTP_ENDPOINT` | `body` | any JSON value |
| `CRON` | — | no arguments |
| `LIFECYCLE_LISTENER` | `entityId`, `entityOrderNo`, `newStatus` | `Long`, `String`, `String` |
| `OUTBOUND_WAVE_WAVE_ACTION` | `orders` | `List<OutboundPlanOrderDTO>` |
| `OUTBOUND_WAVE_SPLIT_ACTION` | `wave` | `OutboundWaveDTO` |
| `REQUEST_VALIDATE`, `REQUEST_SUPPLY`, `REQUEST_AFTER_INVOKE`, `CUSTOM_REQUEST_INVOKE` | `context` | `RequestHandleContext` |
| `CUSTOM_API` | `parameter` | `CustomApiParameter`, constructed explicitly from `{workStationId, requestParameter}` (no no-arg constructor) |

Additional rules:

- Test-run does **not** write `p_script_plugin_execution_log`; it is not production traffic.
- It records `TEST_RUN` audit with archetype, script, success, and duration. Arguments and results
  are never audited.
- Test-run is hosted by `wes-server`, so only plugins whose `applySystem` is `wes` can be run.
  `station` plugins (including `CUSTOM_API`) return a clear "not available on this node" error.
- The API is `POST /plugin-platform/scriptPlugin/{pluginKey}/test-run`, developer-only.
- The UI must display: "Test-run executes for real against host services; use a test environment."

### 4.8 Capabilities and contract

- `GET /plugin-platform/scriptPlugin/capabilities`
  → `{ canDevelop, canAdminister, roles }` for the current user. Available to any authenticated
  user; it only drives UI visibility, while the server remains authoritative.
- `GET /plugin-platform/scriptPlugin/contract?applySystem=wes`
  → `{ contractVersion, applySystem, archetypes: [{type, scriptMethod, hostSignature, blocking}],
  lifecycleEntityNames, promptFragment }`, built from `PluginContractCatalog`. This prevents the
  client from hard-coding archetype lists that could drift from the runtime.

### 4.9 REST surface

Existing endpoints remain unchanged except for the added authorization checks and optional disable
reason. New endpoints:

| Method | Path | Capability |
|---|---|---|
| GET | `/capabilities` | authenticated |
| GET | `/contract?applySystem=` | authenticated |
| GET | `/{pluginKey}/config` | admin |
| POST | `/{pluginKey}/config` | admin |
| GET | `/{pluginKey}/executions` | developer |
| GET | `/{pluginKey}/executions/stats` | developer |
| POST | `/{pluginKey}/metadata` | developer |
| POST | `/{pluginKey}/test-run` | developer |
| GET | `/{pluginKey}/audit` | developer |
| GET | `/audit` | admin |

`POST /{pluginKey}/disable` accepts an optional body:
`{ "reason": "..." }`, max 255 characters. An absent reason defaults to `disabled by user`.

All request DTOs carry Bean Validation annotations; the controller uses `@Valid`, and the
implementation class uses `@Validated` per project convention.

### 4.10 Persistence and Liquibase

New changelog `db.changelog-20261023.xml`, registered in `db.changelog-master.xml`. Never edit an
applied changelog.

Changesets:

1. **Audit table** — `p_script_plugin_operation_audit` with the columns and indexes from §4.3;
   precondition `tableExists` / `onFail="MARK_RAN"`.
2. **Roles** — `pluginDeveloper` and `pluginAdmin` in `u_role`, with `warehouse_codes='[]'` and
   `status=1`; idempotent inserts (`INSERT ... SELECT ... WHERE NOT EXISTS`).
3. **Menu system** — a `type=1` root row with `systemCode='plugin-platform'`, title key
   `pluginPlatform.system.title`, and reserved IDs in the `1900000000` range.
4. **Menus** — `type=2` rows:
   - `pluginPlatform.menu.plugins` → `/plugin-platform/plugins`
   - `pluginPlatform.menu.audit` → `/plugin-platform/audit`
   Menu `title` values are i18n keys (`LayoutAside` and the app switcher call `t(title)`); the client
   provides `en-us` and `zh-cn` translations.
   Every seeded row must supply the non-null `u_menu` columns (`create_user`, `permissions`,
   `order_num`, `enable`, `create_time`, `update_time`), following `init_menu.sql`.
5. **Permissions** — `type=3` rows with `permissions`:
   - `get:/plugin-platform/`
   - `post:/plugin-platform/`
   The gateway performs prefix matching, so these grant broad workbench access; per-action roles
   are enforced server-side.
6. **Role-menu grants** — both new roles get the plugin list menu and both permission rows; only
   `pluginAdmin` gets the audit menu.

The existing super role behavior (`*:*` and all menus) is unchanged.

### 4.11 Error handling

- Business failures continue to use `CommonException` where a stable code is unnecessary.
- Authorization uses the typed `ScriptPluginWorkbenchErrorDescEnum` codes from §4.2.
- Config validation, test-run, and metadata failures return actionable English messages.
- No secrets, prompts, script bodies, or config values are included in error messages or audit
  details.
- Logs are English, use `@Slf4j`, and never log request payloads at info level.

## 5. Client (Plan 3B)

### 5.1 Routing constraint

`RouterGuard` matches routes by exact `location.pathname`; `TabsLayout` keys tabs by pathname and
does not preserve query strings across tab restores. Dynamic path segments
(`/plugins/:pluginKey`) would require reworking global tab routing, which is out of scope.

Therefore Phase 3 uses one exact workbench route with a query parameter:

| Route | Page | Menu |
|---|---|---|
| `/plugin-platform/plugins` | AMIS plugin list | yes |
| `/plugin-platform/plugin-workbench` | Custom React workspace (`?mode=new` or `?pluginKey=...`) | no |
| `/plugin-platform/audit` | AMIS global audit (admin) | yes (admin only) |

The workbench component reads `location.search`, reloads when `pluginKey` changes, and replaces the
query with `?pluginKey=...` after creating a draft. One workbench tab hosts one plugin at a time;
this is an accepted UX constraint.

### 5.2 Shared client modules

Under `client/src/pages/plugin_platform/`:

| File | Responsibility |
|---|---|
| `types.ts` | TypeScript interfaces for plugin, revision, declaration, config, execution, audit, capabilities, contract, test-run. No `any`. |
| `api.ts` | Typed wrappers over `request` from `@/utils/requestInterceptor` for every backend endpoint. |
| `sse.ts` | Fetch-based SSE client for `POST /plugin-platform/ai/draft/stream`: sets `Authorization` from `localStorage.ws_token`, reads `ReadableStream`, parses `event:`/`data:` frames, supports `AbortController`. `EventSource` cannot POST or set headers. |
| `usePluginCapabilities.ts` | Loads `/capabilities`; returns `{canDevelop, canAdminister, loading}`. UI gating only. |
| `ScriptEditor.tsx` | AMIS `editor` renderer via `schema2component`, with Groovy highlighting (fall back to Java if the AMIS build does not expose Groovy). Reuses AMIS's bundled Monaco; no direct `monaco-editor` initialization. |
| `DiffViewer.tsx` | AMIS `diff-editor` renderer for two script versions. |
| `PluginStatusTag.tsx` | Status/validation badges with i18n. |

### 5.3 Plugin list (AMIS)

Columns: pluginKey, name, applySystem/applyModule, status, last validation, current revision,
update time. Filters: status, applySystem, key/name search. Row actions: open workbench; new plugin.

The list is read-only; all mutations are in the workbench so capability checks and audit are
uniform. The "New plugin" action is visible to all workbench users; the server rejects users
without the developer role.

### 5.4 New-plugin wizard (custom React + antd `Steps`)

1. **Basics** — pluginKey, name, description, applySystem/applyModule. Archetypes come from the
   contract endpoint.
2. **Requirement and AI generation** — requirement text, optional base revision, optional archetype
   hints. Creates the DRAFT, then starts the SSE stream. Displays stage badges
   (`declarations` → `compiling` → `wiring` → `retry#N`), streaming source, and a Cancel button that
   aborts the fetch.
3. **Review** — script editor per file, declarations table, config schema, validation report.
   Manual edits can be saved as a revision.
4. **Finish** — run validation. A developer sees "draft saved; an administrator must enable it."
   An admin sees "Enable now", which calls the admin-gated endpoint.

### 5.5 Plugin detail tabs (custom React)

- **Overview** — status, current revision, validation result; edit name/description/applyModule;
  actions gated by capabilities: validate, enable, disable (with reason dialog), archive, rollback.
- **Scripts** — editor per script file, declarations table, config schema editor; save as a new
  revision with a change summary.
- **Revisions** — revision list (number, origin, createdBy, time, short hash); select two versions
  for a side-by-side diff; rollback (admin).
- **Config** — schema-driven form and current values; visible and editable only for admins.
- **Test run** — choose a declaration, edit a JSON argument template for its archetype, run, and
  show result/error/duration; persistent warning that real host services are invoked.
- **Executions** — stats cards (total, failure rate, average, maximum) plus a type breakdown and a
  paged detail table with filters.
- **AI history** — existing generation history endpoint: model, prompt version, status, created
  revision.
- **Audit** — per-plugin audit table (developer and admin).
- **Chat drawer** — requirement plus base revision, streamed generation, and a refresh of the
  detail/revisions after completion. The backend saves the revision automatically; the drawer shows
  the new revision number.

### 5.6 i18n and frontend conventions

- Add all strings under `pluginPlatform.*` in `client/src/locales/en-us.json` and `zh-cn.json`;
  ja/ko fall back through the existing mechanism.
- All logs, toasts, and errors are English; no hard-coded Chinese in components.
- No debug `console.log`; no global `console.error` suppression.
- Stable IDs as React keys; never array indexes.
- Props are read-only; AMIS `renderSchema()` results are `ReactElement`s and are never `destroy()`ed.
- Follow `docs/standards/frontend.md`.

### 5.7 Legacy cleanup

Delete `client/src/pages/plugin_platform/plugin_management.tsx` and `plugin_store.tsx`, remove their
`path2Compoment` entries and dead `pluginSystem.*` translation keys, and replace them with the
routes in §5.1. No compatibility aliases: the legacy pages call endpoints that do not exist.

## 6. Key flows

### 6.1 Requirement → enable

1. Developer creates a draft in the wizard (developer check).
2. AI stream generates and saves a revision; provenance and actor are recorded; audit
   `SAVE_REVISION`.
3. Developer reviews/edits and runs validation (developer check; audit `VALIDATE`).
4. Developer runs a test-run (developer check; audit `TEST_RUN`, no production log).
5. Admin enables; the server re-runs validation, enables only on PASSED, and writes audit `ENABLE`
   with the actor. AI never enables.
6. The runtime rebuilds within seconds through the existing Redis management message.

### 6.2 Rollback

Admin selects an earlier revision; `rollback` switches the plugin pointer to that revision, keeps
the status unchanged, publishes `PLUGIN_CHANGED`, and audits `ROLLBACK` with the target revision.
No new revision row is created.

### 6.3 Config change

Admin saves values; schema validation runs; the config row is upserted; `PLUGIN_CHANGED` is
published; audit `CONFIG_SAVE` records field names only.

## 7. Testing

### Backend

- `PluginWorkbenchAccessService`: developer, admin, both, super role, unauthenticated, denial audit.
- `ScriptPluginOperationAuditService`: actor resolution, system actor, truncation, append-only
  repository usage.
- Config validation: unknown key, missing required, type coercion per type, lock-version conflict.
- Execution query/stats: pagination, filters, window clamping, empty window.
- Metadata: immutable fields, archived rejection.
- `TestRunArgumentBuilder`: one test per archetype, including `CustomApiParameter` construction.
- Runtime test-run integration: success, script exception, timeout, scope closed.
- Controller/service tests: developer cannot enable; admin can enable and produces audit rows;
  admin-only config; per-plugin audit access.
- Full backend build on JDK 17 with `ignoreFailures=false`.

### Client

- `npx tsc --noEmit` where supported, plus a production `npm run build`.
- Static review of permission branches, SSE cancellation, error/empty states, and i18n key
  completeness.
- No automated client test framework exists; do not claim browser E2E coverage.

### Not runnable on this host

Docker is unavailable, so the manual end-to-end acceptance walkthrough (Phase 1b, 2, and 3) cannot
be executed here. Tests and static checks are the evidence; manual acceptance remains a release
gate.

## 8. Acceptance criteria mapping

| Criterion | Evidence in Phase 3 |
|---|---|
| End-to-end requirement → enable walkthrough | Wizard + AI stream + validation + admin enable; covered by controller/service tests and documented manual steps |
| Rollback works | Revisions tab + admin-gated rollback endpoint + audit row; unit test for pointer switch |
| Enable requires admin role | Service-level admin check, denial audit, controller test |
| Audit trail | Append-only operation audit for every state-changing action, per-plugin and global views |

## 9. Risks and mitigations

- **Role lookup on every action** → low-frequency operations; no cache, immediate revocation.
- **Broad gateway prefix permissions** → intentional; fine-grained checks are server-side, so a
  gateway grant alone cannot enable a plugin.
- **Audit writes on failure paths** → keep writes small and exception-safe; an audit failure must
  not mask the original business error (log a warning instead).
- **Test-run side effects** → explicitly labeled; real host services are called by design; no
  production execution log and no argument/result audit.
- **SSE via fetch** → the client must handle partial frames, UTF-8 chunk boundaries, abort, and
  reconnection by restarting generation (no resume protocol exists).
- **Privilege escalation through internal overloads** → internal save/disable methods are
  package-private or non-controller entry points; only the AI service and scheduler use them.

## 10. Limitations

- Manual E2E/acceptance remains unexecuted on this host.
- Config values are plaintext; secrets are not masked or encrypted.
- Test-run is available only on the hosting wes node; station `CUSTOM_API` test-run is not exposed.
- Audit retention is unbounded in Phase 3; a retention job is a future item.
- One workbench tab hosts one plugin at a time (routing constraint in §5.1).
- The audit does not capture request payloads; it is an operation trail, not a replay log.

## 11. References

- Original design: `docs/superpowers/specs/2026-10-08-script-plugin-ai-workbench-design.md`
- Backend standards: `docs/standards/backend.md`
- Frontend standards: `docs/standards/frontend.md`
- Liquibase standards: `docs/standards/liquibase.md`
- Phase 2 plan: `docs/superpowers/plans/2026-10-22-script-plugin-phase2-ai-pipeline.md`
