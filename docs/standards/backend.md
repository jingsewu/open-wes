# Coding Standards and Best Practices

These are a set of coding standards and best practices to be followed during development. They cover various aspects, including code design, database design, validation, and testing. The main goals are to promote consistency, maintainability, and best practices across the codebase.

## Table of Contents

- [Code Rules](#code-rules)
  - [Transaction](#transaction)
  - [Error Definition](#error-definition)
  - [Utils Usage](#utils-usage)
  - [Domain Entity](#domain-entity)
  - [Domain Service](#domain-service)
  - [Domain Aggregate](#domain-aggregate)
  - [Domain Repository](#domain-repository)
  - [Domain Events](#domain-events)
  - [API Implementation](#api-implementation)
  - [Server](#server)
- [Database Rules](#database-rules)
- [Validation Rules](#validation-rules)
- [Plugin Rules](#plugin-rules)
  - [Script Plugin Rules](#script-plugin-rules)
  - [Jar Plugin Rules (Legacy)](#jar-plugin-rules-legacy)
- [Unit Test Rules](#unit-test-rules)
- [Logging and Configuration](#logging-and-configuration)

## Code Rules

### Transaction

1. **Avoid Big Transactions, Use Asynchronous and Eventually Consistent Approach**: Big transactions can lead to performance issues and potential deadlocks. Instead, use an asynchronous and eventually consistent approach to handle complex operations.
2. **Use @Transactional Annotation Appropriately**: We only use the `@Transactional` annotation on Aggregates and repositories. Any other places should not use this annotation.

### Error Definition

1. **Module-Specific Error Codes**: Each module should define its own error codes instead of relying on a common error code class. This promotes better encapsulation and maintainability within each module. For example, we have defined `InboundErrorDescEnum`, `OutboundErrorDescEnum`, and so on.

### Utils Usage

1. **Module Independence**: The `modules-utils` module should not depend on other modules, meaning it should not import or contain any domain classes. This module should only define common utility functions and classes.

2. **Redis Operations**: Use the `RedisUtils` class instead of `RedisTemplate` to perform Redis operations. For example:
   ```java
   redisUtils.get(key);
   ```

3. **Domain Event for Asynchronous Communication**: Use Domain Events to implement asynchronous communication between different domains. For example:
   ```java
   DomainEventPublisher.sendAsyncDomainEvent(stockTransferEvent);
   DomainEventPublisher.sendAsyncDomainEvent(orderEvent);
   ```

4. **Message Queue for Asynchronous Communication**: Use the `MqClient` class to implement asynchronous communication between servers via a message queue. For example:
   ```java
   mqClient.sendAsyncMessage(stockTransferEvent);
   mqClient.sendAsyncMessage(orderEvent);
   ```

5. **Distributed Locking**: Use the `DistributedLock` class to implement distributed locking mechanisms. For example:
   ```java
   distributedLock.acquireLock("lockKey", 1000);
   distributedLock.releaseLock("lockKey");
   ```

### Domain Entity

1. **Aggregate Root**: Aggregate root entities must extend `AggregatorRoot`. This base class provides domain event publishing and optimistic locking support.

2. **Domain Entity Immutability**: Domain entities should not have setter methods. Instead, use methods that represent the entity's state transitions. For example:
   ```java
   // Instead of outboundPlanOrder.setStatus(xxx);
   outboundPlanOrder.complete();
   
   // In the OutboundPlanOrder class
   void complete() {
      this.status = COMPLETE;
   }
   ```

3. **Domain Entity Lifecycle**: Domain entities should have methods representing their full lifecycle. For example, the `OutboundPlanOrder` class should have methods like `initialize()` to set the initial status, `preAllocate()` to change the status to `ASSIGNED`, and so on. This approach makes the entity more "alive" and encapsulates the business logic within the entity.

   This practice promotes a better understanding of the entity's behavior and state transitions, making it easier to maintain and extend the codebase.

   Example:
   ```java
   public class OutboundPlanOrder extends AggregatorRoot implements Serializable {
   
       public void initialize() {
           this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.NEW;
       }
   
       public boolean preAllocate(List<OutboundPreAllocatedRecord> planPreAllocatedRecords) {
           this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.ASSIGNED;
           // Additional logic...
           return true;
       }
   
       public void cancel() {
           log.info("outbound plan order id: {}, orderNo: {} cancel", this.id, this.orderNo);
           if (OutboundPlanOrderStatusEnum.isCancellable(this.outboundPlanOrderStatus)) {
               this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.CANCELED;
               this.details.forEach(OutboundPlanOrderDetail::cancel);
           }
       }
   
       // Other lifecycle methods...
   }
   ```

4. **String Field Length**: String type field lengths must be no more than 512 characters. If you need to store more than 512 characters, use a text type and split the data into another table.

5. **Optimistic Locking**: Use `@Version` on a version field for optimistic locking and concurrency control.

### Domain Service
1. **Domain Service Responsibilities**: Domain services should only contain calculation logic, and they must be stateless. For example, the `OutboundWaveService` contains a function `wavePickings` that takes a list of `OutboundPlanOrder` objects as input and returns the waved `OutboundPlanOrder` objects.
   ```java
   public interface OutboundWaveService {
       Collection<List<OutboundPlanOrder>> wavePickings(List<OutboundPlanOrder> outboundPlanOrders);
   }
   ```

### Domain Aggregate
1. **Domain Aggregate Usage**: Domain aggregates will aggregate multiple domain entities. If we need to update multiple entities, we should build an aggregator and use the `@Transactional` annotation to ensure data consistency. Since we use the `@Transactional` annotation, we should pull queries out to improve speed.

### Domain Repository
1. **Domain Repository Responsibilities**:  Domain repositories should only contain find or save functions. They should not contain any update functions. For example:
```java
// Not compliant
void updateContainerStatus(Long containerId, ContainerStatusEnum status);

// Compliant
void saveContainer(Container container);
// Use like
Container container = containerRepository.findById(containerId);
container.putAway();
containerRepository.save(container);
```
2. **Function Naming**: All query functions should be named as find or findAll, and save functions should be named as save or saveAll.

### Domain Events

1. **Publishing Events from Entities**: Aggregate root entities publish domain events via `addAsynchronousDomainEvents()`. Events are dispatched after the transaction commits.
   ```java
   public void complete() {
       this.status = COMPLETE;
       addAsynchronousDomainEvents(new OrderCompletedEvent(this.id));
   }
   ```

2. **Event Subscribers**: For cross-cutting concerns (e.g., metrics, audit), use Guava `@Subscribe` methods on `@Component` beans. The `DomainEventProcessor` (a `BeanPostProcessor`) auto-registers any bean with `@Subscribe` methods to the EventBus — no manual wiring needed.

### API Implementation

1. **Standard Annotations**: API implementation classes must use the following annotation set:
   ```java
   @Primary
   @Service
   @Validated
   @DubboService
   @RequiredArgsConstructor
   public class EntityApiImpl implements IEntityApi { }
   ```

2. **Class Naming Conventions**:

   | Type | Pattern | Example |
   |------|---------|---------|
   | API interface | `I[Entity]Api` | `IInboundPlanOrderApi` |
   | API impl | `[Entity]ApiImpl` | `InboundPlanOrderApiImpl` |
   | Service | `[Entity]Service` / `[Entity]ServiceImpl` | `OutboundWaveService` |
   | Repository | `[Entity]Repository` / `[Entity]RepositoryImpl` | `ContainerRepository` |
   | DTO | `[Entity]DTO` | `ContainerDTO` |
   | Transfer | `[Entity]Transfer` | `InboundPlanOrderTransfer` |
   | Error enum | `[Module]ErrorDescEnum` | `InboundErrorDescEnum` |

3. **Logging**: Use `@Slf4j` (Lombok) for logger declaration. All log messages must be written in English.

### Server
1. **Server Module Responsibilities**: The server module cannot include business codes. It should only contain common configurations and the Spring Boot starter class.

## Database Rules

1. **Column Length Standards**: Follow these guidelines for column lengths:
   - Code: 64 characters
   - Name: 128 characters
   - Description: 255 characters
   - Remark: 500 characters
   - Enum: 20 characters

2. **POJO Class Design**: POJO classes must be designed as standard POJO classes, meaning they should not contain any special types or special database-specific configurations. Use `@Comment` for column comments (comments may be in Chinese for domain terminology).

   Instead of:
   ```java
   @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库'")
   private String warehouseCode;
   ```

   Use:
   ```java
   @Column(nullable = false, length = 64)
   @Comment("仓库")
   private String warehouseCode;
   ```

   This approach separates the database-specific configurations from the POJO class, promoting better maintainability and portability of the code.

## Validation Rules

1. **API Interface Method Validation**: Add `@Valid` or other validation annotations like `@NotEmpty` in the API interface method parameters.

   Example:
   ```java
   public interface IBatchAttributeConfigApi {
   
       void save(@Valid BatchAttributeConfigDTO batchAttributeConfigDTO);
   
       void update(@Valid BatchAttributeConfigDTO batchAttributeConfigDTO);
   
       BatchAttributeConfigDTO getByOwnerAndSkuFirstCategory(@NotNull String ownerCode, @NotNull String skuFirstCategory);
   }
   ```

2. **Service Implementation Validation**: Add `@Validated` in the service implementation classes.

   Example:
   ```java
   @Validated
   @Service
   public class BatchAttributeConfigApplicationImpl implements IBatchAttributeConfigApi {
       // ...
   }
   ```

   If your parameters are objects that need validation, add `@Validated(value = ValidationSequence.class)` instead of `@Validated`, and the object needs to implement the `IValidate` interface.

## Plugin Rules

Script plugins are the default for new plugin work: a plugin is a versioned bundle of Groovy scripts plus extension declarations, stored in the database and materialized into a running server at enable time. The PF4J jar-plugin path is kept for existing plugins and is frozen; do not add new jar plugins.

### Script Plugin Rules

1. **Contract Catalog Is the Single Source of Truth**: The allowed extension points, script methods and blocking semantics are defined once in `PluginContractCatalog.ARCHETYPES` (`org.openwes.script.plugin.api.contract`). The AI prompt, this documentation and the enable-time validation all read that catalog. The table below is generated by `PluginContractCatalog.toMarkdownTable()` and enforced by `PluginDocumentationConsistencyTest` — never edit it by hand; change the catalog and regenerate.

<!-- BEGIN GENERATED: script-plugin-archetypes -->
| Archetype | Host extension point | Script method | Blocking |
|-----------|----------------------|---------------|----------|
| `OUTBOUND_WAVE_WAVE_ACTION` | `org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveWaveAction` | `wave` | yes |
| `OUTBOUND_WAVE_SPLIT_ACTION` | `org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveSplitAction` | `split` | yes |
| `REQUEST_VALIDATE` | `org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin` | `validate` | yes |
| `REQUEST_SUPPLY` | `org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin` | `supply` | no |
| `REQUEST_AFTER_INVOKE` | `org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin` | `afterInvoke` | no |
| `CUSTOM_REQUEST_INVOKE` | `org.openwes.plugin.extension.business.api.platform.request.ICustomRequestPlugin` | `invoke` | yes |
| `CUSTOM_API` | `org.openwes.plugin.extension.business.station.ICustomApiPlugin` | `execute` | yes |
| `LIFECYCLE_LISTENER` | `org.openwes.plugin.extension.business.IEntityLifecycleListener` | `afterStatusChange` | no |
| `HTTP_ENDPOINT` | dispatcher (`/plugin-platform/ext/{pluginKey}/**`) | `handle` | yes |
| `CRON` | dispatcher (scheduler) | `run` | no |
<!-- END GENERATED: script-plugin-archetypes -->

2. **Script Method Signature: `ctx` Is Always the Last Parameter**: The runtime appends the extension's `ScriptContext` as the final argument of every invocation, after the host method arguments. Declare every script method with the host parameters first and `ctx` last; a method that does not accept the appended argument fails at invocation time (the smoke stage only checks that the declared method exists). Script instances are created with Spring's `createBean` inside a child context whose parent is the application context, so `@Autowired`, `@Resource`, `@Value` and `@PostConstruct` all work. Use `ctx.log()` for logging, `ctx.bean(...)` or the facade methods (`ctx.outbound()`, `ctx.pickingOrder()`, `ctx.callback()`, `ctx.station()`) for host beans, and `ctx.configString/configInt/configBool` for plugin config. Secrets come from `ctx.config` only — never hardcode them in a script.

```groovy
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi
import org.springframework.beans.factory.annotation.Autowired

class Wave {
    @Autowired
    IOutboundPlanOrderApi outbound

    Object wave(Object orders, Object ctx) {
        ctx.log().info('grouping {} orders for plugin {}', orders.size(), ctx.pluginKey())
        return orders.collect { [it] }
    }
}
```

3. **Script Plugin Failure Policy**: Failures never disappear silently, and only blocking archetypes propagate the failure to the host. Blocking means the `ScriptPluginInvocationException` reaches the host, which then decides how to react: the `OUTBOUND_WAVE_WAVE_ACTION` and `OUTBOUND_WAVE_SPLIT_ACTION` hosts catch any exception from the plugin (and accept a `null` return) and fall back to the built-in grouping/splitting, so a wave plugin never aborts wave creation.

| Situation | Behavior |
|-----------|----------|
| Blocking archetype throws or times out | `ScriptPluginInvocationException` is thrown to the host; unless the host defines a fallback (as the wave hosts do), the host operation aborts. |
| Non-blocking archetype throws or times out | The error is logged (`script plugin ... failed (non-blocking, continuing)`) and the default value for the return type is returned; the host continues. |
| `HTTP_ENDPOINT` throws or times out | The dispatcher logs the error and responds HTTP 500 with `Response.fail()`; the HTTP call fails. |
| `CRON` throws | The failure is logged and counted; after `script-plugin.cron.max-consecutive-failures` (default `10`) consecutive failures the plugin is disabled automatically through the management plane. |

Every invocation is reported to `p_script_plugin_execution_log` with plugin key, revision, extension type, success flag, duration and error message. Shipping the log must never break the invocation path; a failed shipment is only warned. Script execution is bounded by `script-plugin.execution.timeout-ms` (default `2000`).

4. **Priority and Built-in Fallback**: `IPlugin.priority()` returns `0` by default; the higher value wins when several plugins can serve the same extension point. `PluginSdkUtils.getExtractObject` and `LifecycleListenerRegistry` merge PF4J jar plugins with materialized script extensions and sort them by priority descending. Ties are broken by stable order: jar plugins are appended first, then script plugins in registry order (plugin key ascending). A script proxy answers `priority()` from its declaration's `priority` field. When no plugin for an extension point is enabled, the extracted list is empty and the calling code must keep its built-in behavior — a plugin never replaces the default unless it is enabled.

5. **Enable, Disable and Rollback Flow**: A plugin starts as a draft and receives immutable revisions, each with a content hash; `current_revision_id` points at the active revision. A revision can be saved only while the plugin is `DRAFT` or `DISABLED`; saving on an `ENABLED` plugin is rejected because the runtime would hot-deploy an unvalidated revision, and saving on an `ARCHIVED` plugin is rejected because archived plugins are read-only. To deploy a changed revision of an enabled plugin, disable it, save the new revision, then enable it again (validation runs). Enable is accepted only from `DRAFT` or `DISABLED`, only when a current revision exists, and only when the plugin's `sdk_version` equals the platform contract (`ScriptPluginApiConstants.CONTRACT_VERSION`, currently `1.0`). Validation runs on the node whose app matches `apply_system`: wes validates in-process; a remote app such as station is asked through the pending-validation channel — the management plane registers the pending request and publishes a management message, and the target node's `StationValidationPoller` polls `getPendingValidation` every `script-plugin.validation.poll-interval-ms` (default `5000` ms), dry-runs the bundle and reports the verdict back within the configured timeout. The Redis message itself wakes `ScriptPluginRegistry.refreshAll`; it is not what delivers the validation request. A failed validation leaves the plugin disabled and records the failing stage and message.

   The five validation stages are:
   1. `sdkVersion` — the plugin's `sdk_version` equals the platform contract version.
   2. `declarations` — static legality: known extension type, script and method present, declared method matches the archetype's script method, referenced script exists, `apiType` for request archetypes, `entityName` for lifecycle listeners, `customApiCode` for custom APIs, valid non-duplicate HTTP path/method, parseable cron expression.
   3. `compile` — every Groovy script compiles.
   4. `context` — dry-run materialization in a throwaway child context, including injection and `@PostConstruct`.
   5. `smoke` — every declared script actually declares the declared method.

   On success, `enable` publishes the management message; running nodes rematerialize without a restart (Redis message, with a 60 s registry-version poll as the safety net). A failed rematerialization keeps the previous revision running. `disable` only applies to an `ENABLED` plugin and makes every node drop the materialization, restoring the built-in behavior. `rollback` is a pointer switch to any revision that belongs to the plugin — earlier, current or later (archived plugins cannot be rolled back) — and republishes the bundle; revision history is never mutated. Rollback does not re-run the five validation stages, so operators should only roll back to a revision that already passed enable-time validation.

6. **Lifecycle Listeners Are Not `IPlugin`s**: `IEntityLifecycleListener` intentionally does **not** extend `IPlugin`. In the merge it therefore keeps priority `0` and stable order. Listeners are dispatched by `getEntityName()` matching the entity name through `LifecycleListenerRegistry`, one call per registered listener; a script `LIFECYCLE_LISTENER` declaration must set `entityName` and expose the `afterStatusChange` method.

7. **Runtime Wiring**: `wes-server` depends on both `script-plugin-core` (management plane) and `script-plugin-runtime`, and sets `script-plugin.registry.mode: local` so enable-time validation runs in-process. `station-server` depends on `script-plugin-runtime` only and sets `mode: remote`; it must also be annotated with `@EnableScheduling`, because registry polling, cron dispatch and the validation poller are `@Scheduled`. Both applications scan `org.openwes`, which covers `org.openwes.script.plugin.runtime`. Any other server embedding the runtime uses `mode: remote` and a Dubbo reference to the management plane.

   **v1 limitations:** (a) `apply_module` is stored and displayed but not enforced — a node materializes every enabled plugin for its `apply_system`, because the frozen `IScriptPluginApi.getEnabledBundles(String applySystem)` has no module argument; (b) compile/context dry-run and `@PostConstruct` are not time-bounded — only script invocations honour `script-plugin.execution.timeout-ms`; (c) self-provided HTTP endpoints are served by the app hosting the plugin — the gateway routes `/plugin-platform/**` to wes, so station HTTP endpoints are only reachable directly; (d) the workbench authorizes with server-side role checks against configurable role codes (`plugin.security.developer-role-codes`, default `pluginDeveloper`; `plugin.security.admin-role-codes`, default `pluginAdmin`; the existing super role is implicit). The configured codes are read at startup, so changing a code requires a wes-server restart. Every state-changing workbench action is appended to `p_script_plugin_operation_audit`; the gateway's `get:/plugin-platform/` and `post:/plugin-platform/` permissions only gate broad access, not individual actions; (e) a hot revision switch closes the previous child context, so an in-flight invocation on the old revision can fail during the switch (failure policies contain it).

8. **AI Draft Pipeline (`ai-plugin`)** — AI may only save revisions, never enable, disable, roll back or archive. Model output is untrusted: it is parsed into `AiDraftDocument`, checked against `PluginContractCatalog.typesForApp`, then run through the same static validation as enable-time; on wes it also gets the in-process materialization dry-run, while station drafts are static-only in v2 (preview handshake lands with the workbench). Every AI revision stores `RevisionOriginEnum.AI`, the model id and `PluginPromptTemplates.PROMPT_VERSION`. Never log requirements, prompts, model output or generated sources at info level. Generated scripts must read secrets through `ctx.config`, never hardcode them; prompts state the same rule. The SSE endpoint is `/plugin-platform/ai/draft/stream`; the gateway and any reverse proxy must disable response buffering for it. Every AI endpoint (`stream`, `generations`, `refreshCorpus`) requires the developer role through `PluginWorkbenchAccessService`, and each accepted AI revision is audited as `SAVE_REVISION` with actor, revision id and origin.

9. **Workbench Authorization and Audit** — `PluginWorkbenchAccessService` is the single authorization point for the workbench; controllers and domain services never read roles directly. Administrator implies developer, and the configured super role is implicitly both. Every management action (`createDraft`, `saveRevision`, `validate`, `enable`, `disable`, `archive`, `rollback`, `updateMetadata`, config save, test-run) goes through a workbench service (`ScriptPluginWorkbenchService`, `ScriptPluginConfigService`, or `ScriptPluginTestRunService`) and is appended to `p_script_plugin_operation_audit` with actor, action, plugin key, revision id, success and a short detail. Audit failures are logged and never change the business result. Denials are audited only for state-changing actions; read-only denials are logged at warn level. Audit details must never contain script bodies, prompts, config values, request bodies, test arguments or test results. `disableBySystem` is the only path that skips the role check, and only for internal callers such as the cron circuit breaker. Test-run executes the real script on the wes node inside a throwaway revision context, writes no execution log and is rejected for non-wes plugins; config saves use the `lockVersion` optimistic lock, and validation never echoes values.

10. **Example: Lazy Waving**: `server/modules-plugin/plugin-example/README.md` is a runnable sample, not a built module. It declares one `OUTBOUND_WAVE_WAVE_ACTION` whose `wave` method lives in `lazy-waving.groovy`, plus two string config fields: `packingStatusApiCode` (default `GET_PACKING_STATUS`) and `packingStatusOnlineValue` (default `1`). It is not seeded automatically: create the draft in the workbench (`/plugin-platform/plugin-workbench?mode=new`), save a revision, validate it and enable it as an administrator. `ctx.outbound()` and `ctx.callback()` are wes-only facade methods; read secrets through `ctx.config` only.

### Jar Plugin Rules (Legacy)

The jar-plugin path is frozen: it remains for plugins already packaged with PF4J, but new work must use a script plugin. These rules describe the real interfaces.

1. **One Domain Plugin Interface per Domain**: Every business domain has one extension-point interface that extends `IPlugin` and exposes that domain's lifecycle hooks. `IPlugin` extends PF4J's `ExtensionPoint` and provides default no-op `initialize()` and `destory()` methods and a `priority()` that defaults to `0` (higher wins).

```java
public interface IOutboundWavePlugin extends IPlugin, IEntityLifecycleListener<Long, String> {
    default String getEntityName() {
        return "OutboundWave";
    }
}
```

   `IEntityLifecycleListener<U, V>` supplies `afterStatusChange(U entityId, V entityOrderNo, String newStatus)` and requires `getEntityName()`. Implementations return the host entity name they listen to: `IOutboundPlanOrderPlugin` returns `OutboundPlanOrder`, `IOutboundWavePlugin` returns `OutboundWave`, and `IContainerTaskPlugin` returns `ContainerTask`.

2. **Action Interfaces Extend the Domain Interface**: Additional capabilities are separate action interfaces that extend the domain interface, so an implementation registered for the domain is also discoverable through the action. Methods default to `null`, and the caller treats `null` as "no plugin result, use built-in behavior".

```java
public interface IOutboundWaveWaveAction extends IOutboundWavePlugin {
    default List<List<OutboundPlanOrderDTO>> wave(List<OutboundPlanOrderDTO> orders) {
        return null;
    }
}

public interface IOutboundWaveSplitAction extends IOutboundWavePlugin {
    default List<PickingOrderDTO> split(OutboundWaveDTO wave) {
        return null;
    }
}
```

3. **Lookup, Priority and Fallback**: Business code looks up extensions through the injected `PluginSdkUtils.getExtractObject(Class<T>)`. It merges PF4J jar extensions with materialized script-plugin proxies, sorts by `priority()` descending, and keeps jar plugins first for equal priorities. When no plugin is enabled the returned list is empty; the caller must then run its built-in logic. A plugin result of `null` means "no result" as well.

```java
List<IOutboundWaveWaveAction> plugins = pluginSdkUtils.getExtractObject(IOutboundWaveWaveAction.class);
if (!plugins.isEmpty()) {
    List<List<OutboundPlanOrderDTO>> waves = plugins.get(0).wave(orders);
    if (waves != null) {
        return waves;
    }
}
// built-in wave grouping
```

4. **Jar Plugin Configuration and Packaging**: A jar plugin is packaged with a `plugin.properties` descriptor and implementations are annotated with PF4J's `@Extension`; the module processes `org.pf4j:pf4j` through `annotationProcessor`. Its configuration is read with `PluginUtils.getPluginConfig(pluginId, Class<T>)`, which resolves the stored config through `IPluginApi.getPluginConfig` and returns a fresh instance when no config is stored. The former jar sample inside `server/modules-plugin/plugin-example/` has been replaced by the script-plugin sample; the jar path receives no new samples.

## Unit Test Rules

1. **Test Location**: All unit tests must be written within the respective module, not in the `xxx-server` module.

2. **Test Coverage**: Create unit tests for the domain layer, including entities and services. You can refer to the outbound tests as an example.

3. **Avoid Starting Spring Boot**: Unit tests should avoid running the Spring Boot application.

## Logging and Configuration

### Logging
1. **Logger Declaration**: Use `@Slf4j` (Lombok) for logger declaration. Do not manually create Logger instances.
2. **Log Language**: All log messages must be written in English.

### Configuration
1. **Configuration Classification**: Configurations are divided into two main categories: Environment Configuration and Business Configuration.
2. **Environment Configuration**: Environment configurations must be configured in Nacos.
3. **Business Configuration**: Business configurations must be configured in the database (e.g., MySQL).
4. **Nacos Restrictions**: Nacos cannot be used to configure any business configurations.
