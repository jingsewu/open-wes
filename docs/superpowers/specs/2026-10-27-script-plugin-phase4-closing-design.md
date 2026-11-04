# Script Plugin Phase 4 — Closing Design

**Date:** 2026-10-27
**Status:** Approved design, pending implementation plan
**Extends:** `docs/superpowers/specs/2026-10-08-script-plugin-ai-workbench-design.md` (§Phasing, Phase 4)
**Depends on:** Phases 1b, 2, 3A and 3B on `feature/script-plugin-ai-workbench`

## 1. Purpose

Phase 4 closes the script-plugin programme. The original design defines its deliverable as
"docs rewrite, CLAUDE.md, one `plugin-example` scenario rewritten as a script plugin sample" with the
acceptance criterion "documentation generated from the contract catalog stays consistent with code".

This phase makes that criterion executable:

- the Plugin Rules chapter of `docs/standards/backend.md` describes the real interfaces and no longer
  references dead APIs;
- the archetype table is generated from `PluginContractCatalog` and checked by a test;
- the legacy jar-plugin example is replaced by a runnable lazy-waving script-plugin sample, checked by
  the real static validator and the Groovy compiler;
- `CLAUDE.md` tells future sessions where the platform lives and which rules are authoritative.

## 2. Scope

**In scope**

- Rewrite the `## Plugin Rules` chapter of `docs/standards/backend.md`.
- Add `PluginContractCatalog.toMarkdownTable()` and a docs/catalog consistency test.
- Rewrite `server/modules-plugin/plugin-example/` as a lazy-waving script-plugin sample and remove
  it from the Gradle build.
- Add a static-validation test for the sample.
- Update `CLAUDE.md`.

**Out of scope**

- The manual E2E/acceptance walkthrough (Docker/model key absence is unchanged).
- Merging the branch, deployment changes, or Nacos changes.
- Committing the untracked Phase 2/3A/3B plan documents.
- Client changes.
- Config encryption, audit retention, station test-run, and the other v1 limitations.

## 3. Decisions

| Decision | Choice |
|---|---|
| Sample scenario | Lazy waving (`OUTBOUND_WAVE_WAVE_ACTION`) |
| Sample location | In-place rewrite of `server/modules-plugin/plugin-example/`; removed from `settings.gradle` |
| Enforcement | Two JUnit tests: sample static validation; docs/catalog consistency |
| Orphan interface | Delete `IContainerTaskGeneratorAction` (zero references; binary-compat risk accepted) |
| Docs order | Script Plugin Rules first, Jar Plugin Rules (Legacy) second |
| Generated table | Marker-delimited block produced by `PluginContractCatalog.toMarkdownTable()` |

## 4. Documentation Rewrite

### 4.1 Chapter structure

`## Plugin Rules` opens with a short paragraph: script plugins are the default for new work; the
PF4J jar-plugin path is kept for existing plugins and is frozen. The chapter then has two
subsections.

```
### Script Plugin Rules
1. Contract Catalog Is the Single Source of Truth   (generated archetype table)
2. Script Method Signature: ctx Is Always the Last Parameter
3. Failure Policy
4. Priority and Built-in Fallback
5. Enable, Disable and Rollback Flow                 (five validation stages)
6. Lifecycle Listeners Are Not IPlugins
7. Runtime Wiring and v1 Limitations
8. AI Draft Pipeline
9. Workbench Authorization and Audit
10. Example: Lazy Waving                             (links to the sample README)

### Jar Plugin Rules (Legacy)
1. One Domain Plugin Interface per Domain
2. Action Interfaces Extend the Domain Interface
3. Lookup, Priority and Fallback
4. Jar Plugin Configuration and Packaging
```

The existing script-plugin text (items 4–12 in the current file) is preserved, renumbered under the
subsection, and tightened where it duplicates the catalog. The Table of Contents is updated to show
`Script Plugin Rules` and `Jar Plugin Rules (Legacy)` under `Plugin Rules`.

### 4.2 Accurate legacy rules

The current rules 1–3 reference APIs that do not exist. The rewrite uses the real interfaces:

- `IPlugin extends ExtensionPoint` with `initialize()`, `destory()` and `priority()`;
- domain interfaces such as `IOutboundWavePlugin extends IPlugin, IEntityLifecycleListener<Long, String>`
  and `IOutboundPlanOrderPlugin`, which declare `getEntityName()`;
- action interfaces such as `IOutboundWaveWaveAction extends IOutboundWavePlugin`, which declares
  `wave(List<OutboundPlanOrderDTO>)` and defaults to `null`;
- lookup through `PluginSdkUtils.getExtractObject(Class<T>)`, which merges jar extensions and
  materialized script proxies, sorts by priority descending and returns an empty list when none is
  enabled;
- jar-plugin config through `PluginUtils.getPluginConfig(pluginId, Class<T>)` (legacy, file-backed).

`IContainerTaskGeneratorAction`, `getGeneratorPlugin()` and `PluginUtils.getExtractObject` must not
appear anywhere in `backend.md`. The orphaned
`server/modules-plugin/plugin-extension/.../IContainerTaskGeneratorAction.java` is deleted; a grep
shows no Java references outside its own declaration. The jar path is frozen, so the binary-compat
risk is accepted; if that changes, the alternative is a `@Deprecated` annotation instead of deletion.

### 4.3 Generated archetype table

`PluginContractCatalog` gains:

```java
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

The docs table is wrapped in markers:

```markdown
<!-- BEGIN GENERATED: script-plugin-archetypes -->
| Archetype | Host extension point | Script method | Blocking |
|-----------|----------------------|---------------|----------|
...
<!-- END GENERATED: script-plugin-archetypes -->
```

The dispatcher labels mirror `ExtHttpDispatcher` and the cron dispatcher; they are documentation
labels, not runtime constants. The generated block and the current table are identical for every
catalog-backed row, so this change does not alter the documented contract.

### 4.4 Example section

The last script-plugin rule links to `server/modules-plugin/plugin-example/README.md` and summarizes
the sample: one `OUTBOUND_WAVE_WAVE_ACTION` declaration, two string config fields, and the Groovy
script. It states that the sample is not seeded automatically and must be created through the
workbench.

## 5. Example Rewrite

### 5.1 Layout

Delete the Java sources, `build.gradle` and `src/main/resources/plugin.properties`, and remove
`include 'modules-plugin:plugin-example'` from `server/settings.gradle`. The directory becomes a
non-built sample:

```
server/modules-plugin/plugin-example/
├── README.md
├── plugin.json
└── lazy-waving.groovy
```

The old class files under `build/` are gitignored and may remain locally; they are not part of the
change set.

### 5.2 `plugin.json`

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

`description` is not part of `ScriptPluginBundleDTO`; the test's `ObjectMapper` disables
`FAIL_ON_UNKNOWN_PROPERTIES`. `sdkVersion` must equal the platform contract version (`1.0`); the
validator enforces it.

### 5.3 `lazy-waving.groovy`

The script is a faithful, defensive translation of the legacy `LazyOutboundWavePlugin`:

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

The behavior matches the legacy scenario: orders whose customer wave number has a completed order
are withheld unless the packing station reports online; orders without a customer wave number are
grouped by `customerOrderType`; the rest are grouped by `customerWaveNo`.

### 5.4 README

The README contains:

- the scenario summary and the mapping to `OUTBOUND_WAVE_WAVE_ACTION`;
- the file list and the declaration/config tables;
- workbench import steps (`/plugin-platform/plugin-workbench?mode=new`, create draft, save revision,
  configure, validate, enable as admin);
- a note that `ctx.outbound()` and `ctx.callback()` are wes-only, that config secrets must be read
  through `ctx.config`, and that the sample is not auto-seeded;
- the validation command
  `./gradlew :modules-plugin:script-plugin-api:test --tests '*ScriptPluginExampleValidationTest'`;
- a link to `docs/standards/backend.md` §Script Plugin Rules.

## 6. Tests

Both tests live in `script-plugin-api`. A shared test helper locates the repository root by walking
up from the working directory until `server/settings.gradle` exists; it fails with the starting path
when not found. This keeps the tests independent of whether Gradle or an IDE runs them.

### 6.1 `ScriptPluginExampleValidationTest`

Location:
`server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/example/ScriptPluginExampleValidationTest.java`

- read `server/modules-plugin/plugin-example/plugin.json` with an `ObjectMapper` configured to ignore
  unknown properties, into a `ScriptPluginBundleDTO`;
- read every `*.groovy` file in the sample directory, keyed by filename, into `bundle.setScripts(...)`;
- run `new ScriptPluginStaticValidator(new GroovyScriptEngine()).validate(bundle)`;
- assert the failure list is empty, printing every failure in the assertion message;
- assert every declared type is allowed for `bundle.getApplySystem()` by
  `PluginContractCatalog.typesForApp(...)`.

This exercises the `sdkVersion`, `declarations` and `compile` stages. It does not exercise the
Spring-context materialization or runtime invocation; no E2E claim is made.

### 6.2 `PluginDocumentationConsistencyTest`

Location:
`server/modules-plugin/script-plugin-api/src/test/java/org/openwes/script/plugin/api/contract/PluginDocumentationConsistencyTest.java`

- read `docs/standards/backend.md`;
- extract the text between the two generated-table markers, normalize CRLF to LF and trim;
- assert it equals `PluginContractCatalog.toMarkdownTable().trim()`;
- assert the file no longer contains `IContainerTaskGeneratorAction`, `getGeneratorPlugin` or
  `PluginUtils.getExtractObject`.

The test fails with a readable diff when the catalog changes without regenerating the docs, or when
someone edits the generated block.

## 7. CLAUDE.md

- Update the `modules-plugin/` line in Project Structure to name the legacy jar modules, the three
  script-plugin modules and the sample.
- Add a concise `## Script Plugin Platform` section after Backend Conventions:
  - DB-backed Groovy bundles, no jar packaging;
  - module responsibilities (`script-plugin-api` catalog/DTOs, `script-plugin-core` persistence and
    `/plugin-platform/**`, `script-plugin-runtime` materialization/dispatchers/registry, legacy jar
    modules, `plugin-example` sample);
  - wes hosts management + runtime, station hosts runtime only;
  - `PluginSdkUtils.getExtractObject` merge point and built-in fallback;
  - workbench routes `/plugin-platform/plugins`, `/plugin-platform/plugin-workbench`
    (`?mode=new` / `?pluginKey=...`) and `/plugin-platform/audit`; client path
    `client/src/pages/plugin_platform/`;
  - the relevant test command;
  - a pointer to `docs/standards/backend.md` §Script Plugin Rules and
    `PluginDocumentationConsistencyTest`.
- Add Key File Locations rows for the catalog, management, runtime, sample, client workbench and
  rules chapter.

No rule text is duplicated in CLAUDE.md.

## 8. Verification

| Check | Command | Expected |
|---|---|---|
| New tests | `cd server && ./gradlew :modules-plugin:script-plugin-api:test` | all pass, including both new tests |
| Full backend build | `cd server && ./gradlew build -x bootJar` | success (the known-good Phase 2 command) |
| Sample no longer a module | `grep -n "plugin-example" server/settings.gradle` | no output |
| Dead interface gone | `grep -rn "IContainerTaskGeneratorAction" server --include=*.java` | no output |
| Change set | `git status --short` | only the intended docs, sample, test, settings and CLAUDE.md files |

No client build is required: Phase 4 does not touch `client/`.

## 9. Risks and Mitigations

- **Deleting the orphan interface breaks external jar plugins.** The repository has zero references
  and the jar path is frozen; the sample no longer uses it. If the risk is unacceptable, keep the
  interface and only remove its documentation.
- **Tests depend on the repository layout.** They locate the root via `server/settings.gradle` and
  fail loudly when the docs or sample are missing. That is intentional: the docs and sample are part
  of the repository contract.
- **Generated-block drift.** Editing inside the markers fails the test with a diff. Regenerate from
  `PluginContractCatalog.toMarkdownTable()` instead.
- **The sample is validated statically only.** The Groovy compiler and declaration validator run, but
  no host beans or database are invoked. The README and this spec must not claim runtime E2E.
- **Removing the Gradle module leaves stale local `build/` files.** They are gitignored and do not
  affect the build or the change set.

## 10. References

- Original design: `docs/superpowers/specs/2026-10-08-script-plugin-ai-workbench-design.md`
- Phase 3 design: `docs/superpowers/specs/2026-10-22-script-plugin-phase3-workbench-design.md`
- Backend standards: `docs/standards/backend.md` (§Script Plugin Rules)
- Contract catalog: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/contract/PluginContractCatalog.java`
- Legacy sample: `server/modules-plugin/plugin-example/`
