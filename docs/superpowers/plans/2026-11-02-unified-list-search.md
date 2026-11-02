# Unified List Search Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Every list page's search band is generated from `searchable` columns, with the same 展开/收起, 设置查询字段, 查询 and 重置 controls.

**Architecture:** `crudPage` always emits `autoGenerateFilter` with three columns; pages declare query controls on their existing columns. Ten pages lose their explicit `filters` arrays. One narrow-width SCSS rule gives the auto-filter toolbar its own row below 1200px.

**Tech Stack:** React 17, TypeScript, AMIS 6.13, SCSS, Node schema-lint script.

**Spec:** `docs/superpowers/specs/2026-11-02-unified-list-search-design.md`

## Global Constraints

- `columnsNum: 3` is declared only in `crudPage`; never set `autoGenerateFilter` in a page.
- A page never passes `filters`; all query controls are columns with `searchable`.
- Search priority is column order: the first two active `searchable` columns fill the collapsed row. The spec's migration table lists the set of searchable columns; this plan applies column order to it, so a few relative positions differ from the table.
- No backend, database, locale, API, or route changes.
- Keep every existing column label, `dbField`, dictionary source, and column order unchanged.
- English comments and logs only. Do not add `console.log`.
- Type-check command: `cd client && npx tsc --noEmit --skipLibCheck`. The pre-existing failures in `src/pages/wms/station/index.tsx` and `src/utils/polyfill.tsx` are baseline; any other error fails the task.
- Lint command: `cd client && npm run lint:amis -- --strict`. Expected baseline: `0 error(s), 0 warning(s)`.
- The client type-check can take 60–90 seconds; do not interrupt it.
- Commit after every task. Stage only the files named in that task. Leave `client/src/utils/polyfill.tsx` unstaged — it is an unrelated working-tree change.
- End every commit message with:
  `Co-Authored-By: Claude Code <noreply@anthropic.com>`
- Browser checks use the running dev stack at `http://localhost:3000`. If it is not running, start it from `client/` with `npm run dev`. Sign in with the local dev credentials supplied by the environment; never write credentials into the repository.
- Browser capture uses the project skill `capturing-browser-screenshots` (`scripts/cdp-capture.mjs`) and its OpenWES recipe. Read every captured image before reporting a result.
- The SCSS rule depends on AMIS-owned class names (`cxd-Table-searchableForm`, `cxd-AutoFilterToolbar`); the 1024px check must include it after any AMIS upgrade.

---

## File Structure

| File | Responsibility | Task |
|---|---|---|
| `client/src/scss/amis-console.scss` | Shared narrow-width toolbar rule | 1 |
| `client/src/pages/plugin_platform/list/index.tsx` | Plugin list search band | 2 |
| `client/src/pages/wms/config_center/print/print_record.tsx` | Print record search band | 3 |
| `client/src/pages/wms/config_center/warehouse/location_management.tsx` | Location search band | 4 |
| `client/src/pages/wms/config_center/warehouse/warehouse_management.tsx` | Warehouse search band | 5 |
| `client/src/pages/wms/data_center/inbound/accept_order.tsx` | Accept order search band | 6 |
| `client/src/pages/wms/data_center/inbound/inbound_plan_order/index.tsx` | Inbound plan order search band | 7 |
| `client/src/pages/wms/data_center/inbound/put_away_task.tsx` | Put-away task search band | 8 |
| `client/src/pages/wms/data_center/stock/container_stock.tsx` | Container stock search band | 9 |
| `client/src/pages/wms/data_center/stock/stock_abnormal.tsx` | Stock abnormal search band | 10 |
| `client/src/pages/wms/data_center/stock/stocktake/index.tsx` | Stocktake search band | 11 |
| `client/src/utils/amis/presets.ts` | Remove `filters` support; always emit `autoGenerateFilter` | 12 |
| `client/src/utils/amis/useConsoleShortcuts.ts` | Remove explicit-filter selectors | 12 |
| `docs/standards/amis.md` | Document the single search-band model | 13 |

No new files are created.

---

### Task 1: Shared narrow-width toolbar rule

**Files:**
- Modify: `client/src/scss/amis-console.scss:297-299`
- Modify: `docs/superpowers/specs/2026-11-02-unified-list-search-design.md:4`
- Commit: `docs/superpowers/specs/2026-11-02-unified-list-search-design.md`, `docs/superpowers/plans/2026-11-02-unified-list-search.md`

**Interfaces:**
- Consumes: nothing.
- Produces: a global CSS rule that changes only the collapsed auto-filter toolbar layout below 1200px. Later tasks rely on it for the 1024px acceptance check.

- [ ] **Step 1: Create the working branch**

If the current branch is `master`, create a feature branch. If the execution environment already created a branch or worktree, skip this step.

```bash
cd /d/open-wes
git switch -c feat/unified-list-search
git branch --show-current
```

Expected: `feat/unified-list-search`.

- [ ] **Step 2: Mark the spec approved and commit the design documents**

Change line 4 of `docs/superpowers/specs/2026-11-02-unified-list-search-design.md` from:

```md
- Status: Draft for review
```

to:

```md
- Status: Approved
```

Then commit the spec and this plan:

```bash
cd /d/open-wes
git add docs/superpowers/specs/2026-11-02-unified-list-search-design.md docs/superpowers/plans/2026-11-02-unified-list-search.md
git commit -m "docs(amis): plan the unified list search" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

- [ ] **Step 3: Add the narrow-width rule**

In `client/src/scss/amis-console.scss`, after this existing rule:

```scss
.cxd-Form--horizontal .cxd-Form-item {
    margin-bottom: 16px;
}
```

insert:

```scss
/*
 * Auto-filter toolbar: below 1200px the 查询/重置/展开 controls no longer fit
 * beside two fields, and AMIS squeezes the first input to ~25px. Give the
 * toolbar its own row; the fields keep the full width. Re-check after an AMIS
 * upgrade (the class names are AMIS-owned).
 */
@media (max-width: 1199.98px) {
    .cxd-Table-searchableForm
        .cxd-Form-groupColumn:has(> .cxd-AutoFilterToolbar) {
        flex-basis: 100% !important;
    }
}
```

- [ ] **Step 4: Compile the stylesheet**

```bash
cd /d/open-wes/client
npx sass src/scss/amis-console.scss C:/Users/niekang/AppData/Local/Temp/amis-console-check.css
```

Expected: exit code 0, no error. Sass passes `:has()` through unchanged.

- [ ] **Step 5: Browser-check the shared rule on the owner page**

Use the `capturing-browser-screenshots` skill. The owner page already uses the auto-filter band, so it proves the rule before any page is migrated.

Create `owner-toolbar.json` in a temporary directory (not in the repository):

```json
{
  "timeout": 30000,
  "steps": [
    { "navigate": "http://localhost:3000/login" },
    { "wait": 1000 },
    {
      "eval": "fetch('http://localhost:8090/user/api/auth/signin', { method: 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ username: '{{ENV:OPENWES_USER}}', password: '{{ENV:OPENWES_PASS}}' }) }).then((r) => r.json()).then((d) => { if (!d.token) throw new Error('signin failed: ' + JSON.stringify(d)); localStorage.setItem('ws_token', d.token); localStorage.setItem('Authorization', 'admin'); localStorage.setItem('i18nextLng', 'zh-CN'); return 'signed in'; })"
    },
    { "navigate": "http://localhost:3000/wms/dashboard" },
    { "waitForText": "入库管理" },
    { "navigate": "http://localhost:3000/wms/config-center/basic/owner-management" },
    { "waitForSelector": ".cxd-Table-searchableForm" },
    { "wait": 800 },
    { "removeSelector": "#webpack-dev-server-client-overlay" },
    { "viewport": { "width": 1440, "height": 900, "deviceScaleFactor": 1 } },
    { "wait": 400 },
    { "screenshot": "owner-1440" },
    { "removeSelector": "#webpack-dev-server-client-overlay" },
    { "viewport": { "width": 1024, "height": 768, "deviceScaleFactor": 1 } },
    { "wait": 400 },
    { "screenshot": "owner-1024" }
  ]
}
```

Run it with the credentials exported in the shell:

```bash
export OPENWES_USER=...   # local dev user, from the environment
export OPENWES_PASS=...   # local dev password, from the environment
node C:/Users/niekang/.claude/skills/capturing-browser-screenshots/scripts/cdp-capture.mjs \
  --steps owner-toolbar.json --out owner-toolbar
```

Read `owner-toolbar/owner-1440.jpg` and `owner-toolbar/owner-1024.jpg`.

Expected:
- 1440: the two collapsed fields and the toolbar (设置查询字段 / 查询 / 重置 / 展开) share one row.
- 1024: the toolbar is on its own right-aligned row; the two collapsed fields occupy the row above and their inputs are at least ~200px wide, not squeezed to a sliver.
- No page error, no empty table, no webpack overlay.

If the 1024 screenshot still shows a squeezed input, the rule did not reach the running stylesheet — stop and fix that before continuing.

- [ ] **Step 6: Commit**

```bash
cd /d/open-wes
git add client/src/scss/amis-console.scss
git commit -m "feat(amis): give auto-filter toolbar its own row on narrow screens" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 2: Plugin list search band

**Files:**
- Modify: `client/src/pages/plugin_platform/list/index.tsx:56-176`

**Interfaces:**
- Consumes: `crudPage`, `codeColumn`, `statusColumn` from `@/utils/amis/presets`; signatures unchanged.
- Produces: no exports. Searchable columns in column order: `pluginKey`, `name`, `applySystem`, `status`. Collapsed row: `pluginKey`, `name`.

- [ ] **Step 1: Add `searchable` to the four columns**

Replace the current `const columns = [...]` entries with these. The later entries (`applyModule`, `lastValidationStatus`, `lastValidationAt`, `currentRevisionNo`, `updateTime`) stay unchanged.

```ts
const columns = [
    codeColumn("pluginKey", "pluginPlatform.list.column.pluginKey", {
        width: 200,
        searchable: true
    }),
    {
        name: "name",
        label: "pluginPlatform.list.column.name",
        width: 180,
        searchable: true
    },
    {
        name: "applySystem",
        label: "pluginPlatform.list.column.applySystem",
        type: "mapping",
        width: 120,
        map: {
            wes: "pluginPlatform.applySystem.wes",
            station: "pluginPlatform.applySystem.station"
        },
        searchable: {
            type: "select",
            options: [
                { label: "pluginPlatform.applySystem.wes", value: "wes" },
                { label: "pluginPlatform.applySystem.station", value: "station" }
            ]
        }
    },
    {
        name: "applyModule",
        label: "pluginPlatform.list.column.applyModule",
        width: 140,
        tpl: "${applyModule || '-'}"
    },
    statusColumn("status", "pluginPlatform.list.column.status", {
        labels: {
            DRAFT: "pluginPlatform.status.draft",
            ENABLED: "pluginPlatform.status.enabled",
            DISABLED: "pluginPlatform.status.disabled",
            ARCHIVED: "pluginPlatform.status.archived"
        },
        tones: {
            DRAFT: "info",
            ENABLED: "success",
            DISABLED: "muted",
            ARCHIVED: "muted"
        },
        width: 110,
        searchable: {
            type: "select",
            options: [
                { label: "pluginPlatform.status.draft", value: "DRAFT" },
                { label: "pluginPlatform.status.enabled", value: "ENABLED" },
                { label: "pluginPlatform.status.disabled", value: "DISABLED" },
                { label: "pluginPlatform.status.archived", value: "ARCHIVED" }
            ]
        }
    }),
    statusColumn(
        "lastValidationStatus",
        "pluginPlatform.list.column.lastValidation",
        {
            labels: {
                PASSED: "pluginPlatform.validation.passed",
                FAILED: "pluginPlatform.validation.failed"
            },
            tones: {
                PASSED: "success",
                FAILED: "danger"
            },
            placeholder: "pluginPlatform.validation.notValidated",
            width: 130
        }
    ),
    dateTimeColumn("lastValidationAt", "pluginPlatform.validation.lastAt", {
        seconds: true,
        width: 170
    }),
    {
        name: "currentRevisionNo",
        label: "pluginPlatform.list.column.currentRevision",
        width: 130,
        tpl: "${currentRevisionNo ? 'v' + currentRevisionNo : '-'}"
    },
    dateTimeColumn("updateTime", "pluginPlatform.list.column.updateTime", {
        seconds: true,
        width: 170
    })
]
```

- [ ] **Step 2: Delete the explicit filter block**

Delete the entire `const filters = [...]` block (currently lines 128–163) and delete the `filters,` line in the `crudPage` call. The call must read:

```ts
const schema = crudPage({
    title: "pluginPlatform.list.title",
    name: "PluginTable",
    // Custom endpoint: it returns the whole collection, not a paged /search result.
    api: {
        method: "get",
        url: "/plugin-platform/scriptPlugin/list",
        adaptor: adaptPluginList
    },
    loadDataOnce: true,
    columns,
    operationWidth: 90,
    pageActions: [newPluginButton],
    rowActions: [
        {
            type: "button",
            level: "link",
            className: "owes-action",
            label: "pluginPlatform.list.open",
            actionType: "link",
            link: "/plugin-platform/plugin-workbench?pluginKey=${pluginKey}"
        }
    ],
    empty: "pluginPlatform.list.empty"
})
```

- [ ] **Step 3: Type-check**

```bash
cd /d/open-wes/client
npx tsc --noEmit --skipLibCheck 2>&1 | grep "error TS" | grep -vE "src/pages/wms/station/index.tsx|src/utils/polyfill.tsx" && { echo "FAIL: new type errors"; exit 1; } || echo "PASS: only baseline errors"
```

Expected: `PASS: only baseline errors`.

- [ ] **Step 4: Lint**

```bash
cd /d/open-wes/client
npm run lint:amis -- --strict
```

Expected: `0 error(s), 0 warning(s) in ... schema file(s).`

- [ ] **Step 5: Commit**

```bash
cd /d/open-wes
git add client/src/pages/plugin_platform/list/index.tsx
git commit -m "feat(amis): unify plugin list search band" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 3: Print record search band

**Files:**
- Modify: `client/src/pages/wms/config_center/print/print_record.tsx:13-110`

**Interfaces:**
- Consumes: `crudPage`, `codeColumn`, `dateTimeColumn`, `statusColumn`; `printStatus` from `@/utils/amis/statusMaps`; `work_station` from the select constants.
- Produces: no exports. Searchable columns in column order: `module`, `printNode`, `templateCode`, `workStationId`, `printTime`, `printer`, `printStatus`. Collapsed row: `module`, `printNode`.

- [ ] **Step 1: Add `searchable` to the columns**

Replace the column definitions so the array reads:

```ts
const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "module",
        label: "printRecord.module",
        type: "mapping",
        source: "${dictionary.ModuleEnum}",
        searchable: { type: "select", source: "${dictionary.ModuleEnum}" }
    },
    {
        name: "printNode",
        label: "printRecord.printNode",
        type: "mapping",
        source: "${dictionary.PrintNodeEnum}",
        searchable: { type: "select", source: "${dictionary.PrintNodeEnum}" }
    },
    codeColumn("templateCode", "printRecord.templateCode", {
        searchable: true
    }),
    {
        name: "templateName",
        label: "printRecord.templateName"
    },
    {
        name: "workStationId",
        label: "printRecord.workStationId",
        type: "mapping",
        source: work_station,
        searchable: { type: "select", source: work_station }
    },
    dateTimeColumn("printTime", "printRecord.printTime", {
        seconds: true,
        searchable: { type: "input-date-range", valueFormat: "x" }
    }),
    {
        name: "printer",
        label: "printRecord.printer",
        searchable: true
    },
    statusColumn("printStatus", "printRecord.printStatus", {
        ...printStatus,
        searchable: {
            type: "select",
            source: "${dictionary.PrintStatusEnum}"
        }
    }),
    {
        name: "errorMessage",
        label: "printRecord.errorMessage",
        type: "tpl",
        tpl: "${errorMessage}",
        className: "owes-text-danger"
    },
    ...auditColumns({ visible: true, createdOnly: true })
]
```

- [ ] **Step 2: Delete the explicit filter block**

Delete the entire `const filters = [...]` block (currently lines 58–99) and the `filters,` line in the `crudPage` call. The call becomes:

```ts
const schema = crudPage({
    title: "printRecord.title",
    name: "PPrintRecord",
    searchIdentity: "PPrintRecord",
    api: api_crud_search,
    columns,
    orderBy: "create_time desc",
    empty: "emptyState.printRecord"
})
```

- [ ] **Step 3: Type-check**

```bash
cd /d/open-wes/client
npx tsc --noEmit --skipLibCheck 2>&1 | grep "error TS" | grep -vE "src/pages/wms/station/index.tsx|src/utils/polyfill.tsx" && { echo "FAIL: new type errors"; exit 1; } || echo "PASS: only baseline errors"
```

Expected: `PASS: only baseline errors`.

- [ ] **Step 4: Lint**

```bash
cd /d/open-wes/client
npm run lint:amis -- --strict
```

Expected: `0 error(s), 0 warning(s) in ... schema file(s).`

- [ ] **Step 5: Commit**

```bash
cd /d/open-wes
git add client/src/pages/wms/config_center/print/print_record.tsx
git commit -m "feat(amis): unify print record search band" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 4: Location management search band

**Files:**
- Modify: `client/src/pages/wms/config_center/warehouse/location_management.tsx:100-195`

**Interfaces:**
- Consumes: `crudPage`, `codeColumn`, `statusColumn`; `warehouse_area_id`, `warehouse_logic_id` from `@/pages/wms/constants/select_search_api_contant`.
- Produces: no exports. Searchable columns in column order: `warehouseAreaId`, `warehouseLogicId`, `aisleCode`, `shelfCode`, `locationCode`. Collapsed row: `warehouseAreaId`, `warehouseLogicId`.

- [ ] **Step 1: Add `searchable` to the five columns**

The `columns` array becomes:

```ts
const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "version",
        label: "Version",
        hidden: true
    },
    {
        name: "warehouseCode",
        label: "table.warehouseCode",
        hidden: true
    },
    {
        name: "warehouseAreaId",
        label: "table.reservoirAreaName",
        type: "mapping",
        source: warehouse_area_id,
        searchable: { type: "select", source: warehouse_area_id }
    },
    {
        name: "warehouseLogicId",
        label: "table.logicalAreaName",
        type: "mapping",
        source: warehouse_logic_id,
        searchable: { type: "select", source: warehouse_logic_id }
    },
    codeColumn("aisleCode", "table.roadwayCoding", { searchable: true }),
    codeColumn("shelfCode", "table.shelfCoding", { searchable: true }),
    codeColumn("locationCode", "table.locationCode", { searchable: true }),
    {
        name: "locationType",
        label: "table.locationType",
        type: "mapping",
        source: "${dictionary.LocationType}"
    },
    {
        name: "heat",
        label: "table.locationHeat"
    },
    {
        name: "occupied",
        label: "table.occupation",
        type: "mapping",
        map: true_false_options
    },
    {
        name: "position",
        hidden: true
    },
    statusColumn("locationStatus", "table.locationStatus", locationStatus),
    ...auditColumns()
]
```

- [ ] **Step 2: Delete the explicit filter block**

Delete the entire `const filters = [...]` block (currently lines 155–183) and the `filters,` line in the `crudPage` call. The call becomes:

```ts
const schema = crudPage({
    title: "locationManagement.title",
    name: "role",
    searchIdentity: "WLocation",
    api: api_crud_search_by_warehouseCode,
    columns,
    pageActions: [addButton({ target: "role", drawer: addDrawer })],
    rowActions: [editButton(editDrawer)],
    empty: "emptyState.locationManagement"
})
```

- [ ] **Step 3: Type-check**

```bash
cd /d/open-wes/client
npx tsc --noEmit --skipLibCheck 2>&1 | grep "error TS" | grep -vE "src/pages/wms/station/index.tsx|src/utils/polyfill.tsx" && { echo "FAIL: new type errors"; exit 1; } || echo "PASS: only baseline errors"
```

Expected: `PASS: only baseline errors`.

- [ ] **Step 4: Lint**

```bash
cd /d/open-wes/client
npm run lint:amis -- --strict
```

Expected: `0 error(s), 0 warning(s) in ... schema file(s).`

- [ ] **Step 5: Commit**

```bash
cd /d/open-wes
git add client/src/pages/wms/config_center/warehouse/location_management.tsx
git commit -m "feat(amis): unify location management search band" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 5: Warehouse management search band

**Files:**
- Modify: `client/src/pages/wms/config_center/warehouse/warehouse_management.tsx:206-360`

**Interfaces:**
- Consumes: `crudPage`, `codeColumn`, `auditColumns`.
- Produces: no exports. Searchable columns in column order: `warehouseCode`, `warehouseName`, `warehouseType`, `warehouseAttrType`, `warehouseLevel`, `warehouseLabel`, `businessType`, `structureType`. Collapsed row: `warehouseCode`, `warehouseName`.

- [ ] **Step 1: Add `searchable` to the eight columns**

Replace the entire `columns` array with:

```ts
const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "version",
        label: "Version",
        hidden: true
    },
    codeColumn("warehouseCode", "table.warehouseCode", {
        searchable: true
    }),
    {
        name: "warehouseName",
        label: "table.warehouseName",
        searchable: true
    },
    {
        name: "warehouseType",
        label: "table.warehouseType",
        type: "mapping",
        source: "${dictionary.WarehouseType}",
        searchable: {
            type: "select",
            source: "${dictionary.WarehouseType}"
        }
    },
    {
        name: "warehouseAttrType",
        label: "table.warehouseProperties",
        type: "mapping",
        source: "${dictionary.WarehouseAttrType}",
        searchable: {
            type: "select",
            source: "${dictionary.WarehouseAttrType}"
        }
    },
    {
        name: "warehouseLevel",
        label: "table.warehouseLevel",
        type: "mapping",
        source: "${dictionary.WarehouseLevel}",
        searchable: {
            type: "select",
            source: "${dictionary.WarehouseLevel}"
        }
    },
    {
        name: "warehouseLabel",
        label: "table.warehouseLables",
        type: "mapping",
        source: "${dictionary.WarehouseLabel}",
        searchable: {
            type: "select",
            source: "${dictionary.WarehouseLabel}"
        }
    },
    {
        name: "businessType",
        label: "table.mainBusiness",
        type: "mapping",
        source: "${dictionary.WarehouseBusinessType}",
        searchable: {
            type: "select",
            source: "${dictionary.WarehouseBusinessType}"
        }
    },
    {
        name: "structureType",
        label: "table.warehouseStructure",
        type: "mapping",
        source: "${dictionary.WarehouseStructureType}",
        searchable: {
            type: "select",
            source: "${dictionary.WarehouseStructureType}"
        }
    },
    {
        name: "virtualWarehouse",
        label: "table.virtualWarehouse",
        type: "mapping",
        map: true_false_options
    },
    {
        name: "country",
        label: "table.country"
    },
    {
        name: "province",
        label: "table.province"
    },
    {
        name: "city",
        label: "table.city"
    },
    {
        name: "district",
        label: "table.districtCounty"
    },
    {
        name: "name",
        label: "table.contact"
    },
    {
        name: "tel",
        label: "table.phoneNumber"
    },
    {
        name: "mail",
        label: "table.email"
    },
    {
        name: "fax",
        label: "table.fax"
    },
    ...auditColumns()
]
```

- [ ] **Step 2: Delete the explicit filter block**

Delete the entire `const filters = [...]` block (currently lines 299–346) and the `filters,` line in the `crudPage` call. The call becomes:

```ts
const schema = crudPage({
    title: "warehouseManagement.title",
    name: "role",
    searchIdentity: "MWarehouseMainData",
    api: api_crud_search,
    columns,
    orderBy: "update_time desc",
    pageActions: [addButton({ target: "role", drawer: addDrawer })],
    rowActions: [editButton(editDrawer)],
    operationWidth: 150,
    empty: "emptyState.warehouseManagement"
})
```

- [ ] **Step 3: Type-check**

```bash
cd /d/open-wes/client
npx tsc --noEmit --skipLibCheck 2>&1 | grep "error TS" | grep -vE "src/pages/wms/station/index.tsx|src/utils/polyfill.tsx" && { echo "FAIL: new type errors"; exit 1; } || echo "PASS: only baseline errors"
```

Expected: `PASS: only baseline errors`.

- [ ] **Step 4: Lint**

```bash
cd /d/open-wes/client
npm run lint:amis -- --strict
```

Expected: `0 error(s), 0 warning(s) in ... schema file(s).`

- [ ] **Step 5: Commit**

```bash
cd /d/open-wes
git add client/src/pages/wms/config_center/warehouse/warehouse_management.tsx
git commit -m "feat(amis): unify warehouse management search band" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 6: Accept order search band

**Files:**
- Modify: `client/src/pages/wms/data_center/inbound/accept_order.tsx:112-153`

**Interfaces:**
- Consumes: `crudPage`, `codeColumn`, `statusColumn`, `auditColumns`; `acceptOrderStatus` from `@/utils/amis/statusMaps`.
- Produces: no exports. Searchable columns in column order: `identifyNo`, `acceptOrderStatus`, `orderNo`. Collapsed row: `identifyNo`, `acceptOrderStatus`.

- [ ] **Step 1: Add `searchable` to the three columns**

The `columns` array becomes:

```ts
const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "warehouseCode",
        label: "table.warehouseCode",
        hidden: true
    },
    codeColumn("identifyNo", "table.containerCode", { searchable: true }),
    statusColumn("acceptOrderStatus", "table.status", {
        ...acceptOrderStatus,
        searchable: {
            type: "select",
            source: "${dictionary.AcceptOrderStatus}"
        }
    }),
    codeColumn("orderNo", "table.orderNo", { searchable: true }),
    numberColumn("totalBox", "table.boxesNumber"),
    numberColumn("totalQty", "table.totalQuantity"),
    ...auditColumns(),
    remarkColumn()
]
```

- [ ] **Step 2: Delete the explicit filter block**

Delete the entire `const filters = [...]` block (currently lines 78–110) and the `filters,` line in the `crudPage` call. The call becomes:

```ts
const schema = crudPage({
    title: "menu.acceptOrder",
    name: "AcceptOrderTable",
    searchIdentity: "WAcceptOrder",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "accept_order_status, update_time desc",
    pageActions: [],
    tableActions: [
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "accept_order",
            defaultParams: {
                searchIdentity: "WAcceptOrder",
                showColumns: columns
            }
        })
    ],
    rowActions: [detailButton(detailDrawer)],
    empty: "emptyState.acceptOrder"
})
```

This removes the dead `ownerCode`, `skuCode`, and `skuName` controls. They were not columns, were not in `showColumns`, and have no fields on the search bean.

- [ ] **Step 3: Type-check**

```bash
cd /d/open-wes/client
npx tsc --noEmit --skipLibCheck 2>&1 | grep "error TS" | grep -vE "src/pages/wms/station/index.tsx|src/utils/polyfill.tsx" && { echo "FAIL: new type errors"; exit 1; } || echo "PASS: only baseline errors"
```

Expected: `PASS: only baseline errors`.

- [ ] **Step 4: Lint**

```bash
cd /d/open-wes/client
npm run lint:amis -- --strict
```

Expected: `0 error(s), 0 warning(s) in ... schema file(s).`

- [ ] **Step 5: Commit**

```bash
cd /d/open-wes
git add client/src/pages/wms/data_center/inbound/accept_order.tsx
git commit -m "feat(amis): unify accept order search band" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 7: Inbound plan order search band

**Files:**
- Modify: `client/src/pages/wms/data_center/inbound/inbound_plan_order/index.tsx:22-61` and `:268-325`

**Interfaces:**
- Consumes: `crudPage`, `codeColumn`, `statusColumn`, `numberColumn`; `inboundPlanOrderStatus` from `@/utils/amis/statusMaps`.
- Produces: no exports. Searchable columns in column order: `lpnCode`, `customerOrderNo`, `inboundPlanOrderStatus`, `orderNo`, `sender`, `skuKindNum`, `storageType`, `trackingNumber`, `shippingMethod`. Collapsed row: `lpnCode`, `customerOrderNo`.

- [ ] **Step 1: Add `searchable` to the columns**

Replace the `columns` array with:

```ts
const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "warehouseCode",
        label: "table.warehouseCode",
        hidden: true
    },
    codeColumn("lpnCode", "table.LPNNo", { searchable: true }),
    codeColumn("customerOrderNo", "table.customerOrderNo", {
        searchable: true
    }),
    statusColumn(
        "inboundPlanOrderStatus",
        "table.status",
        {
            ...inboundPlanOrderStatus,
            searchable: {
                type: "select",
                source: "${dictionary.InboundPlanOrderStatus}"
            }
        }
    ),
    codeColumn("orderNo", "table.orderNo", { searchable: true }),
    {
        name: "sender",
        label: "table.shipper",
        searchable: true
    },
    numberColumn("skuKindNum", "table.skuTypes", { searchable: true }),
    {
        name: "storageType",
        label: "table.storageType",
        type: "mapping",
        source: "${dictionary.StorageType}",
        searchable: {
            type: "select",
            source: "${dictionary.StorageType}"
        }
    },
    numberColumn("totalBox", "table.boxesNumber"),
    numberColumn("totalQty", "table.totalQuantity"),
    codeColumn("trackingNumber", "table.theTrackingNumber", {
        searchable: true
    }),
    {
        name: "shippingMethod",
        label: "table.modeOfCarriage",
        searchable: true
    },
    ...auditColumns(),
    remarkColumn()
]
```

- [ ] **Step 2: Delete the explicit filter block**

Delete the entire `const filters = [...]` block (currently lines 268–316) and the `filters,` line in the `crudPage` call. The complete call must read:

```ts
const schema = crudPage({
    title: "menu.inboundOrder",
    name: "InboundPlanOrderTable",
    searchIdentity: "WInboundPlanOrder",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "id ,inbound_plan_order_status desc",
    pageActions: [],
    tableActions: [
        importButton({
            label: "importExcel.label",
            dialog: importDialog
        }),
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "inbound_plan_order",
            method: "POST",
            defaultParams: {
                searchIdentity: "WInboundPlanOrder",
                showColumns: columns
            }
        })
    ],
    bulkActions: [closeAction],
    rowActions: [detailButton(detailDrawer)],
    operationWidth: 80,
    empty: "emptyState.inboundPlanOrder"
})
```

Only `filters,` is removed; every other property is unchanged.

- [ ] **Step 3: Type-check**

```bash
cd /d/open-wes/client
npx tsc --noEmit --skipLibCheck 2>&1 | grep "error TS" | grep -vE "src/pages/wms/station/index.tsx|src/utils/polyfill.tsx" && { echo "FAIL: new type errors"; exit 1; } || echo "PASS: only baseline errors"
```

Expected: `PASS: only baseline errors`.

- [ ] **Step 4: Lint**

```bash
cd /d/open-wes/client
npm run lint:amis -- --strict
```

Expected: `0 error(s), 0 warning(s) in ... schema file(s).`

- [ ] **Step 5: Commit**

```bash
cd /d/open-wes
git add client/src/pages/wms/data_center/inbound/inbound_plan_order/index.tsx
git commit -m "feat(amis): unify inbound plan order search band" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 8: Put-away task search band

**Files:**
- Modify: `client/src/pages/wms/data_center/inbound/put_away_task.tsx:19-53` and `:103-158`

**Interfaces:**
- Consumes: `crudPage`, `codeColumn`, `dateTimeColumn`, `statusColumn`, `auditColumns`; `putAwayTaskStatus` from `@/utils/amis/statusMaps`.
- Produces: no exports. Searchable columns in column order: `containerCode`, `containerSpecCode`, `taskNo`, `taskType`, `taskStatus`, `workStationId`, `createTime` (via audit), `updateTime`. Collapsed row: `containerCode`, `containerSpecCode`.

- [ ] **Step 1: Add `searchable` to the columns**

Replace the `columns` array with:

```ts
const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "warehouseCode",
        label: "table.warehouseCode",
        hidden: true
    },
    codeColumn("containerCode", "table.containerNumber", {
        searchable: true
    }),
    {
        name: "containerSpecCode",
        label: "table.containerSpecificationNumber",
        searchable: true
    },
    codeColumn("locationCode", "table.locationCode"),
    codeColumn("taskNo", "table.putAwayTaskNo", { searchable: true }),
    {
        name: "taskType",
        label: "table.putAwayMethod",
        type: "mapping",
        source: "${dictionary.PutAwayTaskType}",
        searchable: {
            type: "select",
            source: "${dictionary.PutAwayTaskType}"
        }
    },
    statusColumn("taskStatus", "table.putAwayStatus", {
        ...putAwayTaskStatus,
        searchable: {
            type: "select",
            source: "${dictionary.PutAwayTaskStatus}"
        }
    }),
    {
        name: "workStationId",
        label: "station.operatingStation",
        searchable: true
    },
    ...auditColumns({
        seconds: true,
        createdOnly: true,
        createdSearchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
    }),
    dateTimeColumn("updateTime", "table.completionTime", {
        seconds: true,
        toggled: true,
        searchable: {
            type: "input-date-range",
            valueFormat: "x"
        }
    })
]
```

- [ ] **Step 2: Delete the explicit filter block**

Delete the entire `const filters = [...]` block (currently lines 103–148) and the `filters,` line in the `crudPage` call. The complete call must read:

```ts
const schema = crudPage({
    title: "wms.menu.putAwayTasks",
    name: "PutAwayTaskTable",
    searchIdentity: "WPutAwayTask",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "task_status, update_time desc",
    pageActions: [],
    tableActions: [
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "put_away_task",
            method: "POST",
            defaultParams: {
                searchIdentity: "WPutAwayTask",
                showColumns: columns
            }
        })
    ],
    rowActions: [detailButton(detailDrawer)],
    empty: "emptyState.putAwayTask"
})
```

Only `filters,` is removed; every other property is unchanged.

- [ ] **Step 3: Type-check**

```bash
cd /d/open-wes/client
npx tsc --noEmit --skipLibCheck 2>&1 | grep "error TS" | grep -vE "src/pages/wms/station/index.tsx|src/utils/polyfill.tsx" && { echo "FAIL: new type errors"; exit 1; } || echo "PASS: only baseline errors"
```

Expected: `PASS: only baseline errors`.

- [ ] **Step 4: Lint**

```bash
cd /d/open-wes/client
npm run lint:amis -- --strict
```

Expected: `0 error(s), 0 warning(s) in ... schema file(s).`

- [ ] **Step 5: Commit**

```bash
cd /d/open-wes
git add client/src/pages/wms/data_center/inbound/inbound_plan_order/index.tsx
git commit -m "feat(amis): unify inbound plan order search band" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 9: Container stock search band

**Files:**
- Modify: `client/src/pages/wms/data_center/stock/container_stock.tsx:31-142` and `:144-266`

**Interfaces:**
- Consumes: `crudPage`, `codeColumn`, `numberColumn`, `auditColumns`; `owner_code`, `warehouse_area_code` from the select constants.
- Produces: no exports. Searchable columns in column order: `skuCode`, `barcode`, `ownerCode`, `warehouseAreaCode`, `containerCode`, `createTime` (via audit). Collapsed row: `skuCode`, `barcode`.

- [ ] **Step 1: Add `createdSearchable` to the audit mapper**

Replace:

```ts
const audit = auditColumns({ seconds: true }).map((column) => ({
```

with:

```ts
const audit = auditColumns({
    seconds: true,
    createdSearchable: {
        type: "input-date-range",
        valueFormat: "x"
    }
}).map((column) => ({
```

Keep the `.map(...)` body unchanged so every audit column still gets its `k.*` `dbField`.

- [ ] **Step 2: Add `searchable` to the five display columns**

Replace the `skuCode`, `barcode`, `ownerCode`, `warehouseAreaCode`, and `containerCode` entries with:

```ts
    {
        ...codeColumn("skuCode", "skuArea.skuCode", { searchable: true }),
        dbField: "a.sku_code"
    },
    {
        ...codeColumn("barcode", "skuArea.barcode", { searchable: true }),
        dbField: "f.bar_code"
    },
    {
        name: "ownerCode",
        label: "skuArea.ownerCode",
        dbField: "a.owner_code",
        source: owner_code,
        searchable: { type: "select", source: owner_code }
    },
```

and

```ts
    {
        name: "warehouseAreaCode",
        label: "workLocationArea.warehouseArea",
        dbField: "wa.warehouse_area_code",
        source: warehouse_area_code,
        searchable: { type: "select", source: warehouse_area_code }
    },
    {
        ...codeColumn("containerCode", "table.containerCode", {
            searchable: true
        }),
        dbField: "k.container_code"
    },
```

- [ ] **Step 3: Delete the explicit filter block**

Delete the entire `const filters = [...]` block (currently lines 144–178). In the `crudPage` call, remove the `filters,` line between `searchObject` and `tableActions`. The call starts:

```ts
const schema = crudPage({
    title: "inventoryDetails.title",
    name: "containerStockTable",
    searchIdentity: "WContainerStock",
    api: api_crud_search_by_warehouseCode,
    columns,
    searchObject: {
        tables:
            "w_container_stock k left join w_sku_batch_stock b on k.sku_batch_stock_id = b.id  left join w_sku_batch_attribute e on k.sku_batch_attribute_id = e.id  left join w_warehouse_area wa on b.warehouse_area_id = wa.id  left join m_sku_main_data a on e.sku_id = a.id left join m_sku_barcode_data f  on a.id = f.sku_id",
        where: "k.total_qty > 0 and k.container_id is not null"
    },
    tableActions: [
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "container_stock"
        })
    ],
    ...(rowActions.length ? { rowActions } : {}),
    empty: "emptyState.containerStock"
})
```

Only remove `filters,`; keep the export action and the conditional `rowActions` spread verbatim.

- [ ] **Step 4: Type-check**

```bash
cd /d/open-wes/client
npx tsc --noEmit --skipLibCheck 2>&1 | grep "error TS" | grep -vE "src/pages/wms/station/index.tsx|src/utils/polyfill.tsx" && { echo "FAIL: new type errors"; exit 1; } || echo "PASS: only baseline errors"
```

Expected: `PASS: only baseline errors`.

- [ ] **Step 5: Lint**

```bash
cd /d/open-wes/client
npm run lint:amis -- --strict
```

Expected: `0 error(s), 0 warning(s) in ... schema file(s).`

- [ ] **Step 6: Commit**

```bash
cd /d/open-wes
git add client/src/pages/wms/data_center/stock/container_stock.tsx
git commit -m "feat(amis): unify container stock search band" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 10: Stock abnormal search band

**Files:**
- Modify: `client/src/pages/wms/data_center/stock/stock_abnormal.tsx:17-122` and `:124-171`

**Interfaces:**
- Consumes: `crudPage`, `codeColumn`, `statusColumn`, `auditColumns`; `stockAbnormalStatus` from `@/utils/amis/statusMaps`.
- Produces: no exports. Searchable columns in column order: `abnormalOrderNo`, `stockAbnormalType`, `stockAbnormalStatus`, `containerCode`, `barCodeList`, `createTime` (via audit). Collapsed row: `abnormalOrderNo`, `stockAbnormalType`.

- [ ] **Step 1: Add `createdSearchable` to the audit mapper**

Replace:

```ts
const audit = auditColumns().map((column) => ({
```

with:

```ts
const audit = auditColumns({
    createdSearchable: {
        type: "input-date-range",
        valueFormat: "x"
    }
}).map((column) => ({
```

Keep the `.map(...)` body unchanged so every audit column still gets its `r.*` `dbField`.

- [ ] **Step 2: Add `searchable` to the five columns**

Replace the `abnormalOrderNo`, `stockAbnormalType`, `stockAbnormalStatus`, `containerCode`, and `barCodeList` entries with:

```ts
    {
        ...codeColumn("abnormalOrderNo", "table.exceptionTrackingNumber", {
            searchable: true
        }),
        dbField: "r.order_no"
    },
    {
        dbField: "r.stock_abnormal_type",
        name: "stockAbnormalType",
        label: "table.exceptionType",
        type: "mapping",
        source: "${dictionary.StockAbnormalType}",
        searchable: {
            type: "select",
            source: "${dictionary.StockAbnormalType}"
        }
    },
```

and

```ts
    {
        ...statusColumn(
            "stockAbnormalStatus",
            "table.status",
            stockAbnormalStatus
        ),
        dbField: "r.stock_abnormal_status",
        searchable: {
            type: "select",
            source: "${dictionary.StockAbnormalStatus}"
        }
    },
```

and

```ts
    {
        ...codeColumn("containerCode", "table.containerCode", {
            searchable: true
        }),
        dbField: "r.container_code"
    },
```

and

```ts
    {
        dbField: "g.bar_code_list",
        name: "barCodeList",
        label: "skuArea.barcode",
        searchable: true
    },
```

- [ ] **Step 3: Delete the explicit filter block**

Delete the entire `const filters = [...]` block (currently lines 124–158) and the `filters,` line in the `crudPage` call. The call becomes:

```ts
const schema = crudPage({
    title: "inventorAnomalies.title",
    name: "stockAbnormalTable",
    searchIdentity: "WStockAbnormalRecord",
    api: api_crud_search_by_warehouseCode,
    columns,
    searchObject: {
        tables:
            "w_stock_abnormal_record r left join (select sku_id, group_concat(distinct bar_code separator ' ') bar_code_list from m_sku_barcode_data group by sku_id) g on r.sku_id = g.sku_id",
        orderBy: "r.create_time desc"
    },
    tableActions: [
        exportButton({
            api: api_crud_search_by_warehouseCode_total,
            filename: "stock_abnormal"
        })
    ],
    rowActions: [
        linkAction({
            label: "button.close",
            confirmTitle: "toast.prompt",
            confirmText: "toast.sureToCloseOrder",
            disabledOn: "${stockAbnormalStatus !== 'NEW'}",
            danger: true,
            api: {
                method: "post",
                url: "/wms/stock/abnormal/record/manualClose",
                data: ["${id}"]
            },
            reload: "stockAbnormalTable"
        }),
        linkAction({
            label: "button.adjustInventoryOrder",
            confirmTitle: "toast.prompt",
            confirmText: "toast.confirmGenerateOrder",
            disabledOn: "${stockAbnormalStatus !== 'NEW'}",
            danger: true,
            api: {
                method: "post",
                url: "/wms/stock/abnormal/record/createAdjustmentOrder",
                data: ["${id}"]
            },
            reload: "stockAbnormalTable"
        })
    ],
    empty: "emptyState.stockAbnormal"
})
```

Only remove `filters,`; keep the row actions verbatim.

- [ ] **Step 4: Type-check**

```bash
cd /d/open-wes/client
npx tsc --noEmit --skipLibCheck 2>&1 | grep "error TS" | grep -vE "src/pages/wms/station/index.tsx|src/utils/polyfill.tsx" && { echo "FAIL: new type errors"; exit 1; } || echo "PASS: only baseline errors"
```

Expected: `PASS: only baseline errors`.

- [ ] **Step 5: Lint**

```bash
cd /d/open-wes/client
npm run lint:amis -- --strict
```

Expected: `0 error(s), 0 warning(s) in ... schema file(s).`

- [ ] **Step 6: Commit**

```bash
cd /d/open-wes
git add client/src/pages/wms/data_center/stock/stock_abnormal.tsx
git commit -m "feat(amis): unify stock abnormal search band" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 11: Stocktake search band

**Files:**
- Modify: `client/src/pages/wms/data_center/stock/stocktake/index.tsx:21-71` and `:73-130`

**Interfaces:**
- Consumes: `crudPage`, `codeColumn`, `statusColumn`, `auditColumns`; `stocktakeOrderStatus` from `@/utils/amis/statusMaps`; `warehouse_area_id`, `warehouse_logic_id`; `yes_no_options`.
- Produces: no exports. Searchable columns in column order: `orderNo`, `stocktakeType`, `stocktakeCreateMethod`, `stocktakeUnitType`, `stocktakeOrderStatus`, `warehouseAreaId`, `warehouseLogicId`, `abnormal`. Collapsed row: `orderNo`, `stocktakeType`.

- [ ] **Step 1: Add `searchable` to the eight columns**

Replace the `columns` array with:

```ts
const columns = [
    {
        name: "id",
        label: "ID",
        hidden: true
    },
    {
        name: "warehouseCode",
        label: "table.warehouse",
        hidden: true
    },
    codeColumn("orderNo", "table.countOrderNumber", { searchable: true }),
    {
        name: "stocktakeType",
        label: "table.orderType",
        type: "mapping",
        source: "${dictionary.StocktakeType}",
        searchable: {
            type: "select",
            source: "${dictionary.StocktakeType}"
        }
    },
    {
        name: "stocktakeCreateMethod",
        label: "table.howItWasCreated",
        type: "mapping",
        source: "${dictionary.StocktakeCreateMethod}",
        searchable: {
            type: "select",
            source: "${dictionary.StocktakeCreateMethod}"
        }
    },
    {
        name: "stocktakeUnitType",
        label: "table.creationType",
        type: "mapping",
        source: "${dictionary.StocktakeUnitType}",
        searchable: {
            type: "select",
            source: "${dictionary.StocktakeUnitType}"
        }
    },
    statusColumn("stocktakeOrderStatus", "table.status", {
        ...stocktakeOrderStatus,
        searchable: {
            type: "select",
            source: "${dictionary.StocktakeOrderStatus}"
        }
    }),
    {
        name: "warehouseAreaId",
        label: "table.warehouseAreaBelongs",
        type: "mapping",
        source: warehouse_area_id,
        searchable: { type: "select", source: warehouse_area_id }
    },
    {
        name: "warehouseLogicId",
        label: "table.logicalAreaName",
        type: "mapping",
        source: warehouse_logic_id,
        searchable: { type: "select", source: warehouse_logic_id }
    },
    {
        name: "abnormal",
        label: "table.exceptionIdentification",
        type: "mapping",
        map: yes_no_options,
        searchable: { type: "select", options: yes_no_options }
    },
    ...auditColumns()
]
```

- [ ] **Step 2: Delete the explicit filter block**

Delete the entire `const filters = [...]` block (currently lines 73–121) and the `filters,` line in the `crudPage` call. The call becomes:

```ts
const schema = crudPage({
    title: "wms.menu.inventoryCheck",
    name: "stocktakeOrderTable",
    searchIdentity: "WStocktakeOrder",
    api: api_crud_search_by_warehouseCode,
    columns,
    orderBy: "id, stocktake_order_status desc",
    pageActions: [add],
    bulkActions: [
        linkAction({
            label: "button.stocktake.order.execute",
            confirmText: "confirm.stocktake.order.execute",
            api: {
                method: "post",
                url: "/wms/stocktake/order/execute",
                data: {
                    warehouseCode: localStorage.getItem("warehouseCode"),
                    orderNos: "${ARRAYMAP(selectedItems, item => item.orderNo)}",
                    taskCount: "${COUNT(selectedItems)}"
                }
            },
            reload: "stocktakeOrderTable"
        })
    ],
    rowActions: [
        detailButton(detailDrawer),
        detailButton(recordDrawer, "button.inventoryRecord")
    ],
    operationWidth: 130,
    empty: "emptyState.stocktake"
})
```

Only remove `filters,`; keep the bulk and row actions verbatim.

- [ ] **Step 3: Type-check**

```bash
cd /d/open-wes/client
npx tsc --noEmit --skipLibCheck 2>&1 | grep "error TS" | grep -vE "src/pages/wms/station/index.tsx|src/utils/polyfill.tsx" && { echo "FAIL: new type errors"; exit 1; } || echo "PASS: only baseline errors"
```

Expected: `PASS: only baseline errors`.

- [ ] **Step 4: Lint**

```bash
cd /d/open-wes/client
npm run lint:amis -- --strict
```

Expected: `0 error(s), 0 warning(s) in ... schema file(s).`

- [ ] **Step 5: Commit**

```bash
cd /d/open-wes
git add client/src/pages/wms/data_center/stock/stocktake/index.tsx
git commit -m "feat(amis): unify stocktake search band" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 12: Remove explicit-filter support from the preset

**Files:**
- Modify: `client/src/utils/amis/presets.ts:286-287`, `:347-370`
- Modify: `client/src/utils/amis/useConsoleShortcuts.ts:13-23`

**Interfaces:**
- Consumes: every page already migrated to `searchable` in Tasks 2–11.
- Produces: `CrudPageOptions` no longer has `filters`; `crudPage` always emits `autoGenerateFilter: { columnsNum: 3, showBtnToolbar: true }`.

- [ ] **Step 1: Delete the `filters` option**

In `client/src/utils/amis/presets.ts`, delete these two lines from `CrudPageOptions`:

```ts
    /** Explicit filter controls. Omit only when there are four or fewer searchable columns. */
    filters?: SchemaLike[]
```

- [ ] **Step 2: Replace the conditional filter branch**

Replace the entire ternary:

```ts
        ...(options.filters
            ? {
                  filterTogglable: options.filters.length > 4,
                  filterDefaultVisible: true,
                  filter: {
                      title: "common.filter",
                      mode: "inline",
                      body: options.filters,
                      actions: [
                          { type: "reset", label: "button.reset" },
                          {
                              type: "submit",
                              level: "primary",
                              label: "common.search"
                          }
                      ]
                  }
              }
            : {
                  autoGenerateFilter: {
                      columnsNum: 3,
                      showBtnToolbar: true
                  }
              }),
```

with:

```ts
        // Query controls come from columns marked `searchable`; column order
        // sets the collapsed priority. See docs/standards/amis.md §5.
        autoGenerateFilter: {
            columnsNum: 3,
            showBtnToolbar: true
        },
```

- [ ] **Step 3: Remove the explicit-filter selectors from the keyboard shortcut**

In `client/src/utils/amis/useConsoleShortcuts.ts`, replace:

```ts
/**
 * AMIS renders an explicit `filter` panel inside `.cxd-Crud-filter`, but
 * auto-generated filters (the default in `crudPage`) live in the table's
 * searchable form panel. Cover both so `/` works on every CRUD page.
 */
const SEARCH_INPUT_SELECTOR = [
    ".cxd-Crud-filter input:not([type='hidden']):not([disabled])",
    ".cxd-Crud-filter textarea:not([disabled])",
    ".cxd-Table-searchableForm input:not([type='hidden']):not([disabled])",
    ".cxd-Table-searchableForm textarea:not([disabled])"
].join(", ")
```

with:

```ts
/**
 * `crudPage` renders its query controls in AMIS's auto-generated searchable
 * form; there is no explicit `filter` panel anymore. `/` focuses the first
 * visible control in that form.
 */
const SEARCH_INPUT_SELECTOR = [
    ".cxd-Table-searchableForm input:not([type='hidden']):not([disabled])",
    ".cxd-Table-searchableForm textarea:not([disabled])"
].join(", ")
```

- [ ] **Step 4: Confirm no page still passes `filters`**

```bash
cd /d/open-wes/client
grep -rn "\bfilters\b" src/pages src/utils/amis/presets.ts | grep -v "ExecutionsTab.tsx" || echo "PASS: no page passes filters"
```

Expected: `PASS: no page passes filters`. (`ExecutionsTab.tsx` contains the unrelated phrase "the stats window is independent of the list filters" in a comment; it is filtered out.)

Then run the targeted check that must be empty:

```bash
grep -rn "options\.filters\|filterTogglable\|cxd-Crud-filter" src || echo "PASS: no explicit-filter code"
```

Expected: `PASS: no explicit-filter code`.

- [ ] **Step 5: Type-check**

```bash
cd /d/open-wes/client
npx tsc --noEmit --skipLibCheck 2>&1 | grep "error TS" | grep -vE "src/pages/wms/station/index.tsx|src/utils/polyfill.tsx" && { echo "FAIL: new type errors"; exit 1; } || echo "PASS: only baseline errors"
```

Expected: `PASS: only baseline errors`. A `filters` property passed by a page would fail here.

- [ ] **Step 6: Lint**

```bash
cd /d/open-wes/client
npm run lint:amis -- --strict
```

Expected: `0 error(s), 0 warning(s) in ... schema file(s).`

- [ ] **Step 7: Commit**

```bash
cd /d/open-wes
git add client/src/utils/amis/presets.ts client/src/utils/amis/useConsoleShortcuts.ts
git commit -m "refactor(amis): remove explicit filter support from crudPage" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 13: Document the unified search band

**Files:**
- Modify: `docs/standards/amis.md:196-218` (§5)
- Modify: `docs/standards/amis.md:465-469` (Wave 2 note)

**Interfaces:**
- Consumes: the behaviour implemented in Tasks 1–12.
- Produces: no code. The standard now describes one search model.

- [ ] **Step 1: Replace §5**

Replace the whole of `## 5. Filters` (from the heading through the `crudPage` sentence immediately before `## 6. Forms and drawers`) with:

```md
## 5. Search bands

Every list page uses the same search band: AMIS `autoGenerateFilter`, emitted
by `crudPage()` with `columnsNum: 3` and `showBtnToolbar: true`. It provides
展开/收起, 设置查询字段, 查询, and 重置. Do not pass an explicit `filters`
array and do not set `autoGenerateFilter` per page.

### Declaring a query field

A query field is a column marked `searchable`:

| UI control | Column definition |
|---|---|
| Text | `searchable: true` |
| Select / dictionary | `searchable: { type: "select", source: "${dictionary.X}" }` or `options` |
| Date range | `searchable: { type: "input-date-range", valueFormat: "x" }` |

The column's label is the query label; every query field has a visible label.
Column order determines the collapsed priority: the first two active
`searchable` columns fill the collapsed row. 设置查询字段 lets each operator
disable fields, and AMIS persists that selection per table page path.

### Audit dates and hidden columns

Use `auditColumns({ createdSearchable: { type: "input-date-range", valueFormat: "x" } })`
for a creation-time query. The column may be hidden by default; hidden columns
still reach the backend through `showColumns`. A field with no display column
can be declared as `{ name, label, hidden: true, searchable: ... }` and kept in
`showColumns`.

### Search semantics

- Search submits on Enter and on 查询; 重置 restores the default state.
- Date ranges use `input-date-range` with `valueFormat: "x"`.
- Do not set `filterTogglable` or an explicit filter `actions`; AMIS manages
  the auto-filter toolbar.
- Query values are ordinary search params, identical to what the column names
  produced before the migration.
```

- [ ] **Step 2: Mark the old Wave 2 rule superseded**

Replace the Wave 2 paragraph:

```md
**Wave 2 — page anatomy (done)**

Move create actions to the page toolbar, export/import to the CRUD toolbar,
replace auto-generated filters with explicit filters where there are more than
four fields, add empty states, and set drawer sizes/titles.
```

with:

```md
**Wave 2 — page anatomy (done; filter rule superseded 2026-11-02)**

Move create actions to the page toolbar, export/import to the CRUD toolbar,
add empty states, and set drawer sizes/titles. The old rule that replaced
auto-generated filters with explicit filters above four fields is superseded
by the unified search band in §5; every list page now uses `searchable`
columns and `crudPage()` always emits `autoGenerateFilter`.
```

- [ ] **Step 3: Check the document for stale terms**

```bash
cd /d/open-wes
grep -n "explicit filters\|filterTogglable\|more than four" docs/standards/amis.md
```

Expected: the Wave 2 note now says the rule is superseded; no other line presents explicit filters as current practice.

- [ ] **Step 4: Commit**

```bash
cd /d/open-wes
git add docs/standards/amis.md
git commit -m "docs(amis): document the unified search band" -m "Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 14: Full regression sweep

**Files:**
- No repository changes.

**Interfaces:**
- Consumes: all tasks above.
- Produces: a verification record. No commit.

- [ ] **Step 1: Run the static gates**

```bash
cd /d/open-wes/client
npm run lint:amis -- --strict
```

Expected: `0 error(s), 0 warning(s) in ... schema file(s).`

```bash
npx tsc --noEmit --skipLibCheck 2>&1 | grep "error TS" | grep -vE "src/pages/wms/station/index.tsx|src/utils/polyfill.tsx" && { echo "FAIL: new type errors"; exit 1; } || echo "PASS: only baseline errors"
```

Expected: `PASS: only baseline errors`.

```bash
grep -rn "options\.filters\|filterTogglable\|cxd-Crud-filter" src || echo "PASS: no explicit-filter code"
```

Expected: `PASS: no explicit-filter code`.

- [ ] **Step 2: Capture the 1440px WMS sweep**

Use the `capturing-browser-screenshots` skill. Create a temporary working directory outside the repository, for example `C:/Users/niekang/AppData/Local/Temp/openwes-search-sweep`, and put this steps file there as `wms-1440.json`:

```json
{
  "viewport": { "width": 1440, "height": 900, "deviceScaleFactor": 1 },
  "timeout": 30000,
  "steps": [
    { "navigate": "http://localhost:3000/login" },
    { "wait": 1000 },
    {
      "eval": "fetch('http://localhost:8090/user/api/auth/signin', { method: 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ username: '{{ENV:OPENWES_USER}}', password: '{{ENV:OPENWES_PASS}}' }) }).then((r) => r.json()).then((d) => { if (!d.token) throw new Error('signin failed: ' + JSON.stringify(d)); localStorage.setItem('ws_token', d.token); localStorage.setItem('Authorization', 'admin'); localStorage.setItem('i18nextLng', 'zh-CN'); return 'signed in'; })"
    },
    { "navigate": "http://localhost:3000/wms/dashboard" },
    { "waitForText": "入库管理" },
    { "navigate": "http://localhost:3000/wms/config-center/print-record" },
    { "waitForSelector": ".cxd-Table-searchableForm" },
    { "wait": 800 },
    { "removeSelector": "#webpack-dev-server-client-overlay" },
    { "screenshot": "01-print-record-1440" },
    { "navigate": "http://localhost:3000/wms/config-center/warehouse/location-management" },
    { "waitForSelector": ".cxd-Table-searchableForm" },
    { "wait": 800 },
    { "removeSelector": "#webpack-dev-server-client-overlay" },
    { "screenshot": "02-location-1440" },
    { "navigate": "http://localhost:3000/wms/config-center/warehouse/warehouse-management" },
    { "waitForSelector": ".cxd-Table-searchableForm" },
    { "wait": 800 },
    { "removeSelector": "#webpack-dev-server-client-overlay" },
    { "screenshot": "03-warehouse-1440" },
    { "navigate": "http://localhost:3000/wms/data-center/receiving-manage" },
    { "waitForSelector": ".cxd-Table-searchableForm" },
    { "wait": 800 },
    { "removeSelector": "#webpack-dev-server-client-overlay" },
    { "screenshot": "04-accept-order-1440" },
    { "navigate": "http://localhost:3000/wms/data-center/inbound/inbound-plan-order" },
    { "waitForSelector": ".cxd-Table-searchableForm" },
    { "wait": 800 },
    { "removeSelector": "#webpack-dev-server-client-overlay" },
    { "screenshot": "05-inbound-plan-1440" },
    { "navigate": "http://localhost:3000/wms/data-center/inbound/putaway-task" },
    { "waitForSelector": ".cxd-Table-searchableForm" },
    { "wait": 800 },
    { "removeSelector": "#webpack-dev-server-client-overlay" },
    { "screenshot": "06-putaway-1440" },
    { "navigate": "http://localhost:3000/wms/data-center/stock/container-stock" },
    { "waitForSelector": ".cxd-Table-searchableForm" },
    { "wait": 800 },
    { "removeSelector": "#webpack-dev-server-client-overlay" },
    { "screenshot": "07-container-stock-1440" },
    { "navigate": "http://localhost:3000/wms/data-center/stock/stock-abnormal" },
    { "waitForSelector": ".cxd-Table-searchableForm" },
    { "wait": 800 },
    { "removeSelector": "#webpack-dev-server-client-overlay" },
    { "screenshot": "08-stock-abnormal-1440" },
    { "navigate": "http://localhost:3000/wms/data-center/stock/stocktake" },
    { "waitForSelector": ".cxd-Table-searchableForm" },
    { "wait": 800 },
    { "removeSelector": "#webpack-dev-server-client-overlay" },
    { "screenshot": "09-stocktake-1440" }
  ]
}
```

Run it from the temporary directory so the relative output paths resolve there:

```bash
cd /c/Users/niekang/AppData/Local/Temp/openwes-search-sweep
export OPENWES_USER=...
export OPENWES_PASS=...
node C:/Users/niekang/.claude/skills/capturing-browser-screenshots/scripts/cdp-capture.mjs \
  --steps wms-1440.json --out shots/1440
```

- [ ] **Step 3: Capture the plugin page at 1440px**

The plugin app is not under `/wms`; enter it through the dashboard app switcher:

```json
{
  "viewport": { "width": 1440, "height": 900, "deviceScaleFactor": 1 },
  "timeout": 30000,
  "steps": [
    { "navigate": "http://localhost:3000/login" },
    { "wait": 1000 },
    {
      "eval": "fetch('http://localhost:8090/user/api/auth/signin', { method: 'POST', headers: { 'content-type': 'application/json' }, body: JSON.stringify({ username: '{{ENV:OPENWES_USER}}', password: '{{ENV:OPENWES_PASS}}' }) }).then((r) => r.json()).then((d) => { if (!d.token) throw new Error('signin failed: ' + JSON.stringify(d)); localStorage.setItem('ws_token', d.token); localStorage.setItem('Authorization', 'admin'); localStorage.setItem('i18nextLng', 'zh-CN'); return 'signed in'; })"
    },
    { "navigate": "http://localhost:3000/wms/dashboard" },
    { "waitForText": "插件平台" },
    { "clickText": "插件平台", "scope": ".ant-menu-horizontal" },
    { "waitForText": "插件标识" },
    { "waitForSelector": ".cxd-Table-searchableForm" },
    { "wait": 800 },
    { "removeSelector": "#webpack-dev-server-client-overlay" },
    { "screenshot": "10-plugin-1440" }
  ]
}
```

Save this as `plugin-1440.json` and run it from the same temporary directory:

```bash
cd /c/Users/niekang/AppData/Local/Temp/openwes-search-sweep
export OPENWES_USER=...
export OPENWES_PASS=...
node C:/Users/niekang/.claude/skills/capturing-browser-screenshots/scripts/cdp-capture.mjs \
  --steps plugin-1440.json --out shots/1440
```

- [ ] **Step 4: Review the 1440px round**

Build one contact sheet from the ten 1440px screenshots. Create `contact-1440.html` in the temporary directory:

```html
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<style>
  html, body { margin: 0; padding: 8px; background: #111; }
  .grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 8px; }
  img { width: 100%; display: block; }
  figcaption { color: #ddd; font: 12px/1.4 monospace; padding: 4px 0 8px; }
</style>
</head>
<body>
<div class="grid">
  <figure><img src="shots/1440/01-print-record-1440.jpg"><figcaption>01 print record</figcaption></figure>
  <figure><img src="shots/1440/02-location-1440.jpg"><figcaption>02 location</figcaption></figure>
  <figure><img src="shots/1440/03-warehouse-1440.jpg"><figcaption>03 warehouse</figcaption></figure>
  <figure><img src="shots/1440/04-accept-order-1440.jpg"><figcaption>04 accept order</figcaption></figure>
  <figure><img src="shots/1440/05-inbound-plan-1440.jpg"><figcaption>05 inbound plan</figcaption></figure>
  <figure><img src="shots/1440/06-putaway-1440.jpg"><figcaption>06 put-away</figcaption></figure>
  <figure><img src="shots/1440/07-container-stock-1440.jpg"><figcaption>07 container stock</figcaption></figure>
  <figure><img src="shots/1440/08-stock-abnormal-1440.jpg"><figcaption>08 stock abnormal</figcaption></figure>
  <figure><img src="shots/1440/09-stocktake-1440.jpg"><figcaption>09 stocktake</figcaption></figure>
  <figure><img src="shots/1440/10-plugin-1440.jpg"><figcaption>10 plugin</figcaption></figure>
</div>
</body>
</html>
```

Save this as `contact-1440-steps.json` and capture it with the same tool:

```json
{
  "viewport": { "width": 1600, "height": 1200, "deviceScaleFactor": 1 },
  "steps": [
    { "navigate": "file:///C:/Users/niekang/AppData/Local/Temp/openwes-search-sweep/contact-1440.html" },
    { "wait": 500 },
    { "screenshot": { "name": "contact-1440", "fullPage": true } }
  ]
}
```

```bash
cd /c/Users/niekang/AppData/Local/Temp/openwes-search-sweep
node C:/Users/niekang/.claude/skills/capturing-browser-screenshots/scripts/cdp-capture.mjs \
  --steps contact-1440-steps.json --out .
```

Read `contact-1440.jpg`. Expected, for every row:

| Page | Collapsed `searchable` columns | Expanded controls |
|---|---|---|
| plugin | `pluginKey`, `name` | 4 |
| print record | `module`, `printNode` | 7 |
| location | `warehouseAreaId`, `warehouseLogicId` | 5 |
| warehouse | `warehouseCode`, `warehouseName` | 8 |
| accept order | `identifyNo`, `acceptOrderStatus` | 3 |
| inbound plan | `lpnCode`, `customerOrderNo` | 9 |
| put-away | `containerCode`, `containerSpecCode` | 8 |
| container stock | `skuCode`, `barcode` | 6 |
| stock abnormal | `abnormalOrderNo`, `stockAbnormalType` | 6 |
| stocktake | `orderNo`, `stocktakeType` | 8 |

Also confirm the toolbar (设置查询字段 / 查询 / 重置 / 展开) is inline with the fields at 1440, no page shows a locale key, and pages with data render rows rather than a permanent empty state.

- [ ] **Step 5: Expand and check the chooser on every page**

For all ten pages, use the capture tool to click `展开`, then `设置查询字段`, and capture the expanded band with the chooser open. Group five pages per contact sheet and read two sheets.

Expected for every page:
- The expanded band has the control count from the table in Step 4.
- The chooser lists exactly the searchable field labels for that page; the collapsed band shows the first two.
- Toggling one field off in the chooser removes it from the band; toggling it back on restores it. Check this once on the plugin page and once on the stocktake page; the chooser persists the selection per page path.

- [ ] **Step 6: Check the narrow-width layout at 1024px**

Repeat Steps 2–4 with these changes:
- top-level `"viewport"`: `{ "width": 1024, "height": 768, "deviceScaleFactor": 1 }`
- every screenshot suffix `-1024`
- contact sheet filenames `contact-1024.html` and `contact-1024.jpg`

Expected:
- The collapsed toolbar is on its own right-aligned row on every page.
- The two collapsed inputs are at least ~200px wide; none is squeezed to a sliver.
- Clicking 展开 still renders three controls per row with no mid-row wrap.
- No page overflows horizontally.

- [ ] **Step 7: Check 1366px and 1200px**

Repeat the 1440px sweep at 1366×768 and at 1200×768, producing `contact-1366.jpg` and `contact-1200.jpg`.

Expected:
- 1366: same collapsed/expanded counts as 1440; the toolbar is inline.
- 1200: the toolbar is inline (the narrow-width rule ends at 1199.98px); the layout matches 1440.
- No page overflows or clips the toolbar.

All ten pages are captured at all four viewports (40 screenshots); do not sample. A page that redirects to the dashboard means the deep link is not supported — reach that page through the left sidebar and keep the same screenshot name, then note the extra click in the verification record.

- [ ] **Step 8: Functional check — query and reset on every page**

For each page, query with the first text searchable field, using a value visible in the first loaded row, then reset:

| Page | Field to query |
|---|---|
| plugin | `pluginKey` |
| print record | `templateCode` |
| location | `locationCode` |
| warehouse | `warehouseCode` |
| accept order | `identifyNo` |
| inbound plan | `lpnCode` |
| put-away | `containerCode` |
| container stock | `skuCode` |
| stock abnormal | `abnormalOrderNo` |
| stocktake | `orderNo` |

Expected for each page:
- 查询 leaves at least the matching row and drops non-matching rows.
- The request to `/search/search` (or the custom plugin endpoint) carries the field name and value where the page uses a server endpoint.
- 重置 clears the field and restores the unfiltered list.
- No 500 response and no empty page caused by a dropped parameter.

Then check the three data paths more closely:

1. **Inbound plan order** (server search + dictionary select + number field): also query `inboundPlanOrderStatus`, `storageType`, and `skuKindNum`; confirm each request carries the parameter.
2. **Container stock** (joined-table search): confirm rows still render the joined SKU/barcode/owner/area columns after a `skuCode` query and reset.
3. **Plugin list** (client-side `loadDataOnce`): confirm a query filters the list without a new request to `/plugin-platform/scriptPlugin/list` — the endpoint is fetched once on load because `loadDataOnceFetchOnFilter` is false.

- [ ] **Step 9: Functional check — audit date ranges**

On put-away task, container stock, and stock abnormal:
1. Expand the band.
2. Confirm the creation-time range control is present (it comes from `auditColumns({ createdSearchable })` even though the column is hidden by default).
3. Submit a range and confirm the request carries `createTime` endpoints (or the same range parameters the old explicit filter sent).
4. Reset.

Expected: the date range filters both the request and the rows; no console error.

- [ ] **Step 10: Check console, chrome, and surrounding behaviour**

For every page captured, confirm:
- no red console error,
- no untranslated locale key (a dotted key rendered as text),
- no webpack overlay,
- pagination still switches pages and keeps the current query values,
- the empty state still renders when a query matches nothing (`emptyState.*` copy, not a blank table),
- the default sort is unchanged on print record and warehouse management (`orderBy` still applies),
- export on inbound plan order, accept order, container stock, stock abnormal, and put-away task still downloads a file and sends the current query params.

Record the result in the task report. Do not commit anything in this task.

---

## Self-Review

**Spec coverage:**

- Auto-generated search only — Task 12 removes `filters`; Tasks 2–11 add `searchable`.
- Three columns per row — Task 12 emits `columnsNum: 3`; Tasks 1 and 14 verify.
- Narrow-width toolbar rule — Task 1 adds it; Task 14 checks 1024/1200.
- Field order follows column order — every page task declares `searchable` on the existing column object; the collapsed rows are listed per task.
- `searchable` forms — text, select, date range, and `auditColumns({ createdSearchable })` are used across Tasks 2–11.
- Dead accept-order filters — Task 6 removes `ownerCode`, `skuCode`, `skuName`.
- Documentation — Task 13 rewrites §5 and marks Wave 2 superseded.
- Verification — Task 14 runs static gates and the browser sweep at 1440, 1366, 1024, and 1200.

**Placeholder scan:** No TBD/TODO/"similar to" implementation steps remain. The only "unchanged" markers are explicit instructions to keep surrounding code verbatim, with the changed lines shown.

**Type consistency:** `searchable` is part of `ColumnOptions` in `presets.ts`; `codeColumn`, `numberColumn`, `dateTimeColumn`, and `statusColumn` all pass it through `withOptions`. `auditColumns` already accepts `createdSearchable`. `CrudPageOptions.filters` is removed only in Task 12, after every page stops passing it, so every intermediate commit type-checks.
