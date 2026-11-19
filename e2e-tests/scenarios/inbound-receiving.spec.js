const { test, expect } = require('@playwright/test');
const { execFileSync } = require('child_process');
const path = require('path');
const { ApiClient } = require('../utils/api-client');
const { StationApiClient } = require('../utils/station-api');
const { uiLogin } = require('../utils/ui-login');

/**
 * Inbound receiving E2E — drives the real receive station UI end-to-end.
 *
 * The receive station page (/wms/workStation/receive) is a custom AntD layout
 * that calls the WES inbound REST APIs directly (queryPlan -> acceptPlan ->
 * completeByContainer); it does NOT go through the station module's
 * /station/api?apiCode= handlers. This test drives that real page:
 *
 *   seed order + target container -> ONLINE(RECEIVE) -> scan LPN -> scan SKU ->
 *   scan container -> pick slot -> input qty -> accept -> "满箱" complete ->
 *   verify AcceptOrder COMPLETE / InboundPlanOrder ACCEPTED / PutAwayTask.
 *
 * Prerequisites (seeded by scripts/seed-inbound.js): an inbound plan order
 * (NEW) for SKU001 with qty 10, a CONTAINER spec RCV_SPEC with slot SLOT1, a
 * target receiving container with slot SLOT1, and workstation 1 in RECEIVE
 * mode. After the test, scripts/restore-station-mode.js puts the workstation
 * back to PICKING so the outbound-picking E2E still works.
 */
test.describe('Inbound — Receive Station Flow', () => {
  let api;
  let station;

  test('receive at workstation: scan LPN -> SKU -> container -> accept -> full', async ({ page, request }) => {
    test.setTimeout(180_000);

    const customerOrderNo = `INB_${Date.now()}`;
    const containerCode = `RCV_${Date.now()}`;

    // ----- Step 0: Seed order + container + put workstation 1 into RECEIVE mode -----
    execFileSync('node', [path.join(__dirname, '..', 'scripts', 'seed-inbound.js'), customerOrderNo, containerCode], {
      stdio: 'ignore',
    });
    console.log(`[setup] seeded inbound order ${customerOrderNo}, container ${containerCode}`);

    // ----- Step 1: Log in through the real login page -----
    await uiLogin(page);

    // Reuse the browser session token so API calls don't invalidate the session.
    const wsToken = await page.evaluate(() => localStorage.getItem('ws_token'));
    api = new ApiClient(request);
    api.setToken(wsToken);
    station = new StationApiClient(request);
    station.setToken(wsToken);
    await api.clearSearchCache();

    // Pin this browser session to workstation 1 + warehouse WH001 (the receive
    // page reads both from localStorage).
    await page.evaluate(() => {
      localStorage.setItem('stationId', '1');
      localStorage.setItem('warehouseCode', 'WH001');
    });

    // ----- Step 2: Bring the workstation ONLINE in RECEIVE mode -----
    await station.online('RECEIVE', true);
    const view = await station.getView();
    expect(view.workStationMode, 'station should be in RECEIVE mode').toBe('RECEIVE');
    expect(view.hasOrder, 'station should have hasOrder=true').toBe(true);
    console.log('[setup] station ONLINE (RECEIVE)');

    // ----- Step 3: Navigate to the receive page -----
    // Deep links get redirected by TabsLayout, so click the sidebar menu; the
    // WorkStationCard auto-navigates to /wms/workStation/receive because the
    // station is ONLINE + RECEIVE.
    await page.evaluate(() => document.querySelector('a[href="/wms/workStation"]')?.click());
    await page.waitForURL((u) => u.pathname === '/wms/workStation/receive', { timeout: 25_000 });

    // ----- Step 4: Scan the LPN / order number -----
    const scanInput = page.locator('input.ant-input-lg');
    await expect(scanInput).toBeVisible({ timeout: 20_000 });
    await scanInput.fill(customerOrderNo);
    await page.locator('button.ant-btn-primary.ant-btn-block').click();

    // ----- Step 5: Work view appears; scan the SKU barcode -----
    // The SKU input only renders once the order has been recognised (queryPlan
    // resolved), so waiting for it is the "order recognised" checkpoint.
    const skuInput = page
      .locator('div.d-flex.items-center', { hasText: /商品条码|product barcode/i })
      .locator('input');
    await expect(skuInput).toBeVisible({ timeout: 20_000 });
    await expect(page.locator('body')).toContainText(customerOrderNo, { timeout: 15_000 });
    console.log('[scan] order recognised:', customerOrderNo);

    await skuInput.fill('SKU001');
    // Wait for React to commit the typed value before Enter — otherwise the
    // Enter handler reads a stale (empty) state.
    await expect(skuInput).toHaveValue('SKU001');
    await skuInput.press('Enter');
    await expect(page.locator('body')).toContainText('SKU001', { timeout: 15_000 });
    console.log('[scan] SKU recognised: SKU001');

    // ----- Step 6: Scan the target container, pick the slot, input qty -----
    const containerInput = page
      .locator('div.d-flex.items-center', { hasText: /容器号|container code/i })
      .locator('input');
    await expect(containerInput).toBeVisible({ timeout: 15_000 });
    await containerInput.fill(containerCode);
    // The shelf (ShelfModel) renders the spec slots on mount, so "slot visible"
    // does not prove the container was scanned. Wait for the typed code to be
    // committed, then Enter.
    await expect(containerInput).toHaveValue(containerCode);
    await containerInput.press('Enter');

    // Each slot cell carries data-testid=<containerSlotSpecCode>.
    const slotCell = page.locator('[data-testid="SLOT1"]');
    await expect(slotCell).toBeVisible({ timeout: 15_000 });
    await slotCell.click();
    // The active-slot input shows the selected slot code.
    await expect(page.locator('input[value="SLOT1"]')).toBeVisible({ timeout: 10_000 });

    const qtyInput = page.locator('input.ant-input-number-input');
    await qtyInput.fill('10');
    await expect(qtyInput).toHaveValue('10');
    await page.locator('button.ant-btn.ml-2', { hasText: /确定|Confirm/ }).click();
    console.log(`[accept] confirmed qty 10 into ${containerCode}`);
    await page.waitForTimeout(1500);

    // ----- Step 7: API checkpoint — accept order must be created (NEW) -----
    const acceptOrder = await pollAcceptOrder(api, containerCode);
    expect(acceptOrder.acceptOrderStatus, 'accept order should start NEW').toBe('NEW');
    console.log('[verify] accept order created:', acceptOrder.orderNo, 'status', acceptOrder.acceptOrderStatus);

    // ----- Step 8: Container full ("满箱") completes the receiving -----
    await page.locator('button.ant-btn-primary', { hasText: /满箱|Container Full/i }).click();
    console.log('[complete] container full requested');

    // ----- Step 9: Verify final states via API -----
    const completed = await pollAcceptOrderComplete(api, containerCode);
    expect(completed.acceptOrderStatus, 'accept order should be COMPLETE').toBe('COMPLETE');

    const plan = await pollInboundPlanOrder(api, customerOrderNo);
    expect(plan.inboundPlanOrderStatus, 'inbound plan order should be ACCEPTED').toBe('ACCEPTED');

    const putAway = await pollPutAwayTask(api, containerCode);
    console.log('[verify] put-away task:', putAway.taskNo, putAway.taskStatus);
  });

  test.afterAll(async () => {
    execFileSync('node', [path.join(__dirname, '..', 'scripts', 'restore-station-mode.js')], { stdio: 'ignore' });
    console.log('[teardown] workstation 1 restored to PICKING');
  });
});

/** Poll WAcceptOrder (by identifyNo = target container code) until a row exists. */
async function pollAcceptOrder(api, containerCode, timeoutMs = 45_000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    const resp = await api.search('WAcceptOrder', { identifyNo: containerCode }, [
      { name: 'identifyNo' },
      { name: 'acceptOrderStatus' },
      { name: 'orderNo' },
    ]);
    if (resp.ok()) {
      const data = await resp.json();
      const items = data.items || [];
      if (items.length > 0) return items[0];
    }
    await new Promise((r) => setTimeout(r, 2000));
  }
  throw new Error(`no WAcceptOrder with identifyNo=${containerCode} within ${timeoutMs}ms`);
}

/** Poll WAcceptOrder until it reaches COMPLETE. */
async function pollAcceptOrderComplete(api, containerCode, timeoutMs = 45_000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    const resp = await api.search('WAcceptOrder', { identifyNo: containerCode }, [
      { name: 'identifyNo' },
      { name: 'acceptOrderStatus' },
      { name: 'orderNo' },
    ]);
    if (resp.ok()) {
      const data = await resp.json();
      const item = (data.items || []).find((o) => o.acceptOrderStatus === 'COMPLETE');
      if (item) return item;
    }
    await new Promise((r) => setTimeout(r, 2000));
  }
  throw new Error(`WAcceptOrder identifyNo=${containerCode} did not reach COMPLETE within ${timeoutMs}ms`);
}

/** Poll WInboundPlanOrder until it reaches ACCEPTED. */
async function pollInboundPlanOrder(api, customerOrderNo, timeoutMs = 45_000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    const resp = await api.search('WInboundPlanOrder', { customerOrderNo }, [
      { name: 'customerOrderNo' },
      { name: 'inboundPlanOrderStatus' },
    ]);
    if (resp.ok()) {
      const data = await resp.json();
      const item = (data.items || []).find((o) => o.inboundPlanOrderStatus === 'ACCEPTED');
      if (item) return item;
    }
    await new Promise((r) => setTimeout(r, 2000));
  }
  throw new Error(`WInboundPlanOrder ${customerOrderNo} did not reach ACCEPTED within ${timeoutMs}ms`);
}

/** Poll WPutAwayTask until a task for the target container exists. */
async function pollPutAwayTask(api, containerCode, timeoutMs = 45_000) {
  const deadline = Date.now() + timeoutMs;
  while (Date.now() < deadline) {
    const resp = await api.search('WPutAwayTask', { containerCode }, [
      { name: 'containerCode' },
      { name: 'taskNo' },
      { name: 'taskStatus' },
    ]);
    if (resp.ok()) {
      const data = await resp.json();
      const items = data.items || [];
      if (items.length > 0) return items[0];
    }
    await new Promise((r) => setTimeout(r, 2000));
  }
  throw new Error(`no WPutAwayTask for ${containerCode} within ${timeoutMs}ms`);
}
