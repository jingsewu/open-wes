# Unified List Search Design

- Date: 2026-11-02
- Status: Approved
- Supersedes: the filter-band rule in `2026-11-02-list-page-ux-unification-design.md` §1 and the `filters` option in §2; `docs/standards/amis.md` §5.

## Context

All 40 top-level list pages already compose `crudPage()` from
`client/src/utils/amis/presets.ts`, but the preset renders two different search
bands:

- No `filters` option: AMIS `autoGenerateFilter` builds the form from columns
  marked `searchable`. It includes 展开/收起 and the 设置查询字段 field chooser.
  This is the owner-management pattern and is used by 29 pages.
- Explicit `filters` option: a fixed inline form with query/reset only. It has
  no field chooser, is expanded by default, and only becomes collapsible when
  there are more than four fields. This is used by 10 pages.

The split was a documented rule (`docs/standards/amis.md` §5: up to four fields
auto-generated, more than four explicit). The UI difference is visible on the
busiest pages: the pages that most need field selection are the ones without it.

One additional list page, plugin audit, has no search at all; its custom
endpoint (`GET /plugin-platform/scriptPlugin/audit`) accepts only `page` and
`pageSize`. It is out of scope.

## Goals

- Every list page with search uses one model: `columns.searchable` drives the
  search band. `crudPage` no longer accepts `filters`.
- All pages expose the same controls: 展开/收起, 设置查询字段, 查询, 重置.
- The preset and `docs/standards/amis.md` describe that single model.
- No backend or database changes.

## Non-goals

- Adding search to the plugin audit page (needs a backend filter contract).
- The five embedded workstation tables; they are not top-level list pages.
- Saved filter views, custom column presets, or new search operators.

## Decisions

### 1. Auto-generated search only

Delete the `filters` branch and the `filterTogglable` handling from
`crudPage`. It always emits:

```ts
autoGenerateFilter: {
    columnsNum: 3,
    showBtnToolbar: true
}
```

`showColumns`, `searchObject`, `orderBy`, `loadDataOnce`, and `empty` keep
their current behaviour.

### 2. Three columns per row

Keep `columnsNum: 3` for every page, plus one narrow-width layout rule.

Measurements from the AMIS 6.13 harness at the project layout (200px sidebar,
16px main padding, 98px horizontal labels):

| Viewport | Form width | 3 per row, expanded | 4 per row, expanded |
|---|---|---|---|
| 1440 | 1151px | 364px item / 228–250px input | 269px item / 133–155px input |
| 1366 | 1077px | 340px item / 204–226px input | 251px item / 115–137px input |
| 1024 | 735px | 226px item / 90–112px input | wraps mid-row; some inputs 6–84px |

At 1024px, four columns cannot fit the form width. AMIS wraps the group
mid-row and produces a 709px item next to inputs as narrow as 6px. Three
columns is the only fixed count that stays intact at the minimum viewport.

Collapsed, AMIS keeps two fields plus the toolbar in one row. The 设置查询字段
dropdown, 查询, 重置, and 展开 controls are wider than a field slot, so at
1024px they squeeze the first text input to about 25px. The same squeeze
exists on the existing auto-filter pages with at least three searchable
fields, including owner management: it is a toolbar-row problem, not a
column-count problem. The fix is one rule in
`client/src/scss/amis-console.scss`, shared by every page:

```scss
@media (max-width: 1199.98px) {
    .cxd-Table-searchableForm
        .cxd-Form-groupColumn:has(> .cxd-AutoFilterToolbar) {
        flex-basis: 100% !important;
    }
}
```

The toolbar becomes a right-aligned row of its own; the fields get the full
width. Measured after the rule: at 1024px the collapsed fields are 347px with
211–233px inputs; at 1100px they are 385px with 249–271px inputs. At 1200px
and above the rule does not apply and the layout is unchanged. The selector
depends on AMIS class names and must be re-checked on an AMIS upgrade.

Four columns was rejected: it improves only the 1024px collapsed state,
breaks the 1024px expanded state, and costs 30–90px of input width at 1366px
and above.

### 3. Field order follows column order

Accepted trade-off of removing explicit `filters`. The collapsed band shows the
first active `searchable` columns in table order. Examples of changed defaults:

- Location management: `warehouseAreaId` / `warehouseLogicId` / `aisleCode`
  instead of `locationCode` first.
- Container stock: `skuCode` / `barcode` / `ownerCode` instead of
  `containerCode` first.

The 设置查询字段 chooser lets each operator disable fields, which changes which
fields occupy the collapsed row; AMIS persists that state per table page path. If operators still
report a poor default after rollout, move the relevant column earlier in a
separate display-order change. Do not reintroduce a second filter code path.

### 4. Express each control through `searchable`

| UI control | Column definition |
|---|---|
| Text | `searchable: true` |
| Select / dictionary | `searchable: { type: "select", source: "..." }` or `options` |
| Date range | `searchable: { type: "input-date-range", valueFormat: "x" }` |

Fields that are not displayed in the table remain expressible as
`hidden: true` columns with `searchable`; the backend already receives hidden
columns through `showColumns`. This migration does not need any after removing
the dead accept-order filters below.

Audit fields use the existing option:

```ts
auditColumns({
    createdSearchable: { type: "input-date-range", valueFormat: "x" }
})
```

## Page migration

All ten pages below lose their `filters` constant and pass `searchable` on the
matching columns instead. Existing labels, dictionary sources, `dbField`
mappings, and column order stay unchanged.

| Page | Searchable columns after migration | Notes |
|---|---|---|
| `plugin_platform/list/index.tsx` | `pluginKey`, `name`, `status`, `applySystem` | Custom `loadDataOnce` endpoint; verify client-side filtering still works. |
| `wms/config_center/print/print_record.tsx` | `templateCode`, `printStatus`, `module`, `printNode`, `printer`, `workStationId`, `printTime` (range) | |
| `wms/config_center/warehouse/location_management.tsx` | `locationCode`, `warehouseAreaId`, `warehouseLogicId`, `aisleCode`, `shelfCode` | Area/logic selects reuse `warehouse_area_id` / `warehouse_logic_id`. |
| `wms/config_center/warehouse/warehouse_management.tsx` | `warehouseCode`, `warehouseName`, `warehouseType`, `warehouseAttrType`, `warehouseLevel`, `warehouseLabel`, `businessType`, `structureType` | All selects reuse their dictionary sources. |
| `wms/data_center/inbound/accept_order.tsx` | `identifyNo`, `acceptOrderStatus`, `orderNo` | Drop the three dead filters listed below. |
| `wms/data_center/inbound/inbound_plan_order/index.tsx` | `orderNo`, `customerOrderNo`, `lpnCode`, `trackingNumber`, `inboundPlanOrderStatus`, `storageType`, `sender`, `shippingMethod`, `skuKindNum` | |
| `wms/data_center/inbound/put_away_task.tsx` | `taskNo`, `containerCode`, `containerSpecCode`, `workStationId`, `taskStatus`, `taskType`, `updateTime` | `createTime` via `auditColumns({ createdSearchable: ... })`; `updateTime` is already a visible date column. |
| `wms/data_center/stock/container_stock.tsx` | `containerCode`, `skuCode`, `barcode`, `ownerCode`, `warehouseAreaCode` | `createTime` via `auditColumns({ createdSearchable: ... })`; the audit mapper adds `dbField`. |
| `wms/data_center/stock/stock_abnormal.tsx` | `abnormalOrderNo`, `containerCode`, `barCodeList`, `stockAbnormalType`, `stockAbnormalStatus` | `createTime` via `auditColumns({ createdSearchable: ... })`; keep the joined `barCodeList` `dbField`. |
| `wms/data_center/stock/stocktake/index.tsx` | `orderNo`, `stocktakeOrderStatus`, `warehouseAreaId`, `warehouseLogicId`, `stocktakeType`, `stocktakeUnitType`, `stocktakeCreateMethod`, `abnormal` | |

## Dead accept-order filters

`ownerCode`, `skuCode`, and `skuName` are not columns on the accept-order list
and are not in `showColumns`, so the dynamically generated search bean has no
fields for them. They cannot filter the base `w_accept_order` table, which has
no owner or SKU columns. They are removed rather than migrated.

If detail-level filtering is needed later, it is a separate backend change:
join `w_accept_order_detail` and expose the fields through `searchObject` with a
defined aggregation/cardinality contract.

## Documentation

Rewrite `docs/standards/amis.md` §5:

- Remove the "more than four fields use explicit `filters`" rule.
- State that every query field is a `searchable` column.
- Document `searchable: true` / select / date-range forms.
- Document that column order determines the collapsed priority.
- Document `auditColumns({ createdSearchable })` and hidden filter columns.
- Update the older migration-wave note so it no longer presents explicit
  filters as the current practice.

## Verification

Static:

- `cd client && npm run lint:amis -- --strict`
- `cd client && npx tsc --noEmit`
- Grep to confirm no page passes `filters` and no `filterTogglable` remains.

Browser, for all ten pages:

- At 1440×900 and 1366×768, confirm the search band renders two fields plus the
  inline toolbar when collapsed, expands to three per row, and the 设置查询字段
  chooser lists the migrated fields.
- At 1024×768, confirm the toolbar moves to its own row, the two collapsed
  fields keep usable inputs (about 210–230px), and the expanded form still
  renders three per row without wrapping.
- At 1200×768 and 1440×900, confirm the narrow-width rule is inactive: the
  toolbar is inline again and the layout matches the current owner page.
- Query and reset each page; confirm the request carries the same parameters
  for the retained fields as before the migration and that results match.
- Confirm pagination, `orderBy`, export/import, and empty states are unchanged.
- Confirm no new console errors and no missing locale keys.

The visual check uses one contact sheet per round, per the project's frontend
verification protocol.

## Risks

- Search priority changes because order follows column order. Mitigation: the
  field chooser plus a later display-order change only if usage data demands it.
- Removing the accept-order filters removes three visible controls. They do not
  currently affect results, but product should confirm the removal.
- The narrow-width toolbar rule uses `:has()` and AMIS-owned class names. A
  browser without `:has()` keeps the current squeeze rather than breaking; an
  AMIS upgrade can rename the classes, so the 1024px check must include the
  rule. The rule applies to every auto-filter page, so verify the existing
  owner page at 1024px and 1440px in the same pass.
- The plugin list filters client-side over a single loaded collection. Verify
  that the auto-filter submit still triggers local filtering with
  `loadDataOnceFetchOnFilter: false`.

## Follow-ups

- Add backend filtering to the plugin audit endpoint, then give it the same
  search band.
- If requested, add detail-level filters to accept order through a defined
  detail-table join.
