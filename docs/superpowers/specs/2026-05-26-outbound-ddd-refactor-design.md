# Outbound Module DDD Deep Refactoring Design

## Background

The `wes-outbound` module (68 Java files) has solid domain logic placement but suffers from fundamental DDD violations that undermine encapsulation and layer clarity. This design addresses all identified issues through a comprehensive refactoring.

## Current Problems

### P1: Entity Encapsulation Broken (Critical)
All 8 domain entities use Lombok `@Data`, exposing public setters. Any code can bypass domain methods (e.g., `order.setOutboundPlanOrderStatus(...)` instead of `order.dispatch()`).

**Affected entities**: OutboundPlanOrder, PickingOrder, OutboundWave, EmptyContainerOutboundOrder, OutboundPlanOrderDetail, PickingOrderDetail, EmptyContainerOutboundOrderDetail, OutboundPreAllocatedRecord.

### P2: `domain/aggregate/` is Actually Application Services (High)
The 6 classes in `domain/aggregate/` are cross-aggregate orchestrators (hold multiple repositories, call external APIs like IStockApi/ITaskApi, annotated with `@Transactional`). These are application services, not DDD aggregates.

### P3: Domain Service Too Fat (High)
`PickingOrderServiceImpl` contains ~150 lines of orchestration logic (`prepareFullContext()`, `prepareAllocateCache()`, `prepareReallocateStockContext()`) that queries 6+ external APIs. This is application-layer orchestration, not domain logic.

### P4: Event Subscribers Contain Business Logic (High)
Event subscribers perform SKU extraction, status branching, warehouse area type decisions. They should be thin dispatchers.

### P5: Missing @Transactional on Event Subscribers (High)
All event subscriber methods lack `@Transactional`, while internally calling `@Transactional` aggregates, creating undefined transaction boundaries.

### P6: Unnecessary Event-Driven Indirection (Medium)
Some events (e.g., `PickingOrderDispatchedEvent` triggering `OutboundPlanOrder.dispatch()`) are steps within the same use case that don't need async event decoupling.

---

## Design

### 1. Entity Encapsulation: @Data -> @Builder

**Replace** `@Data` / `@Accessors(chain = true)` with `@Getter` + `@Builder` on all domain entities.

#### Aggregate Roots

```java
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // Required by JPA/MapStruct
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class OutboundPlanOrder extends AggregatorRoot {
    private Long id;
    private String warehouseCode;
    private String orderNo;
    private OutboundPlanOrderStatusEnum outboundPlanOrderStatus;
    private List<OutboundPlanOrderDetail> details;
    // ... all fields private, no public setters

    // Domain methods mutate state internally
    public void initialize() {
        this.orderNo = OrderNoGenerator.generationOutboundPlanOrderNo();
        this.id = SnowflakeUtils.generateId();
        this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.NEW;
        // ...
    }
    
    public void dispatch() {
        if (this.outboundPlanOrderStatus != OutboundPlanOrderStatusEnum.ASSIGNED) return;
        this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.DISPATCHED;
        // ...
    }
}
```

#### Static Factory Methods (Already Used by PickingOrder)

PickingOrder already has `create()` and `copyAndNew()` static factory methods. OutboundPlanOrder should adopt the same pattern instead of relying on external code to set fields before calling `initialize()`.

```java
// OutboundPlanOrder: Add static factory method
public static OutboundPlanOrder create(String warehouseCode, String customerOrderNo, 
        List<OutboundPlanOrderDetail> details, /* other params */) {
    OutboundPlanOrder order = OutboundPlanOrder.builder()
        .warehouseCode(warehouseCode)
        .customerOrderNo(customerOrderNo)
        .details(details)
        .build();
    order.initialize();
    return order;
}
```

#### Child Entity Setter Handling

For child entities that need parent ID assignment (e.g., `detail.setPickingOrderId(order.id)`):
- Use package-private setter: `@Setter(AccessLevel.PACKAGE)` on the `pickingOrderId` field only
- Or pass parent ID through constructor/builder

#### Value Objects (OutboundPreAllocatedRecord)

Replace `@Accessors(chain = true)` setter chains with `@Builder`:

```java
// Before:
new OutboundPreAllocatedRecord()
    .setOwnerCode(detail.getOwnerCode())
    .setSkuBatchStockId(stock.getId())
    .setQtyPreAllocated(preAllocated);

// After:
OutboundPreAllocatedRecord.builder()
    .ownerCode(detail.getOwnerCode())
    .skuBatchStockId(stock.getId())
    .qtyPreAllocated(preAllocated)
    .build();
```

#### MapStruct Compatibility

MapStruct natively supports Lombok `@Builder`. The generated mapper impl will use the builder for PO -> Domain Entity mapping. No additional configuration needed beyond existing `@Mapper(componentModel = "spring")`.

For Domain Entity -> PO mapping (only needs getters), no changes required.

#### Affected Files (8 entities)
- `OutboundPlanOrder.java`: `@Data` -> `@Getter @Builder`, add factory method
- `OutboundPlanOrderDetail.java`: `@Data` -> `@Getter @Builder`
- `PickingOrder.java`: `@Data @Accessors(chain=true)` -> `@Getter @Builder`, keep existing factory methods
- `PickingOrderDetail.java`: `@Data @Accessors(chain=true)` -> `@Getter @Builder`
- `OutboundWave.java`: `@Data` -> `@Getter @Builder`
- `EmptyContainerOutboundOrder.java`: `@Data` -> `@Getter @Builder`
- `EmptyContainerOutboundOrderDetail.java`: `@Data` -> `@Getter @Builder`
- `OutboundPreAllocatedRecord.java`: `@Data @Accessors(chain=true)` -> `@Getter @Builder`

#### Detail Method Adjustments
- `OutboundPlanOrder.initSkuInfo()`: Change `v.setSkuId()` / `v.setSkuName()` -> `detail.enrichSkuInfo(skuId, skuName)` domain method on OutboundPlanOrderDetail
- `PickingOrder.create()/copyAndNew()`: Use `@Setter(AccessLevel.PACKAGE)` on `PickingOrderDetail.pickingOrderId` for internal assignment

---

### 2. Layer Restructuring: Use Case Pattern

#### 2.1 Delete `domain/aggregate/` Package

Move all 6 classes out of `domain/aggregate/` and refactor into Use Cases under `application/usecase/`.

| Delete | Replace With |
|--------|-------------|
| `OutboundPlanOrderAggregate` | `CancelOutboundPlanOrderUseCase` |
| `OutboundPlanOrderPreAllocatedAggregate` | `PreAllocateOutboundOrderUseCase` |
| `OutboundWaveAggregate` | `CreateOutboundWaveUseCase` |
| `PickingOrderWaveAggregate` | `SplitWaveToPickingOrdersUseCase` |
| `PickingOrderTaskAggregate` | `DispatchPickingOrdersUseCase` + `ReallocatePickingOrderUseCase` |
| `EmptyContainerOutboundAggregate` | `EmptyContainerOutboundUseCase` |

#### 2.2 Use Case List

| Use Case | Responsibility | Called By |
|----------|---------------|-----------|
| `CreateOutboundPlanOrderUseCase` | DTO -> Entity, validate, save | ApiImpl |
| `CancelOutboundPlanOrderUseCase` | Load context, cancel orders/waves/picking/prealloc | ApiImpl |
| `PreAllocateOutboundOrderUseCase` | Build allocate cache, match stock, pre-allocate, lock | EventSubscriber |
| `CreateOutboundWaveUseCase` | Group assigned orders into waves | Scheduler |
| `SplitWaveToPickingOrdersUseCase` | Split wave into picking orders by area | EventSubscriber |
| `DispatchPickingOrdersUseCase` | Allocate stock, create tasks, dispatch picking + outbound orders | Scheduler |
| `ReallocatePickingOrderUseCase` | Handle abnormal, reallocate stock, create new picking orders | ApiImpl + EventSubscriber |
| `ImprovePriorityUseCase` | Update priority on outbound + picking orders | ApiImpl + EventSubscriber |
| `EmptyContainerOutboundUseCase` | Create/execute/complete/cancel empty container outbound | ApiImpl |

#### 2.3 Layer Responsibilities

| Layer | Allowed | Not Allowed |
|-------|---------|-------------|
| **ApiImpl** | DTO <-> Entity conversion, distributed lock, delegate to UseCase, read-only queries via Repository | Business logic, @Transactional on write operations |
| **UseCase** | Load entities, call domain methods, call external APIs, @Transactional, save | Business rule decisions (should be in Entity) |
| **EventSubscriber** | Extract event params, delegate to UseCase, lightweight operations (Redis cache) | Business logic, direct Repository writes |
| **Domain Service** | Pure domain calculations (wave grouping, validation rules) | Persistence, external API calls, @Transactional |
| **Entity** | State changes, business rules, publish domain events | Dependency injection, call Repository |

#### 2.4 Use Case Example

```java
@Service
@RequiredArgsConstructor
public class PreAllocateOutboundOrderUseCase {

    private final OutboundPlanOrderRepository orderRepository;
    private final OutboundPreAllocatedRecordRepository preAllocatedRepo;
    private final IStockApi stockApi;
    private final PickingOrderService pickingOrderService;

    @Transactional(rollbackFor = Exception.class)
    public void execute(Long outboundPlanOrderId) {
        OutboundPlanOrder order = orderRepository.findById(outboundPlanOrderId);
        if (order.getOutboundPlanOrderStatus() != OutboundPlanOrderStatusEnum.NEW) {
            log.error("outbound status must be NEW when preparing allocate stocks");
            return;
        }

        List<Long> skuIds = order.getDetails().stream()
            .map(OutboundPlanOrderDetail::getSkuId).toList();
        List<String> ownerCodes = order.getDetails().stream()
            .map(OutboundPlanOrderDetail::getOwnerCode).distinct().toList();

        OutboundAllocateSkuBatchContext cache = 
            pickingOrderService.prepareAllocateCache(skuIds, order.getWarehouseCode(), ownerCodes);

        List<OutboundPreAllocatedRecord> records = allocateRecords(order, cache);
        boolean result = order.preAllocate(records);
        orderRepository.saveOrderAndDetail(order);

        if (result) {
            List<SkuBatchStockLockDTO> lockDTOs = records.stream()
                .map(this::toLockDTO).toList();
            stockApi.lockSkuBatchStock(lockDTOs);
            preAllocatedRepo.saveAll(records);
        }
    }
    
    // ... private helper methods for allocation logic
}
```

#### 2.5 Thin ApiImpl Example

```java
@Primary
@Service
@Validated
@DubboService
@RequiredArgsConstructor
public class OutboundPlanOrderApiImpl implements IOutboundPlanOrderApi {

    private final CreateOutboundPlanOrderUseCase createUseCase;
    private final CancelOutboundPlanOrderUseCase cancelUseCase;
    private final ImprovePriorityUseCase improvePriorityUseCase;
    private final OutboundPlanOrderRepository orderRepository;
    private final OutboundPlanOrderTransfer transfer;

    @Override
    public void createOutboundPlanOrder(@Valid OutboundPlanOrderDTO dto) {
        createUseCase.execute(dto);
    }

    @Override
    public void cancelOutboundPlanOrder(List<Long> ids) {
        cancelUseCase.execute(ids);
    }

    @Override
    public void improvePriority(List<Long> ids, int priority) {
        improvePriorityUseCase.execute(ids, priority);
    }

    // Query methods remain in ApiImpl (no UseCase needed for reads)
    @Override
    public List<OutboundPlanOrderDTO> getOutboundPlanOrders(List<String> customerOrderNos, String warehouseCode) {
        List<OutboundPlanOrder> orders = orderRepository.findByCustomerOrderNos(warehouseCode, customerOrderNos);
        return transfer.toDTOs(orders);
    }
}
```

#### 2.6 Thin EventSubscriber Example

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundPlanOrderSubscribe {

    private final PreAllocateOutboundOrderUseCase preAllocateUseCase;
    private final OutboundPlanOrderRepository orderRepository;
    private final CallbackApiFacade callbackApiFacade;
    private final OutboundPlanOrderTransfer transfer;
    private final RedisUtils redisUtils;

    @Subscribe
    public void onCreateEvent(@Valid OutboundPlanOrderCreatedEvent event) {
        // All logic (status check, SKU extraction, cache building, pre-allocation)
        // has been moved into PreAllocateOutboundOrderUseCase.execute()
        preAllocateUseCase.execute(event.getAggregatorId());
    }

    @Subscribe
    public void onAssignedEvent(@Valid OutboundPlanOrderAssignedEvent event) {
        // Lightweight operation - OK to keep in subscriber
        String redisKey = OUTBOUND_PLAN_ORDER_ASSIGNED_IDS + event.getWarehouseCode();
        redisUtils.push(redisKey, event.getAggregatorId());
    }

    @Subscribe
    @Transactional(rollbackFor = Exception.class)
    public void onCompleteEvent(@Valid OutboundPlanOrderCompletionEvent event) {
        OutboundPlanOrder order = orderRepository.findById(event.getAggregatorId());
        callbackApiFacade.callback(CallbackApiTypeEnum.OUTBOUND_PLAN_ORDER_COMPLETE,
                order.getCustomerOrderType(), transfer.toDTO(order));
    }
}
```

---

### 3. Domain Service Slimming

#### 3.1 PickingOrderService: Move Orchestration to UseCases

| Method | Current | Move To |
|--------|---------|---------|
| `prepareFullContext()` | Queries 6+ APIs, `@Transactional(readOnly = true)` | `DispatchPickingOrdersUseCase` |
| `prepareAllocateCache()` | Queries 4+ APIs, `@Transactional(readOnly = true)` | `PreAllocateOutboundOrderUseCase` |
| `prepareReallocateStockContext()` | Batch attribute matching, `@Transactional(readOnly = true)` | `ReallocatePickingOrderUseCase` |
| `allocateStocks()` | Pure API proxy | `DispatchPickingOrdersUseCase` (inline) |
| `reallocateStocks()` | Pure API proxy | `ReallocatePickingOrderUseCase` (inline) |
| `dispatchOrders()` | Pure API proxy | `DispatchPickingOrdersUseCase` (inline) |

**Transaction note**: The prep methods currently have `@Transactional(readOnly = true)`. When moved into UseCases that have `@Transactional(rollbackFor = Exception.class)` (read-write), the readOnly hint is lost under default REQUIRED propagation (inner joins outer transaction). Two options:
1. **Accept it** — the prep queries will run in the same read-write transaction as the subsequent writes. This is simpler and the performance impact is negligible for these query sizes.
2. **Extract to separate bean** — keep prep methods in a separate `@Transactional(readOnly = true)` service, called **before** the UseCase's `@Transactional` method. This preserves read-only optimization but adds complexity.

**Recommendation**: Option 1 (accept). The prep queries are small and the read-write transaction is short-lived. Separating them adds a layer with no meaningful benefit.

**After slimming**: PickingOrderService retains only pure domain calculation methods, or may be eliminated entirely if all logic moves to UseCases or Entities.

#### 3.2 OutboundWaveService: Keep Domain Logic

`wavePickings()` contains wave grouping logic with plugin extension points. This is genuine domain logic.

`spiltWave()` contains wave-to-picking-order splitting. This is domain logic but includes entity creation. Keep the splitting calculation in Domain Service, move entity creation to UseCase.

#### 3.3 OutboundPlanOrderService: Keep Validation

`validate()` and `syncValidate()` are domain validation rules. Keep them.

`prepareCancelContext()` is query orchestration. Move to `CancelOutboundPlanOrderUseCase`.

---

### 4. Domain Event Refactoring

#### 4.1 Eliminate Unnecessary Events

Events that represent steps within the same use case should be inlined:

| Event | Current Flow | After |
|-------|-------------|-------|
| `PickingOrderDispatchedEvent` | PickingOrder.dispatch() -> Event -> OutboundPlanOrder.dispatch() | Inline in `DispatchPickingOrdersUseCase`: dispatch both in same transaction |

#### 4.2 Keep Meaningful Async Events

| Event | Reason to Keep |
|-------|---------------|
| `OutboundPlanOrderCreatedEvent` | Pre-allocation is async, decoupled from order creation |
| `OutboundPlanOrderAssignedEvent` | Redis caching, lightweight |
| `OutboundPlanOrderCompletionEvent` | External callback, fire-and-forget |
| `OutboundPlanOrderImprovedPriorityEvent` | Cross-aggregate priority sync |
| `PickingOrderPickedEvent` | OutboundPlanOrder progress update, cross-aggregate |
| `PickingOrderCompletionEvent` | Wave completion check, cross-aggregate |
| `OutboundWaveCompletionEvent` | Short-complete handling, cross-aggregate |
| `LifeCycleStatusChangeEvent` | Plugin extension point, must keep |

#### 4.3 Add @Transactional to All Event Subscribers

```java
@Subscribe
@Transactional(rollbackFor = Exception.class)
public void onPickingEvent(@Valid PickingOrderPickedEvent event) {
    // Now runs in proper transaction boundary
}
```

**Propagation behavior**: Event subscribers use default `Propagation.REQUIRED`. When a subscriber calls a UseCase (also `@Transactional` with REQUIRED), they share the same transaction. This means:
- If the UseCase fails (e.g., stock locking fails in `PreAllocateOutboundOrderUseCase`), the entire subscriber transaction rolls back. This is the **desired behavior** — partial execution (entity saved but stock not locked) would leave inconsistent state.
- If a subscriber needs independent transaction semantics (e.g., fire-and-forget callbacks), use `Propagation.REQUIRES_NEW` on that specific method.
- The `onAssignedEvent` handler (Redis push only, no DB writes) does not need `@Transactional`.

---

### 5. Aggregate Boundary Decision

#### OutboundPreAllocatedRecord: Remains Independent

Rationale:
- Created asynchronously after order creation (different transaction)
- Needs coordination with IStockApi for stock locking
- Loading entire OutboundPlanOrder aggregate for pre-allocation is expensive

Change: Remove `AggregatorRoot` inheritance (it doesn't extend it anyway). Treat as an independent persistent domain object managed by UseCases.

#### EmptyContainerOutboundOrder: Does NOT Extend AggregatorRoot

Unlike OutboundPlanOrder, PickingOrder, and OutboundWave, `EmptyContainerOutboundOrder` and `EmptyContainerOutboundOrderDetail` do not extend `AggregatorRoot` and do not publish domain events. This is an intentional design difference — empty container outbound is a simpler workflow without cross-aggregate event coordination. The @Builder refactoring still applies, but no `AggregatorRoot` inheritance should be added.

#### OutboundWave: Tighten Existing @NoArgsConstructor

OutboundWave already has a public `@NoArgsConstructor`. The migration should **tighten it to PROTECTED** (`@NoArgsConstructor(access = AccessLevel.PROTECTED)`), not add a duplicate.

#### No Other Boundary Changes

The remaining aggregate boundaries (OutboundPlanOrder, PickingOrder, OutboundWave) are well-designed and should not change.

---

### 6. Final Package Structure

```
wes-outbound/src/main/java/org/openwes/wes/outbound/
├── application/
│   ├── api/                                      # Thin API adapters
│   │   ├── OutboundPlanOrderApiImpl.java
│   │   ├── PickingOrderApiImpl.java
│   │   └── EmptyContainerOutboundOrderApiImpl.java
│   ├── usecase/                                  # Use case orchestrators
│   │   ├── CreateOutboundPlanOrderUseCase.java
│   │   ├── CancelOutboundPlanOrderUseCase.java
│   │   ├── PreAllocateOutboundOrderUseCase.java
│   │   ├── CreateOutboundWaveUseCase.java
│   │   ├── SplitWaveToPickingOrdersUseCase.java
│   │   ├── DispatchPickingOrdersUseCase.java
│   │   ├── ReallocatePickingOrderUseCase.java
│   │   ├── ImprovePriorityUseCase.java
│   │   └── EmptyContainerOutboundUseCase.java
│   ├── event/                                    # Thin event dispatchers
│   │   ├── OutboundPlanOrderSubscribe.java
│   │   ├── OutboundWaveSubscribe.java
│   │   └── PickingOrderSubscribe.java
│   └── scheduler/                                # Unchanged
│       ├── OutboundPlanOrderScheduler.java
│       ├── OutboundWaveScheduler.java
│       └── PickingOrderHandleScheduler.java
├── controller/                                   # Unchanged
│   ├── EmptyContainerOutboundController.java
│   └── OutboundPlanOrderController.java
├── domain/
│   ├── entity/                                   # @Builder refactored
│   │   ├── OutboundPlanOrder.java
│   │   ├── OutboundPlanOrderDetail.java
│   │   ├── PickingOrder.java
│   │   ├── PickingOrderDetail.java
│   │   ├── OutboundWave.java
│   │   ├── EmptyContainerOutboundOrder.java
│   │   ├── EmptyContainerOutboundOrderDetail.java
│   │   └── OutboundPreAllocatedRecord.java
│   ├── repository/                               # Unchanged
│   ├── service/                                  # Slimmed: pure domain logic only
│   │   ├── OutboundPlanOrderService.java          # validate(), syncValidate()
│   │   ├── OutboundWaveService.java               # wavePickings(), wave splitting calc
│   │   └── PickingOrderService.java               # May be eliminated or minimal
│   ├── context/                                  # Unchanged
│   │   └── OutboundPlanOrderCancelContext.java
│   └── transfer/                                 # Unchanged
│       ├── EmptyContainerOutboundTransfer.java
│       ├── OutboundPlanOrderTransfer.java
│       ├── OutboundWaveTransfer.java
│       └── PickingOrderTransfer.java
└── infrastructure/                               # Unchanged
    └── persistence/
        ├── mapper/
        ├── po/
        ├── transfer/
        └── impl/
```

**Deleted**: `domain/aggregate/` package (6 files)
**Added**: `application/usecase/` package (9 files)
**Moved**: `application/*.java` -> `application/api/*.java` (3 files)
**Modified**: All entity files (8), all event subscriber files (3), all domain service files (3-6), all api impl files (3)

---

## Implementation Priority

| Phase | Scope | Risk |
|-------|-------|------|
| Phase 0 | **Impact analysis**: Full grep across `server/` for setter calls on outbound entities, MapStruct version check, event subscriber cross-module check | None - read-only |
| Phase 1 | Entity @Builder refactoring (8 entities + MapStruct transfers) | Medium - setter call sites identified in Phase 0 |
| Phase 2 | Create UseCase classes, move aggregate logic | Low - additive, can coexist temporarily |
| Phase 3 | Slim ApiImpl, EventSubscriber, DomainService | Medium - redirect call chains |
| Phase 4 | Delete domain/aggregate/, remove unnecessary events | Low - cleanup |
| Phase 5 | Add @Transactional to event subscribers | Low - additive |
| Phase 6 | **Sync coding standards**: Update `server/Code Rule.md` and `CLAUDE.md` to reflect new patterns | Low - documentation only |

### Phase 6: Coding Standards Update

After all code changes are complete, the following documents must be updated to reflect the new architectural patterns:

#### `server/Code Rule.md`
- **Entity annotations**: Document `@Getter @Builder` as the standard, explicitly forbid `@Data` on domain entities. Add rationale (encapsulation, MapStruct compatibility).
- **Use Case pattern**: Add a new section describing the `application/usecase/` layer, its responsibilities, and its relationship to ApiImpl and EventSubscriber. Include the layer responsibility table from this design.
- **Aggregate vs UseCase**: Clarify that `domain/aggregate/` is no longer used. Cross-aggregate orchestration belongs in UseCase classes, not domain layer.
- **Event subscriber rules**: Document that subscribers must be thin dispatchers (delegate to UseCase), must have `@Transactional` on write paths, and must not contain business logic.
- **Domain Service scope**: Clarify that Domain Services are for pure domain calculations only — no persistence, no external API calls, no `@Transactional`.
- **Static factory methods**: Document the preferred entity construction pattern (static factory or `@Builder`) instead of `new` + setter chains.

#### `CLAUDE.md`
- **DDD Layer Flow**: Update the flow diagram to include UseCase layer:
  ```
  Controller -> IEntityApi (interface) -> EntityApiImpl -> UseCase -> Domain Service -> Entity -> Repository -> JPA
  ```
- **Class Naming**: Add UseCase naming convention (`[Action][Entity]UseCase`, e.g., `CancelOutboundPlanOrderUseCase`).
- **Architecture Patterns**: Add Use Case pattern description and layer responsibility summary.
- **Domain Entity Rules**: Add "Use `@Getter @Builder`, never `@Data`" to the entity rules section.

#### `docs/standards/backend.md` (New File)
Create a dedicated backend standards document (referenced from `CLAUDE.md`) covering:
- Entity encapsulation rules (@Builder pattern, no public setters)
- Use Case pattern guidelines and when to create a new UseCase vs extending an existing one
- Event subscriber thin-dispatcher pattern
- Transaction boundary conventions (UseCase owns @Transactional, Domain Service does not)
- Domain Service vs UseCase responsibility boundary

## Risks

- **MapStruct @Builder compatibility**: MapStruct 1.5+ supports `@Builder` natively. Verify the project's MapStruct version in Phase 0.
- **Setter call sites**: Phase 0 must grep across **all modules** in `server/` (not just wes-outbound) for `.set` calls on outbound domain entities. Other modules access outbound entities only through DTOs in wes-api, but entity setter usage within wes-outbound itself (subscribers, aggregates, services) is extensive and must be catalogued.
- **Event removal (PickingOrderDispatchedEvent)**: Must verify no other module subscribes to this event before removing it.
- **Transaction propagation**: Subscriber `@Transactional(REQUIRED)` + UseCase `@Transactional(REQUIRED)` = shared transaction. This is the desired behavior for write paths. Read-only hints from former `PickingOrderServiceImpl` prep methods are intentionally dropped (see Section 3.1).
