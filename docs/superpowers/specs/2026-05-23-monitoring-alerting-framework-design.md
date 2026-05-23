# Open WES Monitoring & Alerting Framework Design

## Overview

Design an enterprise-grade monitoring and alerting framework for Open WES using the Prometheus + Grafana + Loki stack. The framework must be lightweight (total memory ~2GB), support scaling from single-machine Docker Compose to Kubernetes, and provide comprehensive infrastructure and business metrics with multi-channel alerting.

## Current State

### Existing Infrastructure
- **SkyWalking APM Toolkit**: Logback integration only (no APM Server deployed)
- **Spring Boot Actuator**: Enabled on wes-server only (health, info, metrics, env, scheduledtasks, threadpool)
- **Logback**: File-based logging with rotation (DEBUG 5d, INFO 10d, ERROR 30d), SkyWalking Trace ID in log pattern
- **Fluent Logger**: Dependency present but disabled
- **Docker Health Checks**: Configured for MySQL, Redis, Nacos

### Gaps
- No metrics collection (Prometheus/Micrometer)
- No visualization (Grafana)
- No alerting system
- No centralized log aggregation
- No JVM/application-level performance monitoring
- Gateway and Station servers have no Actuator endpoints

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Grafana (Port 3000)                      │
│            Dashboard + Alert Rule Visualization                  │
├─────────────┬───────────────────┬───────────────────────────────┤
│ Prometheus  │      Loki         │       AlertManager            │
│ (Port 9090) │   (Port 3100)     │       (Port 9093)             │
│ Metrics     │   Log Aggregation │   Alert Routing/Notification  │
│ Storage     │   & Query         │                               │
├─────────────┴───────────────────┴───────────────────────────────┤
│                     Data Collection Layer                        │
├──────────────┬──────────────┬───────────────────────────────────┤
│ Micrometer   │  Promtail    │   Node Exporter / cAdvisor       │
│ (In-app)     │ (Log Agent)  │   (Host/Container Metrics)       │
├──────────────┴──────────────┴───────────────────────────────────┤
│              Open WES Application Services                       │
│  ┌──────────┐ ┌──────────────┐ ┌──────────────┐                │
│  │ Gateway  │ │  WES Server  │ │Station Server│                │
│  │  :8090   │ │    :9010     │ │    :9040     │                │
│  └──────────┘ └──────────────┘ └──────────────┘                │
├─────────────────────────────────────────────────────────────────┤
│              Infrastructure                                      │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐          │
│  │  MySQL   │ │  Redis   │ │  Nacos   │ │  Kafka   │          │
│  │  :3306   │ │  :6379   │ │  :8848   │ │  :9092   │          │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘          │
└─────────────────────────────────────────────────────────────────┘
```

### Data Flows

1. **Metrics**: Application (Micrometer) → `/actuator/prometheus` endpoint → Prometheus scrape → Grafana display
2. **Logs**: Application log files → Promtail collection → Loki storage → Grafana query
3. **Alerts**: Prometheus/Loki alert rules → AlertManager → Webhook/Email/DingTalk/WeCom/Slack
4. **Infrastructure Metrics**: MySQL Exporter, Redis Exporter, Node Exporter → Prometheus

## Component Design

### 1. Application Metrics Collection (Micrometer Integration)

#### 1.1 New Module: `modules-utils/monitoring/`

A shared monitoring module providing:
- Micrometer Prometheus Registry auto-configuration
- Common metrics auto-collection: JVM, thread pools, HikariCP connection pool, Dubbo RPC, Redis operations
- Domain event-driven business metrics listeners

#### 1.2 Actuator Endpoint Unification

All three servers (Gateway, WES, Station) must expose:
- `health` — with MySQL, Redis, Nacos, Kafka component health indicators
- `prometheus` — Micrometer metrics export (new)
- `info` — application info

#### 1.3 Domain Event-Driven Business Metrics

**Core approach**: Business code already publishes domain events via `addAsynchronousDomainEvents()` through Guava's `AsyncEventBus` (see `DomainEventPublisher`). The monitoring module subscribes to these events using Guava's `@Subscribe` annotation (matching the existing subscriber pattern, e.g., `AcceptOrderSubscriber`) and converts them to Micrometer metrics — zero invasion of business code.

```java
// In monitoring module — no business code changes needed
// Uses Guava @Subscribe to match existing domain event pattern
@Component
@RequiredArgsConstructor
public class InboundMetricsSubscriber {
    private final MeterRegistry registry;

    @Subscribe
    public void onAccepted(InboundPlanOrderAcceptedEvent event) {
        registry.counter("wes.inbound.orders.accepted.total",
            "warehouse", event.getWarehouseCode()).increment();
    }

    @Subscribe
    public void onCompleted(InboundOrderCompletionEvent event) {
        registry.counter("wes.inbound.orders.completed.total").increment();
    }
}
```

**Important**: Metrics subscribers must be registered with the Guava `EventBus` via `DomainEventPublisher.registerSubscriber()`, not via Spring's `@EventListener` which uses a different event bus.

**Architecture**:
- One `MetricsSubscriber` per business domain: `InboundMetricsSubscriber`, `OutboundMetricsSubscriber`, `TaskMetricsSubscriber`, `StockMetricsSubscriber`, `StationMetricsSubscriber`
- Subscribers are registered with the Guava AsyncEventBus during application startup via a `MonitoringAutoConfiguration` class
- For metrics not driven by domain events (API latency, thread pools), use Micrometer auto-configuration + WebFilter (Gateway, which uses WebFlux) / Spring MVC Filter (WES/Station servers)
- For Dubbo RPC metrics, integrate `dubbo-spring-boot-observability-starter` to auto-collect provider/consumer call counts and latency

#### 1.4 Predefined Business Metrics

| Metric Name | Type | Source | Description |
|-------------|------|--------|-------------|
| `wes.inbound.orders.accepted.total` | Counter | Domain Event (`InboundPlanOrderAcceptedEvent`) | Inbound orders accepted |
| `wes.inbound.orders.completed.total` | Counter | Domain Event (`InboundOrderCompletionEvent`) | Inbound orders completed |
| `wes.outbound.orders.total` | Counter | Domain Event | Outbound plan orders created |
| `wes.task.processing.duration` | Timer | Domain Event | Task processing duration |
| `wes.task.queue.size` | Gauge | Scheduled poll | Pending task queue length |
| `wes.station.active.count` | Gauge | Scheduled poll | Active workstation count |
| `wes.api.request.duration` | Timer | WebFilter/MVC Filter | API request latency (by route) |
| `wes.stock.operations.total` | Counter | Domain Event | Stock operations (by type) |
| `dubbo.provider.requests.total` | Counter | dubbo-observability | Dubbo provider call count (by service/method) |
| `dubbo.provider.requests.duration` | Timer | dubbo-observability | Dubbo provider call latency |
| `dubbo.consumer.requests.total` | Counter | dubbo-observability | Dubbo consumer call count |

### 2. Collection Frequency & Data Retention

#### Scrape Intervals

| Target | Interval | Rationale |
|--------|----------|-----------|
| Application services (Gateway/WES/Station) | 15s | Prometheus default, balances precision and overhead |
| Infrastructure (MySQL/Redis Exporter) | 30s | Slower change rate, reduces exporter pressure |
| Host metrics (Node Exporter) | 30s | CPU/memory/disk don't need sub-second granularity |

#### Data Retention
- Prometheus local storage: **15 days**
- Extensible to longer retention via Thanos/Mimir if needed

### 3. Grafana Dashboards

#### Default Time Ranges

| Dashboard | Default Range | Default Granularity | Purpose |
|-----------|--------------|--------------------|---------| 
| **Real-time Operations** | Last 1 hour | 1 minute | On-duty monitoring |
| **Business Throughput** | Last 24 hours | 1 hour | Daily peak/valley analysis |
| **Performance Analysis** | Last 6 hours | 5 minutes | Slow request investigation |
| **Trend Report** | Last 7 days | 1 day | Weekly capacity planning |

All dashboards support user-adjustable time range and granularity via Grafana's built-in controls.

#### Dashboard Contents

**Overview Dashboard**: Service health status, active alerts count, key business KPIs (today's order volume, task completion rate)

**JVM Dashboard**: Heap/non-heap memory, GC frequency and duration, thread count, class loading, per-service breakdown

**Business Dashboard**: Inbound/outbound order volume trends, task processing rate and duration distribution, workstation utilization, stock operation trends

**Infrastructure Dashboard**: MySQL connections/QPS/slow queries, Redis memory/hit rate/connections, disk/CPU/memory utilization, Kafka consumer lag

### 4. Log Aggregation (Promtail + Loki)

#### Collection Method
Promtail runs as a sidecar container, mounts application log directories, and pushes to Loki.

#### Log Labels
- `job` — Service name (gateway / wes-server / station-server)
- `level` — Log level (DEBUG / INFO / WARN / ERROR)
- `host` — Host identifier

#### Log Retention Policy
- INFO/DEBUG: 7 days
- WARN/ERROR: 30 days

**Implementation**: Use Loki's compactor with per-stream retention rules based on the `level` label. In `loki-config.yml`, configure `retention_enabled: true` under `compactor`, and define `per_stream_rate_limits` with label matchers: `{level=~"INFO|DEBUG"}` → 7d, `{level=~"WARN|ERROR"}` → 30d. This avoids the complexity of multi-tenant setups.

#### Integration with Existing Logging
- Existing Logback file logging is preserved (local fallback)
- Promtail reads existing log files and pushes to Loki — no changes to current logging config
- SkyWalking Trace ID already present in logs; Loki can search by Trace ID directly

### 5. Alert Rules & Notification

#### 5.1 Alert Severity Levels

| Level | Example Triggers | Notification | Response |
|-------|-----------------|--------------|----------|
| **P1 Critical** | Service down, DB unavailable, disk >95% | All channels + repeat reminders | 15 min response |
| **P2 Warning** | CPU >80% for 5min, API latency >3s, error rate >5% | DingTalk/WeCom/Slack | 1 hour attention |
| **P3 Info** | Disk >70%, connection pool >70%, task queue backlog | Grafana panel annotation only | Handle during work hours |

#### 5.2 Predefined Alert Rules

**Infrastructure Alerts:**
- Service health check failure (sustained 1min, `for: 1m`) → P1 (avoids false alerts during rolling deployments)
- MySQL connection pool usage >80% (sustained 5min) → P2
- Redis memory >80% → P2
- JVM heap memory >85% (sustained 5min) → P2
- Disk usage >90% → P1, >70% → P3
- CPU >80% sustained 5min → P2

**Business Alerts:**
- Inbound/outbound order error rate >5% (sustained 5min) → P2
- Task queue backlog >1000 (sustained 10min) → P2
- API 5xx error rate >1% (sustained 3min) → P1
- API P99 latency >5s (sustained 5min) → P2

**Log Alerts (Loki):**
- ERROR log spike (>50 in 5 minutes) → P2
- OOM or StackOverflow keywords → P1

#### 5.3 Notification Channel Architecture

```
AlertManager
├── Webhook (universal output, any system can integrate)
├── Email (SMTP configuration)
├── DingTalk (via prometheus-webhook-dingtalk)
├── WeCom (via webhook proxy)
└── Slack (AlertManager native support)
```

**Alert suppression**: Same alert not repeated within 5 minutes. P1 alerts repeat every 10 minutes until resolved.

### 6. Docker Compose Deployment

#### New Containers

| Component | Image | Port | Memory Limit | Purpose |
|-----------|-------|------|-------------|---------|
| Prometheus | `prom/prometheus` | 9090 | 512MB | Metrics storage & query |
| Grafana | `grafana/grafana` | 3000 | 256MB | Visualization dashboards |
| Loki | `grafana/loki` | 3100 | 768MB | Log aggregation |
| Promtail | `grafana/promtail` | — | 128MB | Log collection agent |
| AlertManager | `prom/alertmanager` | 9093 | 64MB | Alert routing |
| Node Exporter | `prom/node-exporter` | 9100 | 64MB | Host metrics |
| MySQL Exporter | `prom/mysqld-exporter` | 9104 | 64MB | MySQL metrics |
| Redis Exporter | `oliver006/redis_exporter` | 9121 | 64MB | Redis metrics |
| Kafka Exporter | `danielqsj/kafka-exporter` | 9308 | 64MB | Kafka consumer lag & topic metrics |

**Total additional resources: ~2.0GB memory**

#### Configuration File Structure

```
monitoring/
├── prometheus/
│   ├── prometheus.yml          # Scrape targets & rules
│   └── alert-rules.yml         # Alert rule definitions
├── grafana/
│   ├── provisioning/
│   │   ├── datasources/        # Auto-register Prometheus + Loki
│   │   └── dashboards/         # Auto-import dashboards
│   └── dashboards/
│       ├── overview.json       # Global overview
│       ├── jvm.json            # JVM monitoring
│       ├── business.json       # Business metrics
│       └── infrastructure.json # Infrastructure
├── loki/
│   └── loki-config.yml
├── promtail/
│   └── promtail-config.yml
└── alertmanager/
    └── alertmanager.yml        # Notification channel config
```

## Changes to Existing Code

### Backend Changes

1. **`server/build.gradle`**: Add Micrometer Prometheus Registry dependency to all three servers
2. **New module `modules-utils/monitoring/`**: WesMetrics auto-config, domain event metric listeners
3. **Gateway `bootstrap.yml`**: Add Actuator endpoint exposure (health, prometheus, info)
4. **Station `bootstrap.yml`**: Add Actuator endpoint exposure (health, prometheus, info)
5. **WES `bootstrap.yml`**: Add `prometheus` to existing Actuator endpoint list

### Infrastructure Changes

1. **`docker-compose.yml`**: Add 8 monitoring containers with volume mounts
2. **`docker-compose.prod.yml`**: Mirror monitoring containers for production
3. **New `monitoring/` directory**: All monitoring configuration files
4. **`.github/workflows/deploy.yml`**: Add `monitoring/` directory to SCP deployment

### Frontend Changes (Monitoring App)

1. **New `client/src/pages/monitoring/`**: Monitoring app with Grafana iframe embedding
   - `GrafanaDashboard.tsx` — Reusable iframe component (uses `?kiosk` mode for clean embedding)
   - `overview.tsx`, `jvm.tsx`, `business.tsx`, `infrastructure.tsx` — Individual dashboard pages
2. **`client/src/routes/path2Compoment.tsx`**: Add 4 monitoring routes (`/monitoring/*`)
3. **`client/src/locales/`**: Add monitoring i18n keys (en-us, zh-cn)
4. **Liquibase changeset `db.changelog-20260523.xml`**: Insert monitoring app menu entries into `u_menu`
   - Top-level app: `system_code='monitoring'`, ID 1300000000
   - 4 menu items: Overview, JVM, Business, Infrastructure
5. **Grafana Docker config**: Enable `GF_SECURITY_ALLOW_EMBEDDING`, `GF_AUTH_ANONYMOUS_ENABLED`, `GF_AUTH_ANONYMOUS_ORG_ROLE=Viewer`

### No Changes Required

- Existing Logback configuration (Promtail reads log files directly)
- Existing SkyWalking integration (preserved, can coexist)
- Business domain code (metrics driven by domain events, zero invasion)
