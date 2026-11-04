# Open WES List Page UX/UI Unification Design

**Date:** 2026-11-02
**Status:** Proposed
**Scope:** AMIS list pages under `client/src/pages`
**Related standards:** `docs/standards/amis.md` §1–§14, `docs/standards/frontend.md`

## Overview

The operations-console visual language is already defined: colour, type, spacing,
status semantics, table density, keyboard shortcuts, and a dormant dark token
layer. The remaining inconsistency is structural. Every list page still composes
its own page header, filter band, CRUD toolbar, column set, row actions, and
drawer, so two pages that should feel identical behave differently.

This design converges every in-scope list page onto one preset-driven anatomy.
It does not introduce a new visual identity, palette, or font. The list page is
treated as the primary scanning surface of a warehouse console: the table is the
hero, identifiers are monospaced and copyable, status is a dot plus a word, and
every other element is quiet.

## Current State

### Adoption audit (2026-11-02)

| Dimension | Current state |
|---|---|
| Page skeleton | 49 schema pages use `schema2component`/`AMisRenderer`; only `user/role_management.tsx` composes `crudPage()`; 48 files contain a raw `type: "crud"` |
| Audit columns | 26 files still import `create_update_columns`, so audit columns are always visible |
| Status columns | 4 files use `statusColumn`; most use `type: "mapping"` with a dictionary source and no status dot |
| Data-display helpers | 8 files use any of `codeColumn` / `numberColumn` / `dateTimeColumn` / `remarkColumn` / `auditColumns` |
| Drawers | 1 file uses `formDrawer`; most drawers are hand-built, with inconsistent footer actions and widths |
| Filters | 33 files use `autoGenerateFilter`; explicit `filter` panels and reset behaviour are inconsistent |
| Lint | `npm run lint:amis` reports 0 errors and 0 warnings, because it checks schema hygiene but not preset adoption |

Representative drift:

- `wms/data_center/inbound/accept_order.tsx` — legacy column definitions,
  `type: "mapping"` status, `create_update_columns`, no page-level primary
  action, and a second CRUD inside a detail dialog.
- `wms/data_center/inbound/inbound_plan_order/index.tsx` — partially converted:
  `codeColumn` / `numberColumn` / `auditColumns` are used, but status is still
  `type: "mapping"` and the remark column is copied inline instead of using
  `remarkColumn`.
- `wms/config_center/basic/owner_management.tsx` — hand-built page, drawer, and
  columns; no `formDrawer`, no `statusColumn`, no shared empty state.

### Page inventory

40 top-level list pages are in scope. `user/role_management.tsx` already uses
`crudPage`; the other 39 are migrated in Wave 5.

| Area | Pages | Pattern |
|---|---|---|
| `wms/data_center` (12) | `inbound/inbound_plan_order/index`, `inbound/accept_order`, `inbound/empty_container_inbound`, `inbound/put_away_task`, `outbound/outbound_plan_order/index`, `outbound/picking_order`, `outbound/empty_container_outbound`, `stock/stocktake/index`, `stock/stock_adjust`, `stock/stock_records`, `stock/container_stock`, `stock/stock_abnormal` | 5 create, 7 query/action |
| `wms/config_center/basic` (8) | `batch_attribute_management`, `container_management`, `container_spec_management`, `owner_management`, `put_wall_management`, `sku_management`, `transfer_container_management`, `work_station` | 7 create, 1 query/bulk action |
| `wms/config_center/print` (4) | `print_config`, `print_record`, `print_rule`, `print_template` | 3 create, 1 read-only log |
| `wms/config_center/rule` (2) | `barcode_parse_rule`, `dictionary_management` | 2 create |
| `wms/config_center/warehouse` (5) | `location_management`, `warehouse_area`, `warehouse_area_group`, `warehouse_logic`, `warehouse_management` | 5 create |
| `user` (4) | `login_log`, `menu_management`, `role_management`, `user_management` | 3 create (role already converted), 1 read-only log |
| `api_platform` (3) | `api_keys`, `api_log`, `api_management` | 2 create, 1 read-only log |
| `plugin_platform` (2) | `list/index`, `audit/index` | 1 query/action, 1 read-only log |

Two pages contain a second CRUD inside a detail dialog/drawer:
`accept_order`, `put_away_task`, `picking_order`, and `stock_adjust`. The nested
table is migrated to `nestedTable()`; it is not counted as a top-level page.

### Exceptions

These are not top-level list pages and never receive a `crudPage` conversion:

- Detail drawers and record views: `inbound_plan_order/detail.tsx`,
  `outbound_plan_order/detail.ts`, `stocktake/detail.tsx`,
  `stocktake/record.tsx`.
- Creation wizards/forms: `stocktake/add.tsx`, `outbound_plan_order/add.tsx`.
- Embedded workstation tables: the five `src/pages/wms/station/**` components
  that contain a page + CRUD schema.
- Dashboards: `api_platform/api_dashboard.tsx`, `wms/dashboard/index.tsx`,
  `data_platform/index.tsx` and its widgets.
- Plugin workbench tabs and non-list components under
  `plugin_platform/workbench/**`, plus `DiffViewer`, `ScriptEditor`,
  `DeclarationTable`, `ConfigSchemaTable`, and `ValidationReportView`.
- `wms/config_center/rule/system_config.tsx` (a configuration form, not a list).

The five embedded workstation tables are permanent entries in the lint
allowlist. All other exceptions are excluded by the rule's `type: "page"` +
`type: "crud"` heuristic and do not need allowlist entries.

## Goals and Non-Goals

### Goals

- Every in-scope list page uses `crudPage()` and the shared column/drawer
  helpers.
- A list page has one primary action, one filter band, one CRUD toolbar, one
  table, and one footer.
- Identifiers are monospaced and copyable; numbers are right-aligned; status is
  a dot plus a translated word; audit columns are hidden by default.
- Destructive confirmations name the object and the consequence.
- A lint ratchet prevents new raw list pages and shrinks the legacy allowlist
  as waves land.
- Existing API contracts, routes, permissions, and backend behaviour are
  unchanged.

### Non-Goals

- No new palette, typography, iconography, or motion language.
- No dark-mode activation; the token layer stays dormant.
- No new product features (saved filter views, custom column presets beyond the
  existing toggler, bulk-edit redesign).
- No backend or database changes.
- No mass-formatting or renaming of unrelated schema code.

## Design

### 1. The five-band list page

Every in-scope page renders the same anatomy, as defined in
`docs/standards/amis.md` §3:

```
┌──────────────────────────────────────────────────────────────┐
│ Page title                                      [+ 新增]     │  page.toolbar
├──────────────────────────────────────────────────────────────┤
│ [ filter 1 ] [ filter 2 ] [ filter 3 ]      [查询] [重置]     │  crud.filter
├──────────────────────────────────────────────────────────────┤
│ reload  [导入] [导出]                                        │  crud.headerToolbar
├──────────────────────────────────────────────────────────────┤
│ identifier │ name │ qty │ status │ audit    │ actions         │  table
├──────────────────────────────────────────────────────────────┤
│ selected 3  [批量操作]      10/page  total 128  < 1 2 3 >     │  crud.footerToolbar
└──────────────────────────────────────────────────────────────┘
```

Rules that the preset enforces or that reviewers check:

- The page title is the only H1. The page toolbar holds the single primary
  create action, right-aligned. Pages without a create action pass
  `pageActions: []`; they do not invent one.
- Import and export are secondary utilities in the CRUD toolbar, after
  `reload`.
- Pagination always contains `switch-per-page`, `statistics`, `pagination`.
- The filter band uses explicit filters when there are more than four fields;
  fewer than four may use `autoGenerateFilter`. Explicit filters always include
  the reset and query actions emitted by `crudPage`.
- The table fills the console viewport (`autoFillHeight: true`).
- Empty states explain the next action. A filter that returns nothing shows a
  "no matching records" message; an empty table shows an invitation to create
  or import.

### 2. Preset contract

All changes live in `client/src/utils/amis/presets.ts` and
`client/src/utils/amis/statusMaps.ts`. Pages do not define their own page
boilerplate, column maps, or drawer footers.

#### `crudPage`

```ts
export interface CrudPageOptions {
    // existing fields unchanged
    title: string
    name: string
    searchIdentity: string
    columns: SchemaLike[]
    showColumns?: SchemaLike[]
    api: ApiLike
    orderBy?: string
    pageActions?: SchemaLike[]
    tableActions?: SchemaLike[]
    filters?: SchemaLike[]
    rowActions?: SchemaLike[]
    bulkActions?: SchemaLike[]
    selectable?: boolean
    operationWidth?: number

    /** Required: every list page explains its empty state. */
    empty: string | SchemaLike

    /** Optional page data merged over the default dictionary. */
    data?: Record<string, unknown>
}
```

The preset emits `data: { dictionary: "${ls:dictionary}", ...options.data }`.
No other page-level data is hardcoded.

#### Column helpers

- `codeColumn` for LPN, SKU, container, order, and other operational
  identifiers.
- `numberColumn` for quantities and measures.
- `dateTimeColumn` for timestamps; minute precision by default, seconds only for
  event/log tables.
- `statusColumn` for every status field. Labels are locale keys; tones come from
  a shared map.
- `remarkColumn` for long free text.
- `auditColumns()` replaces `create_update_columns`; audit columns are hidden by
  default and revealed from the column toggler. Log pages pass
  `{ visible: true }`.

#### `deleteButton`

`confirmText` becomes required and must name the object and consequence. The
generic `toast.sureDelete` copy is no longer acceptable for list pages.

#### `linkAction`

New helper for custom row actions:

```ts
linkAction({
    label: "button.close",
    api: api_close,
    confirmText: "confirm.close",
    danger: true,          // adds owes-action--danger
    reload: "OrderTable"
})
```

It always renders `type: "button"`, `level: "link"`, and `className:
"owes-action"` (plus the danger modifier), so custom actions stop being filled
buttons or bare links.

#### `importButton`

New helper that wraps an import dialog:

```ts
importButton({
    dialog: importDialog,
    label: "button.import"
})
```

It renders the trigger in the CRUD toolbar with the upload icon. The dialog
body remains page-specific.

#### `nestedTable`

New helper for a second CRUD inside a detail drawer/dialog:

```ts
nestedTable({
    name: "AcceptOrderDetailTable",
    api: detailSearchApi,
    searchIdentity: "WAcceptOrderDetail",
    columns: detailColumns,
    empty: "emptyState.acceptOrderDetail"
})
```

It sets `syncLocation: false`, the standard footer toolbar, and the same column
helpers as a top-level table, but omits the page header and filter band.

#### `formDrawer`

Continue to use `formDrawer` for every create/edit drawer. No API change is
required for this migration. Reviewers enforce the existing rules: a title that
names the entity and intent, `md` for one column and `lg` for two, footer
actions on the drawer, and `closeOnOutside: false` for long forms.

### 3. Status maps

`client/src/utils/amis/statusMaps.ts` owns the label/tone map for each enum.
Pages import the map and call:

```ts
statusColumn("acceptOrderStatus", "table.status", acceptOrderStatus)
```

Initial maps required by the in-scope pages:

| Map | Enum / dictionary source |
|---|---|
| `acceptOrderStatus` | `AcceptOrderStatus` |
| `inboundPlanOrderStatus` | `InboundPlanOrderStatus` |
| `putAwayTaskStatus` | `PutAwayTaskStatus` |
| `emptyContainerOutboundStatus` | `EmptyContainerOutboundOrderStatus` |
| `emptyContainerOutboundDetailStatus` | `EmptyContainerOutboundDetailStatus` |
| `outboundPlanOrderStatus` | `OutboundPlanOrderStatus` |
| `pickingOrderStatus` | `PickingOrderStatus` |
| `stockAdjustmentOrderStatus` | `StockAdjustmentOrderStatus` |
| `stockAbnormalStatus` | `StockAbnormalStatus` |
| `stocktakeOrderStatus` | `StocktakeOrderStatus` |
| `stocktakeRecordStatus` | `StocktakeRecordStatus` |
| `transferContainerStatus` | `TransferContainerStatus` |
| `printStatus` | `PrintStatusEnum` |
| `apiLogStatus` | `ApiLogStatus` |
| `putWallStatus` | dictionary-backed status |
| `locationStatus` | dictionary-backed status |
| `containerStatus` | dictionary-backed status |

Each map's labels are locale keys added to both `zh-cn.json` and `en-us.json`.
The lint rule catches a missing key. A map is added only when a page needs it;
the file is append-only within the migration.

### 4. Drawers, row actions, and bulk actions

- Create/edit drawers use `formDrawer`; the drawer title names the entity and
  intent (`新增货主`, not `新增`).
- Row actions are text links. At most three visible actions; more than three
  moves into a dropdown.
- Destructive row actions use `linkAction({ danger: true })` and a specific
  confirmation. Never use a filled `level: "danger"` button in a row.
- Bulk actions live in the selection bar, state the count, and use a verb.
  Irreversible bulk actions require an explicit confirmation.
- Custom actions keep their existing API payloads; only their presentation and
  placement change.

### 5. Enforcement: ratchet lint

Add rule `list-page-preset` to `client/scripts/check-amis-schema.mjs`:

- A file is a candidate when it contains both `type: "page"` and
  `type: "crud"`.
- The file passes when it calls `crudPage(`.
- Otherwise, the rule reads `client/scripts/amis-list-page-exceptions.json`. A
  candidate found there is skipped; a candidate missing there is an error.

Allowlist format:

```json
{
  "version": 1,
  "updated": "2026-11-02",
  "entries": [
    {
      "file": "src/pages/wms/station/instances/receive/custom-actions/CallContainer/Content.tsx",
      "reason": "Embedded workstation table; not a top-level list page.",
      "permanent": true
    },
    {
      "file": "src/pages/wms/config_center/basic/owner_management.tsx",
      "reason": "Pending Wave 5C migration.",
      "wave": "5C"
    }
  ]
}
```

The allowlist starts with the 39 unconverted pages and the five permanent
workstation entries. Each wave removes its migrated entries. Wave 5D leaves only
the five `"permanent": true` entries, and `npm run lint:amis -- --strict` must
pass.

A second warning rule flags `create_update_columns` imports in candidate files
and names `auditColumns()` as the replacement. It becomes an error once Wave 5D
removes the last in-scope use.

### 6. Migration waves

#### Phase 0 — commit the pending batch

The working tree already contains 51 modified files from Waves 0–3 and an
untracked `client/scripts/check-amis-schema.mjs`. Commit that batch separately
before Wave 5 so the migration diff is reviewable and a regression can be
attributed to one batch.

#### Wave 5A — pilot (2 pages)

- `wms/config_center/basic/owner_management.tsx` (standard create page)
- `api_platform/api_log.tsx` (read-only log)

These two prove the create-page and log-page variants, the required `empty`
copy, the status maps, `formDrawer`, and the lint allowlist mechanics before the
bulk conversion.

#### Wave 5B — data center (12 pages)

Inbound: `inbound_plan_order/index`, `accept_order`, `empty_container_inbound`,
`put_away_task`. Outbound: `outbound_plan_order/index`, `picking_order`,
`empty_container_outbound`. Stock: `stocktake/index`, `stock_adjust`,
`stock_records`, `container_stock`, `stock_abnormal`.

This wave includes the four two-CRUD pages, the task lists with custom actions,
and the read-only stock log. It is split into inbound/outbound and stock commits
so the nested-table and action patterns are reviewed before the second half.

#### Wave 5C — configuration center (18 pages)

Basic: `batch_attribute_management`, `container_management`,
`container_spec_management`, `put_wall_management`, `sku_management`,
`transfer_container_management`, `work_station` (owner management already landed
in 5A). Print: `print_config`, `print_record`, `print_rule`, `print_template`.
Rule: `barcode_parse_rule`, `dictionary_management`. Warehouse:
`location_management`, `warehouse_area`, `warehouse_area_group`,
`warehouse_logic`, `warehouse_management`.

Most pages are mechanical: replace columns with helpers, replace
`create_update_columns` with `auditColumns`, replace the page/crud boilerplate
with `crudPage`, and move the add action to `pageActions`.

#### Wave 5D — user, API platform, plugin platform (7 pages)

User: `login_log`, `menu_management`, `user_management` (role management is
already converted). API platform: `api_keys`, `api_management` (api log landed in
5A). Plugin platform: `list/index`, `audit/index`.

At the end of 5D: remove every non-permanent allowlist entry, make the
`create_update_columns` rule an error, run `npm run lint:amis -- --strict`, and
delete the legacy constant if no in-scope file still references it.

### 7. Verification

Run in `client/`:

```bash
npm run lint:amis -- --strict
npx tsc --noEmit
npm run dev
```

Browser verification per wave:

- Viewport 1440×900 and 1024×768.
- One contact sheet per wave covering every converted page.
- Clear AMIS's per-table column-toggle localStorage keys before asserting
  default-hidden audit columns. The key is route-derived, for example
  `/user/role-managepage/body/0/crud/body/table`.
- Seed at least one row for the representative pattern pages so status dots,
  drawer context, and row actions are visible: `owner_management`, `api_log`,
  `accept_order`, `picking_order`, `stocktake/index`, and `plugin_platform/list`.
  Pages without seed data are still verified for layout, filters, toolbar,
  pagination, and empty state.
- No console errors and no AMIS "renderer not found" messages.
- The docs §14 definition-of-done checklist is applied per page.

The dev warehouse `test` currently has no rows for the five Wave 3 converted
pages. Seed data is therefore an explicit task in each wave's verification, not
an afterthought.

### 8. Risks and mitigations

| Risk | Mitigation |
|---|---|
| Two-CRUD pages break the one-CRUD-per-page rule | Use `nestedTable()`; verify the detail drawer at 1024px before converting the remaining pages |
| Custom row/bulk actions lose their API payloads | Use `linkAction()`; keep the existing `api` object and `confirmText`; spot-check the network payload |
| Import dialogs are large and page-specific | `importButton()` only moves the trigger; the dialog schema stays unchanged |
| Pages without a create action get an invented primary action | `pageActions: []` is explicit; the preset does not render an empty toolbar |
| Status label translation is incomplete | Shared maps + lint check both locale files; add keys in the same commit as the map |
| Legacy page has no seed data | Verify empty state and structure; seed representative pages per wave; do not claim data-state coverage that was not exercised |
| Lint false positives on new non-list pages | The allowlist requires a reason; permanent entries are limited to embedded workstation tables |

## Success Criteria

- All 39 remaining top-level list pages compose `crudPage()`; the only
  allowlisted files are the five embedded workstation tables.
- No in-scope page imports `create_update_columns`; audit columns use
  `auditColumns()` and are hidden by default.
- Every status field in an in-scope page uses `statusColumn` with a shared map;
  no `type: "mapping"` status columns remain.
- Every drawer form uses `formDrawer`; every destructive row action names the
  object and consequence.
- `npm run lint:amis -- --strict` and `npx tsc --noEmit` pass.
- Each wave produces a contact sheet at 1440×900 and 1024×768 with no console
  errors and no "renderer not found".
- No backend API, route, permission, or data contract changes.
