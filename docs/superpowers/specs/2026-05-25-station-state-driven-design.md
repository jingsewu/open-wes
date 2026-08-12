# Station Module: State-Driven Unified Model Redesign

## Problem Statement

The `modules-station` module was originally designed as a state-driven system where the station only handles human/device interaction without processing business logic. The current implementation has drifted from this intent:

- **Three separate object models** represent the same workstation state: `WorkStationCache` (Redis), `WorkStationVO` (frontend), `WorkStationDTO` (remote WES)
- **29 classes** exist primarily to map and transform between these models
- **12 area handler classes** rebuild the VO from cache on every GET request
- **Business logic has leaked** into the view layer (e.g., `OutboundBaseAreaHandler` decides which area to show)
- **Mode-specific state** is scattered across an inheritance hierarchy (`inputPutWallSlot` on `OutboundWorkStationCache`, `callContainers` on `InboundWorkStationCache`) rather than organized by area

## Design Principles

1. **Cache IS the API response** — no separate VO, no view-building layer
2. **Areas are first-class objects** — each UI zone is a self-contained object stored directly in the cache
3. **State mutation IS view mutation** — handlers update area objects directly; no post-hoc view reconstruction
4. **Inheritance for behavior, not state** — subclasses add mode-specific methods, not fields
5. **Recalculate on change, not on read** — derived fields (`chooseArea`, `toolbar`, `stationProcessingStatus`) are computed when state changes, not on every GET

## Unified WorkStationCache Model

```
WorkStationCache (base class — owns ALL state)
│
│  Top-level identity & state
├── id: Long
├── warehouseCode: String
├── warehouseAreaId: Long
├── stationCode: String
├── workStationMode: WorkStationModeEnum
├── workStationStatus: WorkStationStatusEnum
├── chooseArea: ChooseAreaEnum
├── hasOrder: boolean
├── stationProcessingStatus: WorkStationProcessingStatusEnum
├── workStationConfig: WorkStationConfigDTO
├── eventCode: ApiCodeEnum
├── callContainers: List<String>          // inbound only, null otherwise
│
│  Area objects (first-class, stored in cache)
├── workLocationArea: WorkLocationArea
│   └── workLocationViews: List<WorkLocationView>
│       └── workLocationSlots: List<WorkLocationSlot>
│           ├── slotCode, workLocationCode, level, bay, groupCode, enable
│           └── arrivedContainer: ArrivedContainerCache  // nullable, null = empty slot
│
├── skuArea: SkuArea
│   ├── scanCode: String
│   └── operationViews: List<SkuTaskInfo>
│       ├── skuMainDataDTO: SkuMainDataDTO
│       ├── skuBatchAttributeDTO: SkuBatchAttributeDTO
│       └── operationTaskDTOs: List<OperationTaskDTO>
│
├── putWallArea: PutWallArea              // null when mode != PICKING
│   ├── activePutWallCode: String
│   ├── inputPutWallSlot: String
│   ├── putWallDisplayStyle: String
│   ├── putWallTagConfigDTO: PutWallTagConfigDTO
│   └── putWallViews: List<PutWallDTO>
│
├── orderArea: OrderArea
│   └── currentOrder: OrderVO
│       ├── orderNo: String
│       ├── orderType: String
│       ├── stocktakeCreateMethod, stocktakeMethod, stocktakeType (stocktake-specific)
│
├── tips: List<Tip>
│   └── tipType, type, data, duration, tipCode
│
└── toolbar: Toolbar
    ├── enableReportAbnormal: boolean
    ├── enableSplitContainer: boolean
    └── enableReleaseSlot: boolean
```

### Inheritance: Behavior Only, No Extra State

```
WorkStationCache (base — all state + common behavior)
├── online(), containerArrived(), containerLeave()
├── recalculateChooseArea(), recalculateToolbar(), recalculateProcessingStatus()
│
├── OutboundWorkStationCache extends WorkStationCache
│   └── operate(), resetActivePutWall(), queryTasksAndReturnRemovedContainers()
│       reportAbnormal(), getProcessingOperationTasks()
│
├── InboundWorkStationCache extends WorkStationCache
│   └── saveCallContainers(), completeTasks()
│
└── StocktakeWorkStationCache extends WorkStationCache
    └── queryTasksAndReturnRemovedContainers(), removeOperationTask()
```

Subclasses add **zero fields** — only mode-specific methods that operate on the base class's area objects.

## Area Objects: Structure and Behavior

Each area encapsulates both data and mutation logic. They replace the 12 area handler classes.

### WorkLocationArea

Owns the physical slot layout and arrived containers.

```java
public class WorkLocationArea {
    private List<WorkLocationView> workLocationViews;

    // Mutations
    public void placeContainer(ArrivedContainerCache container);
    public void removeContainer(String containerCode);
    
    // Queries
    public boolean hasContainers();
    public List<ArrivedContainerCache> getContainersByStatus(ProcessStatusEnum status);
    public List<ArrivedContainerCache> removeProceedContainers();
}
```

Key change: `ArrivedContainerCache` lives directly on `WorkLocationSlot.arrivedContainer` instead of a flat list. No mapping needed.

### SkuArea

Owns scan state and operation task views.

```java
public class SkuArea {
    private String scanCode;
    private List<SkuTaskInfo> operationViews;

    // Mutations
    public void updateOperationViews(String skuCode, List<SkuTaskInfo> tasks);
    public void markTasksProcessing(String skuCode, String containerCode, String face);
    public void removeCompletedTask(Long taskId);
    public void reportAbnormal(Map<Long, Integer> taskAbnormalQtyMap);
    public void clear();
    
    // Queries
    public boolean hasProcessingTasks();
    public boolean hasTasks();
    public boolean hasAbnormalTasks();
    public OperationTaskDTO getFirstProcessingTask();
    public OperationTaskDTO getFirstTask();
}
```

Key change: replaces flat `operateTasks` list on cache. `processTasks(skuCode)` logic moves to `markTasksProcessing()`.

### PutWallArea

Owns put wall state including active wall and operator input.

```java
public class PutWallArea {
    private String activePutWallCode;
    private String inputPutWallSlot;
    private String putWallDisplayStyle;
    private PutWallTagConfigDTO putWallTagConfigDTO;
    private List<PutWallDTO> putWallViews;

    // Mutations
    public void input(String slotCode);
    public void clearInput();
    public void resetActivePutWall(Set<String> processingSlotCodes);
    public void updateSlotStatus(String slotCode, PutWallSlotStatusEnum status);
    
    // Queries
    public boolean hasWaitingBindingSlots();
    public Optional<PutWallSlotDTO> getSlot(String slotCode);
}
```

Key change: consolidates `inputPutWallSlot` and `activePutWallCode` (from `OutboundWorkStationCache`) and `putWallSlots` list (from base `WorkStationCache`).

### ArrivedContainerCache (simplified)

```java
public class ArrivedContainerCache {
    private String containerCode;
    private String face;
    private ProcessStatusEnum processStatus;
    private String groupCode;
    private String robotCode;
    private String robotType;
    private ContainerSpecDTO containerSpec;
    private boolean empty;

    public void init();        // → UNDO
    public void processing();  // → PROCESSING
    public void proceed();     // → PROCEED
}
```

Key change: `locationCode`, `workLocationCode`, `level`, `bay` removed — these are slot properties, not container properties.

## State Mutation Model

Handlers mutate area objects directly. Derived state is recalculated on change, not on read.

### Common Lifecycle Methods on WorkStationCache

```java
public void online(WorkStationDTO dto, OnlineEvent event) {
    // Initialize all areas from DTO (fetched once at online time)
    this.workLocationArea = buildWorkLocationArea(dto.getWorkLocations());
    if (workStationMode == PICKING) {
        this.putWallArea = buildPutWallArea(dto.getPutWalls());
    }
    this.skuArea = new SkuArea();
    this.toolbar = new Toolbar();
    this.tips = new ArrayList<>();
}

public void containerArrived(ArrivedContainerCache container) {
    workLocationArea.placeContainer(container);
    recalculateProcessingStatus();
}

public void containerLeave(String containerCode) {
    workLocationArea.removeContainer(containerCode);
    recalculateProcessingStatus();
}
```

### Recalculate Pattern

```java
// Replaces OutboundBaseAreaHandler.setChooseArea() — now on cache entity
protected void recalculateChooseArea() {
    if (skuArea.hasProcessingTasks()) {
        this.chooseArea = PUT_WALL_AREA;
    } else if (putWallArea != null && putWallArea.hasWaitingBindingSlots()) {
        this.chooseArea = PUT_WALL_AREA;
    } else if (skuArea.hasTasks()) {
        this.chooseArea = SKU_AREA;
    } else {
        this.chooseArea = PUT_WALL_AREA;
    }
}

// Replaces OutboundBaseAreaHandler.setToolbar()
protected void recalculateToolbar() {
    toolbar.setEnableReportAbnormal(
        workLocationArea.hasContainers() && !skuArea.hasAbnormalTasks()
    );
    toolbar.setEnableReleaseSlot(true);
    toolbar.setEnableSplitContainer(true);
}

// Replaces InboundBaseAreaHandler/StocktakeBaseAreaHandler.setStationProcessingStatus()
protected void recalculateProcessingStatus() { ... }
```

### Handler Mutation Examples

```java
// ContainerArrivedHandler
workStationCache.getWorkLocationArea().placeContainer(container);
workStationCache.recalculateProcessingStatus();

// ScanBarcodeHandler
workStationCache.getSkuArea().markTasksProcessing(skuCode, containerCode, face);
workStationCache.recalculateChooseArea();

// TapPutWallSlotHandler
workStationCache.getSkuArea().removeCompletedTask(taskId);
workStationCache.getPutWallArea().updateSlotStatus(slotCode, newStatus);
workStationCache.recalculateChooseArea();
workStationCache.recalculateToolbar();

// InputHandler (no more casting!)
workStationCache.getPutWallArea().input(input);

// ReportAbnormalHandler (no more casting!)
workStationCache.getSkuArea().reportAbnormal(taskAbnormalQtyMap);
workStationCache.recalculateToolbar();

// CallContainerHandler (no more casting!)
workStationCache.addCallContainers(event.getContainerCodes());
workStationCache.recalculateProcessingStatus();
```

## Persistence

### Redis

No PO layer. `WorkStationCache` is annotated with `@RedisHash` and serialized directly. One class, one serialization path — no polymorphic deserialization issues for state (subclass methods don't affect serialization).

```java
@RedisHash("work_station_cache")
public class WorkStationCache {
    @Id
    private Long id;
    // ... all area objects serialized by Jackson
}
```

### What Gets Deleted

- `WorkStationCachePO`, `InboundWorkStationCachePO`, `OutboundWorkStationCachePO`, `StocktakeWorkStationCachePO`
- `WorkStationCacheTransfer`, `ArriveContainerCacheTransfer` (MapStruct mappers + generated impls)

### Repository

```java
public interface WorkStationCacheRepository extends CrudRepository<WorkStationCache, Long> {
    // No generic <T> needed
}
```

## API Layer

### station-api Module

Area classes move into `station-api` as the shared model:

```
station-api/
├── model/
│   ├── WorkStationCache.java
│   ├── WorkLocationArea.java
│   ├── SkuArea.java
│   ├── PutWallArea.java
│   ├── OrderArea.java
│   ├── ArrivedContainerCache.java
│   ├── Tip.java
│   └── Toolbar.java
└── constants/
    ├── ChooseAreaEnum.java
    └── ProcessStatusEnum.java
```

### Controller

```java
@GetMapping
public WorkStationCache getView() {
    Long workStationId = HttpStationContext.getWorkStationId();
    return workStationService.getOrThrow(workStationId);
}
```

### What Gets Deleted

- `WorkStationVO`, `WorkLocationExtend`
- `WorkStationCacheDTO`, `InboundWorkStationCacheDTO`, `OutboundWorkStationCacheDTO`, `StocktakeWorkStationCacheDTO`, `ArrivedContainerCacheDTO`
- `ViewHelper`, `ViewContext`, `ViewHandlerTypeEnum`
- All 12 area handler classes under `controller/view/handler/`

## Frontend Impact

Minimal changes — mostly renames:

| Change | Impact |
|--------|--------|
| `skuArea.pickingViews` -> `skuArea.operationViews` | Rename in TypeScript interface + components |
| `scanCode` (top-level) -> `skuArea.scanCode` | Move access path |
| `WorkStationView<T>` generic -> `WorkStationView` | Simplify type |
| `putWallArea` gains `activePutWallCode`, `inputPutWallSlot` | New fields exposed, no behavior change |

## Complete Change Summary

### Deleted (29 classes)

| Category | Classes | Count |
|----------|---------|-------|
| View layer | `ViewHelper`, `ViewContext`, `ViewHandlerTypeEnum` | 3 |
| Generic area handlers | `BaseAreaHandler`, `SkuAreaHandler`, `ContainerAreaHandler`, `PutWallAreaHandler`, `OrderAreaHandler`, `TipsHandler` | 6 |
| Mode-specific area handlers | `OutboundBaseAreaHandler`, `OutboundSkuAreaHandler`, `OutboundPutWallAreaHandler`, `OutboundContainerAreaHandler`, `InboundBaseAreaHandler`, `StocktakeBaseAreaHandler`, `StocktakeSkuAreaHandler` | 7 |
| PO classes | `WorkStationCachePO`, `InboundWorkStationCachePO`, `OutboundWorkStationCachePO`, `StocktakeWorkStationCachePO` | 4 |
| DTO classes | `WorkStationCacheDTO`, `InboundWorkStationCacheDTO`, `OutboundWorkStationCacheDTO`, `StocktakeWorkStationCacheDTO`, `ArrivedContainerCacheDTO` | 5 |
| Transfer mappers | `WorkStationCacheTransfer`, `ArriveContainerCacheTransfer` | 2 |
| VO | `WorkStationVO`, `WorkLocationExtend` | 2 |

### Added (~6 area model classes in station-api)

`WorkLocationArea`, `SkuArea`, `PutWallArea`, `OrderArea`, `Toolbar`, `Tip`

### Modified

| Class | Change |
|-------|--------|
| `WorkStationCache` | Flat fields -> area objects. Add common behavior + `recalculate*()` methods. |
| `OutboundWorkStationCache` | Remove fields, keep behavior methods operating on areas. |
| `InboundWorkStationCache` | Remove fields, keep behavior methods operating on areas. |
| `StocktakeWorkStationCache` | Behavior methods operate on areas. |
| `ArrivedContainerCache` | Remove slot-related fields (belong on slot, not container). |
| `WorkStationService` | Remove generic `<T>`. Factory still creates subclasses. |
| `WorkStationCacheRepository` | Remove generic `<T>`. |
| `StationApiController` | `GET /api` returns cache directly. |
| Business handlers | Mutate area objects instead of flat lists. No more casting. |
| Extensions | Same pattern, operate on areas. |

### Unchanged

- `HandlerExecutor` / `HandlerExecutorImpl`
- `BusinessHandlerFactory`
- `ExtensionFactory` and all extension implementations (interface unchanged)
- All event classes
- `ApiCodeEnum`, `ProcessStatusEnum`
- All remote service classes
