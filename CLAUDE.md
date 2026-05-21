# Open WES - AI Coding Guidelines

## Project Overview

Open WES is a Warehouse Execution System. Monorepo structure:
- `server/` — Java 17, Spring Boot 3.2.2, Gradle, DDD architecture
- `client/` — React 17, TypeScript, MobX + AMIS framework
- Infrastructure: Docker Compose, MySQL 8.0, Redis 7.2, Nacos 2.0, Kafka

**Read `server/Code Rule.md` first** — it defines DDD rules, database standards, plugin patterns, and validation rules that all backend code must follow.

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
modules-utils/        # Shared utilities (must NOT depend on other modules)
server/               # Spring Boot runners (wes-server, station-server, gateway-server)
```

### Client (`client/src/`)
```
pages/        # Page components (wms/, api_platform/, data_platform/, user/)
components/   # Shared React components
stores/       # MobX state tree stores
routes/       # React Router config + lazy-loaded route mapping
locales/      # i18n translation files (en/zh/ja/ko)
utils/        # HTTP client, helpers
```

## Architecture Patterns

### DDD Layer Flow
```
Controller -> IEntityApi (interface) -> EntityApiImpl -> Domain Service -> Entity -> Repository -> JPA
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
| Backend coding standards | `server/Code Rule.md` |
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
