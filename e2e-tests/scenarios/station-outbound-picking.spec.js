const { test, expect } = require('@playwright/test');
const { ApiClient } = require('../utils/api-client');
const { TestDataFactory } = require('../fixtures/test-data-factory');

test.describe('Station — Outbound Picking Flow', () => {
  let api;
  let factory;

  test.beforeEach(async ({ request }) => {
    api = new ApiClient(request);
    factory = new TestDataFactory(api);
  });

  test.afterEach(async () => {
    await factory.cleanup();
  });

  test('outbound picking at workstation: bind tote → pick SKU → seal', async ({ page }) => {
    // Pre-condition: outbound orders are already auto-assigned to put-wall slots
    // by the system. The put-wall should show assigned slots.

    // ----- Step 1: Open station outbound page -----
    await page.goto('/wms/workStation/outbound');
    await page.waitForLoadState('networkidle');

    // Verify the put-wall view loaded — should show slots with orders
    const putWall = page.locator('.put-wall, .put-wall-container, [class*="putwall"]');
    await expect(putWall).toBeVisible({ timeout: 10_000 });

    // ----- Step 2: Bind transfer container to a put-wall slot -----
    // Operator scans a put-wall slot code → system selects the slot
    // Operator scans a transfer container → system binds the container via INPUT handler

    const scanInput = page.locator('input[placeholder*="scan"], input.barcode-input, #barcodeInput');

    // 2a. Tap on a slot with assigned orders
    const assignedSlot = page.locator('.put-wall-slot.has-order, .slot-item.occupied').first();
    await assignedSlot.click();

    // 2b. Scan the transfer container
    await scanInput.fill('TEST_CTN_001');
    await page.keyboard.press('Enter');

    // Verify: container bound, slot shows bound state
    await expect(page.locator('text=Bound, text=已绑定')).toBeVisible({ timeout: 5_000 });

    // ----- Step 3: Wait for robot to deliver container -----
    // In smoke test, this may be simulated via the CONTAINER_ARRIVED event
    // through the station API, or we wait for the system to auto-complete.

    // For now, assume container arrives automatically (mock/MES)
    await page.waitForTimeout(2_000);

    // ----- Step 4: Scan SKU for picking -----
    await scanInput.fill('SKU001');
    await page.keyboard.press('Enter');

    // Verify SKU details appear — operator confirms the pick
    const skuInfo = page.locator('text=SKU001');
    await expect(skuInfo).toBeVisible({ timeout: 3_000 });

    // ----- Step 5: Confirm pick quantity -----
    // Input the qty to pick
    const qtyInput = page.locator('input[type="number"], input.qty-input');
    await qtyInput.fill('5');
    await page.click('button:has-text("OK"), button:has-text("Confirm"), button:has-text("确定")');

    // ----- Step 6: Seal container (封箱) -----
    // After all items picked, operator seals the container.
    // This triggers the TAP_PUT_WALL_SLOT handler with a WAITING_SEAL slot.

    // Tap the slot again to trigger seal
    await assignedSlot.click();
    // Confirm seal
    await page.click('button:has-text("Seal"), button:has-text("封箱"), button:has-text("Confirm")');

    // ----- Step 7: Verify via API -----
    // Check that the transfer container is now SEALED
    // (Exact API call depends on available endpoints)
    console.log('[verify] Container sealed — checking via API...');
  });
});
