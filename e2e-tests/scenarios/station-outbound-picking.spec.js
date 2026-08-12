const { test, expect } = require('@playwright/test');
const { StationApiClient } = require('../utils/station-api');
const { uiLogin } = require('../utils/ui-login');

/**
 * Station outbound picking E2E.
 *
 * Prerequisites (seeded by scripts/seed-env.js): warehouse WH001, SKU001 with
 * container stock CTN001, put-wall PW01 bound to workstation ST001, and the WES
 * outbound pipeline that dispatches picking orders to put-wall slots.
 *
 * The test drives the REAL station UI: binds a transfer container to a slot via
 * the put-wall scan input, simulates the robot delivering the source container
 * via the station API (CONTAINER_ARRIVED), scans the SKU in the SKU area, then
 * taps the slot to complete the pick and again to seal the container.
 */
test.describe('Station — Outbound Picking Flow', () => {
  test('outbound picking at workstation: bind → deliver → scan SKU → pick → seal', async ({ page, request }) => {
    test.setTimeout(180_000);

    // ----- Step 0: Log in through the real login page -----
    await uiLogin(page);

    // Reuse the browser session token for all API calls. A second signin would
    // overwrite the per-user token in Redis and invalidate the browser session.
    const wsToken = await page.evaluate(() => localStorage.getItem('ws_token'));
    const station = new StationApiClient(request);
    station.setToken(wsToken);

    // Pin this browser session to workstation ST001.
    await page.evaluate(() => localStorage.setItem('stationId', '1'));

    // ----- Step 1: Setup via station API -----
    // Clear the bean-searcher class cache (a bad showColumns run poisons an
    // identity until cleared), then bring the station online (re-initializes the
    // Redis cache from DB) and make sure at least one slot is WAITING_BINDING.
    await station.clearSearchCache();
    await station.online();
    const slotCode = await station.ensureWaitingBindingSlot();
    console.log(`[setup] picking slot: ${slotCode}`);
    const transferContainer = `TOTE_${Date.now()}`;
    const skuCode = 'SKU001';
    const sourceContainer = 'CTN001';

    // ----- Step 2: Navigate to the station outbound page -----
    // page.goto deep links get redirected to /wms/dashboard by TabsLayout, so we
    // click the 工作站 sidebar menu; the WorkStationCard auto-navigates to
    // /wms/workStation/outbound because the station is ONLINE + PICKING.
    await page.evaluate(() => document.querySelector('a[href="/wms/workStation"]')?.click());
    await page.waitForURL((u) => u.pathname === '/wms/workStation/outbound', { timeout: 25_000 });
    await page.waitForSelector('input[data-testid="scanSkuCode"]', { timeout: 20_000 });

    // ----- Step 3: Bind a transfer container to the slot -----
    // The put-wall has an invisible scan input; scanning the slot code selects
    // the slot, then scanning the container code binds it.
    const scanInput = page.locator('input[class*="opacity-0"], input.absolute.inset-0').first();
    await expect(scanInput).toBeVisible({ timeout: 15_000 });
    await scanInput.fill(slotCode);
    await page.keyboard.press('Enter');
    await page.waitForTimeout(800);
    await scanInput.fill(transferContainer);
    await page.keyboard.press('Enter');
    await page.waitForTimeout(1500);
    console.log(`[bind] slot ${slotCode} <- ${transferContainer}`);

    // Verify the slot is BOUND via API.
    const viewAfterBind = await station.getView();
    const boundSlot = viewAfterBind.putWallArea.putWallViews[0].putWallSlots.find((s) => s.putWallSlotCode === slotCode);
    expect(boundSlot, `slot ${slotCode} should be BOUND`).not.toBeUndefined();
    expect(boundSlot.putWallSlotStatus).toBe('BOUND');

    // ----- Step 4: Robot delivers the source container (simulated) -----
    await station.containerArrived(sourceContainer);
    await page.waitForTimeout(2000);

    // ----- Step 5: Scan the SKU in the SKU area -----
    const skuInput = page.locator('input[data-testid="scanSkuCode"]');
    await skuInput.fill(skuCode);
    await page.keyboard.press('Enter');
    // The SKU area should now show the SKU being picked.
    await expect(page.locator('text=' + skuCode)).toBeVisible({ timeout: 10_000 });
    console.log('[pick] scanned SKU', skuCode);

    // ----- Step 6: Tap the slot to confirm the pick -----
    // After scanning, the bound slot becomes DISPATCH (待分拨); tapping it
    // completes the pick and the slot moves to WAITING_SEAL (待封箱).
    const dispatchSlot = page.locator('[data-testid="dispatch"]').first();
    await expect(dispatchSlot).toBeVisible({ timeout: 15_000 });
    await dispatchSlot.click();
    await page.waitForTimeout(1500);

    // ----- Step 7: Tap the slot again to seal the container -----
    const sealSlot = page.locator('[data-testid="waitingSeal"]').first();
    await expect(sealSlot).toBeVisible({ timeout: 15_000 });
    await sealSlot.click();
    await page.waitForTimeout(2000);

    // ----- Step 8: Verify via API -----
    const view = await station.getView();
    const slot = view.putWallArea.putWallViews[0].putWallSlots.find((s) => s.putWallSlotCode === slotCode);
    expect(slot, `slot ${slotCode} should be IDLE after seal`).not.toBeUndefined();
    expect(slot.putWallSlotStatus).toBe('IDLE');
    // The picking order behind the slot should be PICKED.
    const searchResp = await station.search('WPickingOrder', { id: String(boundSlot.pickingOrderId) }, [
      { name: 'id' },
      { name: 'pickingOrderNo' },
      { name: 'pickingOrderStatus' },
    ]);
    const pickingData = await searchResp.json();
    const pickingOrder = (pickingData.items || []).find((o) => String(o.id) === String(boundSlot.pickingOrderId));
    expect(pickingOrder, 'picking order should be PICKED').not.toBeUndefined();
    expect(pickingOrder.pickingOrderStatus).toBe('PICKED');
    console.log('[verify] slots:', view.putWallArea.putWallViews[0].putWallSlots.map((s) => `${s.putWallSlotCode}:${s.putWallSlotStatus}`).join(' '));
  });
});
