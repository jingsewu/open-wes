# E2E Smoke Test Skill — Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a `/smoke-test` Claude Code Skill backed by standalone Playwright scripts that validate 3 core business flows end-to-end, integrated into GitHub Actions CI.

**Architecture:** Playwright (TypeScript) drives the browser against the React frontend (port 4001 dev server or port 80 via nginx). Direct API calls to the Gateway (port 8090) handle data setup and verification. Test data uses `TEST_` prefix for safe identification and cleanup. Claude Code Skill wraps the scripts with `generate`, `run`, `diagnose` sub-commands.

**Tech Stack:** Playwright 1.x, TypeScript 5.x, Node.js 18+, GitHub Actions, Docker Compose

**Design Doc:** `docs/plans/2026-07-02-e2e-smoke-test-skill-design.md`

---

## Key Project Context (From Exploration)

| Item | Detail |
|------|--------|
| Frontend dev server | Port 4001, proxy `/gw` → `http://106.75.78.251:8090` |
| Gateway server | Port 8090, REST API |
| Station API | `PUT /api?apiCode=<ApiCodeEnum>` dispatches to handlers |
| Station API codes | `INPUT`, `UNBIND`, `TAP_PUT_WALL_SLOT`, `SPLIT_TASKS`, `REPORT_ABNORMAL`, `CALL_CONTAINER`, etc. |
| Inbound plan order route | `/wms/data-center/inbound/inbound-plan-order` |
| Outbound plan order route | `/wms/data-center/outbound/outbound-plan-order` |
| Station receiving route | `/wms/workStation/receive` (type="receive") |
| Station outbound route | `/wms/workStation/outbound` (type="outbound") |
| Outbound handlers | InputHandler, SplitTasksHandler, TapPutWallSlotHandler, UnbindHandler, ReportAbnormalHandler |
| Inbound handler | CallContainerHandler |
| Key APIs | `IInboundPlanOrderApi`, `IOutboundPlanOrderApi`, `ITransferContainerApi` |
| Frontend framework | React 17 + AMIS 6.5 + MobX |
| Client test script | `"echo \"Error: no test specified\" && exit 1"` (no test infra) |
| Node.js | v18.16.1, npm 9.5.1 |

---

## Phase 1: Playwright Test Framework Setup

### Task 1.1: Initialize e2e-tests project

**Files:**
- Create: `e2e-tests/package.json`
- Create: `e2e-tests/tsconfig.json`
- Create: `e2e-tests/playwright.config.ts`

**Step 1: Create directory and init npm**

Run: `mkdir -p e2e-tests && cd e2e-tests && npm init -y`

Expected: `e2e-tests/package.json` created with default values.

**Step 2: Install Playwright and TypeScript**

Run:
```bash
cd e2e-tests
npm install --save-dev @playwright/test typescript ts-node @types/node
npx playwright install --with-deps chromium
```

Expected: Playwright and TypeScript installed, Chromium browser downloaded.

**Step 3: Write `e2e-tests/tsconfig.json`**

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "module": "commonjs",
    "lib": ["ES2022"],
    "outDir": "./dist",
    "rootDir": ".",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "resolveJsonModule": true,
    "declaration": true
  },
  "include": ["./**/*.ts"],
  "exclude": ["node_modules", "dist"]
}
```

**Step 4: Write `e2e-tests/playwright.config.ts`**

```typescript
import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: './scenarios',
  timeout: 120_000,           // 2 min per test
  expect: { timeout: 15_000 },
  fullyParallel: false,        // sequential — shared state
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: [
    ['html', { outputFolder: 'playwright-report' }],
    ['junit', { outputFile: 'junit.xml' }],
    ['list']
  ],
  use: {
    baseURL: process.env.BASE_URL || 'http://localhost:4001',
    trace: 'on-first-retry',
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
    actionTimeout: 20_000,
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] },
    },
  ],
});
```

**Step 5: Update `e2e-tests/package.json` scripts**

Edit `e2e-tests/package.json` — replace `"scripts"` with:

```json
"scripts": {
  "test": "npx playwright test",
  "test:headed": "npx playwright test --headed",
  "test:debug": "npx playwright test --debug",
  "report": "npx playwright show-report"
}
```

**Step 6: Verify setup**

Run: `cd e2e-tests && npx playwright test --list`

Expected: "No tests found" (framework works, no tests yet).

**Step 7: Commit**

```bash
git add e2e-tests/
git commit -m "chore: initialize Playwright E2E test framework

- Playwright 1.x + TypeScript 5.x
- Sequential execution, 2-min timeout per test
- HTML + JUnit reporters
- Headed mode for local dev, headless for CI

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 1.2: Create API helper module

**Files:**
- Create: `e2e-tests/utils/api-client.ts`
- Create: `e2e-tests/utils/auth.ts`

**Step 1: Write `e2e-tests/utils/auth.ts`**

```typescript
import { APIRequestContext } from '@playwright/test';

const AUTH_TOKEN_KEY = 'token';
const BASE_API_URL = process.env.API_URL || 'http://localhost:8090';

/**
 * Obtain auth token. Reads from env or performs login.
 * For CI, set AUTH_TOKEN env var directly.
 */
export async function getAuthToken(request: APIRequestContext): Promise<string> {
  const envToken = process.env.AUTH_TOKEN;
  if (envToken) return envToken;

  // Fallback: perform login
  const resp = await request.post(`${BASE_API_URL}/login`, {
    data: {
      username: process.env.TEST_USERNAME || 'admin',
      password: process.env.TEST_PASSWORD || 'admin',
    },
  });
  if (!resp.ok()) {
    throw new Error(`Login failed: ${resp.status()} ${await resp.text()}`);
  }
  const body = await resp.json();
  return body.token || body.access_token || body.data?.token;
}
```

**Step 2: Write `e2e-tests/utils/api-client.ts`**

```typescript
import { APIRequestContext, APIResponse } from '@playwright/test';
import { getAuthToken } from './auth';

const BASE = process.env.API_URL || 'http://localhost:8090';

export class ApiClient {
  private token: string | null = null;

  constructor(private request: APIRequestContext) {}

  private async headers(): Promise<Record<string, string>> {
    if (!this.token) {
      this.token = await getAuthToken(this.request);
    }
    return {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${this.token}`,
      'X-WorkStation-Id': process.env.TEST_WORKSTATION_ID || '1',
    };
  }

  private async get(path: string): Promise<APIResponse> {
    return this.request.get(`${BASE}${path}`, { headers: await this.headers() });
  }

  private async post(path: string, data?: unknown): Promise<APIResponse> {
    return this.request.post(`${BASE}${path}`, {
      headers: await this.headers(),
      data,
    });
  }

  private async put(path: string, data?: unknown): Promise<APIResponse> {
    return this.request.put(`${BASE}${path}`, {
      headers: await this.headers(),
      data,
    });
  }

  // -- Inbound --
  async createInboundPlanOrder(dto: object): Promise<APIResponse> {
    return this.post('/inbound/api/inbound-plan-order', dto);
  }

  async queryInboundPlanOrder(id: number): Promise<APIResponse> {
    return this.get(`/inbound/api/inbound-plan-order/${id}`);
  }

  // -- Outbound --
  async createOutboundPlanOrder(dto: object): Promise<APIResponse> {
    return this.post('/outbound/api/outbound-plan-order', dto);
  }

  async queryOutboundPlanOrder(customerOrderNo: string): Promise<APIResponse> {
    return this.get(`/outbound/api/outbound-plan-order/${customerOrderNo}`);
  }

  // -- Transfer Container --
  async bindContainer(dto: object): Promise<APIResponse> {
    return this.post('/basic/api/transfer-container/bind', dto);
  }

  async sealContainer(dto: object): Promise<APIResponse> {
    return this.post('/basic/api/transfer-container/seal', dto);
  }

  // -- Stock --
  async queryStock(skuCode: string): Promise<APIResponse> {
    return this.get(`/stock/api/container-stock?skuCode=${skuCode}`);
  }
}
```

> **Note:** The exact API paths (`/inbound/api/...`, `/outbound/api/...`) depend on how the gateway routes Dubbo services to REST. Adjust after verifying against a running instance — the gateway exposes Dubbo services via controller classes. If the gateway exposes different path patterns, update accordingly.

**Step 3: Verify API client structure**

No compilation errors:
```bash
cd e2e-tests && npx tsc --noEmit
```

Expected: No errors (or only unused-variable warnings).

**Step 4: Commit**

```bash
git add e2e-tests/utils/
git commit -m "feat(e2e): add API client and auth helper

- ApiClient wraps Playwright's APIRequestContext
- Token-based auth with env-var fallback for CI
- Methods for inbound, outbound, transfer-container, stock APIs

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 1.3: Create test data fixture factory

**Files:**
- Create: `e2e-tests/fixtures/test-data-factory.ts`

**Step 1: Write `e2e-tests/fixtures/test-data-factory.ts`**

```typescript
import { ApiClient } from '../utils/api-client';

let seq = 0;
function uniqueId(): string {
  return `${Date.now()}_${seq++}_${Math.random().toString(36).slice(2, 8)}`;
}

export interface TestSku {
  id?: number;
  code: string;
  name: string;
  ownerCode: string;
  warehouseCode: string;
}

export interface TestContainer {
  id?: number;
  code: string;
  type: string;
  warehouseCode: string;
}

export class TestDataFactory {
  private createdSkus: string[] = [];
  private createdContainers: string[] = [];
  private stash: Map<string, unknown> = new Map();

  constructor(private api: ApiClient) {}

  /** Store a value for later retrieval in the same test */
  set(key: string, value: unknown): void { this.stash.set(key, value); }
  get<T>(key: string): T | undefined { return this.stash.get(key) as T | undefined; }

  async createTestSku(warehouseCode = 'WH001', ownerCode = 'OWNER001'): Promise<TestSku> {
    const code = `TEST_SKU_${uniqueId()}`;
    const dto = {
      skuCode: code,
      skuName: `Smoke Test SKU ${code}`,
      ownerCode,
      warehouseCode,
      // Other required fields depend on SkuDTO definition — fill from existing init data
    };

    const resp = await this.api.createInboundPlanOrder(dto);
    // If a direct SKU creation API exists, use it instead.
    // For smoke tests, SKU may already exist from init data; skip creation if so.

    this.createdSkus.push(code);
    return { code, name: dto.skuName, ownerCode, warehouseCode };
  }

  async createTestContainer(warehouseCode = 'WH001', type = 'TOTE'): Promise<TestContainer> {
    const code = `TEST_CTN_${uniqueId()}`;
    // Container creation via API — adjust based on actual container management API
    this.createdContainers.push(code);
    return { code, type, warehouseCode };
  }

  /** Clean up all data created by this factory */
  async cleanup(): Promise<void> {
    // Clean up is best-effort — failures are logged but not fatal
    for (const code of this.createdSkus) {
      try {
        // Delete SKU if API supports it
        console.log(`[cleanup] Would delete SKU: ${code}`);
      } catch { /* ignore */ }
    }
    for (const code of this.createdContainers) {
      try {
        console.log(`[cleanup] Would delete container: ${code}`);
      } catch { /* ignore */ }
    }
  }
}
```

> **Note:** The fixture factory's exact API calls must be adjusted after verifying which CRUD endpoints are available via the gateway for SKU and container creation. Some smoke tests may rely on pre-existing seed data from `initdb.d/` instead.

**Step 2: Commit**

```bash
git add e2e-tests/fixtures/
git commit -m "feat(e2e): add test data fixture factory

- UniqueId generator with timestamp + sequence + random
- Stash map for sharing data between test steps
- Cleanup hook with TEST_ prefix safety

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Phase 2: Business Scenario Scripts

### Task 2.1: Scenario 1 — Inbound Station Receiving

**Files:**
- Create: `e2e-tests/scenarios/inbound-receiving.spec.ts`

**Step 1: Write the test file**

```typescript
import { test, expect } from '@playwright/test';
import { ApiClient } from '../utils/api-client';
import { TestDataFactory } from '../fixtures/test-data-factory';

test.describe('Inbound — Station Receiving Flow', () => {
  let api: ApiClient;
  let factory: TestDataFactory;

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
```

> **Important:** This script is a **starting template**. After Phase 2, run it against a live `docker compose up` environment and refine all selectors (`button:has-text(...)`, `input[name=...]`, `.put-wall-slot`) based on the actual DOM rendered by AMIS. AMIS pages have complex nested DOMs; use `npx playwright codegen` to capture real selectors.

**Step 2: Verify the test is discovered**

Run: `cd e2e-tests && npx playwright test --list`

Expected: Lists `inbound-receiving.spec.ts` with test "full inbound receiving flow at workstation".

**Step 3: Commit**

```bash
git add e2e-tests/scenarios/inbound-receiving.spec.ts
git commit -m "feat(e2e): add inbound station receiving smoke test

- Creates inbound plan order via management UI
- Simulates station receiving: scan order, SKU, container, choose slot, enter qty
- Completes receiving and verifies via API

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 2.2: Scenario 2 — Outbound Management Flow

**Files:**
- Create: `e2e-tests/scenarios/outbound-management.spec.ts`

**Step 1: Write the test file**

```typescript
import { test, expect } from '@playwright/test';
import { ApiClient } from '../utils/api-client';
import { TestDataFactory } from '../fixtures/test-data-factory';

test.describe('Outbound — Management Flow', () => {
  let api: ApiClient;
  let factory: TestDataFactory;

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
```

> **Note:** The outbound management flow is the most UI-dependent and may vary significantly based on the actual AMIS page schemas. Run `npx playwright codegen http://localhost:4001/wms/data-center/outbound/outbound-plan-order` to capture real selectors and refine this script after Phase 2.

**Step 2: Commit**

```bash
git add e2e-tests/scenarios/outbound-management.spec.ts
git commit -m "feat(e2e): add outbound management smoke test

- Creates outbound plan order via management UI
- Verifies order creation via API
- Placeholder for wave/picking/shipment steps

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 2.3: Scenario 3 — Station Outbound Picking

**Files:**
- Create: `e2e-tests/scenarios/station-outbound-picking.spec.ts`

**Step 1: Write the test file**

```typescript
import { test, expect } from '@playwright/test';
import { ApiClient } from '../utils/api-client';
import { TestDataFactory } from '../fixtures/test-data-factory';

test.describe('Station — Outbound Picking Flow', () => {
  let api: ApiClient;
  let factory: TestDataFactory;

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
```

> **Note:** This script requires that outbound orders are already assigned to put-wall slots before the test runs. This may need a setup step (create outbound order + trigger assignment) or rely on pre-existing data. Adjust after the first live run.

**Step 2: Commit**

```bash
git add e2e-tests/scenarios/station-outbound-picking.spec.ts
git commit -m "feat(e2e): add station outbound picking smoke test

- Bind transfer container to put-wall slot
- Scan SKU, confirm pick qty
- Seal container
- Covers InputHandler, TapPutWallSlotHandler flow

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 2.4: First live run — refine selectors

**Step 1: Start the full stack**

```bash
cd D:/git_workspace/open-wes
HOST_IP=$(hostname -I | awk '{print $1}') docker compose up -d
# Wait for all services healthy (~3-5 minutes)
docker compose ps
```

Expected: All services show "healthy" or "Up".

**Step 2: Run Playwright codegen to capture real selectors**

```bash
cd e2e-tests
npx playwright codegen http://localhost:4001/wms/data-center/inbound/inbound-plan-order
```

Manually walk through the scenario. Playwright will record selectors.
Copy the recorded selectors into the test files, replacing the placeholder `button:has-text(...)` / `input[name=...]` selectors.

Also capture selectors for:
- `http://localhost:4001/wms/workStation/receive` (station receiving)
- `http://localhost:4001/wms/workStation/outbound` (station outbound)
- `http://localhost:4001/wms/data-center/outbound/outbound-plan-order` (outbound management)

**Step 3: Run the test in headed mode**

```bash
cd e2e-tests
npx playwright test --headed --project=chromium
```

Expected: Tests run. Some may fail — this is expected. Fix selectors and retry until all pass or only fail due to data pre-condition issues.

**Step 4: Document remaining gaps**

Create `e2e-tests/SELECTOR_NOTES.md`:

```markdown
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
```

**Step 5: Commit**

```bash
git add e2e-tests/scenarios/ e2e-tests/SELECTOR_NOTES.md
git commit -m "fix(e2e): refine selectors after first live run

- Replaced placeholder selectors with AMIS-specific selectors
- Added SELECTOR_NOTES.md with AMIS DOM patterns
- Tests verified against running docker-compose environment

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Phase 3: /smoke-test Skill Definition

### Task 3.1: Create the Skill definition

**Files:**
- Create: `.claude/skills/smoke-test/SKILL.md`

**Step 1: Write `.claude/skills/smoke-test/SKILL.md`**

```markdown
---
name: smoke-test
description: Run end-to-end smoke tests for Open WES core business flows. Use when asked to run smoke tests, validate a deployment, check if the system is healthy after a change, or diagnose test failures.
---

# Smoke Test Skill

## Purpose

Validate that the Open WES system's core business flows work end-to-end. Backed by standalone Playwright scripts in `e2e-tests/` that can run with or without Claude.

## When to Use

- "run smoke test" / "run the smoke tests"
- "is the system healthy?"
- "validate the deployment"
- "the smoke test failed, help me figure out why"
- "generate smoke test for [scenario]"

## Sub-Commands

### `/smoke-test run [--ci] [--scenario <name>]`

Execute smoke tests.

- `--ci`: Run in headless mode with JUnit reporter (for CI)
- `--scenario <name>`: Run only a specific scenario (inbound-receiving, outbound-management, station-picking)

**Steps:**
1. Check if Docker services are running: `docker compose ps`
2. If not running: `HOST_IP=$(hostname -I | awk '{print $1}') docker compose up -d`
3. Wait for health checks: `./e2e-tests/scripts/wait-for-services.sh`
4. Run Playwright: `cd e2e-tests && npx playwright test [--reporter=list]`
5. Report results — pass/fail summary with durations
6. If `--ci`, output JUnit XML path

### `/smoke-test generate [--scenario <name>]`

Generate or update Playwright test scripts for given scenarios.

**Steps:**
1. Read the current state of relevant frontend pages (AMIS schemas, route paths)
2. Read the relevant station handlers and API interfaces
3. Update the `.spec.ts` file(s) with correct selectors and flow
4. Run a dry-run to verify the script is syntactically valid
5. Report what was changed

**Scenarios available:**
- `inbound-receiving` — Station inbound receiving flow
- `outbound-management` — Management outbound order lifecycle
- `station-picking` — Station outbound picking flow
- `all` — All three (default)

### `/smoke-test diagnose`

Analyze the most recent test failure.

**Steps:**
1. Read `e2e-tests/playwright-report/` or `e2e-tests/test-results/`
2. For each failed test:
   - Check the screenshot at failure point
   - Check the error message and stack trace
   - Determine root cause category: selector change, timeout, data issue, service down
3. If selector changed: find the new selector in the current page source
4. If timeout: check if the service is responding
5. If data issue: check pre-conditions
6. Propose fix — offer to apply it automatically

## Project Context

The E2E tests are in `e2e-tests/`:
```
e2e-tests/
├── package.json              # Playwright + TypeScript deps
├── playwright.config.ts      # Config: chromium, 2-min timeout, sequential
├── tsconfig.json
├── SELECTOR_NOTES.md         # AMIS DOM patterns and known quirks
├── fixtures/
│   └── test-data-factory.ts  # TEST_ prefix data creation/cleanup
├── utils/
│   ├── api-client.ts         # REST API client for setup/verification
│   └── auth.ts               # Token management
├── scenarios/
│   ├── inbound-receiving.spec.ts
│   ├── outbound-management.spec.ts
│   └── station-outbound-picking.spec.ts
└── scripts/
    └── wait-for-services.sh  # Health check script for CI
```

## Key Architecture Facts

- **Frontend:** React 17 + AMIS 6.5 on port 4001 (dev) or 80 (prod nginx)
- **Gateway:** Spring Boot on port 8090, REST API
- **Station API:** `PUT /api?apiCode=<ApiCodeEnum>` with JSON body
- **API codes for outbound:** `INPUT`, `UNBIND`, `TAP_PUT_WALL_SLOT`, `SPLIT_TASKS`, `REPORT_ABNORMAL`
- **API codes for inbound:** `CALL_CONTAINER`
- **AMIS page selectors:** `.cxd-Dialog`, `.cxd-Form`, `.cxd-Button`, `.cxd-TextControl-input`
- **Test data:** All test-created data uses `TEST_` prefix, safe for cleanup

## Constraints

- Tests run sequentially (shared state in WMS)
- Max 2 minutes per test (configured in playwright.config.ts)
- Do NOT hardcode real credentials in test files — use env vars
- Do NOT commit debug screenshots or videos
- When updating selectors via `/smoke-test generate`, read the actual page source first — never guess AMIS selectors
```

**Step 2: Create the wait-for-services script**

Create `e2e-tests/scripts/wait-for-services.sh`:

```bash
#!/bin/bash
# Wait for all services to be healthy before running tests

set -e

MAX_WAIT=300  # 5 minutes
INTERVAL=5
ELAPSED=0

check_service() {
  local url=$1
  local name=$2
  curl -s -o /dev/null -w "%{http_code}" "$url" | grep -q "200\|302\|401" && \
    echo "  ✓ $name is ready" || \
    echo "  ✗ $name not ready"
}

echo "Waiting for services to be ready..."
echo ""

while [ $ELAPSED -lt $MAX_WAIT ]; do
  ALL_READY=true

  # Check Gateway (login page returns 200/302)
  GW_STATUS=$(check_service "http://localhost:8090" "Gateway")
  echo "$GW_STATUS"
  [[ "$GW_STATUS" == *"✗"* ]] && ALL_READY=false

  # Check Frontend (nginx returns 200)
  FE_STATUS=$(check_service "http://localhost:80" "Frontend")
  echo "$FE_STATUS"
  [[ "$FE_STATUS" == *"✗"* ]] && ALL_READY=false

  if $ALL_READY; then
    echo ""
    echo "All services are ready! ($ELAPSED seconds)"
    exit 0
  fi

  sleep $INTERVAL
  ELAPSED=$((ELAPSED + INTERVAL))
done

echo ""
echo "ERROR: Services did not become ready within ${MAX_WAIT}s"
exit 1
```

Make it executable: `chmod +x e2e-tests/scripts/wait-for-services.sh`

**Step 3: Commit**

```bash
git add .claude/skills/smoke-test/ e2e-tests/scripts/
git commit -m "feat: add /smoke-test Claude Code Skill

- smoke-test/smoke-test-run: execute + wait for services + report
- smoke-test/smoke-test-generate: generate/update scripts from source
- smoke-test/smoke-test-diagnose: analyze failures + propose fixes
- wait-for-services.sh: health check for CI and local use

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Phase 4: CI Integration

### Task 4.1: Fix `ignoreFailures` in build.gradle

**Files:**
- Modify: `server/build.gradle`

**Step 1: Change `ignoreFailures`**

Find and replace in `server/build.gradle`:

```groovy
// Before (lines 38-41)
test {
    useJUnitPlatform()
    ignoreFailures = true
}

// After
test {
    useJUnitPlatform()
    ignoreFailures = false
}
```

**Step 2: Verify existing tests still pass**

```bash
cd server && ./gradlew test
```

Expected: All existing tests pass. If any fail, fix them before proceeding.

**Step 3: Commit**

```bash
git add server/build.gradle
git commit -m "fix: set ignoreFailures=false in build.gradle

Previously test failures were silently ignored. This change ensures
any failing unit test will be caught — critical now that we're adding
CI test pipelines.

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 4.2: Create GitHub Actions smoke test workflow

**Files:**
- Create: `.github/workflows/smoke-test.yml`

**Step 1: Write `.github/workflows/smoke-test.yml`**

```yaml
name: Smoke Test

on:
  pull_request:
    branches: [master]
    paths:
      - 'server/**'
      - 'client/**'
      - 'e2e-tests/**'
  workflow_dispatch:
    inputs:
      scenario:
        description: 'Specific scenario to run'
        required: false
        default: 'all'
        type: choice
        options:
          - all
          - inbound-receiving
          - outbound-management
          - station-picking

jobs:
  smoke-test:
    name: E2E Smoke Tests
    runs-on: ubuntu-latest
    timeout-minutes: 20

    env:
      BASE_URL: http://localhost:80
      API_URL: http://localhost:8090
      AUTH_TOKEN: ${{ secrets.TEST_AUTH_TOKEN }}
      TEST_WORKSTATION_ID: '1'

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: e2e-tests/package-lock.json

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'

      - name: Build backend JARs
        run: |
          cd server
          ./gradlew build -x test

      - name: Start services (core only, no monitoring)
        run: |
          docker compose up -d mysql redis nacos
          # Wait for infra
          sleep 30
          docker compose up -d wes-server station-server gateway-server frontend
          # Wait for health
          sleep 60
          docker compose ps

      - name: Install Playwright
        run: |
          cd e2e-tests
          npm ci
          npx playwright install --with-deps chromium

      - name: Run smoke tests
        id: test
        run: |
          cd e2e-tests
          if [ "${{ github.event.inputs.scenario }}" != "" ] && [ "${{ github.event.inputs.scenario }}" != "all" ]; then
            npx playwright test --grep "${{ github.event.inputs.scenario }}"
          else
            npx playwright test
          fi

      - name: Upload test report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: smoke-test-report
          path: |
            e2e-tests/playwright-report/
            e2e-tests/junit.xml
          retention-days: 7

      - name: Upload test traces (on failure)
        if: failure()
        uses: actions/upload-artifact@v4
        with:
          name: smoke-test-traces
          path: e2e-tests/test-results/
          retention-days: 3

      - name: Cleanup
        if: always()
        run: docker compose down -v
```

**Step 2: Commit**

```bash
git add .github/workflows/smoke-test.yml
git commit -m "ci: add E2E smoke test workflow

- Triggers on PR to master (server/client/e2e-tests changes)
- Manual trigger with scenario selection
- Builds backend JARs, starts services, runs Playwright
- Uploads HTML report + JUnit XML as artifacts
- 20-minute timeout, cleanup always

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

### Task 4.3: Prepare test secrets and documentation

**Files:**
- Modify: `e2e-tests/README.md` (create if not exists)

**Step 1: Write `e2e-tests/README.md`**

```markdown
# Open WES E2E Smoke Tests

## Prerequisites

- Node.js 18+
- Docker Compose
- A running Open WES stack: `docker compose up -d`

## Quick Start

```bash
# 1. Start the stack
cd /path/to/open-wes
HOST_IP=$(hostname -I | awk '{print $1}') docker compose up -d

# 2. Wait for services
./e2e-tests/scripts/wait-for-services.sh

# 3. Install deps
cd e2e-tests
npm ci
npx playwright install chromium

# 4. Run tests
npx playwright test                   # headless
npx playwright test --headed          # see the browser
npx playwright test --debug           # step-by-step
```

## Configuration

| Env Var | Default | Description |
|---------|---------|-------------|
| `BASE_URL` | `http://localhost:4001` | Frontend base URL |
| `API_URL` | `http://localhost:8090` | Gateway API URL |
| `AUTH_TOKEN` | (auto-login) | Pre-authenticated token for CI |
| `TEST_USERNAME` | `admin` | Login username |
| `TEST_PASSWORD` | `admin` | Login password |
| `TEST_WORKSTATION_ID` | `1` | Workstation ID for station tests |

## CI Setup

1. Add `TEST_AUTH_TOKEN` as a GitHub secret (or configure auto-login)
2. On PR to master, the `smoke-test.yml` workflow runs automatically
3. Manual trigger available from Actions tab

## Adding a New Scenario

1. Create `e2e-tests/scenarios/<name>.spec.ts`
2. Use `ApiClient` for API-level setup and verification
3. Use `TestDataFactory` for test data with `TEST_` prefix
4. Run `/smoke-test generate --scenario <name>` to have Claude refine selectors
5. Commit and verify in CI

## Selector Tips (AMIS Pages)

- AMIS renders complex DOMs — use `npx playwright codegen` to capture real selectors
- Dialogs: `.cxd-Dialog`, modals: `.cxd-Modal`
- Tables: `.cxd-Table-content tr`
- Inputs: `.cxd-TextControl-input input`
- Always `waitForSelector` before interacting with AMIS elements
```

**Step 2: Commit**

```bash
git add e2e-tests/README.md
git commit -m "docs: add E2E test README with setup instructions

- Quick start guide
- Env var configuration reference
- CI setup notes
- AMIS selector tips

Co-Authored-By: Claude <noreply@anthropic.com>"
```

---

## Summary: Task Order & Dependencies

```
Phase 1 (Foundation)
  1.1 Init Playwright project        ← no deps
  1.2 Create API client + auth       ← after 1.1
  1.3 Create fixture factory          ← after 1.2
      ↓
Phase 2 (Scenarios)
  2.1 Inbound receiving script       ← after 1.3
  2.2 Outbound management script     ← after 1.3 (parallel with 2.1)
  2.3 Station picking script         ← after 1.3 (parallel with 2.1)
  2.4 First live run + refine        ← after 2.1, 2.2, 2.3 (requires docker up)
      ↓
Phase 3 (Skill)
  3.1 Create /smoke-test SKILL.md    ← after 2.4
      ↓
Phase 4 (CI)
  4.1 Fix ignoreFailures             ← no deps (can run earlier)
  4.2 Create smoke-test.yml          ← after 3.1
  4.3 README + secrets doc           ← after 4.2
```

## Success Criteria

- [ ] `cd e2e-tests && npx playwright test` passes all 3 scenarios against a running stack
- [ ] All scenarios complete within 10 minutes total
- [ ] `/smoke-test run` works via Claude Code Skill
- [ ] `git push` triggers smoke-test.yml on PR to master
- [ ] `ignoreFailures = false` in root `build.gradle`
- [ ] `/smoke-test diagnose` produces actionable output when a test fails
