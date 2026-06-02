# RCS Simulator: Robot Types and Behavior Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Introduce proper robot type classification and strategy-based behavior so the simulator can model KIVA (pod-lifting AGV) and BIN_ROBOT (fork+basket bin robot) workflows differently.

**Architecture:** Strategy pattern — `RobotBehavior` interface with `KivaBehavior` and `BinRobotBehavior` implementations. VirtualRobot holds a reference to its behavior and the behavior's `executeTick()` drives the task forward. TaskExecutionService delegates to behavior instead of switching on task status. Layout JSON gets a new `pods` section.

**Tech Stack:** Java 17, Spring Boot 3.2.2, JUnit 5 (no Spring context in tests), Jackson, Lombok

---

### Task 1: RobotType enum + RobotBehavior interface

**Files:**
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/RobotType.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/RobotBehavior.java`

- [ ] **Step 1.1: Create RobotType enum**

```java
package org.openwes.simulator.domain;

public enum RobotType {
    KIVA("AGV"),
    BIN_ROBOT("BinRobot");

    private final String displayName;

    RobotType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

- [ ] **Step 1.2: Create RobotBehavior interface**

```java
package org.openwes.simulator.domain;

import java.util.List;

public interface RobotBehavior {
    /** Advance task execution for one tick. Returns true if task is complete. */
    boolean executeTick(VirtualRobot robot, SimulatedTask task, double deltaSeconds);

    /** Check if robot can accept this task. */
    boolean canAcceptTask(SimulatedTask task);

    /** Initialize task state (called when task is first assigned to this robot). */
    void initTask(VirtualRobot robot, SimulatedTask task);

    /** Handle manual process-complete trigger. */
    void processComplete(VirtualRobot robot, SimulatedTask task);
}
```

- [ ] **Step 1.3: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/RobotType.java
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/RobotBehavior.java
git commit -m "feat(simulator): add RobotType enum and RobotBehavior interface"
```

---

### Task 2: KivaBehavior implementation

**Files:**
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/KivaBehavior.java`

- [ ] **Step 2.1: Create KivaBehavior**

KivaBehavior manages its own internal state machine and task-scoped state via a `Map<String, KivaState>` keyed by task code:

```java
package org.openwes.simulator.domain;

import lombok.RequiredArgsConstructor;
import org.openwes.simulator.config.SimulatorProperties;
import org.openwes.simulator.service.PathService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class KivaBehavior implements RobotBehavior {

    public enum KivaState {
        INIT, MOVING_TO_POD, LIFTING_POD, MOVING_TO_WS, DROPPING_POD, WAITING_PROCESS
    }

    private final SimulatorProperties properties;
    private final PathService pathService;
    private final Map<String, KivaState> taskStates = new ConcurrentHashMap<>();
    private final Map<String, List<Position>> taskPaths = new ConcurrentHashMap<>();
    private final Map<String, Long> stateEnteredAt = new ConcurrentHashMap<>();
    private final Map<String, String> targetPodId = new ConcurrentHashMap<>();

    @Override
    public boolean canAcceptTask(SimulatedTask task) {
        return true; // KIVA can handle any pod-moving task
    }

    @Override
    public void initTask(VirtualRobot robot, SimulatedTask task) {
        taskStates.put(task.getTaskCode(), KivaState.INIT);
        stateEnteredAt.put(task.getTaskCode(), System.currentTimeMillis());

        // The first pickup location is the pod location
        String podLocationCode = task.getPickupLocationCodes().iterator().next();
        targetPodId.put(task.getTaskCode(), podLocationCode);

        robot.setStatus(RobotStatus.MOVING);
    }

    @Override
    public boolean executeTick(VirtualRobot robot, SimulatedTask task, double deltaSeconds) {
        KivaState state = taskStates.get(task.getTaskCode());
        if (state == null) return false;

        long now = System.currentTimeMillis();

        switch (state) {
            case INIT:
                String podCode = targetPodId.get(task.getTaskCode());
                Position podPos = task.getPickupPosition();
                taskPaths.put(task.getTaskCode(), pathService.calculatePath(robot.getCurrentPosition(), podPos));
                taskStates.put(task.getTaskCode(), KivaState.MOVING_TO_POD);
                break;

            case MOVING_TO_POD:
                advanceTowards(robot, task, task.getPickupPosition(), deltaSeconds);
                if (pathService.hasReached(robot.getCurrentPosition(), task.getPickupPosition())) {
                    robot.setCurrentPosition(task.getPickupPosition().copy());
                    taskStates.put(task.getTaskCode(), KivaState.LIFTING_POD);
                    stateEnteredAt.put(task.getTaskCode(), now);
                }
                break;

            case LIFTING_POD:
                if (isDelayElapsed(task.getTaskCode(), now, properties.getKiva().getLiftDelayMs())) {
                    robot.setCarryingPodId(targetPodId.get(task.getTaskCode()));
                    taskStates.put(task.getTaskCode(), KivaState.MOVING_TO_WS);
                    taskPaths.put(task.getTaskCode(), pathService.calculatePath(
                            robot.getCurrentPosition(), task.getDestinationPosition()));
                    stateEnteredAt.put(task.getTaskCode(), now);
                }
                break;

            case MOVING_TO_WS:
                robot.setStatus(RobotStatus.MOVING);
                advanceTowards(robot, task, task.getDestinationPosition(), deltaSeconds);
                if (pathService.hasReached(robot.getCurrentPosition(), task.getDestinationPosition())) {
                    robot.setCurrentPosition(task.getDestinationPosition().copy());
                    taskStates.put(task.getTaskCode(), KivaState.DROPPING_POD);
                    stateEnteredAt.put(task.getTaskCode(), now);
                }
                break;

            case DROPPING_POD:
                robot.setStatus(RobotStatus.UNLOADING);
                if (isDelayElapsed(task.getTaskCode(), now, properties.getKiva().getLiftDelayMs())) {
                    robot.setCarryingPodId(null); // pod stays at WS
                    taskStates.put(task.getTaskCode(), KivaState.WAITING_PROCESS);
                    stateEnteredAt.put(task.getTaskCode(), now);
                }
                break;

            case WAITING_PROCESS:
                robot.setStatus(RobotStatus.WAITING);
                long entered = stateEnteredAt.getOrDefault(task.getTaskCode(), now);
                if (properties.getKiva().getProcessDelayMs() > 0
                        && (now - entered) >= properties.getKiva().getProcessDelayMs()) {
                    completeTask(robot, task);
                    return true;
                }
                break;
        }

        return false;
    }

    @Override
    public void processComplete(VirtualRobot robot, SimulatedTask task) {
        KivaState state = taskStates.get(task.getTaskCode());
        if (state == KivaState.WAITING_PROCESS) {
            completeTask(robot, task);
        }
    }

    private void completeTask(VirtualRobot robot, SimulatedTask task) {
        task.setStatus(TaskStatus.COMPLETED);
        robot.setStatus(RobotStatus.IDLE);
        cleanup(task.getTaskCode());
    }

    private void advanceTowards(VirtualRobot robot, SimulatedTask task, Position target, double deltaSeconds) {
        List<Position> path = taskPaths.get(task.getTaskCode());
        if (path == null) return;
        robot.setCurrentPosition(path);
        double distance = robot.getSpeed() * deltaSeconds;
        Position newPos = pathService.moveAlongPath(robot.getCurrentPosition(), path, distance);
        robot.setCurrentPosition(newPos);
    }

    private boolean isDelayElapsed(String taskCode, long now, int delayMs) {
        Long entered = stateEnteredAt.get(taskCode);
        return entered != null && (now - entered) >= delayMs;
    }

    public void cleanup(String taskCode) {
        taskStates.remove(taskCode);
        taskPaths.remove(taskCode);
        stateEnteredAt.remove(taskCode);
        targetPodId.remove(taskCode);
    }
}
```

- [ ] **Step 2.2: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/KivaBehavior.java
git commit -m "feat(simulator): implement KivaBehavior for pod-lifting AGV"
```

---

### Task 3: BinRobotBehavior implementation

**Files:**
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/BinRobotBehavior.java`

- [ ] **Step 3.1: Create BinRobotBehavior**

```java
package org.openwes.simulator.domain;

import lombok.RequiredArgsConstructor;
import org.openwes.simulator.config.SimulatorProperties;
import org.openwes.simulator.service.PathService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class BinRobotBehavior implements RobotBehavior {

    public enum BinRobotState {
        INIT, MOVING_TO_LOCATION, PICKING_BIN, STORING_IN_BASKET,
        MOVING_TO_NEXT_LOCATION, MOVING_TO_WS, UNLOADING_BASKET
    }

    private final SimulatorProperties properties;
    private final PathService pathService;
    private final Map<String, BinRobotState> taskStates = new ConcurrentHashMap<>();
    private final Map<String, List<Position>> taskPaths = new ConcurrentHashMap<>();
    private final Map<String, Long> stateEnteredAt = new ConcurrentHashMap<>();
    private final Map<String, Iterator<Position>> positionIterator = new ConcurrentHashMap<>();

    @Override
    public boolean canAcceptTask(SimulatedTask task) {
        return task.getPickupLocationCodes() != null && !task.getPickupLocationCodes().isEmpty();
    }

    @Override
    public void initTask(VirtualRobot robot, SimulatedTask task) {
        taskStates.put(task.getTaskCode(), BinRobotState.INIT);
        stateEnteredAt.put(task.getTaskCode(), System.currentTimeMillis());

        // Initialize basket
        if (robot.getBasketItems() == null) {
            robot.setBasketItems(new ArrayList<>());
        }
        robot.getBasketItems().clear();

        // Position iterator over pre-resolved positions
        positionIterator.put(task.getTaskCode(), task.getPickupPositionIterator());
    }

    @Override
    public boolean executeTick(VirtualRobot robot, SimulatedTask task, double deltaSeconds) {
        BinRobotState state = taskStates.get(task.getTaskCode());
        if (state == null) return false;

        long now = System.currentTimeMillis();

        switch (state) {
            case INIT:
                Iterator<Position> firstIt = positionIterator.get(task.getTaskCode());
                if (firstIt != null && firstIt.hasNext()) {
                    Position firstPos = firstIt.next();
                    task.setPickupPosition(firstPos);
                    taskPaths.put(task.getTaskCode(), pathService.calculatePath(
                            robot.getCurrentPosition(), firstPos));
                    taskStates.put(task.getTaskCode(), BinRobotState.MOVING_TO_LOCATION);
                    stateEnteredAt.put(task.getTaskCode(), now);
                }
                break;

            case MOVING_TO_LOCATION:
            case MOVING_TO_NEXT_LOCATION:
                robot.setStatus(RobotStatus.MOVING);
                advanceTowards(robot, task, task.getPickupPosition(), deltaSeconds);
                if (pathService.hasReached(robot.getCurrentPosition(), task.getPickupPosition())) {
                    robot.setCurrentPosition(task.getPickupPosition().copy());
                    taskStates.put(task.getTaskCode(), BinRobotState.PICKING_BIN);
                    stateEnteredAt.put(task.getTaskCode(), now);
                }
                break;

            case PICKING_BIN:
                robot.setStatus(RobotStatus.LOADING);
                if (isDelayElapsed(task.getTaskCode(), now, properties.getBinRobot().getPickDelayMs())) {
                    robot.getBasketItems().add("BIN-" + task.getTaskCode() + "-" + robot.getBasketItems().size());
                    taskStates.put(task.getTaskCode(), BinRobotState.STORING_IN_BASKET);
                    stateEnteredAt.put(task.getTaskCode(), now);
                }
                break;

            case STORING_IN_BASKET:
                robot.setStatus(RobotStatus.LOADING);
                if (isDelayElapsed(task.getTaskCode(), now, properties.getBinRobot().getStoreDelayMs())) {
                    Iterator<Position> it = positionIterator.get(task.getTaskCode());
                    if (it != null && it.hasNext()) {
                        // More bins to pick
                        Position nextPos = it.next();
                        task.setPickupPosition(nextPos);
                        taskPaths.put(task.getTaskCode(), pathService.calculatePath(
                                robot.getCurrentPosition(), nextPos));
                        taskStates.put(task.getTaskCode(), BinRobotState.MOVING_TO_NEXT_LOCATION);
                    } else {
                        // All bins picked, go to workstation
                        task.setPickupPosition(task.getDestinationPosition()); // reuse pickupPosition for WS target
                        taskPaths.put(task.getTaskCode(), pathService.calculatePath(
                                robot.getCurrentPosition(), task.getDestinationPosition()));
                        taskStates.put(task.getTaskCode(), BinRobotState.MOVING_TO_WS);
                    }
                    stateEnteredAt.put(task.getTaskCode(), now);
                }
                break;

            case MOVING_TO_WS:
                robot.setStatus(RobotStatus.MOVING);
                advanceTowards(robot, task, task.getDestinationPosition(), deltaSeconds);
                if (pathService.hasReached(robot.getCurrentPosition(), task.getDestinationPosition())) {
                    robot.setCurrentPosition(task.getDestinationPosition().copy());
                    taskStates.put(task.getTaskCode(), BinRobotState.UNLOADING_BASKET);
                    stateEnteredAt.put(task.getTaskCode(), now);
                }
                break;

            case UNLOADING_BASKET:
                robot.setStatus(RobotStatus.UNLOADING);
                if (isDelayElapsed(task.getTaskCode(), now, properties.getBinRobot().getUnloadDelayMs())) {
                    robot.getBasketItems().clear();
                    task.setStatus(TaskStatus.COMPLETED);
                    robot.setStatus(RobotStatus.IDLE);
                    cleanup(task.getTaskCode());
                    return true;
                }
                break;
        }

        return false;
    }

    @Override
    public void processComplete(VirtualRobot robot, SimulatedTask task) {
        // Bin robot does not use WAITING_PROCESS, so this is a no-op
    }

    private void advanceTowards(VirtualRobot robot, SimulatedTask task, Position target, double deltaSeconds) {
        List<Position> path = taskPaths.get(task.getTaskCode());
        if (path == null) return;
        double distance = robot.getSpeed() * deltaSeconds;
        Position newPos = pathService.moveAlongPath(robot.getCurrentPosition(), path, distance);
        robot.setCurrentPosition(newPos);
    }

    private boolean isDelayElapsed(String taskCode, long now, int delayMs) {
        Long entered = stateEnteredAt.get(taskCode);
        return entered != null && (now - entered) >= delayMs;
    }

    public void cleanup(String taskCode) {
        taskStates.remove(taskCode);
        taskPaths.remove(taskCode);
        stateEnteredAt.remove(taskCode);
        locationIterator.remove(taskCode);
    }
}
```

- [ ] **Step 3.2: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/BinRobotBehavior.java
git commit -m "feat(simulator): implement BinRobotBehavior for fork+basket bin robot"
```

---

### Task 4: Update VirtualRobot — add RobotType, basketItems, carryingPodId, behavior

**Files:**
- Modify: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/VirtualRobot.java`

- [ ] **Step 4.1: Update VirtualRobot fields**

Change `private String robotType` to `private RobotType robotType`. Add basket and pod fields. Add behavior reference.

```java
package org.openwes.simulator.domain;

import lombok.Data;

import java.util.List;

@Data
public class VirtualRobot {
    private String robotCode;
    private RobotType robotType;                    // was: String
    private RobotStatus status = RobotStatus.IDLE;
    private Position currentPosition;
    private String currentLocationCode;
    private String assignedTaskCode;
    private String carriedContainerCode;
    private double speed;
    private double batteryLevel = 1.0;

    // KIVA-specific: currently carried pod id
    private String carryingPodId;

    // BIN_ROBOT-specific: bins in basket (max basketSlots)
    private List<String> basketItems;

    // Strategy reference
    private RobotBehavior behavior;

    public boolean isIdle() {
        return status == RobotStatus.IDLE;
    }

    public boolean isInError() {
        return status == RobotStatus.ERROR;
    }
}
```

- [ ] **Step 4.2: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/VirtualRobot.java
git commit -m "refactor(simulator): update VirtualRobot with RobotType enum and strategy fields"
```

---

### Task 5: Update WarehouseLayout — add PodConfig, RobotType enum, basketSlots

**Files:**
- Modify: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/WarehouseLayout.java`

- [ ] **Step 5.1: Add PodConfig inner class and pods list**

```java
// Add after ChargingStation inner class:
@Data
public static class PodConfig {
    private String id;
    private double x;
    private double y;
}

// Add field to WarehouseLayout:
private List<PodConfig> pods;
```

- [ ] **Step 5.2: Change RobotConfig.robotType to RobotType enum and add basketSlots**

```java
@Data
public static class RobotConfig {
    private String robotCode;
    private RobotType robotType;      // was: String
    private double startX;
    private double startY;
    private double speed;
    private int basketSlots;          // new: only used for BIN_ROBOT
}
```

- [ ] **Step 5.3: Add pod location indexing to buildLocationIndex()**

```java
// Add after charging stations block in buildLocationIndex():
if (pods != null) {
    for (PodConfig pod : pods) {
        locationPositions.put(pod.getId(), new Position(pod.getX(), pod.getY(), 0));
    }
}
```

- [ ] **Step 5.4: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/WarehouseLayout.java
git commit -m "feat(simulator): add PodConfig and RobotType enum to WarehouseLayout"
```

---

### Task 6: Update RobotStatus to add WAITING

**Files:**
- Modify: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/RobotStatus.java`

- [ ] **Step 6.1: Add WAITING status**

```java
package org.openwes.simulator.domain;

public enum RobotStatus {
    IDLE,
    MOVING,
    LOADING,
    UNLOADING,
    WAITING,      // new: waiting for human processing (KIVA)
    CHARGING,
    ERROR
}
```

Note: `MOVING_TO_PICKUP` and `MOVING_TO_DESTINATION` are removed — replaced by `MOVING`. The behavior's internal state tracks which phase the movement is in.

- [ ] **Step 6.2: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/RobotStatus.java
git commit -m "refactor(simulator): simplify RobotStatus, add WAITING state"
```

---

### Task 7: Update SimulatedTask — add requiredRobotType and pickupLocationCodes

**Files:**
- Modify: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/SimulatedTask.java`

- [ ] **Step 7.1: Update SimulatedTask fields**

Change `private String startLocation` (single) to `private Collection<String> startLocations`. Add `requiredRobotType` field.

```java
package org.openwes.simulator.domain;

import lombok.Data;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Data
public class SimulatedTask {
    private String taskCode;
    private String taskGroupCode;
    private String containerCode;
    private String containerFace;
    private Collection<String> startLocations;     // was: String startLocation
    private Collection<String> destinations;
    private int priority;
    private int groupPriority;
    private String businessTaskType;
    private String containerTaskType;
    private Long customerTaskId;

    // New: robot type requirement
    private String requiredRobotType;

    private TaskStatus status = TaskStatus.QUEUED;
    private String assignedRobotCode;
    private Position pickupPosition;
    private Position destinationPosition;
    private Instant createdAt = Instant.now();
    private Instant completedAt;

    // Pre-resolved positions for each pickup location
    private transient List<Position> pickupPositions;

    // Convenience method: get pickup location codes as List<String>
    public List<String> getPickupLocationCodes() {
        return startLocations == null ? List.of() : List.copyOf(startLocations);
    }

    // Convenience method: get pickup positions iterator
    public Iterator<Position> getPickupPositionIterator() {
        return pickupPositions == null ? Collections.emptyIterator() : pickupPositions.iterator();
    }

    public boolean isActive() {
        return !status.isTerminal();
    }
}
```

Note: Keep `startLocation` as a backward-compat getter/setter that delegates to the first element of `startLocations`, OR update all callers. Since this is internal to the simulator, updating callers directly is cleaner.

Update `TaskReceiveController.createTasks()` to set `startLocations` from the payload's `startLocation` as a single-element list.

- [ ] **Step 7.2: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/SimulatedTask.java
git commit -m "refactor(simulator): add requiredRobotType and startLocations to SimulatedTask"
```

---

### Task 8: Update SimulatorProperties — add kiva and bin-robot config groups

**Files:**
- Modify: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/config/SimulatorProperties.java`

- [ ] **Step 8.1: Add Kiva and BinRobot config inner classes**

```java
@Data
public static class Kiva {
    private int liftDelayMs = 1500;
    private int processDelayMs = 5000;
}

@Data
public static class BinRobot {
    private int pickDelayMs = 1500;
    private int storeDelayMs = 1000;
    private int unloadDelayMs = 1000;
}
```

- [ ] **Step 8.2: Add fields to SimulatorProperties**

```java
private Kiva kiva = new Kiva();
private BinRobot binRobot = new BinRobot();
```

- [ ] **Step 8.3: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/config/SimulatorProperties.java
git commit -m "feat(simulator): add kiva and bin-robot config properties"
```

---

### Task 9: Refactor RobotFleetService — behavior assembly and type-filtered assignment

**Files:**
- Modify: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/service/RobotFleetService.java`

- [ ] **Step 9.1: Update initializeRobots to assemble behavior and set RobotType enum**

Inject `PathService` and `SimulatorProperties` into RobotFleetService.

```java
package org.openwes.simulator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.simulator.config.SimulatorProperties;
import org.openwes.simulator.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class RobotFleetService {

    private final PathService pathService;
    private final SimulatorProperties properties;
    private final Map<String, VirtualRobot> robots = new ConcurrentHashMap<>();
    private final Map<String, Position> initialPositions = new ConcurrentHashMap<>();

    public void initializeRobots(List<WarehouseLayout.RobotConfig> configs) {
        robots.clear();
        initialPositions.clear();
        for (WarehouseLayout.RobotConfig config : configs) {
            VirtualRobot robot = new VirtualRobot();
            robot.setRobotCode(config.getRobotCode());
            robot.setRobotType(config.getRobotType());
            robot.setCurrentPosition(new Position(config.getStartX(), config.getStartY(), 0));
            robot.setSpeed(config.getSpeed());
            robot.setStatus(RobotStatus.IDLE);

            // Assemble behavior based on robot type
            switch (config.getRobotType()) {
                case KIVA:
                    robot.setBehavior(new KivaBehavior(properties, pathService));
                    robot.setBasketItems(Collections.emptyList());
                    break;
                case BIN_ROBOT:
                    robot.setBehavior(new BinRobotBehavior(properties, pathService));
                    robot.setBasketItems(new ArrayList<>(Math.max(config.getBasketSlots(), 6)));
                    break;
            }

            robots.put(config.getRobotCode(), robot);
            initialPositions.put(config.getRobotCode(), new Position(config.getStartX(), config.getStartY(), 0));
        }
        log.info("Initialized {} virtual robots", robots.size());
    }

    public Optional<VirtualRobot> findNearestIdleRobot(Position target, String requiredRobotType) {
        return robots.values().stream()
                .filter(VirtualRobot::isIdle)
                .filter(r -> requiredRobotType == null
                        || r.getRobotType().name().equals(requiredRobotType))
                .min(Comparator.comparingDouble(r -> r.getCurrentPosition().distanceTo(target)));
    }

    /** Legacy overload — finds any idle robot regardless of type */
    public Optional<VirtualRobot> findNearestIdleRobot(Position target) {
        return findNearestIdleRobot(target, null);
    }

    public void assignTask(VirtualRobot robot, String taskCode, String containerCode) {
        robot.setStatus(RobotStatus.MOVING);
        robot.setAssignedTaskCode(taskCode);
        robot.setCarriedContainerCode(containerCode);
        log.info("Assigned task {} to robot {}", taskCode, robot.getRobotCode());
    }

    public void releaseRobot(VirtualRobot robot) {
        robot.setStatus(RobotStatus.IDLE);
        robot.setAssignedTaskCode(null);
        robot.setCarriedContainerCode(null);
        robot.setCarryingPodId(null);
        if (robot.getBasketItems() != null) {
            robot.getBasketItems().clear();
        }
        log.info("Released robot {}", robot.getRobotCode());
    }

    public void setError(VirtualRobot robot) {
        robot.setStatus(RobotStatus.ERROR);
        log.warn("Robot {} set to ERROR state", robot.getRobotCode());
    }

    public void recoverFromError(VirtualRobot robot) {
        if (robot.isInError()) {
            robot.setStatus(RobotStatus.IDLE);
            robot.setAssignedTaskCode(null);
            robot.setCarriedContainerCode(null);
            robot.setCarryingPodId(null);
            if (robot.getBasketItems() != null) {
                robot.getBasketItems().clear();
            }
            log.info("Robot {} recovered from ERROR", robot.getRobotCode());
        }
    }

    public VirtualRobot getRobot(String robotCode) {
        return robots.get(robotCode);
    }

    public List<VirtualRobot> getAllRobots() {
        return new ArrayList<>(robots.values());
    }

    public void resetAll() {
        for (VirtualRobot robot : robots.values()) {
            robot.setStatus(RobotStatus.IDLE);
            robot.setAssignedTaskCode(null);
            robot.setCarriedContainerCode(null);
            robot.setCarryingPodId(null);
            if (robot.getBasketItems() != null) {
                robot.getBasketItems().clear();
            }
            Position initial = initialPositions.get(robot.getRobotCode());
            if (initial != null) {
                robot.setCurrentPosition(initial.copy());
            }
        }
        log.info("Reset all robots to initial state");
    }
}
```

- [ ] **Step 9.2: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/service/RobotFleetService.java
git commit -m "refactor(simulator): add behavior assembly and type-filtered robot assignment"
```

---

### Task 10: Refactor TaskExecutionService — delegate to behavior strategy

**Files:**
- Modify: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/service/TaskExecutionService.java`

- [ ] **Step 10.1: Rewrite tick() to delegate to behavior.executeTick()**

The key change: instead of switching on task status, call `robot.getBehavior().executeTick(robot, task, deltaSeconds)`. Keep the random failure logic and queue drain.

```java
package org.openwes.simulator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.simulator.config.SimulatorProperties;
import org.openwes.simulator.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
    private final RobotFleetService fleetService;
    private final LayoutService layoutService;
    private final WesCallbackService callbackService;
    private final SimulatorProperties properties;

    private final Map<String, SimulatedTask> allTasks = new ConcurrentHashMap<>();
    private final Queue<SimulatedTask> taskQueue = new ConcurrentLinkedQueue<>();

    public void submitTask(SimulatedTask task) {
        allTasks.put(task.getTaskCode(), task);

        WarehouseLayout layout = layoutService.getCurrentLayout();

        // Resolve pickup locations from layout
        List<String> pickups = task.getPickupLocationCodes();
        List<Position> resolvedPositions = pickups.stream()
                .map(loc -> layout.getPositionForLocation(loc))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        task.setPickupPositions(resolvedPositions);
        if (!resolvedPositions.isEmpty()) {
            task.setPickupPosition(resolvedPositions.get(0));
        } else {
            task.setPickupPosition(new Position(0, 0, 0));
        }

        // Resolve destination
        String dest = task.getDestinations() != null && !task.getDestinations().isEmpty()
                ? task.getDestinations().iterator().next() : null;
        if (dest != null) {
            Position pos = layout.getPositionForLocation(dest);
            task.setDestinationPosition(pos != null ? pos : new Position(0, 0, 0));
        }

        // Find nearest idle robot of matching type
        String requiredType = task.getRequiredRobotType();
        Optional<VirtualRobot> robot = fleetService.findNearestIdleRobot(task.getPickupPosition(), requiredType);
        if (robot.isPresent()) {
            assignRobotToTask(robot.get(), task);
        } else {
            task.setStatus(TaskStatus.QUEUED);
            taskQueue.add(task);
            log.info("Task {} queued — no idle {} robots", task.getTaskCode(), requiredType);
        }

        callbackService.reportTaskStatus(task.getTaskCode(), "PROCESSING", null, task.getContainerCode(), null);
    }

    public void cancelTask(String taskCode) {
        SimulatedTask task = allTasks.get(taskCode);
        if (task == null || task.getStatus().isTerminal()) return;

        task.setStatus(TaskStatus.CANCELED);
        if (task.getAssignedRobotCode() != null) {
            VirtualRobot robot = fleetService.getRobot(task.getAssignedRobotCode());
            if (robot != null) {
                if (robot.getBehavior() instanceof KivaBehavior) {
                    ((KivaBehavior) robot.getBehavior()).cleanup(taskCode);
                } else if (robot.getBehavior() instanceof BinRobotBehavior) {
                    ((BinRobotBehavior) robot.getBehavior()).cleanup(taskCode);
                }
                fleetService.releaseRobot(robot);
            }
        }
        log.info("Task {} canceled", taskCode);
    }

    public void tick() {
        double deltaSeconds = properties.getTickIntervalMs() / 1000.0;
        long now = System.currentTimeMillis();

        List<SimulatedTask> completedTasks = new ArrayList<>();

        for (SimulatedTask task : allTasks.values()) {
            if (task.getStatus().isTerminal()) continue;
            if (task.getStatus() == TaskStatus.QUEUED) continue;

            VirtualRobot robot = fleetService.getRobot(task.getAssignedRobotCode());
            if (robot == null || robot.isInError()) continue;

            // Delegate to behavior strategy
            boolean done = robot.getBehavior().executeTick(robot, task, deltaSeconds);
            if (done) {
                completedTasks.add(task);
            }

            // Random failure check
            if (properties.getFailureRatePercent() > 0 && !task.getStatus().isTerminal()) {
                if (ThreadLocalRandom.current().nextInt(100) < properties.getFailureRatePercent()) {
                    failTask(task, robot);
                }
            }
        }

        // Post-process completed tasks
        for (SimulatedTask task : completedTasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) {
                completeTaskCallbacks(task);
            }
        }

        // Try to assign queued tasks
        drainQueue();
    }

    @Scheduled(fixedDelayString = "${simulator.tick-interval-ms:200}")
    public void scheduledTick() {
        tick();
    }

    public void failTaskForRobot(String robotCode) {
        allTasks.values().stream()
                .filter(t -> robotCode.equals(t.getAssignedRobotCode()) && t.isActive())
                .findFirst()
                .ifPresent(task -> {
                    VirtualRobot robot = fleetService.getRobot(robotCode);
                    if (robot != null) {
                        failTask(task, robot);
                    }
                });
    }

    public List<SimulatedTask> getActiveTasks() {
        return allTasks.values().stream().filter(SimulatedTask::isActive).collect(Collectors.toList());
    }

    public List<SimulatedTask> getAllTasks() {
        return new ArrayList<>(allTasks.values());
    }

    public SimulatedTask getTask(String taskCode) {
        return allTasks.get(taskCode);
    }

    public void reset() {
        allTasks.clear();
        taskQueue.clear();
    }

    private void assignRobotToTask(VirtualRobot robot, SimulatedTask task) {
        task.setStatus(TaskStatus.ASSIGNED);
        task.setAssignedRobotCode(robot.getRobotCode());
        fleetService.assignTask(robot, task.getTaskCode(), task.getContainerCode());
        robot.getBehavior().initTask(robot, task);
        log.info("Assigned robot {} to task {} (type: {})", robot.getRobotCode(), task.getTaskCode(), robot.getRobotType());
    }

    private void completeTaskCallbacks(SimulatedTask task) {
        task.setCompletedAt(Instant.now());
        VirtualRobot robot = fleetService.getRobot(task.getAssignedRobotCode());
        if (robot == null) return;

        fleetService.releaseRobot(robot);

        String destination = task.getDestinations() != null && !task.getDestinations().isEmpty()
                ? task.getDestinations().iterator().next() : "UNKNOWN";

        callbackService.reportContainerArrived(
                task.getContainerCode(), destination,
                robot.getRobotCode(), robot.getRobotType().name(),
                task.getTaskGroupCode() != null ? task.getTaskGroupCode() : task.getTaskCode(),
                destination, null, null);

        callbackService.reportTaskStatus(task.getTaskCode(), "WCS Succeeded",
                robot.getRobotCode(), task.getContainerCode(), destination);

        log.info("Task {} completed by robot {} ({})", task.getTaskCode(), robot.getRobotCode(), robot.getRobotType());
    }

    private void failTask(SimulatedTask task, VirtualRobot robot) {
        task.setStatus(TaskStatus.FAILED);
        task.setCompletedAt(Instant.now());
        fleetService.releaseRobot(robot);

        callbackService.reportTaskStatus(task.getTaskCode(), "WCS Failed",
                robot.getRobotCode(), task.getContainerCode(), null);

        log.warn("Task {} failed on robot {}", task.getTaskCode(), robot.getRobotCode());
    }

    private void drainQueue() {
        Iterator<SimulatedTask> it = taskQueue.iterator();
        while (it.hasNext()) {
            SimulatedTask task = it.next();
            if (task.getStatus() != TaskStatus.QUEUED) {
                it.remove();
                continue;
            }
            String requiredType = task.getRequiredRobotType();
            Optional<VirtualRobot> robot = fleetService.findNearestIdleRobot(task.getPickupPosition(), requiredType);
            if (robot.isPresent()) {
                it.remove();
                assignRobotToTask(robot.get(), task);
            } else {
                break;
            }
        }
    }
}
```

- [ ] **Step 10.2: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/service/TaskExecutionService.java
git commit -m "refactor(simulator): delegate task execution to RobotBehavior strategy"
```

---

### Task 11: Update WebSocketPushService — add basket and pod data

**Files:**
- Modify: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/service/WebSocketPushService.java`

- [ ] **Step 11.1: Extend robotToMap with basketItems, carryingPodId, robotType enum name**

```java
private Map<String, Object> robotToMap(VirtualRobot robot) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("robotCode", robot.getRobotCode());
    map.put("robotType", robot.getRobotType().name());      // was: getRobotType() returning String
    map.put("status", robot.getStatus().name());
    map.put("x", robot.getCurrentPosition().getX());
    map.put("y", robot.getCurrentPosition().getY());
    map.put("rotation", robot.getCurrentPosition().getRotation());
    map.put("carriedContainerCode", robot.getCarriedContainerCode());
    map.put("taskCode", robot.getAssignedTaskCode());
    map.put("batteryLevel", robot.getBatteryLevel());
    map.put("basketItems", robot.getBasketItems());         // new
    map.put("carryingPodId", robot.getCarryingPodId());     // new
    return map;
}
```

- [ ] **Step 11.2: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/service/WebSocketPushService.java
git commit -m "feat(simulator): add basketItems and carryingPodId to WebSocket push"
```

---

### Task 12: Update Controller — add getRobotDetail and processComplete endpoints

**Files:**
- Modify: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/controller/SimulatorManagementController.java`

- [ ] **Step 12.1: Add getRobotDetail endpoint**

```java
@GetMapping("/robots/{robotCode}")
public Map<String, Object> getRobotDetail(@PathVariable String robotCode) {
    VirtualRobot robot = fleetService.getRobot(robotCode);
    if (robot == null) {
        return Map.of("status", "error", "message", "Robot not found: " + robotCode);
    }
    Map<String, Object> detail = new LinkedHashMap<>();
    detail.put("robotCode", robot.getRobotCode());
    detail.put("robotType", robot.getRobotType().name());
    detail.put("status", robot.getStatus().name());
    detail.put("position", Map.of("x", robot.getCurrentPosition().getX(), "y", robot.getCurrentPosition().getY()));
    detail.put("batteryLevel", robot.getBatteryLevel());
    detail.put("basketItems", robot.getBasketItems());
    detail.put("carryingPodId", robot.getCarryingPodId());
    detail.put("assignedTaskCode", robot.getAssignedTaskCode());
    return detail;
}
```

- [ ] **Step 12.2: Add processComplete endpoint**

```java
@PostMapping("/robots/{robotCode}/process-complete")
public Map<String, String> processComplete(@PathVariable String robotCode) {
    VirtualRobot robot = fleetService.getRobot(robotCode);
    if (robot == null) {
        return Map.of("status", "error", "message", "Robot not found: " + robotCode);
    }
    if (robot.getBehavior() != null) {
        // Find the active task for this robot
        taskExecutionService.getActiveTasks().stream()
                .filter(t -> robotCode.equals(t.getAssignedRobotCode()))
                .findFirst()
                .ifPresent(task -> robot.getBehavior().processComplete(robot, task));
    }
    return Map.of("status", "PROCESS_COMPLETED");
}
```

- [ ] **Step 12.3: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/controller/SimulatorManagementController.java
git commit -m "feat(simulator): add robot detail and process-complete endpoints"
```

---

### Task 13: Update TaskReceiveController — set requiredRobotType and startLocations

**Files:**
- Modify: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/controller/TaskReceiveController.java`

- [ ] **Step 13.1: Update createTasks to set requiredRobotType and startLocations**

In the `TaskReceiveController.createTasks()` loop, replace:
- `task.setStartLocation(payload.getStartLocation())` → `task.setStartLocations(List.of(payload.getStartLocation()))`
- Add `task.setRequiredRobotType(payload.getRequiredRobotType())`

Add the `requiredRobotType` field to `CreateTaskPayload`:

```java
private String requiredRobotType;
```

- [ ] **Step 13.2: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/controller/TaskReceiveController.java
git commit -m "feat(simulator): add requiredRobotType and startLocations to task creation"
```

---

### Task 14: Update layout JSON and application.yml

**Files:**
- Modify: `server/modules-simulator/rcs-simulator/src/main/resources/layouts/default-layout.json`
- Modify: `server/modules-simulator/rcs-simulator/src/main/resources/application.yml`

- [ ] **Step 14.1: Update default-layout.json**

Add `pods` array. Rename FLT-xxx → BIN-xxx. Change `"FORKLIFT"` → `"BIN_ROBOT"`. Add `basketSlots: 6` to BIN robots.

```json
{
  "warehouse": {
    "width": 50,
    "height": 30,
    "gridSize": 1.0
  },
  "pods": [
    { "id": "POD-01", "x": 22, "y": 6 },
    { "id": "POD-02", "x": 25, "y": 6 },
    { "id": "POD-03", "x": 28, "y": 6 },
    { "id": "POD-04", "x": 22, "y": 18 },
    { "id": "POD-05", "x": 25, "y": 18 },
    { "id": "POD-06", "x": 28, "y": 18 }
  ],
  "shelves": [ /* unchanged */ ],
  "workstations": [ /* unchanged */ ],
  "chargingStations": [ /* unchanged */ ],
  "robots": [
    { "robotCode": "AGV-001", "robotType": "KIVA", "startX": 10, "startY": 12, "speed": 2.0 },
    { "robotCode": "AGV-002", "robotType": "KIVA", "startX": 14, "startY": 12, "speed": 2.0 },
    { "robotCode": "AGV-003", "robotType": "KIVA", "startX": 18, "startY": 12, "speed": 1.8 },
    { "robotCode": "AGV-004", "robotType": "KIVA", "startX": 22, "startY": 12, "speed": 2.0 },
    { "robotCode": "AGV-005", "robotType": "KIVA", "startX": 26, "startY": 12, "speed": 2.2 },
    { "robotCode": "AGV-006", "robotType": "KIVA", "startX": 30, "startY": 12, "speed": 2.0 },
    { "robotCode": "BIN-001", "robotType": "BIN_ROBOT", "startX": 34, "startY": 10, "speed": 1.5, "basketSlots": 6 },
    { "robotCode": "BIN-002", "robotType": "BIN_ROBOT", "startX": 34, "startY": 20, "speed": 1.5, "basketSlots": 6 }
  ]
}
```

- [ ] **Step 14.2: Update application.yml — add kiva and bin-robot config**

```yaml
simulator:
  tick-interval-ms: 200
  default-robot-speed: 2.0
  loading-delay-ms: 1500
  max-robots: 20
  failure-rate-percent: 0
  layout-file: ${LAYOUT_FILE:classpath:layouts/default-layout.json}
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:8092,http://3d-viewer:8092}
  kiva:
    lift-delay-ms: 1500
    process-delay-ms: 5000
  bin-robot:
    pick-delay-ms: 1500
    store-delay-ms: 1000
    unload-delay-ms: 1000
```

- [ ] **Step 14.3: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/resources/
git commit -m "feat(simulator): update layout JSON with pods and robot types, add new config"
```

---

### Task 15: Update existing tests

**Files:**
- Modify: `server/modules-simulator/rcs-simulator/src/test/java/org/openwes/simulator/service/RobotFleetServiceTest.java`
- Modify: `server/modules-simulator/rcs-simulator/src/test/java/org/openwes/simulator/service/TaskExecutionServiceTest.java`
- Modify: `server/modules-simulator/rcs-simulator/src/test/java/org/openwes/simulator/service/LayoutServiceTest.java`

- [ ] **Step 15.1: Update RobotFleetServiceTest — use RobotType enum, add type-filtered test**

```java
package org.openwes.simulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.simulator.config.SimulatorProperties;
import org.openwes.simulator.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RobotFleetServiceTest {

    private RobotFleetService fleetService;

    @BeforeEach
    void setUp() {
        PathService pathService = new PathService();
        SimulatorProperties props = new SimulatorProperties();
        fleetService = new RobotFleetService(pathService, props);
        List<WarehouseLayout.RobotConfig> configs = List.of(
                makeConfig("AGV-001", RobotType.KIVA, 0, 0, 2.0),
                makeConfig("AGV-002", RobotType.KIVA, 10, 10, 2.0),
                makeConfig("BIN-001", RobotType.BIN_ROBOT, 5, 5, 1.5, 6)
        );
        fleetService.initializeRobots(configs);
    }

    @Test
    void initializeRobots_createsAllRobots() {
        assertEquals(3, fleetService.getAllRobots().size());
        assertTrue(fleetService.getAllRobots().stream().allMatch(VirtualRobot::isIdle));
    }

    @Test
    void initializeRobots_assignsCorrectRobotType() {
        VirtualRobot agv = fleetService.getRobot("AGV-001");
        assertEquals(RobotType.KIVA, agv.getRobotType());
        assertNotNull(agv.getBehavior());
        assertInstanceOf(KivaBehavior.class, agv.getBehavior());
    }

    @Test
    void initializeRobots_assignsBinRobotBehaviorWithBasket() {
        VirtualRobot bin = fleetService.getRobot("BIN-001");
        assertEquals(RobotType.BIN_ROBOT, bin.getRobotType());
        assertNotNull(bin.getBasketItems());
        assertInstanceOf(BinRobotBehavior.class, bin.getBehavior());
    }

    @Test
    void findNearestIdleRobot_returnsClosest() {
        Position target = new Position(1, 1, 0);
        Optional<VirtualRobot> robot = fleetService.findNearestIdleRobot(target);
        assertTrue(robot.isPresent());
        assertEquals("AGV-001", robot.get().getRobotCode());
    }

    @Test
    void findNearestIdleRobot_filtersByType() {
        Position target = new Position(1, 1, 0);
        // Only KIVA robots
        Optional<VirtualRobot> robot = fleetService.findNearestIdleRobot(target, "KIVA");
        assertTrue(robot.isPresent());
        assertEquals(RobotType.KIVA, robot.get().getRobotType());

        // Only BIN_ROBOT
        robot = fleetService.findNearestIdleRobot(target, "BIN_ROBOT");
        assertTrue(robot.isPresent());
        assertEquals(RobotType.BIN_ROBOT, robot.get().getRobotType());
    }

    @Test
    void findNearestIdleRobot_allBusy_returnsEmpty() {
        fleetService.getAllRobots().forEach(r -> r.setStatus(RobotStatus.MOVING));
        Optional<VirtualRobot> robot = fleetService.findNearestIdleRobot(new Position(0, 0, 0));
        assertTrue(robot.isEmpty());
    }

    @Test
    void releaseRobot_clearsStrategyFields() {
        VirtualRobot robot = fleetService.getRobot("AGV-001");
        fleetService.assignTask(robot, "TASK-001", "C-001");
        robot.setCarryingPodId("POD-01");
        fleetService.releaseRobot(robot);

        assertTrue(robot.isIdle());
        assertNull(robot.getAssignedTaskCode());
        assertNull(robot.getCarriedContainerCode());
        assertNull(robot.getCarryingPodId());
    }

    @Test
    void releaseRobot_clearsBasket() {
        VirtualRobot robot = fleetService.getRobot("BIN-001");
        robot.getBasketItems().add("BIN-1");
        robot.getBasketItems().add("BIN-2");
        fleetService.releaseRobot(robot);
        assertTrue(robot.getBasketItems().isEmpty());
    }

    private WarehouseLayout.RobotConfig makeConfig(String code, RobotType type, double x, double y, double speed) {
        return makeConfig(code, type, x, y, speed, 0);
    }

    private WarehouseLayout.RobotConfig makeConfig(String code, RobotType type, double x, double y, double speed, int basketSlots) {
        WarehouseLayout.RobotConfig config = new WarehouseLayout.RobotConfig();
        config.setRobotCode(code);
        config.setRobotType(type);
        config.setStartX(x);
        config.setStartY(y);
        config.setSpeed(speed);
        config.setBasketSlots(basketSlots);
        return config;
    }
}
```

- [ ] **Step 15.2: Update LayoutServiceTest — add pod section assertion**

In `loadDefaultLayout_parsesAllSections`, add:
```java
assertNotNull(layout.getPods());
assertEquals(6, layout.getPods().size());
```

In `loadDefaultLayout_robotCodesAreUnique`, update to check for BIN_ROBOT robot codes (was FORKLIFT):
```java
assertTrue(layout.getRobots().stream().anyMatch(
    r -> r.getRobotType() == RobotType.KIVA));
assertTrue(layout.getRobots().stream().anyMatch(
    r -> r.getRobotType() == RobotType.BIN_ROBOT));
```

- [ ] **Step 15.3: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/test/
git commit -m "test(simulator): update tests for robot type system and behavior strategies"
```

---

### Task 16: Build and verify

- [ ] **Step 16.1: Run tests**

```bash
cd server && ./gradlew :modules-simulator:rcs-simulator:test
```

Expected: All tests pass.

- [ ] **Step 16.2: Run full build**

```bash
cd server && ./gradlew :modules-simulator:rcs-simulator:build
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 16.3: Final commit (if any fixes needed)**

```bash
git commit -m "fix(simulator): address test and build issues from robot type refactor"
```
