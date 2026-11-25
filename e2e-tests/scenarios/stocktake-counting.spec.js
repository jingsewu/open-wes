const { test, expect } = require('@playwright/test');
const { execFileSync } = require('child_process');
const path = require('path');
const { ApiClient } = require('../utils/api-client');
const { StationApiClient } = require('../utils/station-api');
const { uiLogin } = require('../utils/ui-login');
const { navigateTo } = require('../utils/navigate');

/**
 * Stocktake E2E — management order creation + station counting.
 *
 * Flow:
 *   seed container + stock + STOCKTAKE station mode ->
 *   management UI: 新增 -> area/type/method -> count by container -> select
 *   container -> 生成盘点单 -> select the row -> 执行盘点单 ->
 *   station UI: 领用单据 -> filter/select the new task -> receive ->
 *   simulate container arrival -> scan SKU -> submit the counted qty ->
 *   verify record / task / order reach DONE.
 *
 * Prerequisites (seeded by scripts/seed-stocktake.js): a CONTAINER-type spec
 * STK_SPEC, a stock container with one SKU001 container-stock line in area 1,
 * and workstation 1 in STOCKTAKE mode with both station caches cleared.
 * scripts/restore-station-mode.js puts workstation 1 back to PICKING after the
 * test so the outbound-picking E2E still works.
 */
test.describe('Stocktake — Management + Station Counting', () => {
  let api;
  let station;

  test('inventory order: create -> execute -> count at station -> DONE', async ({ page, request }) => {
    test.setTimeout(240_000);

    // ----- Step 0: Seed a dedicated stock container + STOCKTAKE mode -----
    const containerCode = `STK_${Date.now()}`;
    const seedOut = execFileSync(
      'node',
      [path.join(__dirname, '..', 'scripts', 'seed-stocktake.js'), containerCode, '10'],
      { encoding: 'utf-8', env: process.env, stdio: ['ignore', 'pipe', 'pipe'] }
    );
    const resultLine = seedOut.split('\n').find((l) => l.startsWith('RESULT '));
    if (!resultLine) throw new Error(`seed-stocktake.js did not print RESULT:\n${seedOut}`);
    const fixture = JSON.parse(resultLine.slice('RESULT '.length));
    console.log(`[setup] container ${fixture.containerCode}, ${fixture.skuCode} qty ${fixture.qty}`);

    // ----- Step 1: Log in and pin this session to WH001 / workstation 1 -----
    await uiLogin(page);
    const wsToken = await page.evaluate(() => localStorage.getItem('ws_token'));
    api = new ApiClient(request);
    api.setToken(wsToken);
    station = new StationApiClient(request);
    station.setToken(wsToken);
    await api.clearSearchCache();
    await page.evaluate(() => {
      localStorage.setItem('warehouseCode', 'WH001');
      localStorage.setItem('stationId', '1');
    });

    // ----- Step 2: Management UI — create the stocktake order -----
    await navigateTo(page, '/wms/data-center/stock/stocktake');
    await page.locator('.inventoryCountAddButton').click();
    await page.waitForSelector('.cxd-Modal', { timeout: 15_000 });

    // Wizard step 1 (area) and step 2 (type/method) both use selectFirst, so
    // clicking 下一步 is enough.
    await page.locator('.cxd-Modal .nextButton').first().click();
    await expect(page.locator('.cxd-Modal .stocktakeType')).toBeVisible({ timeout: 15_000 });
    await page.locator('.cxd-Modal .nextButton').first().click();

    // Wizard step 3 — switch to the "count by container" tab.
    const dialog = page.locator('.cxd-Modal').last();
    await expect(dialog.locator('.stocktakeType, .cxd-Tabs').first()).toBeVisible({ timeout: 15_000 });
    await dialog.getByText(/按容器盘点|Count by container/).first().click();

    // Pick the container from the transfer. Its service is ordered newest
    // first, so the freshly seeded container is on the first page; selecting
    // the row writes containerCode + stocktakeUnitType into the wizard data.
    const transferRow = dialog
      .locator('.cxd-Transfer tr, .cxd-Transfer [class*="item"]', { hasText: fixture.containerCode })
      .first();
    await expect(transferRow).toBeVisible({ timeout: 20_000 });
    await transferRow.click();
    await expect(dialog).toContainText(fixture.containerCode, { timeout: 10_000 });

    await dialog.locator('.generateInventoryOrder').click();
    console.log('[management] stocktake order submitted');

    // ----- Step 3: Resolve the created order through the API -----
    const order = await pollStocktakeOrder(api, fixture.containerCode);
    expect(order.stocktakeOrderStatus, 'new order should be NEW').toBe('NEW');
    console.log(`[management] order ${order.orderNo} created`);

    // ----- Step 4: Management UI — select the row and execute the order -----
    const orderRow = page.locator('.cxd-Table-content tr', { hasText: order.orderNo }).first();
    await expect(orderRow).toBeVisible({ timeout: 20_000 });
    await clickRowCheckbox(orderRow);

    await page.locator('button', { hasText: /执行盘点单|Execute inventory order/ }).first().click();
    const confirm = page.locator('.cxd-Modal').last();
    await expect(confirm).toBeVisible({ timeout: 10_000 });
    await confirm.locator('button', { hasText: /确认|确定|Confirm/ }).last().click();
    console.log('[management] execute requested');

    const task = await pollStocktakeTask(api, order.id);
    expect(task.stocktakeTaskStatus, 'task should start NEW').toBe('NEW');
    console.log(`[management] task ${task.taskNo} created`);

    // ----- Step 5: Station — ONLINE in STOCKTAKE mode and open the station page -----
    await station.online('STOCKTAKE', true);
    const view = await station.getView();
    expect(view.workStationMode, 'station should be in STOCKTAKE mode').toBe('STOCKTAKE');

    await page.evaluate(() => document.querySelector('a[href="/wms/workStation"]')?.click());
    await page.waitForURL((u) => u.pathname === '/wms/workStation/stocktake', { timeout: 25_000 });
    await page.waitForSelector('[data-testid="inventoryCountTakeOrder"]', { timeout: 20_000 });

    // ----- Step 6: Station UI — receive the stocktake task -----
    await page.locator('[data-testid="inventoryCountTakeOrder"]').click();
    const receiveModal = page.locator('.ant-modal').last();
    await expect(receiveModal).toBeVisible({ timeout: 15_000 });

    // Filter by our task number so stale NEW tasks from earlier runs cannot be
    // picked by mistake, then select and receive it. Enter submits the AMIS
    // search form — do not click a "查询" button: the "设置查询字段" toggle
    // also matches that text and opens a popover over the row checkbox.
    const taskFilter = receiveModal.locator('input[name="taskNo"]').first();
    await expect(taskFilter).toBeVisible({ timeout: 15_000 });
    await taskFilter.fill(task.taskNo);
    await taskFilter.press('Enter');

    const taskRow = receiveModal.locator('tr.takeOrderTableRow', { hasText: task.taskNo }).first();
    await expect(taskRow).toBeVisible({ timeout: 15_000 });
    await clickRowCheckbox(taskRow);
    await receiveModal.locator('.batchTakeOrders').click();
    console.log(`[station] received task ${task.taskNo}`);

    await pollTaskStatus(api, task.id, 'STARTED');

    // ----- Step 7: Simulate the robot delivering the container -----
    // The station generates the stocktake records from the arrived container
    // on the next cache refresh, so wait for the SKU tasks before scanning.
    await station.containerArrived(fixture.containerCode);
    console.log('[station] container arrived');
    await waitForSkuTasks(station);

    // ----- Step 8: Scan the SKU, confirm the qty, submit -----
    const scanInput = page.locator('[data-testid="stocktake-scan-input"]');
    await expect(scanInput).toBeVisible({ timeout: 20_000 });
    await scanInput.fill(fixture.skuCode);
    await scanInput.press('Enter');

    const qtyInput = page.locator('input.ant-input-number-input');
    await expect(qtyInput).toBeVisible({ timeout: 20_000 });
    await expect(qtyInput).toHaveValue(String(fixture.qty));

    await page.locator('[data-testid="stocktake-submit"]').click();
    console.log('[station] qty submitted');

    // ----- Step 9: Verify record -> task -> order all become DONE -----
    const record = await pollRecordDone(api, fixture.containerCode);
    expect(record.stocktakeRecordStatus, 'record should be DONE').toBe('DONE');

    const doneTask = await pollTaskStatus(api, task.id, 'DONE');
    expect(doneTask.stocktakeTaskStatus, 'task should be DONE').toBe('DONE');

    const doneOrder = await pollOrderStatus(api, order.id, 'DONE');
    expect(doneOrder.stocktakeOrderStatus, 'order should be DONE').toBe('DONE');
    console.log(`[verify] record DONE, task ${task.taskNo} DONE, order ${order.orderNo} DONE`);
  });

  test.afterAll(async () => {
    execFileSync('node', [path.join(__dirname, '..', 'scripts', 'restore-station-mode.js')], { stdio: 'ignore' });
    console.log('[teardown] workstation 1 restored to PICKING');
  });
});

/** Click the AMIS row selection checkbox (label wrapper, input fallback). */
async function clickRowCheckbox(row) {
  const label = row.locator('.cxd-Checkbox').first();
  if (await label.count()) {
    await label.click();
    return;
  }
  await row.locator('input[type="checkbox"]').first().check({ force: true });
}

/** Poll the station view until the arrived container's SKU tasks are loaded. */
async function waitForSkuTasks(station, timeoutMs = 30_000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    const view = await station.getView();
    const groups = view.skuArea?.operationViews || [];
    if (groups.some((g) => (g.operationTaskDTOs || []).length > 0)) return;
    await new Promise((r) => setTimeout(r, 1000));
  }
  throw new Error(`station SKU tasks were not generated within ${timeoutMs}ms`);
}

/** Poll WStocktakeOrderDetail by container until the created order is visible. */
async function pollStocktakeOrder(api, containerCode, timeoutMs = 45_000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    const detailResp = await api.search('WStocktakeOrderDetail', { unitCode: containerCode }, [
      { name: 'stocktakeOrderId' },
      { name: 'unitCode' },
    ]);
    if (detailResp.ok()) {
      const detail = ((await detailResp.json()).items || []).find((d) => d.unitCode === containerCode);
      if (detail) {
        const orderResp = await api.search('WStocktakeOrder', { id: String(detail.stocktakeOrderId) }, [
          { name: 'id' },
          { name: 'orderNo' },
          { name: 'stocktakeOrderStatus' },
        ]);
        if (orderResp.ok()) {
          const order = ((await orderResp.json()).items || [])[0];
          if (order) return order;
        }
      }
    }
    await new Promise((r) => setTimeout(r, 2000));
  }
  throw new Error(`no WStocktakeOrder for container ${containerCode} within ${timeoutMs}ms`);
}

/** Poll WStocktakeTask by stocktake order id until the executed task exists. */
async function pollStocktakeTask(api, orderId, timeoutMs = 45_000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    const resp = await api.search('WStocktakeTask', { stocktakeOrderId: String(orderId) }, [
      { name: 'id' },
      { name: 'taskNo' },
      { name: 'stocktakeTaskStatus' },
      { name: 'stocktakeOrderId' },
    ]);
    if (resp.ok()) {
      const task = ((await resp.json()).items || [])[0];
      if (task) return task;
    }
    await new Promise((r) => setTimeout(r, 2000));
  }
  throw new Error(`no WStocktakeTask for order ${orderId} within ${timeoutMs}ms`);
}

/** Poll WStocktakeRecord by container until it reaches DONE. */
async function pollRecordDone(api, containerCode, timeoutMs = 60_000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    const resp = await api.search('WStocktakeRecord', { containerCode }, [
      { name: 'id' },
      { name: 'containerCode' },
      { name: 'stocktakeRecordStatus' },
      { name: 'qtyOriginal' },
      { name: 'qtyStocktake' },
    ]);
    if (resp.ok()) {
      const record = ((await resp.json()).items || []).find(
        (r) => r.stocktakeRecordStatus === 'DONE'
      );
      if (record) return record;
    }
    await new Promise((r) => setTimeout(r, 2000));
  }
  throw new Error(`WStocktakeRecord for ${containerCode} did not reach DONE within ${timeoutMs}ms`);
}

/** Poll WStocktakeTask by id until it reaches the expected status. */
async function pollTaskStatus(api, taskId, status, timeoutMs = 60_000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    const resp = await api.search('WStocktakeTask', { id: String(taskId) }, [
      { name: 'id' },
      { name: 'taskNo' },
      { name: 'stocktakeTaskStatus' },
    ]);
    if (resp.ok()) {
      const task = ((await resp.json()).items || []).find((t) => t.stocktakeTaskStatus === status);
      if (task) return task;
    }
    await new Promise((r) => setTimeout(r, 2000));
  }
  throw new Error(`WStocktakeTask ${taskId} did not reach ${status} within ${timeoutMs}ms`);
}

/** Poll WStocktakeOrder by id until it reaches the expected status. */
async function pollOrderStatus(api, orderId, status, timeoutMs = 60_000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    const resp = await api.search('WStocktakeOrder', { id: String(orderId) }, [
      { name: 'id' },
      { name: 'orderNo' },
      { name: 'stocktakeOrderStatus' },
    ]);
    if (resp.ok()) {
      const order = ((await resp.json()).items || []).find((o) => o.stocktakeOrderStatus === status);
      if (order) return order;
    }
    await new Promise((r) => setTimeout(r, 2000));
  }
  throw new Error(`WStocktakeOrder ${orderId} did not reach ${status} within ${timeoutMs}ms`);
}
