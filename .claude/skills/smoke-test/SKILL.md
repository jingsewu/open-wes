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
3. Update the `.spec.js` file(s) with correct selectors and flow
4. Run a dry-run to verify the script is syntactically valid
5. Report what was changed

**Scenarios available:**
- `inbound-receiving` — Station receive flow: seed order → scan LPN → SKU → container → accept → "满箱" complete
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
├── playwright.config.js      # Config: chromium, 2-min timeout, sequential
├── tsconfig.json
├── SELECTOR_NOTES.md         # AMIS DOM patterns and known quirks
├── fixtures/
│   └── test-data-factory.js  # TEST_ prefix data creation/cleanup
├── utils/
│   ├── api-client.js         # REST API client for setup/verification
│   └── auth.js               # Token management
├── scenarios/
│   ├── inbound-receiving.spec.js
│   ├── outbound-management.spec.js
│   └── station-outbound-picking.spec.js
└── scripts/
    ├── seed-inbound.js        # Inbound order + target container + RECEIVE mode
    ├── restore-station-mode.js# Revert workstation 1 to PICKING after inbound test
    └── wait-for-services.sh  # Health check script for CI
```

## Key Architecture Facts

- **Frontend:** React 17 + AMIS 6.5 on port 4001 (dev) or 80 (prod nginx)
- **Gateway:** Spring Boot on port 8090, REST API
- **Station API:** `PUT /api?apiCode=<ApiCodeEnum>` with JSON body
- **API codes for outbound:** `INPUT`, `UNBIND`, `TAP_PUT_WALL_SLOT`, `SPLIT_TASKS`, `REPORT_ABNORMAL`
- **Inbound receive (no station apiCode — the receive UI calls WES inbound REST directly):**
  - `POST /wms/inbound/plan/query/{identifyNo}/{warehouseCode}` — scan LPN/customer order no
  - `POST /wms/inbound/plan/accept` — accept a line (`AcceptRecordDTO`)
  - `POST /wms/inbound/accept/completeByContainer?containerCode=` — "满箱完成收货"
  - `POST /wms/basic/container/get` — target container lookup (spec + slot)
- **AMIS page selectors:** `.cxd-Dialog`, `.cxd-Form`, `.cxd-Button`, `.cxd-TextControl-input`
- **Test data:** All test-created data uses `TEST_` prefix, safe for cleanup

## Constraints

- Tests run sequentially (shared state in WMS)
- Max 2 minutes per test (configured in playwright.config.js)
- Do NOT hardcode real credentials in test files — use env vars
- Do NOT commit debug screenshots or videos
- When updating selectors via `/smoke-test generate`, read the actual page source first — never guess AMIS selectors
