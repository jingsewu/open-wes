# Selector Notes

## AMIS-specific patterns
- Dialogs: `.cxd-Dialog`, `.cxd-Modal`
- Forms: `.cxd-Form`, `.cxd-FormItem`
- Tables: `.cxd-Table`, `.cxd-Table-content`
- Buttons: `.cxd-Button`, `.cxd-Button--primary`
- Inputs: `.cxd-TextControl-input`, `.cxd-Number-input`
- Combo-box: `.cxd-Combo`, `.cxd-Select`

## Known quirks
- AMIS re-renders frequently — use `waitForSelector` before interactions
- Some inputs are generated dynamically — use `locator('input')` with `nth()`
- Page transitions need `waitForLoadState('networkidle')`

## Receive station UI (`/wms/workStation/receive`)
Custom AntD layout (not AMIS, not apiCode-driven). It calls WES inbound REST
endpoints directly. Only mounts when the station cache `workStationMode` is
RECEIVE — the test must flip the DB `w_work_station.work_station_mode` to
RECEIVE and clear `_wms:basic:work:station:cache::1` + `WorkStation:1` before
ONLINE(RECEIVE), then restore to PICKING after (`scripts/restore-station-mode.js`).

- **Scan-order view** (shown while `hasOrder=true` and no order scanned yet):
  - LPN input: `input.ant-input-lg` (only large AntD input on the page)
  - Confirm button: `button.ant-btn-primary.ant-btn-block` (确定/Confirm)
- **Work view** — two `Col span={12}` columns:
  - SKU input (left): `div.d-flex.items-center` containing `请扫描商品条码`/`Please scan the product barcode` → `input`
  - Container input (right): `div.d-flex.items-center` containing `请扫描容器号`/`Please scan the container code` → `input`
  - Shelf slot cell: `[data-testid="<containerSlotSpecCode>"]` (e.g. `[data-testid="SLOT1"]`)
  - Active-slot echo input: `input[value="<slotCode>"]`
  - Qty InputNumber: `input.ant-input-number-input`
  - Accept (确定): `button.ant-btn.ml-2`
  - Container full (满箱): `button.ant-btn-primary` with text 满箱/Container Full
- i18n: receive keys are flat (`receive.station.*`); match both zh (`确定`/`满箱`) and en (`Confirm`/`Container Full`) with regex `:has-text(/确定|Confirm/)`.
- AntD `type="primary"` is a style class, NOT the HTML `type` attribute — target `.ant-btn-primary`, not `button[type="primary"]`.
- After scanning the container code, wait for the shelf to render (`[data-testid=...]`) before clicking a slot; the qty must be filled before 确定 (qtyAccepted is `@NotNull @Min(1)`).

## Stocktake management page (`/wms/data-center/stock/stocktake`)

- Add button: `.inventoryCountAddButton` (button.add → AMIS modal `.cxd-Modal`)
- Wizard next: `.cxd-Modal .nextButton`; step 2 type/method selectFirst renders `.stocktakeType`
- Target tab: text 按容器盘点 / Count by container (AMIS tabs)
- Container transfer: `.cxd-Transfer` rows; select the seeded container by
  clicking its row (writes `containerCode` + `stocktakeUnitType` into the wizard).
  The service is ordered newest-first, so the just-seeded container is on page 1.
  The standalone input's tag is not part of the wizard scope, so selecting the
  transfer row is required.
- Submit: `.generateInventoryOrder` (生成盘点单)
- Order rows: `.cxd-Table-content tr`; selection checkbox `.cxd-Checkbox`
- Execute bulk action: button text 执行盘点单 / Execute inventory order; confirm
  dialog `.cxd-Modal` → 确认/确定/Confirm
- App-side fixes this scenario depends on (all in `client/src`):
  - `stocktake/add.tsx`: the container tab wrote `containerCodes` while the DTO
    field is `containerCode` (NPE on create). Both transfers now also write
    `stocktakeUnitType`, and the submit validation trusts the selected data
    instead of the (lagging) tabs value.
  - `select_search_api_contant.tsx`: the two target-list services used
    `page=${page}`, which resolves to "" inside a service and makes bean-searcher
    throw `NumberFormatException`. They now use `page=1&perPage=1000`, and the
    container query is ordered `c.id desc`.
  - A container is only listable when its spec type is `CONTAINER` (STK_SPEC in
    `seed-stocktake.js`, not TOTE_SPEC).

## Stocktake station page (`/wms/workStation/stocktake`)

The page mounts only when the station cache mode is STOCKTAKE (flip
`w_work_station.work_station_mode` + clear `_wms:basic:work:station:cache::1` and
`WorkStation:1`, same as the receive page).

- Tab action "领用单据": `[data-testid="inventoryCountTakeOrder"]`
- Receive modal: AntD `.ant-modal`; task rows `tr.takeOrderTableRow`; row
  checkbox `.cxd-Checkbox`; filter input `input[name="taskNo"]` (press Enter to
  submit — do NOT click a "查询"-matching button; "设置查询字段" also matches and
  opens a popover over the checkbox); receive button `.batchTakeOrders`
- After `CONTAINER_ARRIVED` the server generates the stocktake records and the
  SKU area fills; poll `GET /station/api?stationCode=1` until
  `skuArea.operationViews[].operationTaskDTOs` is non-empty before scanning.
- Scan input: `input[placeholder="Scan Barcode"]`; qty confirmation:
  `input.ant-input-number-input` (defaults to the original qty) + button text
  `SUBMIT`; a mismatch triggers an AntD confirm modal before submitting.
- App-side fixes this scenario depends on (all in `client/src`):
  - `stocktake/layout.tsx` was not an `observer` and read `workStationEvent`
    from props (never passed) — the SKU/container panes never re-rendered after
    SCAN_BARCODE / CONTAINER_ARRIVED. It now reads the MobX store via
    `useWorkStation()` and is wrapped in `observer`.
  - `Operation.tsx` / `SKUInfo.tsx` had `operationTaskDTOS` (wrong case) and an
    unsafe `[0]` access; both now use `operationTaskDTOs?.[0]`.
  - `mobxConfig.ts` enabled `disableErrorBoundaries: true`. In MobX 4 a throwing
    computed skips its `computationDepth--`, so one render error poisons the
    global depth and every later observable write throws an obfuscated
    invariant. Keep it disabled (MobX default).
