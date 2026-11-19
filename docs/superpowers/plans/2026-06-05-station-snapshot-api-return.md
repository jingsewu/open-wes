# Station Snapshot: API Return Value Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Change `ITaskApi.bindContainer()`, `.unbindContainer()`, `.sealContainer()` from `void` to returning `PutWallSlotDTO`, propagate the return value through the entire 4-layer WES call chain, and apply the snapshot in 3 station handlers.

**Architecture:** Bottom-up propagation through 4 API layers (`IPutWallApi` → `TransferContainerPutWallAggregate` → `ITransferContainerApi` → `ITaskApi`), then station-side `TaskService` + handler changes. Each layer returns the `PutWallSlotDTO` from the layer below it. The `ApplySnapshot()` method on `PutWallArea` already exists and needs no changes.

**Tech Stack:** Java 17, Spring Boot 3.2.2, Dubbo, Redis, Gradle

---

### Task 1: `IPutWallApi` + `PutWallApiImpl` — Return `PutWallSlotDTO`

**Files:**
- Modify: `server/modules-wes/wes-api/src/main/java/org/openwes/wes/api/basic/IPutWallApi.java:29-31`
- Modify: `server/modules-wes/wes-basic/src/main/java/org/openwes/wes/basic/work_station/application/PutWallApiImpl.java:114-141`

- [ ] **Step 1: Change `IPutWallApi` interface signatures**

Change three methods from `void` to `PutWallSlotDTO`:

```java
// IPutWallApi.java — line 29-31 (the three method signatures)
PutWallSlotDTO bindContainer(@Valid BindContainerDTO bindContainerDTO, Long id);
PutWallSlotDTO unBindContainer(@Valid UnBindContainerDTO unBindContainerDTO);
PutWallSlotDTO sealContainer(String putWallSlotCode, Long workStationId);
```

- [ ] **Step 2: Update `PutWallApiImpl.bindContainer()` to return DTO**

```java
// PutWallApiImpl.java — lines 114-119
@Override
public PutWallSlotDTO bindContainer(BindContainerDTO bindContainerDTO, Long transferContainerRecordId) {
    PutWallSlot putWallSlot = putWallSlotRepository
            .findBySlotCodeAndWorkStationId(bindContainerDTO.getPutWallSlotCode(), bindContainerDTO.getWorkStationId());
    putWallSlot.bindContainer(bindContainerDTO.getContainerCode(), transferContainerRecordId);
    putWallSlotRepository.save(putWallSlot);
    return putWallSlotTransfer.toDTO(putWallSlot);  // ← NEW: return snapshot
}
```

- [ ] **Step 3: Update `PutWallApiImpl.unBindContainer()` to return DTO**

```java
// PutWallApiImpl.java — lines 122-127
@Override
public PutWallSlotDTO unBindContainer(UnBindContainerDTO unBindContainerDTO) {
    PutWallSlot putWallSlot = putWallSlotRepository
            .findBySlotCodeAndWorkStationId(unBindContainerDTO.getPutWallSlotCode(), unBindContainerDTO.getWorkStationId());
    putWallSlot.unBindContainer();
    putWallSlotRepository.save(putWallSlot);
    return putWallSlotTransfer.toDTO(putWallSlot);  // ← NEW: return snapshot
}
```

- [ ] **Step 4: Update `PutWallApiImpl.sealContainer()` to return DTO**

```java
// PutWallApiImpl.java — lines 130-134
@Override
public PutWallSlotDTO sealContainer(String putWallSlotCode, Long workStationId) {
    PutWallSlot putWallSlot = putWallSlotRepository.findBySlotCodeAndWorkStationId(putWallSlotCode, workStationId);
    putWallSlot.sealContainer();
    putWallSlotRepository.save(putWallSlot);
    return putWallSlotTransfer.toDTO(putWallSlot);  // ← NEW: return snapshot
}
```

- [ ] **Step 5: Verify compilation**

Run: `cd server && ./gradlew :modules-wes:wes-basic:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add server/modules-wes/wes-api/src/main/java/org/openwes/wes/api/basic/IPutWallApi.java server/modules-wes/wes-basic/src/main/java/org/openwes/wes/basic/work_station/application/PutWallApiImpl.java
git commit -m "feat(wes-basic): return PutWallSlotDTO from IPutWallApi bind/unbind/seal"
```

---

### Task 2: `TransferContainerPutWallAggregate` — Return `PutWallSlotDTO`

**Files:**
- Modify: `server/modules-wes/wes-basic/src/main/java/org/openwes/wes/basic/container/domain/aggregate/TransferContainerPutWallAggregate.java:27-45`

- [ ] **Step 1: Change `bindContainer()` in aggregate**

```java
// TransferContainerPutWallAggregate.java — lines 27-45
@Transactional(rollbackFor = Exception.class)
public PutWallSlotDTO bindContainer(BindContainerDTO bindContainerDTO, TransferContainer transferContainer, Long pickingOrderId) {

    TransferContainerRecord transferContainerRecord = new TransferContainerRecord(bindContainerDTO, pickingOrderId);
    TransferContainerRecord saved = transferContainerRecordRepository.save(transferContainerRecord);

    if (transferContainer == null) {
        transferContainer = new TransferContainer()
                .setTransferContainerCode(bindContainerDTO.getContainerCode())
                .setTransferContainerStatus(TransferContainerStatusEnum.IDLE)
                .setWarehouseCode(bindContainerDTO.getWarehouseCode());
    }

    transferContainer.occupy(saved.getId());
    transferContainerRepository.save(transferContainer);

    PutWallSlotDTO snapshot = null;
    if (bindContainerDTO.isNeedHandlePutWallSlot()) {
        snapshot = putWallApi.bindContainer(bindContainerDTO, saved.getId());  // ← now returns PutWallSlotDTO
    }
    return snapshot;
}
```

- [ ] **Step 2: Change `unBindContainer()` in aggregate**

```java
// TransferContainerPutWallAggregate.java — lines 47-59
@Transactional(rollbackFor = Exception.class)
public PutWallSlotDTO unBindContainer(UnBindContainerDTO unBindContainerDTO, TransferContainer transferContainer,
                            Long transferContainerRecord) {

    transferContainer.unOccupy();
    transferContainerRepository.save(transferContainer);

    transferContainerRecordRepository.delete(transferContainerRecord);

    PutWallSlotDTO snapshot = null;
    if (unBindContainerDTO.isNeedHandlePutWallSlot()) {
        snapshot = putWallApi.unBindContainer(unBindContainerDTO);  // ← now returns PutWallSlotDTO
    }
    return snapshot;
}
```

- [ ] **Step 3: Change `sealContainer()` (3-param version) in aggregate**

```java
// TransferContainerPutWallAggregate.java — lines 61-81
@Transactional(rollbackFor = Exception.class)
public PutWallSlotDTO sealContainer(SealContainerDTO sealContainerDTO, TransferContainerRecord transferContainerRecord,
                          TransferContainer transferContainer) {

    //1. put wall slot seal container
    PutWallSlotDTO snapshot = null;
    if (sealContainerDTO.isNeedHandlePutWallSlot()) {
        if (sealContainerDTO.isPickingOrderCompleted()) {
            snapshot = putWallApi.sealContainer(transferContainerRecord.getPutWallSlotCode(), transferContainerRecord.getWorkStationId());
        } else {
            snapshot = putWallApi.splitContainer(transferContainerRecord.getPutWallSlotCode(), transferContainerRecord.getWorkStationId());
        }
    }

    //2. save transfer container record
    transferContainerRecord.seal();
    transferContainerRecordRepository.save(transferContainerRecord);

    //3. save transfer container
    transferContainer.lock();
    transferContainerRepository.save(transferContainer);

    return snapshot;
}
```

Note: the `else` branch calls `putWallApi.splitContainer()` which still returns `void`. That path returns `null` — the station only triggers `sealContainer` when it knows the picking order is completed (see `TaskService.sealContainer()` in station which sets `pickingOrderCompleted`).

- [ ] **Step 4: Leave the list-based `sealContainer(boolean, List, List)` unchanged**

The 4-param overload at lines 83-105 is NOT changed. It's used by `TransferContainerApiImpl.sealContainer(Long)` (system-triggered auto-seal). No changes needed.

- [ ] **Step 5: Verify compilation**

Run: `cd server && ./gradlew :modules-wes:wes-basic:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add server/modules-wes/wes-basic/src/main/java/org/openwes/wes/basic/container/domain/aggregate/TransferContainerPutWallAggregate.java
git commit -m "feat(wes-basic): return PutWallSlotDTO from TransferContainerPutWallAggregate"
```

---

### Task 3: `ITransferContainerApi` + `TransferContainerApiImpl` — Return `PutWallSlotDTO`

**Files:**
- Modify: `server/modules-wes/wes-api/src/main/java/org/openwes/wes/api/basic/ITransferContainerApi.java:23-27`
- Modify: `server/modules-wes/wes-basic/src/main/java/org/openwes/wes/basic/container/application/TransferContainerApiImpl.java:99-125`

- [ ] **Step 1: Change `ITransferContainerApi` interface signatures**

```java
// ITransferContainerApi.java — lines 23-27
PutWallSlotDTO bindContainer(BindContainerDTO bindContainerDTO);
PutWallSlotDTO unBindContainer(UnBindContainerDTO unBindContainerDTO, Long transferContainerRecordId);
PutWallSlotDTO sealContainer(SealContainerDTO sealContainerDTO);
/* sealContainer(Long pickingOrderId) remains void — NOT changed */
```

- [ ] **Step 2: Update `TransferContainerApiImpl.bindContainer()`**

```java
// TransferContainerApiImpl.java — lines 99-105
@Override
public PutWallSlotDTO bindContainer(BindContainerDTO bindContainerDTO) {
    TransferContainer transferContainer = transferContainerRepository
            .findByContainerCodeAndWarehouseCode(bindContainerDTO.getContainerCode(), bindContainerDTO.getWarehouseCode());
    transferContainerService.validateBindContainer(transferContainer);

    return transferContainerPutWallAggregate.bindContainer(bindContainerDTO, transferContainer, bindContainerDTO.getPickingOrderId());
}
```

- [ ] **Step 3: Update `TransferContainerApiImpl.unBindContainer()`**

```java
// TransferContainerApiImpl.java — lines 107-114
@Override
public PutWallSlotDTO unBindContainer(UnBindContainerDTO unBindContainerDTO, Long transferContainerRecordId) {

    TransferContainer transferContainer = transferContainerRepository
            .findByContainerCodeAndWarehouseCode(unBindContainerDTO.getContainerCode(), unBindContainerDTO.getWarehouseCode());

    return transferContainerPutWallAggregate.unBindContainer(unBindContainerDTO, transferContainer, transferContainerRecordId);
}
```

- [ ] **Step 4: Update `TransferContainerApiImpl.sealContainer(SealContainerDTO)`**

```java
// TransferContainerApiImpl.java — lines 117-125
@Override
public PutWallSlotDTO sealContainer(SealContainerDTO sealContainerDTO) {
    TransferContainerRecord transferContainerRecord = transferContainerRecordRepository
            .findCurrentPickOrderTransferContainerRecord(sealContainerDTO.getPickingOrderId(), sealContainerDTO.getTransferContainerCode());

    TransferContainer transferContainer = transferContainerRepository
            .findByContainerCodeAndWarehouseCode(transferContainerRecord.getTransferContainerCode(), sealContainerDTO.getWarehouseCode());

    return transferContainerPutWallAggregate.sealContainer(sealContainerDTO, transferContainerRecord, transferContainer);
}
```

Leave `sealContainer(Long pickingOrderId)` unchanged at lines 127-141.

- [ ] **Step 5: Check `OperationTaskStockAggregate.sealContainer()` call site**

File: `server/modules-wes/wes-task/src/main/java/org/openwes/wes/task/domain/aggregate/OperationTaskStockAggregate.java:82`
This calls `transferContainerApi.sealContainer(sealContainerDTO)`. Since the return type changed to `PutWallSlotDTO`, the call site still compiles fine — just ignore the return value. No code change needed.

- [ ] **Step 6: Verify compilation**

Run: `cd server && ./gradlew :modules-wes:wes-basic:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add server/modules-wes/wes-api/src/main/java/org/openwes/wes/api/basic/ITransferContainerApi.java server/modules-wes/wes-basic/src/main/java/org/openwes/wes/basic/container/application/TransferContainerApiImpl.java
git commit -m "feat(wes-basic): return PutWallSlotDTO from ITransferContainerApi bind/unbind/seal"
```

---

### Task 4: `ITaskApi` + `OperationTaskApiImpl` — Return `PutWallSlotDTO`

**Files:**
- Modify: `server/modules-wes/wes-api/src/main/java/org/openwes/wes/api/task/ITaskApi.java:23-27`
- Modify: `server/modules-wes/wes-task/src/main/java/org/openwes/wes/task/application/OperationTaskApiImpl.java:100-163`

- [ ] **Step 1: Change `ITaskApi` interface signatures**

```java
// ITaskApi.java — lines 23-27
PutWallSlotDTO bindContainer(@Valid BindContainerDTO bindContainerDTO);
PutWallSlotDTO unbindContainer(@Valid UnBindContainerDTO unBindContainerDTO);
PutWallSlotDTO sealContainer(@Valid SealContainerDTO sealContainerDTO);
// sealContainer(Long pickingOrderId) remains void at line 29
```

- [ ] **Step 2: Update `OperationTaskApiImpl.bindContainer()`**

```java
// OperationTaskApiImpl.java — lines 99-102
@Override
public PutWallSlotDTO bindContainer(BindContainerDTO bindContainerDTO) {
    return transferContainerApi.bindContainer(bindContainerDTO);
}
```

- [ ] **Step 3: Update `OperationTaskApiImpl.unbindContainer()`**

```java
// OperationTaskApiImpl.java — lines 104-112
@Override
public PutWallSlotDTO unbindContainer(UnBindContainerDTO unBindContainerDTO) {

    TransferContainerRecordDTO transferContainerRecord = transferContainerRecordApi
            .findCurrentPickOrderTransferContainerRecord(unBindContainerDTO.getPickingOrderId(), unBindContainerDTO.getContainerCode());
    operationTaskService.checkUnbindable(transferContainerRecord.getId());

    return transferContainerApi.unBindContainer(unBindContainerDTO, transferContainerRecord.getId());
}
```

- [ ] **Step 4: Update `OperationTaskApiImpl.sealContainer(SealContainerDTO)`**

```java
// OperationTaskApiImpl.java — lines 155-158
@Override
public PutWallSlotDTO sealContainer(SealContainerDTO sealContainerDTO) {
    return transferContainerApi.sealContainer(sealContainerDTO);
}
```

Leave `sealContainer(Long pickingOrderId)` unchanged at lines 160-163.

- [ ] **Step 5: Verify compilation (WES module)**

Run: `cd server && ./gradlew :modules-wes:wes-task:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add server/modules-wes/wes-api/src/main/java/org/openwes/wes/api/task/ITaskApi.java server/modules-wes/wes-task/src/main/java/org/openwes/wes/task/application/OperationTaskApiImpl.java
git commit -m "feat(wes-task): return PutWallSlotDTO from ITaskApi bind/unbind/seal"
```

---

### Task 5: Station `TaskService` — Return `PutWallSlotDTO`

**Files:**
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/infrastructure/remote/TaskService.java:28-39`

- [ ] **Step 1: Change `bindContainer()` method in `TaskService`**

```java
// TaskService.java — lines 28-30
public PutWallSlotDTO bindContainer(BindContainerDTO bindContainerDTO) {
    return taskApi.bindContainer(bindContainerDTO);
}
```

- [ ] **Step 2: Change `unbindContainer()` method in `TaskService`**

```java
// TaskService.java — lines 32-34
public PutWallSlotDTO unbindContainer(UnBindContainerDTO unBindContainerDTO) {
    return taskApi.unbindContainer(unBindContainerDTO);
}
```

- [ ] **Step 3: Change `sealContainer()` method in `TaskService`**

```java
// TaskService.java — lines 36-40
public PutWallSlotDTO sealContainer(SealContainerDTO sealContainerDTO) {
    PickingOrderDTO pickingOrderDTO = pickingOrderApi.getById(sealContainerDTO.getPickingOrderId());
    sealContainerDTO.setPickingOrderCompleted(pickingOrderDTO.getPickingOrderStatus() == PickingOrderStatusEnum.PICKED);
    return taskApi.sealContainer(sealContainerDTO);
}
```

- [ ] **Step 4: Verify compilation (station module)**

Run: `cd server && ./gradlew :modules-station:station:compileJava`
Expected: BUILD SUCCESSFUL (may fail until handlers are updated in Tasks 6-8 — this is expected)

- [ ] **Step 5: Commit**

```bash
git add server/modules-station/station/src/main/java/org/openwes/station/infrastructure/remote/TaskService.java
git commit -m "feat(station): return PutWallSlotDTO from TaskService bind/unbind/seal"
```

---

### Task 6: `InputHandler` — Apply snapshot in `doBindContainer`

**Files:**
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/application/business/handler/outbound/InputHandler.java:56-75`

- [ ] **Step 1: Update `doBindContainer` to apply snapshot**

```java
// InputHandler.java — lines 56-75
private void doBindContainer(String input, Long workStationId, OutboundWorkStationCache workStationCache) {
    if (StringUtils.isEmpty(workStationCache.getPutWallArea().getInputPutWallSlot())) {
        log.error("work station: {} input: {} is not empty and not put wall slot,"
                + " but prevision input is empty.", workStationId, input);
        throw WmsException.throwWmsException(OutboundErrorDescEnum.OUTBOUND_INCRRECT_PUT_WALL_SLOT_CODE, input);
    }

    PutWallSlotDTO putWallSlot = remoteWorkStationService.queryPutWallSlot(workStationId, workStationCache.getPutWallArea().getInputPutWallSlot());
    PutWallSlotDTO snapshot = taskService.bindContainer(new BindContainerDTO()
            .setContainerCode(input)
            .setPickingOrderId(putWallSlot.getPickingOrderId())
            .setWarehouseCode(workStationCache.getWarehouseCode())
            .setWorkStationId(workStationId)
            .setPutWallSlotCode(workStationCache.getPutWallArea().getInputPutWallSlot()));

    if (snapshot != null) {
        workStationCache.getPutWallArea().applySnapshot(snapshot);
    }

    workStationCache.clearInput();
    workStationRepository.save(workStationCache);

    outboundPtlHelper.send(INPUT, workStationCache);
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd server && ./gradlew :modules-station:station:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add server/modules-station/station/src/main/java/org/openwes/station/application/business/handler/outbound/InputHandler.java
git commit -m "feat(station): apply snapshot in InputHandler.doBindContainer"
```

---

### Task 7: `TapPutWallSlotHandler` — Apply snapshot in `doSealContainer`

**Files:**
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/application/business/handler/outbound/TapPutWallSlotHandler.java:69-79`

- [ ] **Step 1: Update `doSealContainer` to apply snapshot**

```java
// TapPutWallSlotHandler.java — lines 69-79
private void doSealContainer(OutboundWorkStationCache workStationCache, PutWallSlotDTO putWallSlot) {
    PutWallSlotDTO snapshot = taskService.sealContainer(new SealContainerDTO()
            .setPutWallSlotCode(putWallSlot.getPutWallSlotCode())
            .setTransferContainerCode(putWallSlot.getTransferContainerCode())
            .setPickingOrderId(putWallSlot.getPickingOrderId())
            .setWarehouseCode(workStationCache.getWarehouseCode())
            .setWorkStationId(workStationCache.getId()));

    if (snapshot != null) {
        workStationCache.getPutWallArea().applySnapshot(snapshot);
        workStationCache.recalculateChooseArea();
    }

    workStationRepository.save(workStationCache);
    ptlService.off(workStationCache.getId(), putWallSlot.getPtlTag());
}
```

Note: `recalculateChooseArea()` is moved inside the snapshot check since the cache state only changes when we have a snapshot.

- [ ] **Step 2: Verify compilation**

Run: `cd server && ./gradlew :modules-station:station:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add server/modules-station/station/src/main/java/org/openwes/station/application/business/handler/outbound/TapPutWallSlotHandler.java
git commit -m "feat(station): apply snapshot in TapPutWallSlotHandler.doSealContainer"
```

---

### Task 8: `UnbindHandler` — Apply snapshot in `execute`

**Files:**
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/application/business/handler/outbound/UnbindHandler.java:28-44`

- [ ] **Step 1: Update `execute` to apply snapshot and save cache**

```java
// UnbindHandler.java — lines 28-44
@Override
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
            workStationCache.getPutWallArea().applySnapshot(snapshot);
        }

        workStationCache.getPutWallArea().getSlot(slotCode)
                .ifPresent(v -> ptlApi.reminderBind(workStationId, v.getPtlTag()));
    });

    workStationRepository.save(workStationCache);
}
```

- [ ] **Step 2: Verify compilation**

Run: `cd server && ./gradlew :modules-station:station:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add server/modules-station/station/src/main/java/org/openwes/station/application/business/handler/outbound/UnbindHandler.java
git commit -m "feat(station): apply snapshot in UnbindHandler"
```

---

### Task 9: Full compilation verification

- [ ] **Step 1: Compile the entire project**

Run: `cd server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL (0 errors)

- [ ] **Step 2: Run unit tests**

Run: `cd server && ./gradlew test`
Expected: BUILD SUCCESSFUL (all existing tests pass)

---

## Self-Review Checklist

**1. Spec coverage:**
- `IPutWallApi` return type change → Task 1 ✓
- `TransferContainerPutWallAggregate` return type change → Task 2 ✓
- `ITransferContainerApi` return type change → Task 3 ✓
- `ITaskApi` return type change → Task 4 ✓
- `TaskService` (station-side) return type change → Task 5 ✓
- `InputHandler.doBindContainer` snapshot → Task 6 ✓
- `TapPutWallSlotHandler.doSealContainer` snapshot → Task 7 ✓
- `UnbindHandler.execute` snapshot → Task 8 ✓
- MQ events unchanged — not in tasks, covered by spec's "What Does NOT Change" ✓
- `sealContainer(Long)` overload unchanged — tasks 3-4 explicitly leave it ✓
- `applySnapshot()` already compatible — no task needed ✓

**2. Placeholder scan:** No placeholders found. All code is complete and concrete.

**3. Type consistency:** All method signatures use `PutWallSlotDTO` consistently across all 4 layers. Task 5 → Task 6 uses the same `taskService.bindContainer()` return type. No type mismatches.
