# Outbound Module DDD Deep Refactoring Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor wes-outbound module to enforce DDD encapsulation, introduce Use Case pattern, slim event subscribers and domain services, and update coding standards.

**Architecture:** Replace `@Data` with `@Getter @Builder` on all 8 entities. Move 6 aggregate classes from `domain/aggregate/` to 9 Use Case classes in `application/usecase/`. Slim event subscribers to thin dispatchers. Add `@Transactional` to event subscriber write paths. Update `docs/standards/backend.md` and `CLAUDE.md`.

**Tech Stack:** Java 17, Lombok 1.18.x, MapStruct 1.5.3.Final (supports @Builder natively), Spring Boot 3.2.2, Guava EventBus

**Key paths:**
- Module root: `server/modules-wes/wes-outbound/src/main/java/org/openwes/wes/outbound/`
- Tests: `server/modules-wes/wes-outbound/src/test/java/org/openwes/wes/outbound/`
- Standards: `docs/standards/backend.md`, `CLAUDE.md`

**Important context:**
- MapStruct 1.5.3.Final natively supports `@Builder` — generated mappers will use builder instead of setters automatically
- No external modules import outbound entity classes directly — all cross-module access via DTOs/API interfaces
- `PickingOrderDispatchedEvent` has one external subscriber in `modules-utils/monitoring/OutboundMetricsSubscriber` — must preserve or adapt it
- `EmptyContainerOutboundOrder` does NOT extend `AggregatorRoot` — intentional, do not change
- `OutboundPlanOrderCancelContext` uses `@Data @Accessors(chain = true)` — this is a context/DTO object, OK to keep `@Data`
- Existing tests in `OutboundPlanOrderTest.java` use setters extensively and must be updated

---

## Phase 0: Impact Analysis (read-only)

### Task 0: Verify MapStruct @Builder compatibility and catalogue setter usage

**Files:**
- Read: `server/build.gradle` (MapStruct version)
- Read: All `.java` files in `wes-outbound/src/main/java/` that call `.set` on entity objects

- [ ] **Step 1: Verify MapStruct version supports @Builder**

Run: `cd D:/open-wes/server && grep -r "mapstruct" build.gradle | head -5`
Expected: Version 1.5.3.Final or higher (confirmed: 1.5.3.Final)

- [ ] **Step 2: Grep for all setter calls on entities within wes-outbound**

Run:
```bash
cd D:/open-wes/server
grep -rn "\.set[A-Z]" modules-wes/wes-outbound/src/main/java/ --include="*.java" | grep -v "\.setModified\|/po/\|/transfer/\|PO\." | head -50
```

Document each setter call site. Known sites from analysis:
1. `OutboundPlanOrder.initSkuInfo()` → `v.setSkuId()`, `v.setSkuName()` (lines 101-102)
2. `PickingOrder.create()` → `detail.setPickingOrderId(order.id)` (line 80)
3. `PickingOrder.copyAndNew()` → `detail.setPickingOrderId(newOrder.id)` (line 211)
4. `PickingOrderDetail.copyAndNew()` → 6 setter calls (lines 105-110)
5. `EmptyContainerOutboundOrderApiImpl` → `detail.setContainerId()`, `detail.setContainerCode()` (lines 46-47)
6. `OutboundPlanOrderPreAllocatedAggregate.preAllocate()` → chain setters on `OutboundPreAllocatedRecord` (lines 81-90)
7. `EmptyContainerOutboundAggregate.execute()` → setters on `CreateContainerTaskDTO` (external DTO, not our entity)

- [ ] **Step 3: Verify PickingOrderDispatchedEvent subscribers**

Run:
```bash
cd D:/open-wes/server
grep -rn "PickingOrderDispatchedEvent" --include="*.java" | grep -v "import\|class\|package"
```

Known subscribers:
1. `wes-outbound/OutboundPlanOrderSubscribe.onDispatchedEvent()` — will be inlined into UseCase
2. `modules-utils/monitoring/OutboundMetricsSubscriber.onPickingDispatched()` — must preserve event

**Decision**: Keep `PickingOrderDispatchedEvent` in `PickingOrder.dispatch()` entity. Remove only the `OutboundPlanOrderSubscribe.onDispatchedEvent()` handler and inline that logic into `DispatchPickingOrdersUseCase`. The monitoring subscriber continues to work.

- [ ] **Step 4: Commit analysis notes**

```bash
git add -A && git commit -m "docs: add outbound DDD refactor implementation plan"
```

---

## Phase 1: Entity @Builder Refactoring

### Task 1: Refactor OutboundPlanOrderDetail to @Builder

**Files:**
- Modify: `domain/entity/OutboundPlanOrderDetail.java`
- Test: `src/test/java/.../domain/entity/OutboundPlanOrderTest.java` (uses this entity)

- [ ] **Step 1: Replace @Data with @Getter @Builder**

```java
package org.openwes.wes.outbound.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import org.openwes.common.utils.jpa.ModificationAware;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderDetailStatusEnum;

import java.util.Map;
import java.util.Set;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboundPlanOrderDetail implements ModificationAware {

    private Long id;
    private Long outboundPlanOrderId;
    private Long skuId;
    private String skuCode;
    private String skuName;
    private String ownerCode;

    private Map<String, Object> batchAttributes;

    private Integer qtyRequired;
    @Builder.Default
    private Integer qtyAllocated = 0;
    @Builder.Default
    private Integer qtyActual = 0;
    private Set<Long> warehouseAreaIds;

    private Map<String, String> extendFields;

    private OutboundPlanOrderDetailStatusEnum outboundPlanOrderDetailStatus;

    private Long version;

    private boolean modified;

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void cancel() {
        if (this.outboundPlanOrderDetailStatus != OutboundPlanOrderDetailStatusEnum.NEW) {
            throw new IllegalArgumentException("Outbound Plan Order Detail is not NEW, can not cancel");
        }
        this.outboundPlanOrderDetailStatus = OutboundPlanOrderDetailStatusEnum.CANCELED;
        this.modified = true;
    }

    public void picking(Integer operatedQty) {
        this.qtyActual += operatedQty;
        if (this.qtyActual > this.qtyRequired) {
            throw new IllegalArgumentException("Picking quantity exceeds the required quantity");
        }
        if (this.qtyActual.equals(this.qtyAllocated)) {
            this.outboundPlanOrderDetailStatus = OutboundPlanOrderDetailStatusEnum.PICKED;
        }
        this.modified = true;
    }

    public void shortComplete() {
        if (this.outboundPlanOrderDetailStatus == OutboundPlanOrderDetailStatusEnum.PICKED) {
            return;
        }
        this.outboundPlanOrderDetailStatus = OutboundPlanOrderDetailStatusEnum.PICKED;
        this.modified = true;
    }

    public void preAllocate(OutboundPreAllocatedRecord planPreAllocatedRecord) {
        this.qtyAllocated += planPreAllocatedRecord.getQtyPreAllocated();
        if (this.qtyAllocated > this.qtyRequired) {
            throw new IllegalArgumentException("allocate quantity exceeds the required quantity");
        }
        this.modified = true;
    }

    public void initialize(Long outboundPlanOrderId) {
        this.outboundPlanOrderId = outboundPlanOrderId;
        this.modified = true;
    }

    public void enrichSkuInfo(Long skuId, String skuName) {
        this.skuId = skuId;
        this.skuName = skuName;
    }
}
```

- [ ] **Step 2: Update OutboundPlanOrder.initSkuInfo() to use enrichSkuInfo()**

In `OutboundPlanOrder.java`, replace lines 101-102:
```java
// Before:
v.setSkuId(skuMainDataDTO.getId());
v.setSkuName(skuMainDataDTO.getSkuName());

// After:
v.enrichSkuInfo(skuMainDataDTO.getId(), skuMainDataDTO.getSkuName());
```

- [ ] **Step 3: Build to verify compilation**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Run existing tests**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:test`
Expected: Tests pass (or fail due to setter usage in tests — Task 9 will fix)

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(outbound): OutboundPlanOrderDetail @Data -> @Builder, add enrichSkuInfo()"
```

---

### Task 2: Refactor OutboundPreAllocatedRecord to @Builder

**Files:**
- Modify: `domain/entity/OutboundPreAllocatedRecord.java`
- Modify: `domain/aggregate/OutboundPlanOrderPreAllocatedAggregate.java` (uses chain setters)

- [ ] **Step 1: Replace @Data @Accessors with @Getter @Builder**

```java
package org.openwes.wes.outbound.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;

import java.util.Collection;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboundPreAllocatedRecord {

    private Long id;
    private String ownerCode;
    private Long outboundPlanOrderId;
    private Long outboundPlanOrderDetailId;
    private Collection<Long> warehouseAreaIds;
    private Long skuId;
    private Map<String, Object> batchAttributes;
    private Long skuBatchStockId;
    private Long warehouseAreaId;
    private Integer qtyPreAllocated;
    private Long version;

    public void cancel() {
        this.qtyPreAllocated = 0;
    }
}
```

- [ ] **Step 2: Update OutboundPlanOrderPreAllocatedAggregate setter chains to builder**

In `OutboundPlanOrderPreAllocatedAggregate.java`, replace lines 81-90:
```java
// Before:
OutboundPreAllocatedRecord preAllocatedRecord = new OutboundPreAllocatedRecord()
        .setOwnerCode(detail.getOwnerCode())
        .setSkuBatchStockId(skuBatchStockDTO.getId())
        .setWarehouseAreaId(skuBatchStockDTO.getWarehouseAreaId())
        .setSkuId(skuBatchStockDTO.getSkuId())
        .setBatchAttributes(detail.getBatchAttributes())
        .setOutboundPlanOrderId(detail.getOutboundPlanOrderId())
        .setWarehouseAreaIds(detail.getWarehouseAreaIds())
        .setOutboundPlanOrderDetailId(detail.getId())
        .setQtyPreAllocated(preAllocated);

// After:
OutboundPreAllocatedRecord preAllocatedRecord = OutboundPreAllocatedRecord.builder()
        .ownerCode(detail.getOwnerCode())
        .skuBatchStockId(skuBatchStockDTO.getId())
        .warehouseAreaId(skuBatchStockDTO.getWarehouseAreaId())
        .skuId(skuBatchStockDTO.getSkuId())
        .batchAttributes(detail.getBatchAttributes())
        .outboundPlanOrderId(detail.getOutboundPlanOrderId())
        .warehouseAreaIds(detail.getWarehouseAreaIds())
        .outboundPlanOrderDetailId(detail.getId())
        .qtyPreAllocated(preAllocated)
        .build();
```

- [ ] **Step 3: Build to verify**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "refactor(outbound): OutboundPreAllocatedRecord @Data -> @Builder"
```

---

### Task 3: Refactor PickingOrderDetail to @Builder

**Files:**
- Modify: `domain/entity/PickingOrderDetail.java`

- [ ] **Step 1: Replace @Data @Accessors with @Getter @Builder**

```java
package org.openwes.wes.outbound.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AccessLevel;
import org.openwes.common.utils.jpa.ModificationAware;
import org.openwes.wes.api.outbound.constants.PickingOrderDetailStatusEnum;

import java.util.Collection;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PickingOrderDetail implements ModificationAware {

    private Long id;
    private String ownerCode;
    @Setter(AccessLevel.PACKAGE)
    private Long pickingOrderId;
    private Long outboundOrderPlanDetailId;
    private Long outboundOrderPlanId;
    private Long skuId;
    private Map<String, Object> batchAttributes;
    private Long skuBatchStockId;
    private Integer qtyRequired;
    private Integer qtyActual;
    private Collection<Long> retargetingWarehouseAreaIds;
    private Integer qtyAbnormal;
    private Integer qtyShort;
    private PickingOrderDetailStatusEnum pickingOrderDetailStatus;
    private Long version;
    private boolean modified;

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public void cancel() {
        if (this.pickingOrderDetailStatus != PickingOrderDetailStatusEnum.NEW) {
            throw new IllegalStateException("picking order details status is not NEW, can't be canceled");
        }
        this.pickingOrderDetailStatus = PickingOrderDetailStatusEnum.CANCELED;
        this.modified = true;
    }

    public void picking(Integer operatedQty) {
        this.qtyActual += operatedQty;
        if (this.qtyActual + this.qtyShort > this.qtyRequired) {
            throw new IllegalArgumentException("Picking quantity + short quantity exceeds the required quantity");
        }
        if (this.qtyActual + qtyShort == this.qtyRequired) {
            this.pickingOrderDetailStatus = PickingOrderDetailStatusEnum.PICKED;
        }
        this.modified = true;
    }

    public void reportAbnormal(Integer abnormalQty) {
        this.qtyAbnormal += abnormalQty;
        if (this.qtyAbnormal + this.qtyActual + this.qtyShort > this.qtyRequired) {
            throw new IllegalArgumentException("abnormal quantity exceeds the required quantity");
        }
        this.modified = true;
    }

    public void reallocateAbnormal(Integer allocatedQty) {
        this.qtyAbnormal -= allocatedQty;
        if (this.qtyAbnormal < 0) {
            throw new IllegalArgumentException("abnormal quantity is less than zero");
        }
        this.modified = true;
    }

    public void shortPicking(Integer shortQty) {
        this.qtyAbnormal -= shortQty;
        this.qtyShort += shortQty;
        if (this.qtyAbnormal != 0) {
            throw new IllegalArgumentException("abnormal quantity isn't zero");
        }
        if (this.qtyActual + this.qtyShort > this.qtyRequired) {
            throw new IllegalArgumentException("picking quantity exceeds the required quantity");
        }
        if (this.qtyActual + qtyShort == this.qtyRequired) {
            this.pickingOrderDetailStatus = PickingOrderDetailStatusEnum.PICKED;
        }
        this.modified = true;
    }

    public PickingOrderDetail copyAndNew(Long skuBatchStockId, Integer requiredQty) {
        return PickingOrderDetail.builder()
                .ownerCode(this.ownerCode)
                .pickingOrderId(this.pickingOrderId)
                .outboundOrderPlanDetailId(this.outboundOrderPlanDetailId)
                .outboundOrderPlanId(this.outboundOrderPlanId)
                .skuId(this.skuId)
                .batchAttributes(this.batchAttributes)
                .skuBatchStockId(skuBatchStockId)
                .qtyRequired(requiredQty)
                .qtyActual(0)
                .qtyShort(0)
                .qtyAbnormal(0)
                .retargetingWarehouseAreaIds(this.retargetingWarehouseAreaIds)
                .pickingOrderDetailStatus(this.pickingOrderDetailStatus)
                .modified(true)
                .build();
    }
}
```

Note: `pickingOrderId` has `@Setter(AccessLevel.PACKAGE)` for internal assignment from `PickingOrder.create()` and `copyAndNew()` which are in the same package.

- [ ] **Step 2: Build to verify**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "refactor(outbound): PickingOrderDetail @Data -> @Builder, use builder in copyAndNew()"
```

---

### Task 4: Refactor OutboundPlanOrder to @Builder

**Files:**
- Modify: `domain/entity/OutboundPlanOrder.java`

- [ ] **Step 1: Replace @Data with @Getter @Builder**

Replace the annotation block (lines 28-30):
```java
// Before:
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j

// After:
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
@Slf4j
```

Add required imports:
```java
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
```

Remove the existing `import lombok.Data;` and `import lombok.EqualsAndHashCode;` (keep EqualsAndHashCode, remove Data).

- [ ] **Step 2: Build to verify**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "refactor(outbound): OutboundPlanOrder @Data -> @Builder"
```

---

### Task 5: Refactor PickingOrder to @Builder

**Files:**
- Modify: `domain/entity/PickingOrder.java`

- [ ] **Step 1: Replace @Data @Accessors with @Getter @Builder**

Replace annotations (lines 19-22):
```java
// Before:
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Slf4j

// After:
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
@Slf4j
```

Remove `import lombok.Data;` and `import lombok.experimental.Accessors;`.

- [ ] **Step 2: Build to verify**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "refactor(outbound): PickingOrder @Data -> @Builder"
```

---

### Task 6: Refactor OutboundWave to @Builder

**Files:**
- Modify: `domain/entity/OutboundWave.java`

- [ ] **Step 1: Replace @Data @NoArgsConstructor with @Getter @Builder**

Replace annotations (lines 15-18):
```java
// Before:
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@Slf4j

// After:
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true)
@Slf4j
```

Note: The existing public `@NoArgsConstructor` is tightened to `PROTECTED` per design spec.

- [ ] **Step 2: Build to verify**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "refactor(outbound): OutboundWave @Data -> @Builder, tighten NoArgsConstructor to PROTECTED"
```

---

### Task 7: Refactor EmptyContainerOutboundOrder and Detail to @Builder

**Files:**
- Modify: `domain/entity/EmptyContainerOutboundOrder.java`
- Modify: `domain/entity/EmptyContainerOutboundOrderDetail.java`

- [ ] **Step 1: Replace EmptyContainerOutboundOrder @Data with @Getter @Builder**

```java
// Before:
@Slf4j
@Data
public class EmptyContainerOutboundOrder {

// After:
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class EmptyContainerOutboundOrder {
```

Note: This class does NOT extend `AggregatorRoot` — no `@EqualsAndHashCode(callSuper = true)`.

- [ ] **Step 2: Replace EmptyContainerOutboundOrderDetail @Data with @Getter @Builder**

```java
// Before:
@Slf4j
@Data
public class EmptyContainerOutboundOrderDetail {

// After:
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Slf4j
public class EmptyContainerOutboundOrderDetail {
```

- [ ] **Step 3: Update EmptyContainerOutboundOrderApiImpl to use builder**

In `EmptyContainerOutboundOrderApiImpl.java`, replace lines 44-48:
```java
// Before:
List<EmptyContainerOutboundOrderDetail> details = containerSearchVOs.stream().map(v -> {
    EmptyContainerOutboundOrderDetail detail = new EmptyContainerOutboundOrderDetail();
    detail.setContainerId(v.getId());
    detail.setContainerCode(v.getContainerCode());
    return detail;
}).toList();

// After:
List<EmptyContainerOutboundOrderDetail> details = containerSearchVOs.stream().map(v ->
    EmptyContainerOutboundOrderDetail.builder()
        .containerId(v.getId())
        .containerCode(v.getContainerCode())
        .build()
).toList();
```

- [ ] **Step 4: Build to verify**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(outbound): EmptyContainerOutbound entities @Data -> @Builder"
```

---

### Task 8: Verify MapStruct regeneration with @Builder

**Files:**
- Check: `build/generated/sources/annotationProcessor/` for updated MapStruct impls

- [ ] **Step 1: Clean and rebuild to regenerate MapStruct**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:clean :modules-wes:wes-outbound:compileJava`
Expected: BUILD SUCCESSFUL — MapStruct now generates builder-based mapping instead of setter-based

- [ ] **Step 2: Verify generated code uses builders**

Run:
```bash
grep -r "\.builder()" D:/open-wes/server/modules-wes/wes-outbound/build/generated/ --include="*.java" | head -10
```
Expected: Generated transfer implementations contain `.builder()` calls

- [ ] **Step 3: Full module build with tests (may fail — tests need update)**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:build`
Note: Test failures are expected — Task 9 fixes them.

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "refactor(outbound): verify MapStruct @Builder regeneration"
```

---

### Task 9: Update OutboundPlanOrderTest for @Builder

**Files:**
- Modify: `src/test/java/.../domain/entity/OutboundPlanOrderTest.java`

- [ ] **Step 1: Replace setter calls with builder/domain methods in tests**

The test uses `ObjectUtils.getRandomObject()` which creates random objects — it likely uses reflection and may still work with @Builder if there's a no-arg constructor. The key setter calls to fix:

```java
// Line 59: preAllocatedRecord.setQtyPreAllocated(Integer.MAX_VALUE);
// Replace with building a new record:
OutboundPreAllocatedRecord preAllocatedRecord = OutboundPreAllocatedRecord.builder()
        .qtyPreAllocated(Integer.MAX_VALUE)
        .build();

// Line 64: preAllocatedRecord.setQtyPreAllocated(...)
// Build a new record each time instead of mutating:
preAllocatedRecord = OutboundPreAllocatedRecord.builder()
        .qtyPreAllocated(randomObject.getDetails().iterator().next().getQtyRequired())
        .build();

// Lines 70-71: randomObject.setShortOutbound(false); randomObject.setShortWaiting(false);
// Lines 79-80: randomObject.setShortOutbound(true); randomObject.setShortWaiting(true);
// Line 91: randomObject.setOutboundPlanOrderStatus(OutboundPlanOrderStatusEnum.ASSIGNED);
```

For the `OutboundPlanOrder` setter calls in tests, we need to use `ObjectUtils.getRandomObject()` with builder or use reflection-based test utilities. Since `getRandomObject` likely uses reflection to set fields, it should still work with protected no-arg constructor. The direct `.set` calls need to use builder to create new objects or use test-specific helpers.

Check if `ObjectUtils.getRandomObject` uses reflection (it likely does since it creates random data). If so, the protected no-arg constructor is sufficient.

For the direct setter calls, create test builder helpers:
```java
// Replace lines 70-71, 79-80:
// Use reflection in tests since these are testing internal state scenarios
// Or create objects with builder from scratch for each test case
```

- [ ] **Step 2: Run tests**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:test`
Expected: All tests pass

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "test(outbound): update OutboundPlanOrderTest for @Builder entities"
```

---

## Phase 2: Create Use Case Classes

### Task 10: Create PreAllocateOutboundOrderUseCase

**Files:**
- Create: `application/usecase/PreAllocateOutboundOrderUseCase.java`
- Reference: `domain/aggregate/OutboundPlanOrderPreAllocatedAggregate.java` (source logic)

- [ ] **Step 1: Create UseCase class**

Move logic from `OutboundPlanOrderPreAllocatedAggregate.preAllocate()` and `OutboundPlanOrderSubscribe.onCreateEvent()` into a single UseCase. The status check and SKU extraction from the subscriber move here too.

```java
package org.openwes.wes.outbound.application.usecase;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.api.outbound.dto.OutboundAllocateSkuBatchContext;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import org.openwes.wes.api.stock.dto.SkuBatchStockDTO;
import org.openwes.wes.api.stock.dto.SkuBatchStockLockDTO;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrderDetail;
import org.openwes.wes.outbound.domain.entity.OutboundPreAllocatedRecord;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.OutboundPreAllocatedRecordRepository;
import org.openwes.wes.outbound.domain.service.PickingOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PreAllocateOutboundOrderUseCase {

    private final OutboundPlanOrderRepository outboundPlanOrderRepository;
    private final OutboundPreAllocatedRecordRepository preAllocatedRecordRepository;
    private final IStockApi stockApi;
    private final PickingOrderService pickingOrderService;

    @Transactional(rollbackFor = Exception.class)
    public void execute(Long outboundPlanOrderId) {
        OutboundPlanOrder outboundPlanOrder = outboundPlanOrderRepository.findById(outboundPlanOrderId);
        if (outboundPlanOrder.getOutboundPlanOrderStatus() != OutboundPlanOrderStatusEnum.NEW) {
            log.error("outbound status must be NEW when preparing allocate stocks");
            return;
        }

        List<Long> skuIds = outboundPlanOrder.getDetails()
                .stream().map(OutboundPlanOrderDetail::getSkuId).toList();
        List<String> ownerCodes = outboundPlanOrder.getDetails()
                .stream().map(OutboundPlanOrderDetail::getOwnerCode).distinct().toList();

        OutboundAllocateSkuBatchContext preAllocateCache =
                pickingOrderService.prepareAllocateCache(skuIds, outboundPlanOrder.getWarehouseCode(), ownerCodes);

        List<OutboundPreAllocatedRecord> planPreAllocatedRecords = Lists.newArrayList();
        outboundPlanOrder.getDetails().forEach(detail -> {
            List<SkuBatchStockDTO> skuBatchStocks = preAllocateCache.matchSkuBatchStocks(
                    detail.getSkuId(), detail.getOwnerCode(), detail.getBatchAttributes());
            skuBatchStocks = filterDetailWarehouseAreaIds(detail, skuBatchStocks);
            planPreAllocatedRecords.addAll(preAllocate(detail, skuBatchStocks));
        });

        boolean preAllocateResult = outboundPlanOrder.preAllocate(planPreAllocatedRecords);
        outboundPlanOrderRepository.saveOrderAndDetail(outboundPlanOrder);

        if (!preAllocateResult) {
            return;
        }

        List<SkuBatchStockLockDTO> skuBatchStockLockDTOS = planPreAllocatedRecords.stream().map(preAllocatedRecord -> {
            SkuBatchStockLockDTO skuBatchStockLockDTO = new SkuBatchStockLockDTO();
            skuBatchStockLockDTO.setSkuBatchStockId(preAllocatedRecord.getSkuBatchStockId());
            skuBatchStockLockDTO.setLockQty(preAllocatedRecord.getQtyPreAllocated());
            skuBatchStockLockDTO.setLockType(StockLockTypeEnum.OUTBOUND);
            skuBatchStockLockDTO.setOrderDetailId(preAllocatedRecord.getOutboundPlanOrderDetailId());
            return skuBatchStockLockDTO;
        }).toList();
        stockApi.lockSkuBatchStock(skuBatchStockLockDTOS);

        preAllocatedRecordRepository.saveAll(planPreAllocatedRecords);
    }

    private List<SkuBatchStockDTO> filterDetailWarehouseAreaIds(OutboundPlanOrderDetail detail,
                                                                  List<SkuBatchStockDTO> skuBatchStocks) {
        if (CollectionUtils.isNotEmpty(detail.getWarehouseAreaIds())) {
            skuBatchStocks = skuBatchStocks.stream()
                    .filter(k -> detail.getWarehouseAreaIds().contains(k.getWarehouseAreaId())).toList();
        }
        return skuBatchStocks;
    }

    private List<OutboundPreAllocatedRecord> preAllocate(OutboundPlanOrderDetail detail,
                                                          List<SkuBatchStockDTO> skuBatchStocks) {
        List<OutboundPreAllocatedRecord> preAllocatedRecords = Lists.newArrayList();
        int qtyRequired = detail.getQtyRequired();
        for (SkuBatchStockDTO skuBatchStockDTO : skuBatchStocks) {
            if (qtyRequired < 1) {
                break;
            }
            int preAllocated = Math.min(skuBatchStockDTO.getAvailableQty(), qtyRequired);
            skuBatchStockDTO.setAvailableQty(skuBatchStockDTO.getAvailableQty() - preAllocated);
            qtyRequired -= preAllocated;

            OutboundPreAllocatedRecord preAllocatedRecord = OutboundPreAllocatedRecord.builder()
                    .ownerCode(detail.getOwnerCode())
                    .skuBatchStockId(skuBatchStockDTO.getId())
                    .warehouseAreaId(skuBatchStockDTO.getWarehouseAreaId())
                    .skuId(skuBatchStockDTO.getSkuId())
                    .batchAttributes(detail.getBatchAttributes())
                    .outboundPlanOrderId(detail.getOutboundPlanOrderId())
                    .warehouseAreaIds(detail.getWarehouseAreaIds())
                    .outboundPlanOrderDetailId(detail.getId())
                    .qtyPreAllocated(preAllocated)
                    .build();
            preAllocatedRecords.add(preAllocatedRecord);
        }
        return preAllocatedRecords;
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat(outbound): add PreAllocateOutboundOrderUseCase"
```

---

### Task 11: Create CancelOutboundPlanOrderUseCase

**Files:**
- Create: `application/usecase/CancelOutboundPlanOrderUseCase.java`
- Reference: `domain/aggregate/OutboundPlanOrderAggregate.java` + `domain/service/impl/OutboundPlanOrderServiceImpl.prepareCancelContext()`

- [ ] **Step 1: Create UseCase class**

Merge `OutboundPlanOrderAggregate.cancel()` and `OutboundPlanOrderServiceImpl.prepareCancelContext()`:

```java
package org.openwes.wes.outbound.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.wes.api.stock.IStockApi;
import org.openwes.wes.api.stock.constants.StockLockTypeEnum;
import org.openwes.wes.api.stock.dto.SkuBatchStockLockDTO;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundPreAllocatedRecord;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.OutboundPreAllocatedRecordRepository;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelOutboundPlanOrderUseCase {

    private final OutboundPlanOrderRepository outboundPlanOrderRepository;
    private final OutboundPreAllocatedRecordRepository preAllocatedRecordRepository;
    private final OutboundWaveRepository outboundWaveRepository;
    private final PickingOrderRepository pickingOrderRepository;
    private final IStockApi stockApi;

    @Transactional(rollbackFor = Exception.class)
    public void execute(List<OutboundPlanOrder> outboundPlanOrders) {
        List<Long> orderIds = outboundPlanOrders.stream().map(OutboundPlanOrder::getId).toList();

        List<OutboundPreAllocatedRecord> preAllocatedRecords =
                preAllocatedRecordRepository.findByOutboundPlanOrderIds(orderIds);

        List<String> waveNos = outboundPlanOrders.stream()
                .map(OutboundPlanOrder::getWaveNo)
                .filter(waveNo -> waveNo != null && !waveNo.isEmpty())
                .distinct().toList();
        List<OutboundWave> outboundWaves = CollectionUtils.isEmpty(waveNos)
                ? Collections.emptyList()
                : outboundWaveRepository.findByWaveNos(waveNos);

        List<PickingOrder> pickingOrders = CollectionUtils.isEmpty(waveNos)
                ? Collections.emptyList()
                : pickingOrderRepository.findByWaveNos(waveNos);

        // Cancel all entities
        outboundPlanOrders.forEach(OutboundPlanOrder::cancel);
        outboundPlanOrderRepository.saveAllOrderAndDetails(outboundPlanOrders);

        // Unlock pre-allocated stock
        if (CollectionUtils.isNotEmpty(preAllocatedRecords)) {
            List<SkuBatchStockLockDTO> unlockDTOs = preAllocatedRecords.stream().map(record -> {
                SkuBatchStockLockDTO dto = new SkuBatchStockLockDTO();
                dto.setSkuBatchStockId(record.getSkuBatchStockId());
                dto.setLockQty(-record.getQtyPreAllocated());
                dto.setLockType(StockLockTypeEnum.OUTBOUND);
                dto.setOrderDetailId(record.getOutboundPlanOrderDetailId());
                return dto;
            }).toList();
            stockApi.lockSkuBatchStock(unlockDTOs);

            preAllocatedRecords.forEach(OutboundPreAllocatedRecord::cancel);
            preAllocatedRecordRepository.saveAll(preAllocatedRecords);
        }

        outboundWaves.forEach(OutboundWave::cancel);
        if (CollectionUtils.isNotEmpty(outboundWaves)) {
            outboundWaveRepository.saveAll(outboundWaves);
        }

        pickingOrders.forEach(PickingOrder::cancel);
        if (CollectionUtils.isNotEmpty(pickingOrders)) {
            pickingOrderRepository.saveAllOrderAndDetails(pickingOrders);
        }
    }
}
```

- [ ] **Step 2: Build to verify**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "feat(outbound): add CancelOutboundPlanOrderUseCase"
```

---

### Task 12: Create remaining Use Case classes

**Files:**
- Create: `application/usecase/CreateOutboundWaveUseCase.java`
- Create: `application/usecase/SplitWaveToPickingOrdersUseCase.java`
- Create: `application/usecase/DispatchPickingOrdersUseCase.java`
- Create: `application/usecase/ReallocatePickingOrderUseCase.java`
- Create: `application/usecase/ImprovePriorityUseCase.java`
- Create: `application/usecase/EmptyContainerOutboundUseCase.java`
- Create: `application/usecase/CreateOutboundPlanOrderUseCase.java`

This task creates each UseCase by moving logic from the corresponding aggregate class. Each UseCase follows the same pattern: `@Service`, `@RequiredArgsConstructor`, `@Transactional` on the `execute()` method.

- [ ] **Step 1: Create CreateOutboundWaveUseCase**

Move logic from `OutboundWaveAggregate.waveOrders()`:

```java
package org.openwes.wes.outbound.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CreateOutboundWaveUseCase {

    private final OutboundWaveRepository outboundWaveRepository;
    private final OutboundPlanOrderRepository outboundPlanOrderRepository;

    @Transactional(rollbackFor = Exception.class)
    public void execute(List<OutboundPlanOrder> outboundPlanOrders) {
        String waveNo = OrderNoGenerator.generationOutboundWaveNo();
        Integer maxPriority = outboundPlanOrders.stream()
                .map(OutboundPlanOrder::getPriority).reduce(Integer::max).orElse(0);
        outboundWaveRepository.save(new OutboundWave(waveNo, maxPriority, outboundPlanOrders));

        outboundPlanOrders.forEach(v -> v.wave(waveNo));
        outboundPlanOrderRepository.saveAllOrders(outboundPlanOrders);
    }
}
```

- [ ] **Step 2: Create SplitWaveToPickingOrdersUseCase**

Move logic from `PickingOrderWaveAggregate.split()`:

```java
package org.openwes.wes.outbound.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.openwes.wes.outbound.domain.service.OutboundWaveService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.openwes.common.utils.constants.RedisConstants.NEW_PICKING_ORDER_IDS;

@Service
@RequiredArgsConstructor
@Slf4j
public class SplitWaveToPickingOrdersUseCase {

    private final OutboundWaveService outboundWaveService;
    private final PickingOrderRepository pickingOrderRepository;
    private final OutboundWaveRepository outboundWaveRepository;
    private final RedisUtils redisUtils;

    @Transactional(rollbackFor = Exception.class)
    public void execute(OutboundWave outboundWave) {
        List<PickingOrder> pickingOrders = outboundWaveService.spiltWave(outboundWave);
        List<PickingOrder> savedPickingOrders = pickingOrderRepository.saveAllOrderAndDetails(pickingOrders);

        outboundWave.process();
        outboundWaveRepository.save(outboundWave);

        redisUtils.pushAll(NEW_PICKING_ORDER_IDS,
                savedPickingOrders.stream().map(PickingOrder::getId).toList());
    }
}
```

- [ ] **Step 3: Create ImprovePriorityUseCase**

```java
package org.openwes.wes.outbound.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImprovePriorityUseCase {

    private final OutboundPlanOrderRepository outboundPlanOrderRepository;

    @Transactional(rollbackFor = Exception.class)
    public void execute(List<Long> outboundPlanOrderIds, int priority) {
        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(outboundPlanOrderIds);
        outboundPlanOrders.forEach(v -> v.improvePriority(priority));
        outboundPlanOrderRepository.saveAllOrders(outboundPlanOrders);
    }
}
```

- [ ] **Step 4: Create EmptyContainerOutboundUseCase**

Move logic from `EmptyContainerOutboundAggregate`:

```java
package org.openwes.wes.outbound.application.usecase;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.wes.api.basic.IContainerApi;
import org.openwes.wes.api.ems.proxy.constants.BusinessTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.constants.ContainerTaskTypeEnum;
import org.openwes.wes.api.ems.proxy.dto.CreateContainerTaskDTO;
import org.openwes.wes.api.outbound.constants.EmptyContainerOutboundOrderStatusEnum;
import org.openwes.wes.common.facade.ContainerTaskApiFacade;
import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrder;
import org.openwes.wes.outbound.domain.entity.EmptyContainerOutboundOrderDetail;
import org.openwes.wes.outbound.domain.repository.EmptyContainerOutboundOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmptyContainerOutboundUseCase {

    private final EmptyContainerOutboundOrderRepository repository;
    private final IContainerApi containerApi;
    private final ContainerTaskApiFacade containerTaskApiFacade;

    @Transactional(rollbackFor = Exception.class)
    public void create(EmptyContainerOutboundOrder order, Set<String> containerCodes) {
        containerApi.lockContainer(order.getWarehouseCode(), containerCodes);
        repository.save(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void execute(List<EmptyContainerOutboundOrder> orders) {
        orders.forEach(EmptyContainerOutboundOrder::execute);
        repository.saveAll(orders);

        List<CreateContainerTaskDTO> tasks = orders.stream()
                .flatMap(order -> order.getDetails().stream().map(detail -> {
                    CreateContainerTaskDTO task = new CreateContainerTaskDTO();
                    task.setCustomerTaskId(detail.getId());
                    task.setContainerTaskType(ContainerTaskTypeEnum.OUTBOUND);
                    task.setBusinessTaskType(BusinessTaskTypeEnum.EMPTY_CONTAINER_OUTBOUND);
                    task.setContainerCode(detail.getContainerCode());
                    task.setDestinations(Lists.newArrayList(String.valueOf(order.getWorkStationId())));
                    task.setTaskGroupCode(order.getOrderNo());
                    task.setTaskPriority(0);
                    task.setTaskGroupPriority(0);
                    return task;
                })).toList();

        containerTaskApiFacade.createContainerTasks(tasks);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(List<EmptyContainerOutboundOrder> orders) {
        List<EmptyContainerOutboundOrderDetail> pendingDetails = orders.stream()
                .filter(v -> v.getEmptyContainerOutboundStatus() == EmptyContainerOutboundOrderStatusEnum.PENDING)
                .flatMap(order -> order.getDetails().stream().filter(detail -> !detail.isCompleted()))
                .toList();

        if (ObjectUtils.isNotEmpty(pendingDetails)) {
            containerTaskApiFacade.cancelTasks(
                    pendingDetails.stream().map(EmptyContainerOutboundOrderDetail::getId).toList());
        }

        orders.forEach(EmptyContainerOutboundOrder::cancel);
        repository.saveAll(orders);
    }
}
```

- [ ] **Step 5: Build to verify all Use Cases compile**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`

- [ ] **Step 6: Commit**

```bash
git add -A && git commit -m "feat(outbound): add remaining UseCase classes (CreateWave, SplitWave, ImprovePriority, EmptyContainer)"
```

Note: `DispatchPickingOrdersUseCase`, `ReallocatePickingOrderUseCase`, and `CreateOutboundPlanOrderUseCase` are complex and will be created as part of Phase 3 when we redirect the call chains from the existing aggregates and schedulers.

---

## Phase 3: Redirect Call Chains

### Task 13: Slim Event Subscribers — redirect to UseCases

**Files:**
- Modify: `application/event/OutboundPlanOrderSubscribe.java`
- Modify: `application/event/OutboundWaveSubscribe.java`
- Modify: `application/event/PickingOrderSubscribe.java`

- [ ] **Step 1: Slim OutboundPlanOrderSubscribe**

Replace the full file:
```java
package org.openwes.wes.outbound.application.event;

import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.wes.api.outbound.event.*;
import org.openwes.wes.common.facade.CallbackApiFacade;
import org.openwes.wes.outbound.application.usecase.PreAllocateOutboundOrderUseCase;
import org.openwes.wes.outbound.domain.entity.OutboundPlanOrder;
import org.openwes.wes.outbound.domain.entity.PickingOrderDetail;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.repository.OutboundPlanOrderRepository;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.openwes.wes.outbound.domain.transfer.OutboundPlanOrderTransfer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.openwes.common.utils.constants.RedisConstants.OUTBOUND_PLAN_ORDER_ASSIGNED_IDS;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboundPlanOrderSubscribe {

    private final PreAllocateOutboundOrderUseCase preAllocateUseCase;
    private final OutboundPlanOrderRepository outboundPlanOrderRepository;
    private final PickingOrderRepository pickingOrderRepository;
    private final CallbackApiFacade callbackApiFacade;
    private final RedisUtils redisUtils;
    private final OutboundPlanOrderTransfer outboundPlanOrderTransfer;

    @Subscribe
    public void onCreateEvent(@Valid OutboundPlanOrderCreatedEvent event) {
        log.info("Receive new outbound plan order pre allocate required, order no: {}", event.getOrderNo());
        preAllocateUseCase.execute(event.getAggregatorId());
    }

    @Subscribe
    public void onAssignedEvent(@Valid OutboundPlanOrderAssignedEvent event) {
        String redisKey = OUTBOUND_PLAN_ORDER_ASSIGNED_IDS + event.getWarehouseCode();
        redisUtils.push(redisKey, event.getAggregatorId());
    }

    @Subscribe
    @Transactional(rollbackFor = Exception.class)
    public void onPickingEvent(@Valid PickingOrderPickedEvent event) {
        PickingOrderPickedEvent.PickingDetail pickingDetail = event.getPickingDetail();
        OutboundPlanOrder outboundPlanOrder = outboundPlanOrderRepository.findById(pickingDetail.getOutboundOrderId());
        outboundPlanOrder.picking(pickingDetail.getOperatedQty(), pickingDetail.getOutboundOrderDetailId());
        outboundPlanOrderRepository.saveOrderAndDetail(outboundPlanOrder);
    }

    @Subscribe
    @Transactional(rollbackFor = Exception.class)
    public void onCompleteEvent(@Valid OutboundPlanOrderCompletionEvent event) {
        OutboundPlanOrder outboundPlanOrder = outboundPlanOrderRepository.findById(event.getAggregatorId());
        callbackApiFacade.callback(CallbackApiTypeEnum.OUTBOUND_PLAN_ORDER_COMPLETE,
                outboundPlanOrder.getCustomerOrderType(),
                outboundPlanOrderTransfer.toDTO(outboundPlanOrder));
    }

    @Subscribe
    @Transactional(rollbackFor = Exception.class)
    public void onOutboundWaveCompletionEvent(@Valid OutboundWaveCompletionEvent event) {
        List<OutboundPlanOrder> outboundPlanOrders = outboundPlanOrderRepository.findAllByIds(event.getOutboundPlanOrderIds());
        outboundPlanOrders.stream()
                .filter(v -> v.getOutboundPlanOrderStatus() != org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum.PICKED)
                .forEach(OutboundPlanOrder::shortComplete);
        outboundPlanOrderRepository.saveAllOrderAndDetails(outboundPlanOrders);
    }
}
```

Key changes:
- Removed `onDispatchedEvent()` — logic moves to `DispatchPickingOrdersUseCase`
- `onCreateEvent()` delegates to `PreAllocateOutboundOrderUseCase`
- Added `@Transactional` to write-path handlers
- `onPickingEvent()` and `onOutboundWaveCompletionEvent()` remain as thin handlers (simple load-mutate-save)

- [ ] **Step 2: Slim OutboundWaveSubscribe**

```java
package org.openwes.wes.outbound.application.event;

import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.api.outbound.constants.OutboundWaveStatusEnum;
import org.openwes.wes.api.outbound.constants.PickingOrderStatusEnum;
import org.openwes.wes.api.outbound.event.OutboundWaveCreatedEvent;
import org.openwes.wes.api.outbound.event.PickingOrderCompletionEvent;
import org.openwes.wes.outbound.application.usecase.SplitWaveToPickingOrdersUseCase;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboundWaveSubscribe {

    private final OutboundWaveRepository outboundWaveRepository;
    private final SplitWaveToPickingOrdersUseCase splitWaveUseCase;
    private final PickingOrderRepository pickingOrderRepository;

    @Subscribe
    @Transactional(rollbackFor = Exception.class)
    public void onCreateEvent(@Valid OutboundWaveCreatedEvent event) {
        OutboundWave outboundWave = outboundWaveRepository.findByWaveNo(event.getWaveNo());
        if (outboundWave.getWaveStatus() != OutboundWaveStatusEnum.NEW) {
            return;
        }
        splitWaveUseCase.execute(outboundWave);
    }

    @Subscribe
    @Transactional(rollbackFor = Exception.class)
    public void onPickingOrderCompleteEvent(@Valid PickingOrderCompletionEvent event) {
        PickingOrder pickingOrder = pickingOrderRepository.findById(event.getAggregatorId());
        String waveNo = pickingOrder.getWaveNo();
        List<PickingOrder> pickingOrders = pickingOrderRepository.findByWaveNo(waveNo);

        if (pickingOrders.stream().allMatch(v ->
                v.getPickingOrderStatus() == PickingOrderStatusEnum.PICKED
                        || v.getPickingOrderStatus() == PickingOrderStatusEnum.CANCELED)) {
            OutboundWave outboundWave = outboundWaveRepository.findByWaveNo(waveNo);
            outboundWave.complete();
            outboundWaveRepository.save(outboundWave);
        }
    }
}
```

- [ ] **Step 3: Slim PickingOrderSubscribe — add @Transactional**

```java
package org.openwes.wes.outbound.application.event;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.wes.api.basic.IPutWallApi;
import org.openwes.wes.api.basic.IWarehouseAreaApi;
import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.openwes.wes.api.basic.dto.WarehouseAreaDTO;
import org.openwes.wes.api.outbound.IPickingOrderApi;
import org.openwes.wes.api.outbound.event.OutboundPlanOrderImprovedPriorityEvent;
import org.openwes.wes.api.outbound.event.PickingOrderRemindSealContainerEvent;
import org.openwes.wes.api.task.ITaskApi;
import org.openwes.wes.api.task.dto.OperationTaskPickingDTO;
import org.openwes.wes.api.task.event.OperationTaskAbnormalEvent;
import org.openwes.wes.api.task.event.OperationTaskPickedEvent;
import org.openwes.wes.outbound.domain.entity.PickingOrder;
import org.openwes.wes.outbound.domain.repository.PickingOrderRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PickingOrderSubscribe {

    private final IPickingOrderApi pickingOrderApi;
    private final IWarehouseAreaApi warehouseAreaApi;
    private final IPutWallApi putWallApi;
    private final ITaskApi taskApi;
    private final PickingOrderRepository pickingOrderRepository;

    @Subscribe
    @Transactional(rollbackFor = Exception.class)
    public void onOperationTaskPickedEvent(@Valid OperationTaskPickedEvent event) {
        OperationTaskPickingDTO operationTask = event.getOperationTaskPicking();
        PickingOrder pickingOrder = pickingOrderRepository.findById(operationTask.getOrderId());
        pickingOrder.picking(operationTask.getOperatedQty(), operationTask.getDetailId());
        pickingOrderRepository.saveOrderAndDetail(pickingOrder);
    }

    @Subscribe
    @Transactional(rollbackFor = Exception.class)
    public void onOperationTaskAbnormalEvent(@Valid OperationTaskAbnormalEvent event) {
        PickingOrder pickingOrder = pickingOrderRepository.findById(event.getPickingOrderId());
        pickingOrder.reportAbnormal(event.getAbnormalQty(), event.getPickingOrderDetailId());
        pickingOrderRepository.saveOrderAndDetail(pickingOrder);
        pickingOrderApi.reallocate(Lists.newArrayList(event.getPickingOrderDetailId()));
    }

    @Subscribe
    public void onPickingOrderRemindSealContainerEvent(@Valid PickingOrderRemindSealContainerEvent event) {
        WarehouseAreaDTO warehouseArea = warehouseAreaApi.getById(event.getWarehouseAreaId());
        if (WarehouseAreaWorkTypeEnum.ROBOT == warehouseArea.getWarehouseAreaWorkType()) {
            putWallApi.remindToSealContainer(event.getAggregatorId(), event.getAssignedStationSlots());
        } else if (WarehouseAreaWorkTypeEnum.MANUAL == warehouseArea.getWarehouseAreaWorkType()) {
            taskApi.sealContainer(event.getAggregatorId());
        }
    }

    @Subscribe
    @Transactional(rollbackFor = Exception.class)
    public void onImprovePriority(OutboundPlanOrderImprovedPriorityEvent event) {
        List<PickingOrder> pickingOrders = pickingOrderRepository.findAllByOutboundPlanOrderId(event.getAggregatorId());
        if (ObjectUtils.isEmpty(pickingOrders)) {
            return;
        }
        pickingOrders.forEach(pickingOrder -> pickingOrder.improvePriority(event.getPriority()));
        pickingOrderRepository.saveAllOrders(pickingOrders);
    }
}
```

- [ ] **Step 4: Build to verify**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(outbound): slim event subscribers, add @Transactional, redirect to UseCases"
```

---

### Task 14: Slim ApiImpl — redirect to UseCases

**Files:**
- Modify: `application/OutboundPlanOrderApiImpl.java`
- Modify: `application/EmptyContainerOutboundOrderApiImpl.java`

- [ ] **Step 1: Update OutboundPlanOrderApiImpl**

Replace references from aggregates to UseCases:
- `outboundPlanOrderAggregate.cancel(context)` → `cancelOutboundPlanOrderUseCase.execute(context.getOutboundPlanOrders())`
- `improvePriority` loop → `improvePriorityUseCase.execute(ids, priority)`

Update the dependency injection and method calls accordingly.

- [ ] **Step 2: Update EmptyContainerOutboundOrderApiImpl**

Replace `emptyContainerOutboundAggregate` with `emptyContainerOutboundUseCase` calls.

- [ ] **Step 3: Build to verify**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "refactor(outbound): slim ApiImpl, redirect to UseCases"
```

---

### Task 15: Update Schedulers to use UseCases

**Files:**
- Modify: `application/scheduler/OutboundWaveScheduler.java`

- [ ] **Step 1: Replace OutboundWaveAggregate reference with CreateOutboundWaveUseCase**

Update the scheduler to inject and call `createOutboundWaveUseCase.execute()` instead of `outboundWaveAggregate.waveOrders()`.

- [ ] **Step 2: Build to verify**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`

- [ ] **Step 3: Commit**

```bash
git add -A && git commit -m "refactor(outbound): update schedulers to use UseCases"
```

---

## Phase 4: Cleanup

### Task 16: Move ApiImpl files to application/api/ subpackage

**Files:**
- Move: `application/OutboundPlanOrderApiImpl.java` → `application/api/OutboundPlanOrderApiImpl.java`
- Move: `application/PickingOrderApiImpl.java` → `application/api/PickingOrderApiImpl.java`
- Move: `application/EmptyContainerOutboundOrderApiImpl.java` → `application/api/EmptyContainerOutboundOrderApiImpl.java`

- [ ] **Step 1: Create api/ directory and move files**

```bash
mkdir -p D:/open-wes/server/modules-wes/wes-outbound/src/main/java/org/openwes/wes/outbound/application/api
```

Move each file and update the `package` declaration from `org.openwes.wes.outbound.application` to `org.openwes.wes.outbound.application.api`.

- [ ] **Step 2: Update all internal imports referencing moved classes**

Grep for imports of the moved classes within wes-outbound and update.

- [ ] **Step 3: Build to verify**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`

- [ ] **Step 4: Commit**

```bash
git add -A && git commit -m "refactor(outbound): move ApiImpl to application/api/ subpackage"
```

---

### Task 17: Delete domain/aggregate/ package

**Files:**
- Delete: `domain/aggregate/OutboundPlanOrderAggregate.java`
- Delete: `domain/aggregate/OutboundPlanOrderPreAllocatedAggregate.java`
- Delete: `domain/aggregate/OutboundWaveAggregate.java`
- Delete: `domain/aggregate/PickingOrderWaveAggregate.java`
- Delete: `domain/aggregate/PickingOrderTaskAggregate.java`
- Delete: `domain/aggregate/EmptyContainerOutboundAggregate.java`

- [ ] **Step 1: Verify no remaining references to aggregate classes**

```bash
cd D:/open-wes/server
grep -rn "Aggregate" modules-wes/wes-outbound/src/main/java/ --include="*.java" | grep -v "AggregatorRoot\|usecase\|test"
```

Expected: No references to deleted aggregate classes remain.

- [ ] **Step 2: Delete aggregate files**

```bash
rm -rf D:/open-wes/server/modules-wes/wes-outbound/src/main/java/org/openwes/wes/outbound/domain/aggregate/
```

- [ ] **Step 3: Build to verify**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:compileJava`

- [ ] **Step 4: Full test run**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:test`

- [ ] **Step 5: Commit**

```bash
git add -A && git commit -m "refactor(outbound): delete domain/aggregate/ package, replaced by application/usecase/"
```

---

## Phase 5: Full Build Verification

### Task 18: Full server build and integration check

**Files:**
- All modified files across wes-outbound

- [ ] **Step 1: Full server compilation**

Run: `cd D:/open-wes/server && ./gradlew compileJava`
Expected: BUILD SUCCESSFUL — all modules compile including those that depend on wes-outbound

- [ ] **Step 2: Run all outbound tests**

Run: `cd D:/open-wes/server && ./gradlew :modules-wes:wes-outbound:test`
Expected: All tests pass

- [ ] **Step 3: Commit if any fixes were needed**

```bash
git add -A && git commit -m "fix(outbound): resolve build issues from DDD refactoring"
```

---

## Phase 6: Coding Standards Update

### Task 19: Update docs/standards/backend.md

**Files:**
- Modify: `docs/standards/backend.md`

- [ ] **Step 1: Update Domain Entity section**

Add @Builder as required annotation set, forbid @Data:

In the Domain Entity section (after line 65), add:
```markdown
6. **Entity Annotations**: Domain entities must use `@Getter @Builder` instead of `@Data`. The `@Data` annotation is **forbidden** on domain entities because it exposes public setters that bypass domain methods. Required annotation set:
   ```java
   @Getter
   @Builder
   @AllArgsConstructor(access = AccessLevel.PRIVATE)
   @NoArgsConstructor(access = AccessLevel.PROTECTED) // Required by JPA/MapStruct
   @EqualsAndHashCode(callSuper = true) // Only for entities extending AggregatorRoot
   ```

7. **Entity Construction**: Prefer static factory methods (e.g., `PickingOrder.create()`) or `@Builder` for entity construction. Never use `new Entity()` + setter chains outside the entity itself.
```

- [ ] **Step 2: Replace Domain Aggregate section with Use Case Pattern**

Replace the Domain Aggregate section (line 119-121) with:
```markdown
### Use Case Pattern (replaces Domain Aggregate)
1. **Use Case Responsibilities**: Use cases orchestrate cross-aggregate operations. They live in `application/usecase/` and own the `@Transactional` boundary. Each use case represents one business operation.
   ```java
   @Service
   @RequiredArgsConstructor
   public class CancelOutboundPlanOrderUseCase {
       @Transactional(rollbackFor = Exception.class)
       public void execute(List<OutboundPlanOrder> orders) {
           // Load entities, call domain methods, call external APIs, save
       }
   }
   ```
2. **Naming Convention**: `[Action][Entity]UseCase`, e.g., `CancelOutboundPlanOrderUseCase`, `PreAllocateOutboundOrderUseCase`.
3. **Deprecation**: The `domain/aggregate/` package pattern is deprecated. Cross-aggregate orchestration belongs in Use Case classes, not the domain layer.
```

- [ ] **Step 3: Update Domain Service section**

Add explicit prohibitions:
```markdown
2. **Domain Service Prohibitions**: Domain services must NOT have `@Transactional`, must NOT call external APIs, and must NOT perform persistence operations. They are pure stateless calculation only.
```

- [ ] **Step 4: Update Domain Events section**

Add event subscriber rules:
```markdown
3. **Event Subscriber Rules**: Event subscribers must be thin dispatchers that delegate to Use Cases. They must have `@Transactional(rollbackFor = Exception.class)` on write paths. No business logic in subscribers — only load event params, call UseCase, done.
```

- [ ] **Step 5: Update Transaction section**

Replace "Aggregates and repositories" with "UseCases and repositories":
```markdown
2. **Use @Transactional Annotation Appropriately**: We use the `@Transactional` annotation on **Use Cases and Repositories**. Domain services and entities should not use this annotation. Event subscribers should use `@Transactional` on write-path handlers.
```

- [ ] **Step 6: Update Class Naming Conventions table**

Add UseCase row:
```markdown
   | UseCase | `[Action][Entity]UseCase` | `CancelOutboundPlanOrderUseCase` |
```

- [ ] **Step 7: Commit**

```bash
git add docs/standards/backend.md && git commit -m "docs: update backend standards for UseCase pattern and @Builder entities"
```

---

### Task 20: Update CLAUDE.md

**Files:**
- Modify: `CLAUDE.md`

- [ ] **Step 1: Update DDD Layer Flow**

Replace:
```
Controller -> IEntityApi (interface) -> EntityApiImpl -> Domain Service -> Entity -> Repository -> JPA
```
With:
```
Controller -> IEntityApi (interface) -> EntityApiImpl -> UseCase -> Domain Service -> Entity -> Repository -> JPA
```

- [ ] **Step 2: Add UseCase naming to Class Naming table**

Add row:
```markdown
| UseCase | `[Action][Entity]UseCase` | `CancelOutboundPlanOrderUseCase` |
```

- [ ] **Step 3: Update Domain Entity Rules**

Add to the entity rules: "Use `@Getter @Builder`, never `@Data` on domain entities"

- [ ] **Step 4: Commit**

```bash
git add CLAUDE.md && git commit -m "docs: update CLAUDE.md for UseCase pattern and @Builder entity rules"
```

---

## Summary

| Phase | Tasks | Files Changed | Risk |
|-------|-------|---------------|------|
| Phase 0 | Task 0 | Read-only analysis | None |
| Phase 1 | Tasks 1-9 | 8 entity files + 1 aggregate + 1 API impl + 1 test | Medium |
| Phase 2 | Tasks 10-12 | 8 new UseCase files | Low (additive) |
| Phase 3 | Tasks 13-15 | 3 subscribers + 2 API impls + 1 scheduler | Medium |
| Phase 4 | Tasks 16-17 | 3 moved + 6 deleted | Low (cleanup) |
| Phase 5 | Task 18 | Full build verification | Low |
| Phase 6 | Tasks 19-20 | 2 docs files | Low |
