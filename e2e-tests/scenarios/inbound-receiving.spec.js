const { test, expect } = require('@playwright/test');
const { execFileSync } = require('child_process');
const path = require('path');
const { ApiClient } = require('../utils/api-client');
const { uiLogin } = require('../utils/ui-login');
const { navigateTo } = require('../utils/navigate');

/**
 * Inbound pipeline smoke test.
 *
 * Note: the receive-station backend (scan inbound order -> accept at a RECEIVE
 * mode workstation) is not implemented in this codebase yet — there is no
 * inbound scan-barcode extension and inbound plan orders have no JSON create
 * API (only Excel import). So this test seeds an inbound plan order directly
 * and verifies it shows up in the real management UI.
 */
test.describe('Inbound — Pipeline Flow', () => {
  let api;

  test.beforeEach(async ({ request }) => {
    api = new ApiClient(request);
  });

  test('inbound plan order appears in the management list', async ({ page }) => {
    // ----- Step 0: Seed an inbound plan order (scripts/seed-inbound.js) -----
    const customerOrderNo = `INB_SMOKE_${Date.now()}`;
    execFileSync('node', [path.join(__dirname, '..', 'scripts', 'seed-inbound.js'), customerOrderNo], {
      stdio: 'ignore',
    });
    console.log(`[setup] seeded inbound order ${customerOrderNo}`);

    // ----- Step 1: Log in through the real login page -----
    await uiLogin(page);

    // Reuse the browser session token so API calls don't invalidate the session.
    const wsToken = await page.evaluate(() => localStorage.getItem('ws_token'));
    api.setToken(wsToken);
    await api.clearSearchCache();

    // ----- Step 2: Verify via API -----
    const resp = await api.search('WInboundPlanOrder', { customerOrderNo }, [
      { name: 'customerOrderNo' },
      { name: 'orderNo' },
      { name: 'inboundPlanOrderStatus' },
      { name: 'totalQty' },
    ]);
    expect(resp.ok()).toBeTruthy();
    const data = await resp.json();
    const orders = (data.items || []).filter((o) => o.customerOrderNo === customerOrderNo);
    expect(orders.length, 'seeded inbound order should be found').toBeGreaterThan(0);
    expect(orders[0].inboundPlanOrderStatus).toBe('NEW');

    // ----- Step 3: Verify it renders in the management UI -----
    await navigateTo(page, '/wms/data-center/inbound/inbound-plan-order');
    await page.waitForTimeout(3000);
    await expect(page.locator('body')).toContainText(customerOrderNo, { timeout: 15_000 });
    console.log('[verify] inbound order visible in management list');
  });
});
