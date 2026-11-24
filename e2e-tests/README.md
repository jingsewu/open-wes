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

# 4. Reset the shared outbound/station state (required before each run)
node scripts/reset-pipeline.js

# 5. Run tests
npx playwright test                   # headless
npx playwright test --headed          # see the browser
npx playwright test --debug           # step-by-step
```

`reset-pipeline.js` (and the inbound scenario's seed script) talks to MySQL and
Redis directly, so `DB_HOST` / `DB_PORT` / `REDIS_HOST` / `REDIS_PORT` must be
set when the stack is not on localhost (defaults target the dev VM).

### Running against a deployed server

MySQL and Redis are normally not exposed publicly. Tunnel them from the server,
then point the suite at its frontend and gateway:

```bash
ssh -N -L 13306:127.0.0.1:3306 -L 16379:127.0.0.1:6379 ubuntu@<server>

BASE_URL=http://<server> API_URL=http://<server>:8090 TEST_WORKSTATION_ID=1 \
DB_HOST=127.0.0.1 DB_PORT=13306 REDIS_HOST=127.0.0.1 REDIS_PORT=16379 \
npx playwright test
```

On a fresh environment the WH001 baseline (warehouse, pick area, workstation
ST001, owner, SKU001, batch stock) is created by
`node scripts/seed-server-baseline.js`, followed by `node scripts/seed-env.js`
to add CTN001 stock and the PW01 put wall.

## Configuration

| Env Var | Default | Description |
|---------|---------|-------------|
| `BASE_URL` | `http://localhost:4001` | Frontend base URL |
| `API_URL` | `http://localhost:8090` | Gateway API URL |
| `AUTH_TOKEN` | (auto-login) | Pre-authenticated token for CI |
| `TEST_USERNAME` | `admin` | Login username |
| `TEST_PASSWORD` | `123456` | Login password |
| `TEST_WORKSTATION_ID` | `1` | Workstation ID for station tests |
| `DB_HOST` / `DB_PORT` | `192.168.127.129` / `3306` | MySQL used by seed/reset scripts |
| `REDIS_HOST` / `REDIS_PORT` | `192.168.127.129` / `6379` | Redis used by station-cache resets |
| `REDIS_DB` | `3` | Redis database of the WES Nacos config |

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
