# RCS Simulator: Robot Types and Behavior Design

## Background

The RCS simulator currently defines two robot types as plain strings (`"KIVA"` and `"FORKLIFT"`) with no type-specific behavior. All robots follow the same linear task state machine:

```
QUEUED → ASSIGNED → MOVING_TO_PICKUP → LOADING → MOVING_TO_DESTINATION → UNLOADING → COMPLETED
```

This design introduces proper robot type classification and distinct behavior strategies so the simulator can model two fundamentally different robot workflows:

- **AGV (KIVA)**: Lifts and transports entire shelf pods to workstations
- **Bin Robot (BIN_ROBOT)**: Uses a fork to pick individual bins from shelves, stores them in a 6-slot basket, and delivers to workstations

## 1. Robot Type Enum

Replace the `String robotType` field with a proper enum.

```java
public enum RobotType {
    KIVA("AGV"),
    BIN_ROBOT("BinRobot");

    private final String displayName;
}
```

### Mapping

| Old (String) | New (Enum) | Meaning |
|--------------|------------|---------|
| `"KIVA"` | `KIVA` | AGV that lifts pods |
| `"FORKLIFT"` | `BIN_ROBOT` | Bin robot with fork + basket |

### Robot Code Renaming

| Old Code | New Code | Robot Type |
|----------|----------|------------|
| FLT-001 | BIN-001 | BIN_ROBOT |
| FLT-002 | BIN-002 | BIN_ROBOT |
| AGV-001 ~ 006 | (unchanged) | KIVA |

## 2. Layout JSON Changes

### New `pods` Section

Add 6 pods (movable shelf units) in the middle of the warehouse, accessible by AGVs:

```json
{
  "pods": [
    { "id": "POD-01", "x": 22, "y": 6 },
    { "id": "POD-02", "x": 25, "y": 6 },
    { "id": "POD-03", "x": 28, "y": 6 },
    { "id": "POD-04", "x": 22, "y": 18 },
    { "id": "POD-05", "x": 25, "y": 18 },
    { "id": "POD-06", "x": 28, "y": 18 }
  ]
}
```

Each pod has a single location code (no layered bin storage — pods hold SKUs directly). The pod's `id` (e.g., `"POD-01"`) is used as its location code for task routing. The `buildLocationIndex()` method in `WarehouseLayout` maps pod IDs to their positions.

### Updated Robot Config

```json
{
  "robots": [
    { "robotCode": "AGV-001", "robotType": "KIVA",
      "startX": 24, "startY": 12, "speed": 2.0 },
    { "robotCode": "BIN-001", "robotType": "BIN_ROBOT",
      "startX": 34, "startY": 10, "speed": 1.5,
      "basketSlots": 6 },
    { "robotCode": "BIN-002", "robotType": "BIN_ROBOT",
      "startX": 34, "startY": 20, "speed": 1.5,
      "basketSlots": 6 }
  ]
}
```

- `basketSlots` configures the bin robot's basket capacity (default: 6)
- Existing shelves (A01~D02) are used by bin robots only
- AGVs operate exclusively on pods

### WarehouseLayout Java Class Changes

Add inner class:
```java
public static class PodConfig {
    private String id;
    private double x;
    private double y;
}
```

Update `RobotConfig`:
```java
public static class RobotConfig {
    private String robotCode;
    private RobotType robotType;  // String → RobotType enum
    private double startX;
    private double startY;
    private double speed;
    private int basketSlots;       // new: for BIN_ROBOT only
}
```

## 3. VirtualRobot Changes

```java
public class VirtualRobot {
    private String robotCode;
    private RobotType robotType;       // String → enum
    private RobotStatus status;        // IDLE, MOVING, LOADING, UNLOADING, ERROR
    private Position currentPosition;
    private double speed;
    private double batteryLevel;

    // KIVA-specific: currently carried pod
    private String carryingPodId;

    // BIN_ROBOT-specific: items in basket (max 6)
    private List<String> basketItems;

    // Strategy reference
    private RobotBehavior behavior;
}
```

## 4. Strategy Pattern: RobotBehavior

### Interface

```java
public interface RobotBehavior {
    /** Advance task execution for one tick */
    void executeTick(VirtualRobot robot, SimulatedTask task);

    /** Check if robot can accept this task type */
    boolean canAcceptTask(VirtualRobot robot, SimulatedTask task);

    /** Initialize task state for this robot type */
    void initTask(VirtualRobot robot, SimulatedTask task);
}
```

### 4.1 KivaBehavior (AGV Pod Lifting)

State flow:
```
INIT → MOVING_TO_POD → LIFTING_POD → MOVING_TO_WS
    → DROPPING_POD → WAITING_PROCESS → COMPLETED
```

**executeTick() per state:**

| State | Action | Transition |
|-------|--------|------------|
| INIT | Set target pod position | → MOVING_TO_POD |
| MOVING_TO_POD | Move to pod location | → LIFTING_POD (arrived) |
| LIFTING_POD | Wait `lift-delay-ms` (default 1500ms), set `carryingPodId` | → MOVING_TO_WS |
| MOVING_TO_WS | Move to workstation carrying pod | → DROPPING_POD (arrived) |
| DROPPING_POD | Wait `lift-delay-ms`, clear `carryingPodId` (pod stays at WS) | → WAITING_PROCESS |
| WAITING_PROCESS | Wait for human processing to complete | → COMPLETED (via API or timeout) |
| COMPLETED | Report task completion | — |

**WAITING_PROCESS completion** supports two modes:
- **Fixed delay**: Auto-complete after `process-delay-ms` (default 5000ms)
- **API-driven**: `POST /api/simulator/robots/{robotCode}/process-complete` triggers completion

### 4.2 BinRobotBehavior (Fork + Basket)

State flow:
```
INIT → MOVING_TO_LOCATION → PICKING_BIN → STORING_IN_BASKET
    → (more bins?) → MOVING_TO_NEXT_LOCATION
    → MOVING_TO_WS → UNLOADING_BASKET → COMPLETED
```

**executeTick() per state:**

| State | Action | Transition |
|-------|--------|------------|
| INIT | Set target to first location in `pickupLocationCodes` | → MOVING_TO_LOCATION |
| MOVING_TO_LOCATION | Move to shelf location | → PICKING_BIN (arrived) |
| PICKING_BIN | Wait `pick-delay-ms`, acquire bin | → STORING_IN_BASKET |
| STORING_IN_BASKET | Wait `store-delay-ms`, add bin to `basketItems` | → next unvisited location in `pickupLocationCodes` (more bins) or MOVING_TO_WS (all done) |
| MOVING_TO_NEXT_LOCATION | Move to next bin location | → PICKING_BIN (arrived) |
| MOVING_TO_WS | Move to workstation | → UNLOADING_BASKET (arrived) |
| UNLOADING_BASKET | Clear `basketItems` one by one (`unload-delay-ms` per item) | → COMPLETED |

`basketItems` is a `List<String>` (max `basketSlots` items).

### Behavior Assembly

In `RobotFleetService` initialization:

```java
switch (robotConfig.getRobotType()) {
    case KIVA:
        robot.setBehavior(new KivaBehavior(properties));
        robot.setBasketItems(Collections.emptyList());
        break;
    case BIN_ROBOT:
        robot.setBehavior(new BinRobotBehavior(properties));
        robot.setBasketItems(new ArrayList<>(robotConfig.getBasketSlots()));
        break;
}
```

## 5. Task Execution Flow Changes

### Task Assignment

When receiving a task from WES via `TaskReceiveController`, the `SimulatedTask` now includes a `requiredRobotType` field.

For KIVA tasks, `pickupLocationCodes` contains a single pod location. For BIN_ROBOT tasks, it contains one or more shelf bin locations (the robot iterates through them).

```java
public class SimulatedTask {
    private String taskId;
    private String requiredRobotType;  // "KIVA" or "BIN_ROBOT"
    private List<String> pickupLocationCodes;  // was: single String
    private String destinationLocationCode;
    // ... existing fields
}
```

`RobotFleetService.findNearestIdleRobot()` filters by robot type:
```java
// Only consider robots matching the required type
fleet.getRobots().stream()
    .filter(r -> r.getRobotType().name().equals(requiredType))
    .filter(r -> r.getStatus() == RobotStatus.IDLE)
    .min(Comparator.comparingDouble(r -> distance(r, pickupPosition)))
```

If no robot is available, the task is queued. The queue drain also respects type matching.

### Task Execution Tick

```java
for (SimulatedTask task : activeTasks) {
    VirtualRobot robot = task.getAssignedRobot();
    robot.getBehavior().executeTick(robot, task);
    // WebSocket push is still handled uniformly
}
```

## 6. New REST API

### Get Robot Detail

```
GET /api/simulator/robots/{robotCode}

Response:
{
  "robotCode": "BIN-001",
  "robotType": "BIN_ROBOT",
  "status": "MOVING",
  "position": { "x": 12.0, "y": 8.0 },
  "batteryLevel": 0.85,
  "basketItems": ["ITEM-A", "ITEM-B", "ITEM-C"],
  "basketSlots": 6,
  "carryingPodId": null,
  "currentTask": { ... }
}
```

### Trigger Process Complete

```
POST /api/simulator/robots/{robotCode}/process-complete

Response: { "status": "PROCESS_COMPLETED", "taskStatus": "COMPLETED" }
```

Only valid when robot is in `WAITING_PROCESS` state.

## 7. WebSocket Data Extension

The robot state pushed via WebSocket to the 3D viewer is extended:

```json
{
  "robotCode": "BIN-001",
  "robotType": "BIN_ROBOT",
  "position": { "x": 12.0, "y": 8.0 },
  "status": "MOVING",
  "rotation": 90,
  "basketItems": ["ITEM-A", "ITEM-B"],
  "basketSlots": 6,
  "carryingPodId": null
}
```

3D viewer rendering rules:
- **BIN_ROBOT**: Show fork model + basket with visible bins
- **KIVA + carryingPodId != null**: Show pod on top of AGV
- **KIVA + carryingPodId == null**: Empty AGV (no pod)

## 8. Configuration

New simulator properties in `application.yml`:

```yaml
simulator:
  kiva:
    lift-delay-ms: 1500         # Pod lift/drop duration
    process-delay-ms: 5000      # Default human processing time
  bin-robot:
    pick-delay-ms: 1500         # Bin pick duration
    store-delay-ms: 1000        # Store bin to basket duration
    unload-delay-ms: 1000       # Unload bin from basket duration
```

## Scope Boundaries

- **In scope**: Strategy-based robot behavior, basket/pod state in VirtualRobot, layout JSON updates, type-filtered task assignment, new REST endpoints, WebSocket data extensions
- **Out of scope**: Battery simulation, collision avoidance, pod storage location tracking (pods stay at WS until a follow-up task moves them back)
