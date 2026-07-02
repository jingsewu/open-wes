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

1. Create `e2e-tests/scenarios/<name>.spec.js`
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
