# Station Snapshot: API Return Value Redesign

## Problem

The station state-driven design (`2026-05-25-station-state-driven-design.md`) establishes that **the cache IS the API response** and **state mutation IS view mutation**. Handlers should mutate cache area objects directly so the frontend sees the updated state on the next GET request.

However, the current `bindContainer`, `unbindContainer`, and `sealContainer` operations break this principle:

1. The handler calls a remote `ITaskApi` RPC (which processes the business logic in WES)
2. The RPC returns `void`
3. The handler updates nothing in the local cache beyond clearing input state
4. The local cache remains **stale** — the put wall slot status in the cache still shows the pre-operation state
5. The frontend sees stale data until some MQ event eventually updates the cache (if one exists at all)
6. For `bindContainer`/`unbindContainer`/`sealContainer`, no MQ event is published, so the cache stays stale indefinitely

## Solution

**Change the three `ITaskApi` methods from `void` to returning `PutWallSlotDTO`**, and propagate the return value through the entire call chain so the station handler can immediately apply it as a snapshot via `PutWallArea.applySnapshot()`.

This eliminates the stale cache window without adding extra RPC calls — the snapshot data flows naturally through the existing call chain.

## Change Scope: 4 API Layers

### Layer 1: `IPutWallApi` (wes-api) — The source of truth

Three methods change from `void` to `PutWallSlotDTO`:

```java
PutWallSlotDTO bindContainer(BindContainerDTO dto, Long id);
PutWallSlotDTO unBindContainer(UnBindContainerDTO dto);
PutWallSlotDTO sealContainer(String putWallSlotCode, Long workStationId);
```

Implementation in `PutWallApiImpl` — after `save()`, convert the just-saved entity to DTO:

```java
@Override
public PutWallSlotDTO bindContainer(BindContainerDTO dto, Long transferContainerRecordId) {
    PutWallSlot slot = putWallSlotRepository
        .findBySlotCodeAndWorkStationId(dto.getPutWallSlotCode(), dto.getWorkStationId());
    slot.bindContainer(dto.getContainerCode(), transferContainerRecordId);
    putWallSlotRepository.save(slot);
    return putWallSlotTransfer.toDTO(slot);  // ← NEW
}
```

Same pattern for `unBindContainer()` and `sealContainer()`.

### Layer 2: `TransferContainerPutWallAggregate` (wes-basic)

Three aggregate methods change from `void` to `PutWallSlotDTO`:

```java
PutWallSlotDTO bindContainer(BindContainerDTO dto, TransferContainer tc, Long pickingOrderId);
PutWallSlotDTO unBindContainer(UnBindContainerDTO dto, TransferContainer tc, Long recordId);
PutWallSlotDTO sealContainer(SealContainerDTO dto, TransferContainerRecord record, TransferContainer tc);
```

Each method passes through the return value from `putWallApi.xxx()`:

```java
public PutWallSlotDTO bindContainer(BindContainerDTO dto, TransferContainer tc, Long pickingOrderId) {
    TransferContainerRecord saved = transferContainerRecordRepository.save(new TransferContainerRecord(dto, pickingOrderId));
    // ... transfer container logic ...
    PutWallSlotDTO snapshot = null;
    if (dto.isNeedHandlePutWallSlot()) {
        snapshot = putWallApi.bindContainer(dto, saved.getId());  // ← pass through
    }
    return snapshot;
}
```

**Note on `sealContainer` overloads:** Only the 3-param version (`SealContainerDTO, TransferContainerRecord, TransferContainer`) is changed. The list-based overload used by `TransferContainerApiImpl.sealContainer(Long)` returns void — it's a different flow (auto-seal by system, not interactive station operation).

### Layer 3: `ITransferContainerApi` (wes-api)

```java
PutWallSlotDTO bindContainer(BindContainerDTO dto);
PutWallSlotDTO unBindContainer(UnBindContainerDTO dto, Long recordId);
PutWallSlotDTO sealContainer(SealContainerDTO dto);
```

Implementation in `TransferContainerApiImpl` — return the aggregate result:

```java
@Override
public PutWallSlotDTO bindContainer(BindContainerDTO dto) {
    TransferContainer tc = transferContainerRepository
        .findByContainerCodeAndWarehouseCode(dto.getContainerCode(), dto.getWarehouseCode());
    transferContainerService.validateBindContainer(tc);
    return transferContainerPutWallAggregate.bindContainer(dto, tc, dto.getPickingOrderId());
}
```

**Not changed:** `sealContainer(Long pickingOrderId)` (list-based, system-triggered flow).

### Layer 4: `ITaskApi` (wes-api) — The RPC boundary

```java
PutWallSlotDTO bindContainer(BindContainerDTO dto);
PutWallSlotDTO unbindContainer(UnBindContainerDTO dto);
PutWallSlotDTO sealContainer(SealContainerDTO dto);
```

Implementation in `OperationTaskApiImpl`:

```java
@Override
public PutWallSlotDTO bindContainer(BindContainerDTO dto) {
    return transferContainerApi.bindContainer(dto);
}

@Override
public PutWallSlotDTO unbindContainer(UnBindContainerDTO dto) {
    TransferContainerRecordDTO record = transferContainerRecordApi
        .findCurrentPickOrderTransferContainerRecord(dto.getPickingOrderId(), dto.getContainerCode());
    operationTaskService.checkUnbindable(record.getId());
    return transferContainerApi.unBindContainer(dto, record.getId());
}

@Override
public PutWallSlotDTO sealContainer(SealContainerDTO dto) {
    return transferContainerApi.sealContainer(dto);
}
```

## Station-Side Changes (3 Handlers)

### `TaskService.java` (station)

Return type changes mirror the API:

```java
PutWallSlotDTO bindContainer(BindContainerDTO dto);      // was void
PutWallSlotDTO unbindContainer(UnBindContainerDTO dto);  // was void
PutWallSlotDTO sealContainer(SealContainerDTO dto);      // was void
```

### `InputHandler.doBindContainer()`

```java
private void doBindContainer(String input, Long workStationId, OutboundWorkStationCache workStationCache) {
    // ... existing validation ...
    PutWallSlotDTO snapshot = taskService.bindContainer(new BindContainerDTO()
        .setContainerCode(input)
        .setPickingOrderId(putWallSlot.getPickingOrderId())
        .setWarehouseCode(workStationCache.getWarehouseCode())
        .setWorkStationId(workStationId)
        .setPutWallSlotCode(workStationCache.getPutWallArea().getInputPutWallSlot()));

    if (snapshot != null) {
        workStationCache.getPutWallArea().applySnapshot(snapshot);  // ← NEW
    }
    workStationCache.clearInput();
    workStationRepository.save(workStationCache);
    outboundPtlHelper.send(INPUT, workStationCache);
}
```

### `TapPutWallSlotHandler.doSealContainer()`

```java
private void doSealContainer(OutboundWorkStationCache workStationCache, PutWallSlotDTO putWallSlot) {
    PutWallSlotDTO snapshot = taskService.sealContainer(new SealContainerDTO()
        .setPutWallSlotCode(putWallSlot.getPutWallSlotCode())
        .setTransferContainerCode(putWallSlot.getTransferContainerCode())
        .setPickingOrderId(putWallSlot.getPickingOrderId())
        .setWarehouseCode(workStationCache.getWarehouseCode())
        .setWorkStationId(workStationCache.getId()));

    if (snapshot != null) {
        workStationCache.getPutWallArea().applySnapshot(snapshot);  // ← NEW
    }
    workStationCache.recalculateChooseArea();
    workStationRepository.save(workStationCache);
    ptlService.off(workStationCache.getId(), putWallSlot.getPtlTag());
}
```

### `UnbindHandler.execute()`

```java
public void execute(UnbindEvent body, Long workStationId) {
    OutboundWorkStationCache workStationCache = (OutboundWorkStationCache) workStationService.getOrThrow(workStationId);

    body.getPutWallSlotCodes().forEach(slotCode -> {
        PutWallSlotDTO putWallSlot = remoteWorkStationService.queryPutWallSlot(workStationId, slotCode);
        PutWallSlotDTO snapshot = taskService.unbindContainer(new UnBindContainerDTO()
            .setPickingOrderId(putWallSlot.getPickingOrderId())
            .setContainerCode(putWallSlot.getTransferContainerCode())
            .setWarehouseCode(workStationCache.getWarehouseCode())
            .setWorkStationId(workStationId)
            .setPutWallSlotCode(slotCode));

        if (snapshot != null) {
            workStationCache.getPutWallArea().applySnapshot(snapshot);  // ← NEW
        }
        workStationCache.getPutWallArea().getSlot(slotCode)
            .ifPresent(v -> ptlApi.reminderBind(workStationId, v.getPtlTag()));
    });

    workStationRepository.save(workStationCache);
}
```

## State Transitions Covered

| Operation | Cache Before | Snapshot After | Snapshot Fields |
|-----------|-------------|----------------|-----------------|
| `bindContainer` | WAITING_BINDING | BOUND | `putWallSlotStatus`, `transferContainerCode`, `transferContainerRecordId` |
| `unbindContainer` | BOUND | WAITING_BINDING | `putWallSlotStatus`, `transferContainerCode=null`, `transferContainerRecordId=null` |
| `sealContainer` | WAITING_SEAL | IDLE | `putWallSlotStatus`, `pickingOrderId=null`, `transferContainerCode=null`, `transferContainerRecordId=null` |

`applySnapshot()` on `PutWallArea` already handles all these fields — no changes needed to the snapshot application logic.

## MQ Events Remain as Fallback

The existing MQ consumers (`listenOrderAssigned`, `listenRemindToSealContainer`) are **not removed**. They continue to serve as:

- **Cluster sync**: when multiple station-server instances exist, ensure all nodes get the update
- **Crash recovery**: if the station handler fails after the RPC returns but before `applySnapshot()`, the MQ event eventually corrects the cache

## What Does NOT Change

- `ITransferContainerApi.sealContainer(Long)` — list-based overload, system-triggered flow
- `TransferContainerPutWallAggregate.sealContainer(boolean, List, List)` — same reason
- `IPutWallApi.splitContainer()` — not triggered by station handlers, no cache update needed
- `IPutWallApi.remindToSealContainer()` — publishes MQ event, consumed by existing `listenRemindToSealContainer`
- All MQ consumers — remain unchanged
- All frontend code — no impact
- `PutWallArea.applySnapshot()` — already compatible
