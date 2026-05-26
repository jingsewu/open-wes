# Open WES - AI Coding Guidelines

## Project Overview

Open WES is a Warehouse Execution System. Monorepo structure:
- `server/` — Java 17, Spring Boot 3.2.2, Gradle, DDD architecture
- `client/` — React 17, TypeScript, MobX + AMIS framework
- Infrastructure: Docker Compose, MySQL 8.0, Redis 7.2, Nacos 2.0, Kafka

**Read `docs/standards/backend.md` first** — it defines DDD rules, database standards, plugin patterns, and validation rules that all backend code must follow.

## Project Structure

### Server Modules (`server/`)
```
modules-wes/          # Core domain (inbound, outbound, stock, basic, config, task, stocktake, algo, ems-proxy, printer)
modules-ai/           # AI integration (ai-core, ai-api)
modules-user/         # Auth, user management
modules-station/      # Workstation operations
modules-gateway/      # API gateway
modules-plugin/       # Plugin implementations
modules-search/       # Search functionality
modules-api-platform/ # External API platform
modules-utils/        # Shared utilities (must NOT depend on other modules) — includes monitoring module
server/               # Spring Boot runners (wes-server, station-server, gateway-server)
```

### Client (`client/src/`)
```
pages/        # Page components (wms/, api_platform/, data_platform/, user/, monitoring/)
components/   # Shared React components
stores/       # MobX state tree stores
routes/       # React Router config + lazy-loaded route mapping
locales/      # i18n translation files (en/zh/ja/ko)
utils/        # HTTP client, helpers
```

## Architecture Patterns

### DDD Layer Flow
```
Controller -> IEntityApi (interface) -> EntityApiImpl -> UseCase -> Domain Service -> Entity -> Repository -> JPA
```

### Package Convention
```
org.openwes.[module-group].[module].[domain].[layer]
# Example: org.openwes.wes.outbound.domain.entity.OutboundPlanOrder
```

### Class Naming
| Type | Pattern | Example |
|------|---------|---------|
| API interface | `I[Entity]Api` | `IInboundPlanOrderApi` |
| API impl | `[Entity]ApiImpl` | `InboundPlanOrderApiImpl` |
| Service | `[Entity]Service` / `[Entity]ServiceImpl` | `OutboundWaveService` |
| Repository | `[Entity]Repository` / `[Entity]RepositoryImpl` | `ContainerRepository` |
| DTO | `[Entity]DTO` | `ContainerDTO` |
| Transfer | `[Entity]Transfer` | `InboundPlanOrderTransfer` |
| UseCase | `[Action][Entity]UseCase` | `CancelOutboundPlanOrderUseCase` |
| Error enum | `[Module]ErrorDescEnum` | `InboundErrorDescEnum` |

### API Implementation Annotations
```java
@Primary
@Service
@Validated
@DubboService
@RequiredArgsConstructor
public class EntityApiImpl implements IEntityApi { }
```

### Domain Entity Rules
- Use `@Getter @Builder`, never `@Data` on domain entities
- **No setters** — use lifecycle methods: `initialize()`, `complete()`, `cancel()`, `close()`
- Extend `AggregatorRoot` for aggregate roots
- Use `@Version` for optimistic locking
- Publish events via `addAsynchronousDomainEvents()`

### Repository Rules
- Only `find*` and `save*` methods. **Never** `update*` methods.
- Load entity -> mutate via lifecycle method -> save entire entity

### Transactions
- `@Transaction` only on Aggregates and Repositories
- Pull queries **out** of transactions for performance
- Prefer async domain events over big transactions

## Backend Conventions

- **Redis**: Use `RedisUtils`, not `RedisTemplate`
- **Locking**: Use `DistributedLock` class
- **Async (same server)**: `DomainEventPublisher.sendAsyncDomainEvent()`
- **Async (cross server)**: `MqClient.sendAsyncMessage()`
- **Validation**: `@Valid` on API interface params, `@Validated` on impl classes
- **Logging**: English only, use `@Slf4j`
- **Config**: Environment config in Nacos, business config in database
- **DB columns**: Code=64, Name=128, Description=255, Remark=500, Enum=20 chars
- **POJO**: Use `@Comment("...")` not `columnDefinition` for column comments
- **Plugins**: One `IPlugin` interface per domain with before/after lifecycle hooks

## Frontend Conventions

- **AMIS pages**: Most CRUD/form pages are JSON schema definitions using `schema2component()`. Prefer this pattern for standard pages.
- **State management**: MobX + mobx-state-tree (`src/stores/`). Use `@inject @observer` decorators.
- **HTTP requests**: Always use `request` from `@/utils/requestInterceptor`. Never use raw `fetch` or `axios`.
- **i18n**: Use `useTranslation()` hook or `t()` function. All user-facing strings must be translated.
- **TypeScript**: Define proper interfaces. **Avoid `any` types.** Use `Record<string, unknown>` if type is truly unknown.
- **React keys**: Use stable unique IDs, never array indexes.
- **Styling**: Ant Design components + CSS classes. AMIS pages use AMIS built-in styling.
- **Logging**: All `console.*` and toast messages must be in **English**. **Never** commit debug `console.log` calls. **Never** suppress `console.error` globally.
- **React props**: Props are read-only — never mutate them. `renderSchema()` (AMIS) returns a `ReactElement`, not a class instance; do not call `destroy()`/`dispose()` on it.
- **SVG IDs**: Use namespaced module-scoped constants for gradient/clipPath IDs to prevent duplicates across renders.

**Read `docs/standards/frontend.md`** for detailed rules, anti-patterns, and examples.

## Security Guidelines

These rules are non-negotiable:

1. **NEVER** commit secrets, API keys, or passwords. Use `${ENV_VAR}` placeholders.
2. **NEVER** use `dangerouslySetInnerHTML` without DOMPurify. Prefer `ReactMarkdown` with `rehype-sanitize`.
3. **NEVER** execute dynamically generated SQL directly. Validate with SQL parser, use read-only DB user.
4. **NEVER** use `.anyRequest().permitAll()` in Spring Security. Define explicit authorization rules.
5. **NEVER** hardcode credentials (including as default form values).
6. **Always** validate DTO inputs with Bean Validation annotations (`@Valid`, `@NotNull`, `@NotEmpty`, `@Size`).
7. **Always** use parameterized queries for any user-influenced SQL.
8. Store tokens with expiry handling. Prefer httpOnly cookies over localStorage for sensitive tokens.

## Monitoring & Observability

The project uses Prometheus + Grafana + Loki for monitoring, alerting, and log aggregation.

### Architecture
- **Metrics**: Application (Micrometer) → `/actuator/prometheus` → Prometheus scrape → Grafana
- **Logs**: Application log files → Promtail → Loki → Grafana
- **Alerts**: Prometheus/Loki alert rules → AlertManager → Webhook
- **Infrastructure**: MySQL/Redis/Node/Kafka Exporters → Prometheus

### Monitoring Module (`modules-utils/monitoring/`)
- Auto-configured via `MonitoringAutoConfiguration` (Spring Boot auto-config)
- Business metrics use **Guava `@Subscribe`** on domain events — zero invasion of business code
- `DomainEventProcessor` (BeanPostProcessor) auto-registers any bean with `@Subscribe` methods
- One `*MetricsSubscriber` per domain: Inbound, Outbound, Task, Stock
- All three servers (WES, Station, Gateway) expose `/actuator/prometheus`

### Adding New Business Metrics
1. Create a `@Component` class in `org.openwes.monitoring.metrics`
2. Add `@Subscribe` methods for the domain events you want to track
3. Use `MeterRegistry` to record counters/gauges/timers
4. The subscriber is auto-registered with EventBus — no manual wiring needed

### Frontend Monitoring App
- Top-level app (`system_code='monitoring'`) with menu entries in `u_menu` table
- Pages embed Grafana dashboards via iframe (`?kiosk` mode)
- Grafana configured for anonymous Viewer access + iframe embedding

### Monitoring Config Files
All monitoring infrastructure config lives in `monitoring/` at the repo root:
```
monitoring/
├── prometheus/          # Scrape targets + alert rules
├── grafana/             # Dashboards + datasource provisioning
├── loki/                # Log storage config
├── promtail/            # Log collection config
└── alertmanager/        # Alert routing + notification
```

### Monitoring Ports
| Service | Port |
|---------|------|
| Prometheus | 9090 |
| Grafana | 3000 |
| Loki | 3100 |
| AlertManager | 9093 |

## Build and Run

```bash
# Backend (Java 17 required)
cd server && ./gradlew build

# Frontend
cd client && npm install && npm run dev

# Full stack (Docker)
HOST_IP=$(hostname -I | awk '{print $1}') docker-compose up -d
```

### Server Ports
| Service | Port |
|---------|------|
| Gateway | 8090 |
| WES Server | 9010 |
| Station Server | 9040 |
| Frontend | 80 |
| Prometheus | 9090 |
| Grafana | 3000 |
| Loki | 3100 |
| AlertManager | 9093 |

## Database Migrations

All schema and data changes **must** use Liquibase changelogs — never ad-hoc SQL files.

- Changelogs live in `server/server/wes-server/src/main/resources/db/changelog/`
- Naming: `db.changelog-YYYYMMDD.xml`; register in `db.changelog-master.xml`
- **Never edit** any SQL file already referenced by an existing changeset (`init_dictionary.sql` etc.) — Liquibase checksums them and will refuse to start if they change
- Always use `preConditions onFail="MARK_RAN"` for idempotency
- Split ADD + MIGRATE + DROP into **3 separate changesets** (Hibernate auto-DDL may create the new columns before Liquibase runs)
- **Adding or modifying an `IEnum` enum value** requires a changeset in the same commit to sync the dictionary — `POST /config/dictionary/refresh` is a dev-only helper, not a substitute

**Read `docs/standards/liquibase.md`** for full rules, templates, and the ADD+MIGRATE+DROP pattern.

## Repository Hygiene

**Never commit AI tooling artifacts**: `.superpowers/`, `.claude/settings.local.json`. These are covered by `.gitignore` — if you add new tooling, update `.gitignore` before the first commit.

**Read `docs/standards/repository.md`** for full rules on gitignore management and CI/GitHub Actions standards.

## Key File Locations

| Purpose | Path |
|---------|------|
| Backend coding standards | `docs/standards/backend.md` |
| Frontend standards (detailed) | `docs/standards/frontend.md` |
| Repository hygiene standards | `docs/standards/repository.md` |
| Database migration standards | `docs/standards/liquibase.md` |
| Gradle build config | `server/build.gradle`, `server/settings.gradle` |
| Backend entry points | `server/server/wes-server/`, `station-server/`, `gateway-server/` |
| App config (Nacos) | `initdb.d/nacos_config.sql` |
| Docker | `docker-compose.yml` |
| DB init scripts | `initdb.d/` |
| Frontend entry | `client/src/index.tsx` |
| Route definitions | `client/src/routes/path2Compoment.tsx` |
| Monitoring module | `server/modules-utils/monitoring/` |
| Monitoring infra config | `monitoring/` |
| Monitoring design spec | `docs/superpowers/specs/2026-05-23-monitoring-alerting-framework-design.md` |
| Domain event system | `server/modules-utils/domain-event/` |
