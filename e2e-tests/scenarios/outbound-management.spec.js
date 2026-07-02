const { test, expect } = require('@playwright/test');
const { ApiClient } = require('../utils/api-client');
const { TestDataFactory } = require('../fixtures/test-data-factory');

test.describe('Outbound — Management Flow', () => {
  let api;
  let factory;

  test.beforeEach(async ({ request }) => {
    api = new ApiClient(request);
    factory = new TestDataFactory(api);
  });

  test.afterEach(async () => {
    await factory.cleanup();
  });

  test('full outbound order lifecycle', async ({ page }) => {
    const customerOrderNo = `TEST_OUT_${Date.now()}`;

    // ----- Step 1: Create outbound plan order -----
    await page.goto('/wms/data-center/outbound/outbound-plan-order');
    await page.waitForLoadState('networkidle');

    await page.click('button:has-text("Add")');
    await page.waitForSelector('.cxd-Dialog');

    await page.fill('input[name="customerOrderNo"]', customerOrderNo);
    // Fill destination, priority, etc.
    await page.click('button:has-text("Add Detail")');
    // Fill detail: SKU, qty
    await page.click('button:has-text("Confirm")');

    await expect(page.locator(`text=${customerOrderNo}`)).toBeVisible({ timeout: 10_000 });

    // ----- Step 2: Verify via API -----
    const queryResp = await api.queryOutboundPlanOrder(customerOrderNo);
    expect(queryResp.ok()).toBeTruthy();
    const orderData = await queryResp.json();
    expect(orderData?.status || orderData?.data?.status).toBeDefined();
    console.log('[verify] Outbound order created:', JSON.stringify(orderData));

    // ----- Step 3: Wave assignment (if there's a wave management page) -----
    // This step depends on how wave assignment is triggered in the UI.
    // May be automatic or manual — adjust based on actual workflow.

    // ----- Step 4: Picking order verification -----
    await page.goto('/wms/data-center/outbound/picking-order');
    await page.waitForLoadState('networkidle');
    // Look for picking orders related to our customer order
    // ...

    // ----- Step 5: Confirm shipment -----
    // Navigate to shipment page, confirm, verify stock decreased
    // ...
  });
});
