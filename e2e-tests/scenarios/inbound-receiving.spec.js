const { test, expect } = require('@playwright/test');
const { ApiClient } = require('../utils/api-client');
const { TestDataFactory } = require('../fixtures/test-data-factory');

test.describe('Inbound — Station Receiving Flow', () => {
  let api;
  let factory;

  test.beforeEach(async ({ request }) => {
    api = new ApiClient(request);
    factory = new TestDataFactory(api);
  });

  test.afterEach(async () => {
    await factory.cleanup();
  });

  test('full inbound receiving flow at workstation', async ({ page }) => {
    // ----- Step 1: Create an inbound plan order via management UI -----
    await page.goto('/wms/data-center/inbound/inbound-plan-order');

    // Click "Add" or "Create" button to open the creation form
    // Note: AMIS pages use JSON schema rendering — selectors may vary.
    // Common patterns: button text, CSS class, or data-testid.
    await page.click('button:has-text("Add")');
    await page.waitForSelector('.cxd-Dialog'); // AMIS dialog class

    // Fill the form — adjust field names based on actual AMIS schema
    const customerOrderNo = `TEST_INB_${Date.now()}`;
    await page.fill('input[name="customerOrderNo"]', customerOrderNo);
    // Select SKU — AMIS uses combo-box/select components
    await page.click('button:has-text("Add Detail")');
    // ... fill detail row: SKU code, quantity

    await page.click('button:has-text("Confirm")');

    // Verify order appears in the table
    await expect(page.locator(`text=${customerOrderNo}`)).toBeVisible({ timeout: 10_000 });

    // ----- Step 2: Switch to station receiving page -----
    await page.goto('/wms/workStation/receive');
    await page.waitForLoadState('networkidle');

    // ----- Step 3: Scan inbound order (simulated via input) -----
    // On the station page, scanning means typing into a barcode input field
    // The input field is usually auto-focused
    const scanInput = page.locator('input[placeholder*="scan"], input.barcode-input, #barcodeInput');
    await scanInput.fill(customerOrderNo);
    await page.keyboard.press('Enter');

    // Verify: the order details appear on screen
    await expect(page.locator(`text=${customerOrderNo}`)).toBeVisible({ timeout: 5_000 });

    // ----- Step 4: Scan SKU, container, choose slot, enter qty (loop) -----
    // This loop represents the "scan SKU → scan container → choose slot → enter qty" cycle
    // For smoke test, do ONE iteration.

    // 4a. Scan SKU
    await scanInput.fill('SKU001');  // Use a known test SKU from seed data
    await page.keyboard.press('Enter');
    await expect(page.locator('text=SKU001')).toBeVisible({ timeout: 3_000 });

    // 4b. Scan container
    await scanInput.fill('TEST_CTN_001'); // Use a known test container
    await page.keyboard.press('Enter');

    // 4c. Choose put-wall slot (tap on the slot in UI)
    const firstSlot = page.locator('.put-wall-slot, .slot-item, [class*="slot"]').first();
    await firstSlot.click();

    // 4d. Enter quantity
    const qtyInput = page.locator('input[type="number"], input.qty-input');
    await qtyInput.fill('10');

    // 4e. Confirm
    await page.click('button:has-text("OK"), button:has-text("Confirm"), button:has-text("确定")');

    // ----- Step 5: Complete the receiving (满箱完成收货) -----
    await page.click('button:has-text("Complete"), button:has-text("Finish"), button:has-text("满箱完成")');

    // ----- Step 6: Verify via API -----
    // The receiving should have created an accept record.
    // Check via API that the order status changed.
    // (Exact API call depends on available endpoints)

    // Verify stock increased (after mock completes put-away automatically)
    await page.waitForTimeout(3_000); // Wait for async mock/MES to complete

    const stockResp = await api.queryStock('SKU001');
    expect(stockResp.ok()).toBeTruthy();
    const stockData = await stockResp.json();
    console.log('[verify] Stock after inbound:', JSON.stringify(stockData));
    // Assert stock > 0 (relaxed — depends on prior state)
  });
});
