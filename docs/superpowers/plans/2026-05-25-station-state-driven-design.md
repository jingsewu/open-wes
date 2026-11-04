# Station State-Driven Unified Model Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Refactor modules-station to a true state-driven design where cache areas are first-class objects and the cache IS the API response, eliminating ~29 mapping/transform classes.

**Architecture:** Unified WorkStationCache model with area objects (WorkLocationArea, SkuArea, PutWallArea, OrderArea, Toolbar, Tip). Subclasses retain mode-specific behavior but add no state fields. Cache serializes directly to Redis and serves as the GET /api response. Jackson `@JsonTypeInfo` ensures correct subclass deserialization from Redis.

**Tech Stack:** Java 17, Spring Boot 3.2.2, Spring Data Redis, Jackson (with polymorphic type info), Lombok, MapStruct (being removed)

**Spec:** `docs/superpowers/specs/2026-05-25-station-state-driven-design.md`

---

## Context

The modules-station module has drifted from its original state-driven design intent. Three object models (WorkStationCache, WorkStationVO, WorkStationDTO) represent overlapping state with 29 classes for mapping between them. 12 area handler classes rebuild the VO on every GET request, and business logic has leaked into the view layer. This refactoring consolidates everything into a single cache model structured around UI areas, eliminating the view-building layer entirely.

---

## Phase 1: Create Area Model Classes in station-api

### Task 1: Create area model classes and ArrivedContainerCache relocation

New area models are additive — no existing code breaks yet. `ArrivedContainerCache` must move to `station-api` to avoid inverted module dependency (api must not import from domain).

**Files:**
- Create: `server/modules-station/station-api/src/main/java/org/openwes/station/api/model/ArrivedContainerCache.java` (moved from domain)
- Create: `server/modules-station/station-api/src/main/java/org/openwes/station/api/model/WorkLocationArea.java`
- Create: `server/modules-station/station-api/src/main/java/org/openwes/station/api/model/SkuArea.java`
- Create: `server/modules-station/station-api/src/main/java/org/openwes/station/api/model/PutWallArea.java`
- Create: `server/modules-station/station-api/src/main/java/org/openwes/station/api/model/OrderArea.java`
- Create: `server/modules-station/station-api/src/main/java/org/openwes/station/api/model/Toolbar.java`
- Create: `server/modules-station/station-api/src/main/java/org/openwes/station/api/model/Tip.java`

Note: `ChooseAreaEnum` will be extracted from `WorkStationVO` in Phase 5 when we delete the VO. Creating it now would produce a duplicate that's unused until then.

- [ ] **Step 1: Move ArrivedContainerCache to station-api**

Move `server/modules-station/station/src/main/java/org/openwes/station/domain/entity/ArrivedContainerCache.java` to `server/modules-station/station-api/src/main/java/org/openwes/station/api/model/ArrivedContainerCache.java`. Update package to `org.openwes.station.api.model`. Keep all existing fields and methods. Update all imports in station module to use new package.

```java
package org.openwes.station.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.station.api.constants.ProcessStatusEnum;
import org.openwes.wes.api.basic.dto.ContainerSpecDTO;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ArrivedContainerCache {

    private Long workStationId;
    private String containerCode;
    private String face;
    private Integer rotationAngle;
    private String forwardFace;

    private String locationCode;
    private String workLocationCode;
    private String groupCode = "";
    private String robotCode;
    private String robotType;
    private Integer level;
    private Integer bay;

    private List<String> taskCodes;
    private boolean empty;
    private ProcessStatusEnum processStatus;
    private Map<String, Object> containerAttributes;
    private ContainerSpecDTO containerSpec;

    public void init() {
        this.processStatus = ProcessStatusEnum.UNDO;
    }

    public void proceed() {
        this.processStatus = ProcessStatusEnum.PROCEED;
    }

    public void processing() {
        this.processStatus = ProcessStatusEnum.PROCESSING;
    }
}
```

- [ ] **Step 2: Create WorkLocationArea.java**

```java
package org.openwes.station.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.station.api.constants.ProcessStatusEnum;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkLocationArea {
    private List<WorkLocationView> workLocationViews;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkLocationView {
        private String workLocationCode;
        private String workLocationType;
        private boolean enable;
        private String stationCode;
        private List<WorkLocationSlot> workLocationSlots;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WorkLocationSlot {
        private String slotCode;
        private String workLocationCode;
        private String groupCode;
        private int level;
        private int bay;
        private boolean enable;
        private ArrivedContainerCache arrivedContainer; // null = empty slot
        private Set<String> activeSlotCodes; // active container slots for current task
    }

    /**
     * Place a container onto its matching slot. If the slot doesn't exist
     * but the work location matches, create a dynamic slot (robot/cache shelf case).
     */
    public void placeContainer(ArrivedContainerCache container) {
        if (workLocationViews == null) return;
        for (WorkLocationView view : workLocationViews) {
            if (!view.isEnable() || view.getWorkLocationSlots() == null) continue;
            if (placeOnExistingSlot(view, container)) return;
        }
        // No existing slot found — try dynamic slot creation
        for (WorkLocationView view : workLocationViews) {
            if (!view.isEnable()) continue;
            if (view.getWorkLocationCode().equals(container.getWorkLocationCode())) {
                createDynamicSlot(view, container);
                return;
            }
        }
    }

    private boolean placeOnExistingSlot(WorkLocationView view, ArrivedContainerCache container) {
        for (WorkLocationSlot slot : view.getWorkLocationSlots()) {
            if (slot.getSlotCode().equals(container.getLocationCode())
                    && slot.getWorkLocationCode().equals(container.getWorkLocationCode())) {
                slot.setArrivedContainer(container);
                return true;
            }
        }
        return false;
    }

    private void createDynamicSlot(WorkLocationView view, ArrivedContainerCache container) {
        WorkLocationSlot newSlot = new WorkLocationSlot();
        newSlot.setSlotCode(container.getLocationCode());
        newSlot.setWorkLocationCode(view.getWorkLocationCode());
        newSlot.setGroupCode(container.getGroupCode());
        newSlot.setLevel(container.getLevel());
        newSlot.setBay(container.getBay());
        newSlot.setEnable(true);
        newSlot.setArrivedContainer(container);
        if (view.getWorkLocationSlots() == null) {
            view.setWorkLocationSlots(new ArrayList<>());
        }
        view.getWorkLocationSlots().add(newSlot);
    }

    public void removeContainer(String containerCode) {
        if (workLocationViews == null) return;
        for (WorkLocationView view : workLocationViews) {
            if (view.getWorkLocationSlots() == null) continue;
            view.getWorkLocationSlots().forEach(slot -> {
                if (slot.getArrivedContainer() != null
                        && slot.getArrivedContainer().getContainerCode().equals(containerCode)) {
                    slot.setArrivedContainer(null);
                }
            });
        }
    }

    public boolean hasContainers() {
        if (workLocationViews == null) return false;
        return workLocationViews.stream()
                .filter(v -> v.getWorkLocationSlots() != null)
                .flatMap(v -> v.getWorkLocationSlots().stream())
                .anyMatch(s -> s.getArrivedContainer() != null);
    }

    public List<ArrivedContainerCache> getAllContainers() {
        if (workLocationViews == null) return Collections.emptyList();
        return workLocationViews.stream()
                .filter(v -> v.getWorkLocationSlots() != null)
                .flatMap(v -> v.getWorkLocationSlots().stream())
                .map(WorkLocationSlot::getArrivedContainer)
                .filter(Objects::nonNull)
                .toList();
    }

    public List<ArrivedContainerCache> getContainersByStatus(ProcessStatusEnum status) {
        return getAllContainers().stream()
                .filter(c -> c.getProcessStatus() == status)
                .toList();
    }

    public List<ArrivedContainerCache> getUndoContainers() {
        return getAllContainers().stream()
                .filter(c -> c.getProcessStatus() == ProcessStatusEnum.UNDO
                        || c.getProcessStatus() == ProcessStatusEnum.PROCESSING)
                .toList();
    }

    public List<ArrivedContainerCache> getProcessingContainers() {
        return getContainersByStatus(ProcessStatusEnum.PROCESSING);
    }

    public List<ArrivedContainerCache> removeProceedContainers() {
        List<ArrivedContainerCache> allContainers = getAllContainers();
        Set<String> proceedGroupCodes = new HashSet<>();
        allContainers.stream()
                .collect(Collectors.groupingBy(ArrivedContainerCache::getGroupCode))
                .forEach((groupCode, containers) -> {
                    if (containers.stream().allMatch(c -> c.getProcessStatus() == ProcessStatusEnum.PROCEED)) {
                        proceedGroupCodes.add(groupCode);
                    }
                });

        List<ArrivedContainerCache> removed = allContainers.stream()
                .filter(c -> proceedGroupCodes.contains(c.getGroupCode()))
                .toList();

        if (workLocationViews != null) {
            workLocationViews.stream()
                    .filter(v -> v.getWorkLocationSlots() != null)
                    .flatMap(v -> v.getWorkLocationSlots().stream())
                    .forEach(slot -> {
                        if (slot.getArrivedContainer() != null
                                && proceedGroupCodes.contains(slot.getArrivedContainer().getGroupCode())) {
                            slot.setArrivedContainer(null);
                        }
                    });
        }
        return removed;
    }

    public void setActiveContainerSlots(String containerCode, String face, Set<String> slotCodes) {
        if (workLocationViews == null) return;
        workLocationViews.stream()
                .filter(v -> v.getWorkLocationSlots() != null)
                .flatMap(v -> v.getWorkLocationSlots().stream())
                .forEach(slot -> slot.setActiveSlotCodes(null));
        workLocationViews.stream()
                .filter(v -> v.getWorkLocationSlots() != null)
                .flatMap(v -> v.getWorkLocationSlots().stream())
                .filter(slot -> slot.getArrivedContainer() != null
                        && slot.getArrivedContainer().getContainerCode().equals(containerCode)
                        && Objects.equals(slot.getArrivedContainer().getFace(), face))
                .forEach(slot -> slot.setActiveSlotCodes(slotCodes));
    }

    public void setUndoContainersProcessing(boolean isOneStepRelocation) {
        List<ArrivedContainerCache> undoContainers = getContainersByStatus(ProcessStatusEnum.UNDO);
        if (undoContainers.isEmpty()) return;
        int limit = isOneStepRelocation ? 2 : 1;
        undoContainers.stream().limit(limit).forEach(ArrivedContainerCache::processing);
    }
}
```

- [ ] **Step 3: Create SkuArea.java**

```java
package org.openwes.station.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.dto.OperationTaskDTO;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkuArea {
    private String scanCode;
    private List<SkuTaskInfo> operationViews;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuTaskInfo {
        private SkuMainDataDTO skuMainDataDTO;
        private SkuBatchAttributeDTO skuBatchAttributeDTO;
        private List<OperationTaskDTO> operationTaskDTOs;
    }

    public void updateOperationViews(List<SkuTaskInfo> tasks) {
        if (this.operationViews == null) {
            this.operationViews = new ArrayList<>(tasks);
        } else {
            this.operationViews.addAll(tasks);
        }
    }

    public void markTasksProcessing(String skuCode, String containerCode, String face) {
        if (CollectionUtils.isEmpty(operationViews)) return;

        operationViews.stream()
                .filter(v -> v.getOperationTaskDTOs() != null)
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .forEach(task -> task.setTaskStatus(OperationTaskStatusEnum.NEW));

        operationViews.stream()
                .filter(v -> v.getSkuMainDataDTO() != null
                        && skuCode.equals(v.getSkuMainDataDTO().getSkuCode()))
                .filter(v -> v.getOperationTaskDTOs() != null)
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .filter(task -> Objects.equals(containerCode, task.getSourceContainerCode())
                        && (face == null || face.isEmpty() || Objects.equals(face, task.getSourceContainerFace())))
                .forEach(task -> task.setTaskStatus(OperationTaskStatusEnum.PROCESSING));
    }

    public void removeCompletedTasks() {
        if (operationViews == null) return;
        operationViews.forEach(view -> {
            if (view.getOperationTaskDTOs() != null) {
                view.getOperationTaskDTOs().removeIf(task ->
                        task.getRequiredQty() - task.getOperatedQty() - task.getAbnormalQty() == 0);
            }
        });
        operationViews.removeIf(view ->
                view.getOperationTaskDTOs() == null || view.getOperationTaskDTOs().isEmpty());
    }

    public void reportAbnormal(Map<Long, Integer> taskAbnormalQtyMap) {
        if (operationViews == null) return;
        operationViews.stream()
                .filter(v -> v.getOperationTaskDTOs() != null)
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .filter(task -> task.getTaskStatus() == OperationTaskStatusEnum.PROCESSING)
                .filter(task -> taskAbnormalQtyMap.containsKey(task.getId()))
                .forEach(task -> task.setAbnormalQty(taskAbnormalQtyMap.get(task.getId())));

        operationViews.forEach(view -> {
            if (view.getOperationTaskDTOs() != null) {
                view.getOperationTaskDTOs().removeIf(task ->
                        task.getRequiredQty().equals(task.getAbnormalQty()));
            }
        });
        operationViews.removeIf(view ->
                view.getOperationTaskDTOs() == null || view.getOperationTaskDTOs().isEmpty());
    }

    public boolean hasProcessingTasks() {
        return !getProcessingTasks().isEmpty();
    }

    public boolean hasTasks() {
        return CollectionUtils.isNotEmpty(operationViews)
                && operationViews.stream().anyMatch(v -> CollectionUtils.isNotEmpty(v.getOperationTaskDTOs()));
    }

    public boolean hasAbnormalTasks() {
        if (operationViews == null) return false;
        return operationViews.stream()
                .filter(v -> v.getOperationTaskDTOs() != null)
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .anyMatch(task -> task.getAbnormalQty() > 0);
    }

    public List<OperationTaskDTO> getProcessingTasks() {
        if (operationViews == null) return Collections.emptyList();
        return operationViews.stream()
                .filter(v -> v.getOperationTaskDTOs() != null)
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .filter(task -> task.getTaskStatus() == OperationTaskStatusEnum.PROCESSING)
                .toList();
    }

    public OperationTaskDTO getFirstProcessingTask() {
        return getProcessingTasks().stream().findFirst().orElse(null);
    }

    public OperationTaskDTO getFirstTask() {
        if (operationViews == null) return null;
        return operationViews.stream()
                .filter(v -> CollectionUtils.isNotEmpty(v.getOperationTaskDTOs()))
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .findFirst().orElse(null);
    }

    public List<OperationTaskDTO> getAllTasks() {
        if (operationViews == null) return Collections.emptyList();
        return operationViews.stream()
                .filter(v -> v.getOperationTaskDTOs() != null)
                .flatMap(v -> v.getOperationTaskDTOs().stream())
                .toList();
    }

    public void clear() {
        this.scanCode = null;
        if (this.operationViews != null) {
            this.operationViews.clear();
        }
    }
}
```

- [ ] **Step 4: Create PutWallArea.java**

```java
package org.openwes.station.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.dto.PutWallDTO;
import org.openwes.wes.api.basic.dto.PutWallSlotDTO;
import org.openwes.wes.api.basic.dto.PutWallTagConfigDTO;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PutWallArea {
    private String activePutWallCode;
    private String inputPutWallSlot;
    private String putWallDisplayStyle;
    private PutWallTagConfigDTO putWallTagConfigDTO;
    private List<PutWallDTO> putWallViews;

    public void input(String slotCode) {
        this.inputPutWallSlot = slotCode;
    }

    public void clearInput() {
        this.inputPutWallSlot = null;
    }

    public void resetActivePutWall(Set<String> processingSlotCodes) {
        if (putWallViews == null) return;
        boolean match = putWallViews.stream()
                .flatMap(pw -> pw.getPutWallSlots().stream())
                .anyMatch(slot -> StringUtils.equals(this.activePutWallCode, slot.getPutWallCode())
                        && processingSlotCodes.contains(slot.getPutWallSlotCode()));
        if (!match) {
            this.activePutWallCode = putWallViews.stream()
                    .flatMap(pw -> pw.getPutWallSlots().stream())
                    .filter(slot -> processingSlotCodes.contains(slot.getPutWallSlotCode()))
                    .map(PutWallSlotDTO::getPutWallCode)
                    .findAny().orElse(null);
        }
    }

    public boolean hasWaitingBindingSlots() {
        if (putWallViews == null) return false;
        return putWallViews.stream()
                .flatMap(pw -> pw.getPutWallSlots().stream())
                .filter(PutWallSlotDTO::isEnable)
                .anyMatch(slot -> PutWallSlotStatusEnum.WAITING_BINDING == slot.getPutWallSlotStatus());
    }

    public Optional<PutWallSlotDTO> getSlot(String putWallSlotCode) {
        if (putWallViews == null) return Optional.empty();
        return putWallViews.stream()
                .flatMap(pw -> pw.getPutWallSlots().stream())
                .filter(slot -> StringUtils.equals(slot.getPutWallSlotCode(), putWallSlotCode))
                .findFirst();
    }

    public void validatePicking(PutWallSlotDTO putWallSlot) {
        if (putWallSlot.getPutWallSlotStatus() == PutWallSlotStatusEnum.IDLE) {
            throw new IllegalStateException("put wall slot is idle, please waiting order dispatched");
        }
        if (putWallSlot.getPutWallSlotStatus() == PutWallSlotStatusEnum.WAITING_BINDING) {
            throw new IllegalStateException("put wall slot wait binding, please bound first");
        }
        if (putWallSlot.getPutWallSlotStatus() == PutWallSlotStatusEnum.WAITING_SEAL) {
            throw new IllegalStateException("put wall slot wait sealing, please seal first");
        }
    }
}
```

- [ ] **Step 5: Create OrderArea.java, Toolbar.java, Tip.java**

```java
// OrderArea.java
package org.openwes.station.api.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderArea {
    private OrderVO currentOrder;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderVO {
        private String orderNo;
        private String orderType;
        private String stocktakeCreateMethod;
        private String stocktakeMethod;
        private String stocktakeType;
    }
}
```

```java
// Toolbar.java
package org.openwes.station.api.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Toolbar {
    private boolean enableReportAbnormal;
    private boolean enableSplitContainer;
    private boolean enableReleaseSlot;
}
```

```java
// Tip.java
package org.openwes.station.api.model;

import lombok.*;
import java.util.Objects;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tip {
    private TipTypeEnum tipType;
    private String type;
    private Object data;
    private Long duration;
    private String tipCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tip tip = (Tip) o;
        return Objects.equals(tipType, tip.tipType) && Objects.equals(tipCode, tip.tipCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tipType, tipCode);
    }

    @Getter
    public enum TipTypeEnum {
        EMPTY_CONTAINER_HANDLE_TIP,
        CHOOSE_PICKING_TASK_TIP,
        SEAL_CONTAINER_TIP,
        REPORT_ABNORMAL_TIP,
        SCAN_ERROR_REMIND_TIP,
        FULL_CONTAINER_AUTO_OUTBOUND_TIP,
        PICKING_VOICE_TIP,
        INBOUND_ABNORMAL_TIP,
        BARCODE_2_MANY_SKU_CODE_TIP,
        SKU_ORDERS_OR_OWNER_CODES_TIP,
    }

    @Getter
    @AllArgsConstructor
    public enum TipShowTypeEnum {
        TIP("tip", "tip"),
        CONFIRM("confirm", "confirm dialog"),
        VOICE("voice", "voice broadcast"),
        ;
        private final String value;
        private final String name;
    }
}
```

- [ ] **Step 6: Verify compilation**

Run: `cd /d/open-wes/server && ./gradlew :modules-station:station-api:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: Commit**

```bash
git add server/modules-station/station-api/src/main/java/org/openwes/station/api/model/
git commit -m "feat(station): add area model classes and move ArrivedContainerCache to station-api"
```

---

### Task 2: Unit tests for area model classes

The area models contain non-trivial logic that must be tested before other phases depend on them.

**Files:**
- Create: `server/modules-station/station/src/test/java/org/openwes/station/api/model/WorkLocationAreaTest.java`
- Create: `server/modules-station/station/src/test/java/org/openwes/station/api/model/SkuAreaTest.java`
- Create: `server/modules-station/station/src/test/java/org/openwes/station/api/model/PutWallAreaTest.java`

- [ ] **Step 1: Create WorkLocationAreaTest**

Test cases:
- `placeContainer_onExistingSlot_setsArrivedContainer`
- `placeContainer_noneExisting_createsDynamicSlot`
- `removeContainer_clearsSlot`
- `removeProceedContainers_removesOnlyFullyProceedGroups`
- `removeProceedContainers_keepsMixedStatusGroups`
- `getUndoContainers_returnsUndoAndProcessing`
- `setActiveContainerSlots_clearsOldAndSetsNew`
- `hasContainers_returnsFalseWhenEmpty`

- [ ] **Step 2: Create SkuAreaTest**

Test cases:
- `markTasksProcessing_resetsAllToNewThenSetsMatching`
- `markTasksProcessing_filtersbyContainerAndFace`
- `removeCompletedTasks_removesFullyOperatedTasks`
- `removeCompletedTasks_removesEmptyViews`
- `reportAbnormal_updatesAbnormalQtyAndRemovesZeroPick`
- `hasProcessingTasks_returnsTrueWhenExists`
- `getFirstTask_returnsNullWhenEmpty`
- `clear_resetsAll`

- [ ] **Step 3: Create PutWallAreaTest**

Test cases:
- `resetActivePutWall_switchesWhenCurrentDoesntMatch`
- `resetActivePutWall_keepsWhenCurrentMatches`
- `hasWaitingBindingSlots_checksEnabledOnly`
- `validatePicking_throwsOnIdle`
- `validatePicking_throwsOnWaitingBinding`
- `validatePicking_throwsOnWaitingSeal`

- [ ] **Step 4: Run tests**

Run: `cd /d/open-wes/server && ./gradlew :modules-station:station:test`
Expected: ALL PASS

- [ ] **Step 5: Commit**

```bash
git add server/modules-station/station/src/test/
git commit -m "test(station): add unit tests for area model classes"
```

---

## Phase 2: Refactor WorkStationCache to Use Area Objects

### Task 3: Refactor WorkStationCache base class with Jackson polymorphism

Replace flat fields with area objects. Add `@JsonTypeInfo` for Redis deserialization. Add common lifecycle and recalculate methods.

**Files:**
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/domain/entity/WorkStationCache.java`
- Delete: `server/modules-station/station/src/main/java/org/openwes/station/domain/entity/ArrivedContainerCache.java` (moved to station-api in Task 1)

- [ ] **Step 1: Rewrite WorkStationCache with @JsonTypeInfo**

Key changes:
- Add Jackson polymorphic type info for correct subclass deserialization from Redis:
```java
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "@type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = OutboundWorkStationCache.class, name = "outbound"),
    @JsonSubTypes.Type(value = InboundWorkStationCache.class, name = "inbound"),
    @JsonSubTypes.Type(value = StocktakeWorkStationCache.class, name = "stocktake")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class WorkStationCache {
```

- Remove flat fields: `operateTasks`, `arrivedContainers`, `putWallSlots`, `scannedBarcode`
- Add area objects: `workLocationArea`, `skuArea`, `putWallArea`, `orderArea`, `toolbar`
- Replace `List<WorkStationVO.Tip> tips` with `List<Tip> tips`
- Replace `WorkStationVO.ChooseAreaEnum chooseArea` with `WorkStationVO.ChooseAreaEnum chooseArea` (keep using WorkStationVO.ChooseAreaEnum until Phase 5 deletes it; then switch to standalone ChooseAreaEnum)
- Add `workStationStatus: WorkStationStatusEnum` field
- Add `stationProcessingStatus: WorkStationProcessingStatusEnum` field
- Add `callContainers: List<String>` for inbound mode
- Add `containerTasks: List<ContainerTaskCache>` for inbound mode (inner class from InboundWorkStationCache)

The `online()` method now takes `WorkStationDTO`:
```java
public void online(WorkStationDTO dto, OnlineEvent event) {
    this.workStationMode = dto.getWorkStationMode();
    this.workStationStatus = WorkStationStatusEnum.ONLINE;
    this.hasOrder = event.isHasOrder();
    this.workStationConfig = dto.getWorkStationConfig();
    this.workLocationArea = buildWorkLocationArea(dto.getWorkLocations());
    if (workStationMode == WorkStationModeEnum.PICKING) {
        this.putWallArea = buildPutWallArea(dto);
    }
    this.skuArea = new SkuArea();
    this.toolbar = new Toolbar();
    this.tips = new ArrayList<>();
    this.orderArea = new OrderArea();
}
```

`recalculateChooseArea()`, `recalculateToolbar()`, `recalculateProcessingStatus()` — protected methods, no-op in base (subclasses override).

Remove old methods: `addArrivedContainers()`, `clearArrivedContainers()`, `addOperateTasks()`, `clearOperateTasks()`, `getUndoContainers()`, `getProcessingContainers()`, `removeProceedContainers()`, `setUndoContainersProcessing()`, `getPutWallSlot()`, `getFirstOperationTaskVO()`, `getFirstProcessingTask()`, `processTasks()`.

Keep: `addTip()`, `closeTip()` (updated to use new `Tip` class), `updateConfiguration()`.

Add: `scanBarcode(String barcode)` now sets `skuArea.setScanCode(barcode)`.

- [ ] **Step 2: Delete old ArrivedContainerCache from domain/entity**

Delete `server/modules-station/station/src/main/java/org/openwes/station/domain/entity/ArrivedContainerCache.java`. All imports updated to `org.openwes.station.api.model.ArrivedContainerCache`.

- [ ] **Step 3: Commit (expect compilation errors in handlers/view — resolved in later tasks)**

```bash
git commit -m "refactor(station): restructure WorkStationCache with area objects and Jackson polymorphism"
```

---

### Task 4: Refactor subclasses — remove fields, keep behavior

**Files:**
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/domain/entity/OutboundWorkStationCache.java`
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/domain/entity/InboundWorkStationCache.java`
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/domain/entity/StocktakeWorkStationCache.java`

- [ ] **Step 1: Refactor OutboundWorkStationCache**

Remove fields: `inputPutWallSlot`, `activePutWallCode` (now in PutWallArea).

Update methods to operate on area objects:
- `input(input)` → `getPutWallArea().input(input)`
- `clearInput()` → `getPutWallArea().clearInput(); setChooseArea(null);`
- `operate()` → `getSkuArea().removeCompletedTasks();` + resetActivePutWall via `getPutWallArea().resetActivePutWall(processingSlotCodes)`; `recalculateChooseArea()`
- `resetActivePutWall(skuCode)` → compute processingSlotCodes from `getSkuArea().getProcessingTasks()` then `getPutWallArea().resetActivePutWall(codes)`
- `getProcessingOperationTasks()` → `getSkuArea().getProcessingTasks()`
- `reportAbnormal(handleTasks)` → convert to `Map<Long,Integer>`, call `getSkuArea().reportAbnormal(map)`
- `queryTasksAndReturnRemovedContainers(TaskService)` → use `getWorkLocationArea()` methods
- `getFirstOperationTaskDTO()` → `getSkuArea().getFirstTask()`

Override `recalculateChooseArea()` and `recalculateToolbar()` (logic from OutboundBaseAreaHandler).

- [ ] **Step 2: Refactor InboundWorkStationCache**

Remove fields: `callContainers`, `containerTasks` (now on base).
Update `saveCallContainers()`, `completeTasks()` to use base class fields.
Override `recalculateProcessingStatus()`.

- [ ] **Step 3: Refactor StocktakeWorkStationCache**

No field changes. Update methods to use `getWorkLocationArea()` and `getSkuArea()`.
Override `recalculateChooseArea()` and `recalculateProcessingStatus()`.

- [ ] **Step 4: Commit**

```bash
git commit -m "refactor(station): refactor cache subclasses to use area objects, no extra state"
```

---

## Phase 3: Simplify Persistence Layer

### Task 5: Replace PO layer with direct Redis serialization

**Files:**
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/domain/entity/WorkStationCache.java` (add @RedisHash)
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/infrastructure/repository/impl/WorkStationCacheRepositoryImpl.java`
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/infrastructure/persistence/mapper/WorkStationCachePORepository.java`
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/domain/repository/WorkStationCacheRepository.java`
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/domain/service/WorkStationService.java`
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/domain/service/impl/WorkStationServiceImpl.java`
- Delete: all 4 PO files under `infrastructure/persistence/po/`
- Delete: both Transfer files under `domain/transfer/`

- [ ] **Step 1: Add @RedisHash to WorkStationCache, remove generics from repository chain**

WorkStationCache gets `@RedisHash("WorkStation")` and `@Id` on `id` field.

`WorkStationCacheRepository` — remove `<T extends WorkStationCache>`, use `WorkStationCache` directly.
`WorkStationCachePORepository` — rename to `WorkStationRedisRepository`, change to `CrudRepository<WorkStationCache, Long>`.
`WorkStationCacheRepositoryImpl` — remove generics, remove Transfer usage, delegate directly.

- [ ] **Step 2: Remove generics from WorkStationService**

```java
public interface WorkStationService {
    WorkStationCache initWorkStation(Long workStationId);
    WorkStationCache initWorkStation(WorkStationDTO workStationDTO);
    WorkStationCache getWorkStation(Long workStationId);
    WorkStationCache getOrThrow(Long workStationId);
}
```

`WorkStationServiceImpl` — remove `<T>`, remove `validatePicking()` (moved to PutWallArea). Factory still creates subclasses but returns `WorkStationCache`.

- [ ] **Step 3: Delete PO and Transfer classes**

Delete: `WorkStationCachePO.java`, `InboundWorkStationCachePO.java`, `OutboundWorkStationCachePO.java`, `StocktakeWorkStationCachePO.java`, `WorkStationCacheTransfer.java`, `ArriveContainerCacheTransfer.java`.

- [ ] **Step 4: Add startup Redis cache clear**

Add a `@PostConstruct` or `ApplicationRunner` bean in station-server that clears all `WorkStation:*` keys on startup. This ensures incompatible cached data from before the migration is wiped. Stations simply re-initialize when operators go online.

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class StationCacheMigration implements ApplicationRunner {
    private final StringRedisTemplate redisTemplate;
    
    @Override
    public void run(ApplicationArguments args) {
        Set<String> keys = redisTemplate.keys("WorkStation:*");
        if (keys != null && !keys.isEmpty()) {
            log.info("Clearing {} stale WorkStation cache entries for schema migration", keys.size());
            redisTemplate.delete(keys);
        }
    }
}
```

- [ ] **Step 5: Verify compilation**

Run: `cd /d/open-wes/server && ./gradlew :modules-station:station:compileJava`
(Expect errors in handlers — resolved in Phase 4)

- [ ] **Step 6: Commit**

```bash
git commit -m "refactor(station): remove PO/Transfer layers, direct Redis serialization with cache migration"
```

---

## Phase 4: Update Business Handlers

Split into sub-tasks per handler group for manageable changes with intermediate compilation checks.

### Task 6a: Update common handlers

**Files:** All files under `station/src/main/java/org/openwes/station/application/business/handler/common/`

- [ ] **Step 1: Remove generic type parameters from all common handlers**

All handlers currently use `<T extends WorkStationCache>`. Replace with `WorkStationCache`.

Update: `OnlineHandler`, `OfflineHandler`, `ContainerArrivedHandler`, `ContainerLeaveHandler`, `ScanBarcodeHandler`, `OperationTaskRefreshHandler`, `ChooseAreaHandler`, `ChoosePutWallHandler`, `PauseHandler`, `ResumeHandler`, `CallRobotHandler`, `TapPtlHandler`, `CloseTipHandler`, `CustomApiHandler`, `EmptyContainerHandler`.

Key handler changes:
- `OnlineHandler`: call `remoteWorkStationService.queryWorkStation()` to get DTO, pass to `workStation.online(dto, event)`
- `ContainerArrivedHandler`: replace `addArrivedContainers()` with `getWorkLocationArea().placeContainer()`. Remove `ArriveContainerCacheTransfer` dependency. Create `ArrivedContainerCache` directly from event.
- `ContainerLeaveHandler`: use `getWorkLocationArea().removeContainer()`
- `ScanBarcodeHandler`: `scanBarcode()` now sets `skuArea.scanCode`
- `ChooseAreaHandler`: keep using `WorkStationVO.ChooseAreaEnum` for now (replaced in Phase 5)

- [ ] **Step 2: Verify common handlers compile**

Run: `cd /d/open-wes/server && ./gradlew :modules-station:station:compileJava 2>&1 | head -50`

- [ ] **Step 3: Commit**

```bash
git commit -m "refactor(station): update common handlers to use area objects"
```

### Task 6b: Update outbound handlers

**Files:** `handler/outbound/` directory

- [ ] **Step 1: Update outbound handlers**

- `TapPutWallSlotHandler`: cast to `OutboundWorkStationCache` for `operate()`. Use `getSkuArea().getProcessingTasks()`. Use `getPutWallArea().validatePicking()`.
- `InputHandler`: use `getPutWallArea().input()` directly (no cast needed since putWallArea is on base).
- `ReportAbnormalHandler`: cast to `OutboundWorkStationCache` for `reportAbnormal()`.
- `ReportAbnormalTipHandler`: update Tip references.
- `SplitTasksHandler`, `UnbindHandler`: update accordingly.
- `OutboundPtlHelper`: update references.

- [ ] **Step 2: Verify compilation**

- [ ] **Step 3: Commit**

```bash
git commit -m "refactor(station): update outbound handlers to use area objects"
```

### Task 6c: Update inbound and stocktake handlers

**Files:** `handler/inbound/`, `handler/stocktake/` directories

- [ ] **Step 1: Update inbound handler**

- `CallContainerHandler`: cast to `InboundWorkStationCache` for `saveCallContainers()`.

- [ ] **Step 2: Update stocktake handler**

- `StocktakeSubmitHandler`: update task references to use `getSkuArea()`.

- [ ] **Step 3: Commit**

```bash
git commit -m "refactor(station): update inbound and stocktake handlers"
```

### Task 6d: Update extension handlers

**Files:** `handler/common/extension/` directory tree

- [ ] **Step 1: Update all extensions**

- `OutboundOperationTaskRefreshHandlerExtension`: use `getWorkLocationArea()` and `getSkuArea()`. Remove `WorkStationCacheRepository<OutboundWorkStationCache>` generic.
- `OutboundScanBarcodeHandlerExtension`: use `getSkuArea()` for processTasks. Remove generic from `WorkStationCacheRepository`.
- `InboundContainerLeaveExtension`: cast to `InboundWorkStationCache`. Remove generic.
- `InboundOfflineHandlerExtension`: update references.
- `StocktakeOperationTaskRefreshHandlerExtension`, `StocktakeScanBarcodeHandlerExtension`, `StaketakeOfflineHandlerExtension`: update similarly.
- `EmptyContainerOutboundRefreshHandlerExtension`: update references.
- `ExtensionFactory`: remove generics if needed.

- [ ] **Step 2: Update HandlerExecutor and BusinessHandlerFactory**

Remove any generic `<T>` type parameters.

- [ ] **Step 3: Verify full handler compilation**

Run: `cd /d/open-wes/server && ./gradlew :modules-station:station:compileJava`
Expected: BUILD SUCCESSFUL (minus view layer errors, resolved in Phase 5)

- [ ] **Step 4: Commit**

```bash
git commit -m "refactor(station): update extension handlers and executor"
```

---

## Phase 5: Remove View Layer and Update Controller

### Task 7: Delete view layer, VO, DTOs, update controller

**Files:**
- Modify: `server/modules-station/station/src/main/java/org/openwes/station/controller/StationApiController.java`
- Create: `server/modules-station/station-api/src/main/java/org/openwes/station/api/constants/ChooseAreaEnum.java`
- Delete: entire `controller/view/` directory (17 files)
- Delete: `station-api/src/main/java/org/openwes/station/api/vo/WorkStationVO.java`
- Delete: `station-api/src/main/java/org/openwes/station/api/vo/WorkLocationExtend.java`
- Delete: `station-api/src/main/java/org/openwes/station/api/dto/WorkStationCacheDTO.java`
- Delete: `station-api/src/main/java/org/openwes/station/api/dto/InboundWorkStationCacheDTO.java`
- Delete: `station-api/src/main/java/org/openwes/station/api/dto/OutboundWorkStationCacheDTO.java`
- Delete: `station-api/src/main/java/org/openwes/station/api/dto/StocktakeWorkStationCacheDTO.java`
- Delete: `station-api/src/main/java/org/openwes/station/api/dto/ArrivedContainerCacheDTO.java`

- [ ] **Step 1: Create standalone ChooseAreaEnum**

```java
package org.openwes.station.api.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChooseAreaEnum {
    SKU_AREA("skuArea"),
    CONTAINER_AREA("containerArea"),
    PUT_WALL_AREA("putWallArea"),
    SCAN_AREA("scanArea"),
    ORDER_AREA("orderArea"),
    TIPS("tips");
    private final String value;
}
```

- [ ] **Step 2: Update all references from WorkStationVO.ChooseAreaEnum to ChooseAreaEnum**

Replace `WorkStationVO.ChooseAreaEnum` → `ChooseAreaEnum` across all files.
Replace `WorkStationVO.Tip` → `Tip`, `WorkStationVO.Tip.TipTypeEnum` → `Tip.TipTypeEnum`, `WorkStationVO.Tip.TipShowTypeEnum` → `Tip.TipShowTypeEnum`, `WorkStationVO.Toolbar` → `Toolbar`.

- [ ] **Step 3: Update StationApiController**

```java
@GetMapping
public WorkStationCache getView() {
    Long workStationId = HttpStationContext.getWorkStationId();
    if (workStationId == null) {
        throw WmsException.throwWmsException(StationErrorDescEnum.STATION_ID_IS_NOT_CONFIGURED);
    }
    return workStationService.getOrThrow(workStationId);
}
```

Remove `ViewHelper` dependency. Replace `WorkStationCacheRepository<?>` with `WorkStationCacheRepository`.

- [ ] **Step 4: Update WebSocket controller**

Check `StationWebSocketController.java` — update to use `workStationService.getOrThrow()` instead of `ViewHelper.buildView()`.

- [ ] **Step 5: Delete all view layer files (17 files)**

- [ ] **Step 6: Delete VO and DTO classes (7 files)**

- [ ] **Step 7: Verify full backend compilation**

Run: `cd /d/open-wes/server && ./gradlew :modules-station:station:compileJava :modules-station:station-api:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git commit -m "refactor(station): remove view layer, VO, DTOs - cache is the API response"
```

---

## Phase 6: Frontend Changes

### Task 8: Update TypeScript types and component references

Frontend scope (from grep analysis):
- `pickingViews` → `operationViews`: **12 occurrences across 8 files**
- `scanCode` path change: **31 occurrences across 12 files** (top-level `scanCode` → `skuArea.scanCode`)
- `currentStocktakeOrder` → `currentOrder`: **2 occurrences across 2 files**
- `WorkStationView<` generic removal: **26 occurrences across 15 files**

**Files:**
- Modify: `client/src/pages/wms/station/event-loop/types.ts`
- Modify: `client/src/pages/wms/station/instances/outbound/operations/pickingHandler.tsx`
- Modify: `client/src/pages/wms/station/instances/outbound/operations/components/OutboundSkuInfo.tsx`
- Modify: `client/src/pages/wms/station/instances/outbound/custom-actions/SplitContainer/SplitContent.tsx`
- Modify: `client/src/pages/wms/station/instances/stocktake/operations/StocktakeHandler.tsx`
- Modify: `client/src/pages/wms/station/instances/stocktake/operations/OrderHandler.tsx`
- Modify: `client/src/pages/wms/station/state/WorkStationStore.ts`
- Modify: `client/src/pages/wms/station/state/hooks/useWorkStation.ts`
- Modify: `client/src/pages/wms/station/widgets/common/ScanInput/index.tsx`
- Modify: `client/src/pages/wms/station/widgets/common/Scan/index.tsx`
- Modify: `client/src/pages/wms/station/instances/outbound/operations/components/ScanContainer.tsx`
- Modify: `client/src/pages/wms/station/instances/stocktake/operations/components/Operation.tsx`
- Modify: `client/src/pages/wms/station/tab-actions/action-configs/existTask.tsx`
- Modify: All mock-events.ts files
- Modify: All files using `WorkStationView<any>` generic

- [ ] **Step 1: Update types.ts**

```typescript
export interface SkuArea {
    scanCode?: string                    // moved from top-level
    operationViews: PickingViewItem[]    // renamed from pickingViews
    withoutOrderSkuInfos?: any[]
}

export interface OrderArea {
    currentOrder: StocktakeOrder         // renamed from currentStocktakeOrder
}

export interface WorkStationView {       // removed <T>
    // remove scanCode from top-level (now in skuArea)
    // keep all other fields
}
```

- [ ] **Step 2: Replace pickingViews → operationViews across 8 files**

- [ ] **Step 3: Replace scanCode path across 12 files**

`workStationEvent?.scanCode` → `workStationEvent?.skuArea?.scanCode`

- [ ] **Step 4: Replace currentStocktakeOrder → currentOrder across 2 files**

- [ ] **Step 5: Remove generic `<T>` from WorkStationView across 15 files**

`WorkStationView<any>` → `WorkStationView`
`WorkStationView<T>` → `WorkStationView`

- [ ] **Step 6: Update mock-events.ts files to match new structure**

- [ ] **Step 7: Verify frontend compiles**

Run: `cd /d/open-wes/client && npx tsc --noEmit` or `npm run build`

- [ ] **Step 8: Commit**

```bash
git commit -m "refactor(station-frontend): update types for unified cache model"
```

---

## Phase 7: Cleanup and Final Verification

### Task 9: Remove unused imports, dead code, empty directories

- [ ] **Step 1: Clean up imports across all modified files**

Run full compilation and fix remaining import errors.

- [ ] **Step 2: Delete empty directories**

- `station/src/main/java/org/openwes/station/controller/view/`
- `station/src/main/java/org/openwes/station/infrastructure/persistence/po/`
- `station/src/main/java/org/openwes/station/domain/transfer/`
- `station/src/main/java/org/openwes/station/domain/entity/` (if empty after ArrivedContainerCache move — likely not empty since subclasses remain here)

- [ ] **Step 3: Full build verification**

Run: `cd /d/open-wes/server && ./gradlew build`
Run: `cd /d/open-wes/client && npm run build`
Expected: BOTH PASS

- [ ] **Step 4: Run unit tests**

Run: `cd /d/open-wes/server && ./gradlew :modules-station:station:test`
Expected: ALL PASS

- [ ] **Step 5: Final commit**

```bash
git commit -m "chore(station): cleanup dead code and empty directories"
```

---

## Verification

1. **Backend compilation**: `cd /d/open-wes/server && ./gradlew build` — must pass
2. **Frontend compilation**: `cd /d/open-wes/client && npm run build` — must pass
3. **Unit tests**: `cd /d/open-wes/server && ./gradlew :modules-station:station:test` — must pass
4. **Manual test flow**:
   - Start station-server and wes-server
   - Verify startup log shows "Clearing N stale WorkStation cache entries" (migration)
   - Open station UI, go online in PICKING mode
   - Verify `GET /api` returns the new cache structure with `@type` field and area objects
   - Verify container arrived → shows on workLocationArea
   - Verify scan barcode → updates skuArea with operationViews
   - Verify tap put wall slot → completes picking
   - Verify chooseArea transitions correctly between areas
5. **Redis verification**: Check Redis that `WorkStation:<id>` hash contains `@type` field and area objects correctly serialized
6. **Inbound/Stocktake flows**: Test put-away and stocktake modes similarly
7. **Deserialization test**: Restart station-server while a station is online, verify it deserializes the correct subclass (check `@type` in Redis)

## Deployment Notes

- **Backend and frontend must deploy atomically** — field renames (`pickingViews` → `operationViews`, `scanCode` path) will break if only one side is deployed.
- **Redis cache is cleared on startup** — the `StationCacheMigration` bean handles this. No manual intervention needed. Operators see their station as offline and simply go online again.
- The `StationCacheMigration` bean should be removed in a subsequent release after all environments have been upgraded.

## Accepted Risks

- **SkuArea uses wes-api DTOs directly** (`SkuMainDataDTO`, `OperationTaskDTO`). These are already used throughout the system. Creating station-owned copies would add complexity without clear benefit now. If DTO evolution becomes a problem, extract slim station-specific data classes later.
