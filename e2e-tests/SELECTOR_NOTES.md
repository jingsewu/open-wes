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
