# Open WES Optimization Plan

**Date**: 2026-05-15
**Based on**: Full project code review
**Severity Legend**: CRITICAL (exploit risk) | HIGH (must fix before production) | MEDIUM (should fix) | LOW (nice to have)

---

## Phase 1: Emergency Security Fixes (Week 1)

| # | Issue | Severity | File(s) | Action | Effort |
|---|-------|----------|---------|--------|--------|
| 1.1 | Secrets exposed in repository | CRITICAL | `server/server/wes-server/src/main/resources/bootstrap.yml:29` (Pinecone API key), `initdb.d/nacos_config.sql` (Azure OAuth secret, ZhipuAI key, MySQL root:root) | Replace all hardcoded secrets with `${ENV_VAR}` placeholders. Create `.env.example` with dummy values. Rotate ALL compromised keys immediately. Run `git filter-repo` or BFG to purge secrets from git history. | 0.5d |
| 1.2 | SQL injection via AI module | CRITICAL | `server/modules-ai/ai-core/src/main/java/org/openwes/ai/core/service/impl/AiChatServiceImpl.java:108-140`, `DatabaseSchemaServiceImpl.java:86` | AI-generated SQL is passed directly to `jdbcTemplate.queryForList(sql)`. Fix: (1) Add JSqlParser to validate SQL is SELECT-only, (2) Create a read-only DB user for AI queries, (3) Add query timeout (30s max), (4) Add result-set size limit (1000 rows). | 1-2d |
| 1.3 | XSS in Chatbot | CRITICAL | `client/src/components/Chatbot.tsx:102-104` | `dangerouslySetInnerHTML` used without sanitization on user messages. Replace with `ReactMarkdown` + `rehype-sanitize` (already a dependency but unused). The AI response path (lines 107-111) already uses ReactMarkdown — apply the same pattern to user messages. | 0.5d |
| 1.4 | `.gitignore` nearly empty | CRITICAL | `.gitignore` | Currently only ignores `.idea` and `client/build/README.md`. Add: `*.class`, `*.jar`, `build/`, `dist/`, `node_modules/`, `.env`, `.env.*`, `*.log`, `.DS_Store`, `*.iml`, `.gradle/`, `out/`, `target/`, `client/build/`, `.vscode/`, `*.swp`, `*.pem`, `*.key`. | 0.5d |
| 1.5 | Security permits all requests | CRITICAL | `server/modules-user/user/src/main/java/org/openwes/user/config/security/SecurityConfiguration.java:54` | `.anyRequest().permitAll()` bypasses all auth. Replace with explicit path matchers: permit `/api/auth/**`, `/oauth2/**`, `/actuator/health`; require authentication for all other endpoints. Add JWT validation filter. | 1-2d |

**Phase 1 Total: ~4-6 days**

---

## Phase 2: High-Priority Hardening (Weeks 2-3)

| # | Issue | Severity | File(s) | Action | Effort |
|---|-------|----------|---------|--------|--------|
| 2.1 | Hardcoded login credentials | HIGH | `client/src/pages/components/LoginForm.tsx:25-26` | Default state sets `username: "admin"`, `password: "123456"`. Change to empty strings `""`. | 0.1d |
| 2.2 | Test failures silently ignored | HIGH | `server/build.gradle:38` | `ignoreFailures = true` allows broken tests to pass CI. Change to `false`. Fix any failing tests. | 0.5-2d |
| 2.3 | Docker security hardening | HIGH | `docker-compose.yml`, `server/Dockerfile` | (1) Add `deploy.resources.limits` (memory/CPU) to all services. (2) Add `user: 1000:1000` to Java containers. (3) Set Redis password: `command: redis-server --requirepass ${REDIS_PASSWORD}`. (4) Change MySQL root password to env var. (5) Bind MySQL to internal network only (remove port 3306 exposure or bind to 127.0.0.1). (6) Add health checks. | 1d |
| 2.4 | JWT token handling | HIGH | `client/src/utils/requestInterceptor.ts:16` | Tokens stored in localStorage without expiry checks. Add: (1) Token expiry validation before each request, (2) Refresh token flow, (3) Auto-logout on 401 responses. Consider httpOnly cookies for production. | 2-3d |
| 2.5 | No SSL/TLS on DB connections | HIGH | `initdb.d/nacos_config.sql` (datasource URLs) | All datasource URLs use `useSSL=false`. Change to `useSSL=true&requireSSL=true`. Configure MySQL TLS certificates. | 0.5d |
| 2.6 | Redis no authentication | HIGH | `docker-compose.yml:109`, Nacos config | Redis container has no password. Set `requirepass` in Docker and update `spring.data.redis.password` in Nacos app configs. | 0.5d |

**Phase 2 Total: ~5-8 days**

---

## Phase 3: Code Quality & Architecture (Weeks 4-6)

| # | Issue | Severity | File(s) | Action | Effort |
|---|-------|----------|---------|--------|--------|
| 3.1 | Non-RESTful API design | MEDIUM | Multiple controllers (e.g., `ContainerController`) | POST used for read operations. Audit all controllers: convert query endpoints to `@GetMapping` with query parameters. Keep POST for mutations only. | 3-5d |
| 3.2 | Missing DTO input validation | MEDIUM | DTOs across `modules-wes` | No `@NotNull`, `@Size`, `@Min`, `@Max` annotations on most DTOs. Audit and add Jakarta Bean Validation annotations per Code Rule.md validation rules. | 2-3d |
| 3.3 | Rate limiting on AI endpoints | MEDIUM | `server/modules-ai/` controllers | No rate limiting on `/ai/**` endpoints. Add Bucket4j or Spring Cloud Gateway rate limiter. Suggested: 10 req/min per user for chat, 5 req/min for SQL generation. | 1d |
| 3.4 | TypeScript `any` abuse | MEDIUM | ~40+ files across `client/src/` | Replace `any` with proper interfaces. Priority files: `AMisRenderer.tsx:26-31,92-94`, `RouterGuard.tsx:8,20-22`, `Chatbot.tsx:55`. | 2-3d |
| 3.5 | Array index as React key | MEDIUM | `client/src/components/LayoutAside.tsx:71`, `Chatbot.tsx:91` | Replace `key={index}` with stable unique identifiers (message ID, menu item ID). Audit all `.map()` calls. | 1d |
| 3.6 | No React error boundaries | MEDIUM | `client/src/` | Add error boundary components at route level and feature level. Prevents full-page crashes from component errors. | 1d |
| 3.7 | Mixed MySQL collation | MEDIUM | `initdb.d/create_databases.sql`, `initdb.d/nacos_config.sql` | Main databases use `utf8mb4_unicode_ci`, Nacos tables use deprecated `utf8_bin`. Standardize all to `utf8mb4_unicode_ci`. | 0.5d |
| 3.8 | Missing database indexes | MEDIUM | `initdb.d/nacos_config.sql` | No indexes on `tenant_id`, `gmt_modified`, `config_info` status columns. Audit slow queries and add indexes on frequently queried foreign keys and status columns. | 1-2d |
| 3.9 | Improve test coverage | MEDIUM | All `modules-wes/` submodules | Only ~5 test classes exist. Add: (1) Unit tests for all domain entities and services, (2) Integration tests for critical API flows (inbound, outbound, stock), (3) Target minimum 60% coverage on domain layer. | 2-3 weeks |

**Phase 3 Total: ~3-5 weeks**

---

## Phase 4: Modernization (Weeks 7-10)

| # | Issue | Severity | File(s) | Action | Effort |
|---|-------|----------|---------|--------|--------|
| 4.1 | Spring Boot 3.2.2 outdated | LOW | `server/build.gradle` | Upgrade to latest 3.2.x or 3.4.x. Check all dependency compatibility (Dubbo, ShardingSphere, Spring Cloud). Run full regression. | 2-3d |
| 4.2 | React 17 outdated | LOW | `client/package.json` | Upgrade to React 18+. Replace `ReactDOM.render` with `createRoot`. Review concurrent mode implications. Update testing utilities. | 3-5d |
| 4.3 | Environment separation | LOW | `docker-compose.yml`, Nacos configs | Create per-environment Nacos namespaces (dev/staging/prod). Add Docker Compose profiles. Externalize all environment-specific config via env vars. | 2-3d |
| 4.4 | Frontend bundle optimization | LOW | `client/webpack.config.js` | Lazy-load heavy dependencies (Monaco Editor, Froala, Chatbot modal). Add route-based code splitting for all page groups. | 2-3d |
| 4.5 | Dockerfile optimization | LOW | `server/Dockerfile` | Optimize layer caching (copy gradle files first, then source). Add JVM flags (`-Xmx`, `-XX:+UseG1GC`). Multi-stage build to reduce image size. | 1d |

**Phase 4 Total: ~2-3 weeks**

---

## Summary

| Phase | Focus | Duration | Effort |
|-------|-------|----------|--------|
| Phase 1 | Emergency Security | Week 1 | 4-6 days |
| Phase 2 | High-Priority Hardening | Weeks 2-3 | 5-8 days |
| Phase 3 | Code Quality | Weeks 4-6 | 3-5 weeks |
| Phase 4 | Modernization | Weeks 7-10 | 2-3 weeks |

### Dependencies
- Phase 1.1 (secrets) should be done **first** — all other work is lower priority than secret rotation.
- Phase 1.5 (security config) should be done **before** Phase 2.4 (JWT handling) as they interact.
- Phase 3.9 (test coverage) is ongoing and can run parallel with other Phase 3 items.
- Phase 4.1 and 4.2 (upgrades) should be done **after** Phase 3.9 to have test safety net.

### Quick Wins (< 1 hour each)
- [ ] 2.1: Remove hardcoded credentials from LoginForm.tsx
- [ ] 1.4: Expand .gitignore
- [ ] 2.2: Set `ignoreFailures = false` in build.gradle
