# E2E Smoke Test Skill — Design Document

**Date:** 2026-07-02  
**Author:** Kinser  
**Status:** Approved

---

## 1. Problem Statement

Open WES currently has **no E2E tests, no smoke tests, and no CI test pipeline**. Existing tests are limited to ~33 unit test files (mostly domain entity tests) with `ignoreFailures = true` in `build.gradle`, meaning test failures are silently ignored. There is no way to quickly verify that a code change has not broken critical business flows.

## 2. Goals

- Create a **smoke test skill** (`/smoke-test`) that validates the 3 core business flows end-to-end
- The tests must be **standalone Playwright scripts** — executable without Claude, runnable in CI
- Claude acts as the **generator, maintainer, and diagnostician** of these scripts
- Integrate into **GitHub Actions** — triggered on PRs to `master`

## 3. Architecture Overview

```
┌──────────────────────────────────────────────────┐
│                 Smoke Test Skill                  │
│                                                   │
│  Layer 1: Skill Definition (Claude Code Skill)    │
│  ┌─────────────────────────────────────────────┐ │
│  │  /smoke-test generate   → 生成/更新测试脚本   │ │
│  │  /smoke-test run        → 执行冒烟测试        │ │
│  │  /smoke-test diagnose   → 诊断失败原因        │ │
│  │  /smoke-test add-scenario → 添加新测试场景    │ │
│  └─────────────────────────────────────────────┘ │
│                                                   │
│  Layer 2: Playwright Test Scripts (独立可执行)    │
│  ┌─────────────────────────────────────────────┐ │
│  │  e2e-tests/                                  │ │
│  │  ├── fixtures/       # Test data factories   │ │
│  │  ├── scenarios/      # Business scenarios    │ │
│  │  ├── utils/          # API helpers, asserts  │ │
│  │  └── playwright.config.ts                    │ │
│  └─────────────────────────────────────────────┘ │
│                                                   │
│  Layer 3: CI Integration (.github/workflows)      │
│  ┌─────────────────────────────────────────────┐ │
│  │  smoke-test.yml → start services → run → report│
│  └─────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────┘
```

**Core principle:** Playwright scripts are standalone assets. Claude Skill is the generator and guardian.

## 4. Test Scenarios

### Scenario 1: Inbound — Station Receiving

| Step | Operation | Method | Verification |
|------|-----------|--------|-------------|
| 1 | Create inbound plan order (select SKU, qty) | Management UI | Plan order status = NEW |
| 2 | Scan inbound order number | Station UI | Order recognized |
| 3 | Scan SKU barcode | Station UI | SKU identified |
| 4 | Scan container barcode | Station UI | Container binding |
| 5 | Choose put-wall slot | Station UI | Slot assigned |
| 6 | Input SKU quantity, confirm | Station UI | Item received |
| — | **Loop steps 3-6 for multiple SKUs** | — | — |
| 7 | Container full / all SKUs received → "满箱完成收货" | Station UI | Accept order status = ACCEPTED |
| 8 | Wait & verify (auto put-away via mock/MES) | API | Put-away task = COMPLETED, stock increased |

### Scenario 2: Outbound — Management Flow

| Step | Operation | Method | Verification |
|------|-----------|--------|-------------|
| 1 | Create outbound plan order | Management UI | Plan order status = NEW |
| 2 | Wave assignment | Management UI | Picking order created |
| 3 | Confirm picking | Management UI | Picking order = PICKED |
| 4 | Confirm shipment | Management UI | Stock decreased |

### Scenario 3: Station — Outbound Picking

| Step | Operation | Method | Verification |
|------|-----------|--------|-------------|
| 1 | Orders auto-assigned to put-wall slots | (Automatic) | Orders on put-wall slots |
| 2 | Bind transfer container (scan slot + tote) | Station UI | Container bound via `bind()` |
| 3 | Robot delivers container to station | (Automatic) | Container arrived |
| 4 | Scan SKU for picking | Station UI | Task status progressing |
| 5 | Confirm picked quantity | Station UI | Qty correct |
| 6 | Seal container (封箱) | Station UI | Container status = SEALED, task = COMPLETED |

## 5. Data Management Strategy

### Hybrid Data Model

| Data Layer | Source | Examples |
|------------|--------|----------|
| **Infrastructure data** (immutable) | `initdb.d/` + Nacos configs | Dictionaries, warehouse definitions, workstation templates, barcode rules |
| **Test master data** (created per run) | Fixture factory via API | Test SKUs, containers, owners — all with `TEST_` prefix |
| **Business transaction data** (dynamic) | Created within each scenario | Inbound/outbound orders, waves, picking orders |

### Fixture Factory

```typescript
class TestDataFactory {
  async createTestSku(): Promise<SkuDTO> {
    const code = `TEST_SKU_${Date.now()}_${randomInt()}`
    return await api.createSku({ code, name: 'Smoke Test SKU', ... })
  }
  async createTestContainer(type: 'TOTE' | 'PALLET'): Promise<ContainerDTO> { ... }
  async cleanup(): Promise<void> { /* delete all TEST_* prefixed data */ }
}
```

**Constraints:**
- All test data uses `TEST_` prefix for identification and safe cleanup
- Timestamp + random suffix ensures concurrency safety in CI
- Cleanup runs in `afterAll` hook regardless of pass/fail

## 6. CI Integration

### Workflow: `smoke-test.yml`

- **Trigger:** PR → `master` (paths: `server/**`, `client/**`, `e2e-tests/**`) + manual dispatch
- **Timeout:** 20 minutes
- **Services started:** mysql, redis, nacos, wes-server, station-server, gateway-server, frontend (no monitoring stack to save resources)
- **Report:** Playwright HTML + JUnit XML, uploaded as artifacts
- **Cleanup:** `docker compose down -v` with `if: always()`

### Prerequisite Fix

The following must be changed in `server/build.gradle` to ensure test failures are visible:

```groovy
// Before (line 39)
ignoreFailures = true

// After
ignoreFailures = false
```

## 7. Skill Command Reference

| Command | Purpose | Input | Output |
|---------|---------|-------|--------|
| `/smoke-test generate [scenario]` | Generate/update Playwright scripts | Scenario: inbound/outbound/station/all | Updated `.spec.ts` files |
| `/smoke-test run [--ci]` | Execute smoke tests | `--ci` flag for headless mode | TAP/JUnit report, HTML report |
| `/smoke-test diagnose` | Analyze test failures | (reads last report) | Diagnosis + fix suggestions |

## 8. Build Plan (4 Phases)

| Phase | What | Deliverables |
|-------|------|-------------|
| **1. Playwright Framework** | Install Playwright, write `playwright.config.ts`, API helper, fixture factory | `e2e-tests/` directory |
| **2. Scenario Scripts** | Write inbound, outbound, station `.spec.ts` files, verify locally | `scenarios/*.spec.ts` |
| **3. Skill Definition** | Create `/smoke-test` Skill via `skill-creator`, write prompt for generation/maintenance/diagnosis | `.claude/skills/smoke-test/SKILL.md` |
| **4. CI Integration** | Write `smoke-test.yml`, fix `ignoreFailures`, test end-to-end | `.github/workflows/smoke-test.yml` |

## 9. Success Criteria

- [ ] `/smoke-test run` passes all 3 scenarios on a clean `docker compose up`
- [ ] All scenarios complete within 10 minutes
- [ ] Test scripts can run standalone: `cd e2e-tests && npx playwright test`
- [ ] CI workflow triggers on PR, completes within 20 minutes
- [ ] Failed tests produce actionable diagnosis via `/smoke-test diagnose`
- [ ] `ignoreFailures = false` in root `build.gradle`
