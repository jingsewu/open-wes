# RCS Simulator & 3D Warehouse Visualization Design

## Background

Open WES 定位为仓库执行层系统，通过 ems-proxy 模块与外部 RCS（机器人控制系统）集成，而非自建多机器人调度引擎。为了向客户展示 WES 对接 RCS 的能力，需要：

1. **RCS 模拟器** — 独立服务，模拟外部 RCS 接收任务并执行，无需真实机器人硬件
2. **3D 仓库可视化** — 独立 Web 应用，实时展示仓库布局和机器人运动

## Architecture

```
┌──────────────────┐     HTTP Callback      ┌──────────────────┐
│    Open WES      │ ◄────────────────────► │  RCS Simulator   │
│   (ems-proxy)    │   Container Tasks       │  (Spring Boot)   │
│                  │   Status Callbacks      │                  │
└────────┬─────────┘                        └────────┬─────────┘
         │                                           │
         │ (optional)                     WebSocket   │
         │                                           │
         ▼                                           ▼
┌──────────────────────────────────────────────────────────────┐
│                    3D Warehouse Viewer                        │
│              (React 18 + Three.js / R3F)                     │
│         iframe embedded in Open WES frontend                  │
└──────────────────────────────────────────────────────────────┘
```

### Deployment

- RCS Simulator: Docker container, added to docker-compose.yml as optional service
- 3D Viewer: Docker container (nginx + static files), added to docker-compose.yml as optional service
- Both services are demo-only, not needed in production

---

## Module 1: RCS Simulator

### Purpose

Simulate an external RCS system that:
- Receives container transport tasks from WES via HTTP callbacks
- Manages a fleet of virtual robots (position, status, load)
- Simulates task execution with configurable delays
- Calls back WES with containerArrive / containerLeave / status updates
- Pushes real-time robot positions to 3D Viewer via WebSocket

### Technology Stack

- **Runtime**: Spring Boot 3.2 (consistent with WES)
- **State**: In-memory only, no database required
- **Communication**: HTTP REST (WES callbacks) + WebSocket (3D Viewer)
- **Build**: Gradle subproject under `server/modules-simulator/` (requires adding to `server/settings.gradle`)
- **Health**: Exposes `/actuator/health` for Docker healthcheck
- **CORS**: Configured to allow WebSocket connections from 3D Viewer origin

### Integration with WES

The simulator implements the RCS side of the existing ems-proxy callback contract:

#### Inbound (WES → Simulator)

WES sends HTTP callbacks to the simulator via the api-platform callback mechanism. The simulator must expose REST endpoints matching these callback types:

| Callback Type | Data | Simulator Action |
|---|---|---|
| `CONTAINER_TASK_CREATE` | `CallbackMessage<List<CreateContainerTaskDTO>>` | Accept task, assign to virtual robot, begin simulated execution |
| `CONTAINER_TASK_CANCEL` | `CallbackMessage<List<String>>` (task codes) | Cancel in-progress task, release robot |
| `CONTAINER_TASK_IMPROVE_PRIORITY` | `CallbackMessage<ImprovePriorityDTO>` | Update task priority in queue |
| `CONTAINER_TASK_RELEASE` | `CallbackMessage<List<String>>` (task codes) | Release container at destination |
| `CALL_ROBOT` | `CallbackMessage<CallRobotDTO>` | Dispatch robot to location |

#### Outbound (Simulator → WES)

The simulator calls WES REST APIs to report status:

| API | DTO | When |
|---|---|---|
| `POST /api/container/arrive` | `ContainerArrivedEvent` | Robot delivers container to destination |
| `POST /api/container/leave` | `ContainerOperation` | Container departs from location |
| `POST /api/container/task/status` | `List<UpdateContainerTaskDTO>` | Task status changes (PROCESSING, WCS_SUCCEEDED, WCS_FAILED) |

### Virtual Robot Model

Note: The simulator is a demo module outside core WES. Domain objects here intentionally use simple mutable POJOs rather than DDD lifecycle patterns — the overhead of aggregate roots and event sourcing is not justified for transient in-memory simulation state.

```java
class VirtualRobot {
    String robotCode;           // e.g., "AGV-001"
    String robotType;           // e.g., "KIVA", "FORKLIFT"
    RobotStatus status;         // IDLE, MOVING, LOADING, UNLOADING, CHARGING, ERROR
    Position currentPosition;   // x, y coordinates on warehouse grid
    String currentLocationCode; // mapped to warehouse location
    String assignedTaskCode;    // current task, null if idle
    String carriedContainerCode; // container being carried, null if empty
    double speed;               // grid units per second
    double batteryLevel;        // 0.0 - 1.0
}

class Position {
    double x;
    double y;
    double rotation; // facing direction in degrees
}

enum RobotStatus {
    IDLE, MOVING_TO_PICKUP, LOADING, MOVING_TO_DESTINATION, UNLOADING, CHARGING, ERROR
}
```

### Task Execution Simulation

1. **Receive task** → Find nearest IDLE robot → Assign task → Status = `PROCESSING`
2. **Move to pickup** → Robot status = `MOVING_TO_PICKUP`, simulate position updates along path
3. **Load container** → Robot status = `LOADING`, brief delay (1-2s)
4. **Move to destination** → Robot status = `MOVING_TO_DESTINATION`, simulate position updates
5. **Unload** → Robot status = `UNLOADING`, brief delay (1-2s)
6. **Complete** → Callback WES with `containerArrive` → Status = `WCS_SUCCEEDED` → Robot = `IDLE`

Path simulation: Simple grid-based A* or Manhattan movement (no complex path planning needed). Configurable speed to control demo pace.

### Error Simulation

To demonstrate WES handling of `WCS_FAILED` callbacks:
- **Configurable failure rate**: `simulator.failure-rate-percent` (default 0, set to e.g. 5 for demos)
- **Manual error injection**: `POST /api/simulator/robots/{robotCode}/error` — forces a specific robot into ERROR state mid-task
- **Recovery**: `POST /api/simulator/robots/{robotCode}/recover` — clears error, robot returns to IDLE
- When a robot fails, the simulator callbacks WES with `WCS_FAILED` status, and the assigned task is released for reassignment

### Warehouse Layout Configuration

```json
{
  "warehouse": {
    "width": 50,
    "height": 30,
    "gridSize": 1.0
  },
  "shelves": [
    { "id": "A01", "x": 5, "y": 3, "width": 2, "height": 8, "locationCodes": ["A01-01", "A01-02", "..."] }
  ],
  "workstations": [
    { "id": "WS-01", "x": 2, "y": 15, "type": "PICKING", "locationCode": "WS-01" }
  ],
  "chargingStations": [
    { "id": "CS-01", "x": 48, "y": 1, "locationCode": "CS-01" }
  ],
  "robots": [
    { "robotCode": "AGV-001", "robotType": "KIVA", "startX": 10, "startY": 15, "speed": 2.0 },
    { "robotCode": "AGV-002", "robotType": "KIVA", "startX": 15, "startY": 15, "speed": 2.0 },
    { "robotCode": "AGV-003", "robotType": "KIVA", "startX": 20, "startY": 15, "speed": 1.8 },
    { "robotCode": "AGV-004", "robotType": "KIVA", "startX": 25, "startY": 15, "speed": 2.0 },
    { "robotCode": "AGV-005", "robotType": "KIVA", "startX": 30, "startY": 15, "speed": 2.2 },
    { "robotCode": "AGV-006", "robotType": "KIVA", "startX": 35, "startY": 15, "speed": 2.0 },
    { "robotCode": "FLT-001", "robotType": "FORKLIFT", "startX": 40, "startY": 10, "speed": 1.5 },
    { "robotCode": "FLT-002", "robotType": "FORKLIFT", "startX": 40, "startY": 20, "speed": 1.5 }
  ]
}
```

This layout JSON is loaded at startup and defines the simulated warehouse. It maps `locationCode` values used by WES to x/y grid positions for visualization.

### WebSocket API (Simulator → 3D Viewer)

Endpoint: `ws://simulator:8091/ws/robots`

Push interval is configurable via `simulator.tick-interval-ms` (default 200ms). The 3D Viewer must handle missed frames gracefully — use position interpolation (lerp) between received updates rather than snapping, so animation remains smooth even if a frame is dropped.

**Reconnection strategy**: The Viewer uses exponential backoff reconnection (1s → 2s → 4s → max 30s). During disconnection, the Viewer displays a connection status indicator and freezes robot positions at their last known state (static layout remains visible).

Push messages:

```json
{
  "type": "ROBOT_STATE_UPDATE",
  "timestamp": 1716480000000,
  "robots": [
    {
      "robotCode": "AGV-001",
      "status": "MOVING_TO_DESTINATION",
      "x": 12.5,
      "y": 8.3,
      "rotation": 90,
      "carriedContainerCode": "C-001",
      "taskCode": "CT-20260523-001",
      "batteryLevel": 0.85
    }
  ],
  "tasks": [
    {
      "taskCode": "CT-20260523-001",
      "status": "PROCESSING",
      "containerCode": "C-001",
      "startLocation": "A01-03",
      "destination": "WS-01",
      "assignedRobot": "AGV-001"
    }
  ]
}
```

### REST API (Management)

| Endpoint | Method | Description |
|---|---|---|
| `/api/simulator/robots` | GET | List all virtual robots and their state |
| `/api/simulator/tasks` | GET | List all active/completed tasks |
| `/api/simulator/layout` | GET | Get current warehouse layout |
| `/api/simulator/layout` | PUT | Update warehouse layout |
| `/api/simulator/reset` | POST | Reset all robots to initial state |
| `/api/simulator/config` | GET/PUT | Get/set simulation speed, delay parameters |
| `/api/simulator/robots/{robotCode}/error` | POST | Inject error on a specific robot (for demo) |
| `/api/simulator/robots/{robotCode}/recover` | POST | Recover robot from error state |
| `/api/simulator/layout/validate` | POST | Validate layout locationCodes against WES location master data |

### Simulator Port

| Service | Port |
|---|---|
| RCS Simulator HTTP | 8091 |
| RCS Simulator WebSocket | 8091 (same, path-based) |

---

## Module 2: 3D Warehouse Viewer

### Purpose

Real-time 3D visualization of warehouse operations, showing:
- Warehouse layout (shelves, workstations, charging stations, aisles)
- Robot positions and movement animations
- Container transport processes
- Task status panel

### Technology Stack

- **Framework**: React 18 + TypeScript
- **3D Engine**: Three.js via React Three Fiber (R3F) + Drei helpers
- **State**: Zustand (lightweight, fits well with R3F)
- **Styling**: Tailwind CSS for UI panels
- **Build**: Vite
- **Deployment**: Docker (nginx serving static files)

**Tech stack divergence note**: The 3D Viewer intentionally uses React 18 + Zustand + Tailwind, which differs from the main client's React 17 + MobX + Ant Design. This is necessary because React Three Fiber requires React 18+, Zustand integrates naturally with R3F's render loop, and Tailwind is lighter for a standalone overlay-heavy 3D app. As an independent standalone application deployed in its own container, this divergence has no impact on the main client.

### Integration

- **iframe embedding**: Added as a page in Open WES frontend under the monitoring app, similar to Grafana dashboard pages. The iframe URL is configurable via system config (not hardcoded to `localhost`), following the same pattern as existing Grafana dashboard URLs.
- **Data source**: WebSocket connection to RCS Simulator for real-time robot positions
- **Standalone access**: Can also be opened directly in browser for demo purposes
- **Connection states**: Displays a connection status indicator (connected/reconnecting/disconnected). When disconnected, shows static warehouse layout with frozen robot positions at last known state.

### 3D Scene Components

#### Warehouse Floor
- Grid-textured plane matching warehouse dimensions
- Aisle markings between shelf rows
- Area labels (receiving, shipping, storage zones)

#### Shelves
- Parameterized box geometry based on layout config
- Multiple levels with visible container slots
- Highlight when a container is being picked/placed

#### Robots (AGV/KIVA)
- Simple geometric model: flat rectangular body + wheels + status LED indicator
- Color coding by status:
  - Blue = IDLE
  - Green = MOVING (with direction indicator)
  - Orange = LOADING/UNLOADING
  - Red = ERROR
  - Gray = CHARGING
- Smooth position interpolation between WebSocket updates (lerp)
- Container model visible on top when carrying

#### Containers
- Box geometry with container code label
- Visible on shelves (static) and on robots (moving)
- Highlight effect when being transported

#### Workstations
- Distinct geometry (desk/conveyor shape)
- Operator figure placeholder
- Incoming/outgoing container queue visualization

### UI Panels (2D overlay on 3D scene)

#### Task Panel (right sidebar)
- List of active tasks with status badges
- Task details on click: container, robot, route, timing
- Completed task history (last 20)

#### Robot Panel (left sidebar, collapsible)
- Robot list with status indicators
- Click to follow/track a specific robot (camera follows)
- Battery level bars

#### Controls (top bar)
- Simulation speed slider (0.5x - 5x)
- Camera controls: top-down / perspective / follow-robot
- Reset view button
- Warehouse selector (if multiple layouts configured)

### Camera System
- Default: Perspective view from corner, looking at warehouse center
- Top-down: Orthographic bird's eye view
- Follow mode: Camera tracks selected robot with orbit controls
- Mouse controls: orbit (left drag), pan (right drag), zoom (scroll)

### 3D Viewer Port

| Service | Port |
|---|---|
| 3D Viewer (nginx) | 8092 |

---

## Demo Scenarios

### Inbound Demo Flow

1. User creates inbound plan order in WES UI → accept goods → generate put-away tasks
2. WES sends `CONTAINER_TASK_CREATE` to RCS Simulator (container type = INBOUND)
3. Simulator assigns nearest idle robot, begins movement
4. **3D View**: Robot moves from receiving area to container → loads → moves to target shelf → unloads
5. Simulator callbacks: `containerArrive` at shelf location → WES updates stock
6. Robot returns to idle position

### Outbound Demo Flow

1. User creates outbound plan order → wave → picking tasks
2. WES sends `CONTAINER_TASK_CREATE` to RCS Simulator (container type = OUTBOUND)
3. Simulator assigns robot to fetch container from shelf
4. **3D View**: Robot moves to shelf → loads container → moves to workstation
5. Simulator callbacks: `containerArrive` at workstation → operator picks items
6. After picking, WES sends another task to return container
7. **3D View**: Robot takes container back to shelf

### Multi-Robot Coordination Demo

- Configure 8 virtual robots (6 KIVA + 2 FORKLIFT)
- Trigger multiple inbound + outbound tasks simultaneously
- Show robots operating in parallel, avoiding collisions (simple queue at intersections)
- Demonstrate WES task prioritization affecting robot dispatch order

---

## Project Structure

```
server/
  modules-simulator/
    rcs-simulator/
      src/main/java/org/openwes/simulator/
        RcsSimulatorApplication.java
        config/
          SimulatorConfig.java
          WebSocketConfig.java
        controller/
          TaskReceiveController.java      # Receives WES callbacks
          SimulatorManagementController.java  # Management REST API
        domain/
          VirtualRobot.java
          SimulatedTask.java
          WarehouseLayout.java
          Position.java
        service/
          RobotFleetService.java          # Robot management and task assignment
          TaskExecutionService.java       # Simulates task execution
          PathSimulationService.java      # Simple grid-based movement
          WesCallbackService.java         # Calls back WES APIs
          WebSocketPushService.java       # Pushes state to 3D Viewer
      src/main/resources/
        application.yml
        layouts/
          default-layout.json            # Default warehouse layout
      build.gradle

3d-viewer/                               # Separate project at repo root
  src/
    App.tsx
    components/
      Scene/
        WarehouseScene.tsx               # Main 3D scene
        Floor.tsx
        Shelf.tsx
        Robot.tsx
        Container.tsx
        Workstation.tsx
      Panels/
        TaskPanel.tsx
        RobotPanel.tsx
        Controls.tsx
    hooks/
      useRobotWebSocket.ts              # WebSocket connection
      useSimulatorApi.ts                 # REST API calls
    stores/
      simulatorStore.ts                  # Zustand store
    types/
      index.ts                          # TypeScript interfaces
  package.json
  vite.config.ts
  Dockerfile
```

## Docker Compose Addition

```yaml
# Optional demo services
rcs-simulator:
  build: ./server/modules-simulator/rcs-simulator
  ports:
    - "8091:8091"
  environment:
    - WES_CALLBACK_URL=http://gateway-server:8090
    - LAYOUT_FILE=/config/default-layout.json
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:8091/actuator/health"]
    interval: 10s
    timeout: 5s
    retries: 3
  depends_on:
    - gateway-server
  profiles:
    - demo

3d-viewer:
  build: ./3d-viewer
  ports:
    - "8092:80"
  depends_on:
    rcs-simulator:
      condition: service_healthy
  profiles:
    - demo
```

Using `profiles: demo` ensures these services only start when explicitly requested: `docker compose --profile demo up -d`

---

## WES Frontend Integration

Add a new menu entry under the monitoring app for the 3D viewer:

- Menu path: Monitoring → Warehouse 3D View
- Page: iframe embedding via configurable URL (stored in system config, default `http://localhost:8092`)
- Same pattern as existing Grafana dashboard pages — URL is not hardcoded in frontend code

Liquibase changeset to add menu entry in `u_menu` table.

---

## Configuration

### Simulator → WES Connection

```yaml
# application.yml for rcs-simulator
wes:
  callback-url: http://gateway-server:8090  # WES gateway URL
  api:
    container-arrive: /api/container/arrive
    container-leave: /api/container/leave
    task-status-update: /api/container/task/status

simulator:
  tick-interval-ms: 200        # State update frequency
  default-robot-speed: 2.0     # Grid units per second
  loading-delay-ms: 1500       # Time to load/unload container
  max-robots: 20               # Maximum virtual robots
  failure-rate-percent: 0      # Random task failure rate (0-100, for demo)
  cors:
    allowed-origins: "http://localhost:8092,http://3d-viewer:8092"  # 3D Viewer origins
```

### WES → Simulator Connection

Uses the existing `ApiPO` configuration mechanism — no code changes in WES core needed. A Liquibase changeset seeds the callback API records so the demo works out-of-the-box:

```sql
-- Liquibase changeset: seed RCS Simulator callback configs (demo profile)
INSERT INTO u_api (code, name, api_type, url, method, format, enabled, sync_callback)
VALUES
  ('CONTAINER_TASK_CREATE', 'RCS Simulator - Create Task',
   'CALLBACK', 'http://rcs-simulator:8091/api/tasks/create', 'POST', 'JSON', true, false),
  ('CONTAINER_TASK_CANCEL', 'RCS Simulator - Cancel Task',
   'CALLBACK', 'http://rcs-simulator:8091/api/tasks/cancel', 'POST', 'JSON', true, false),
  ('CONTAINER_TASK_IMPROVE_PRIORITY', 'RCS Simulator - Improve Priority',
   'CALLBACK', 'http://rcs-simulator:8091/api/tasks/improve-priority', 'POST', 'JSON', true, false),
  ('CONTAINER_TASK_RELEASE', 'RCS Simulator - Release Task',
   'CALLBACK', 'http://rcs-simulator:8091/api/tasks/release', 'POST', 'JSON', true, false),
  ('CONTAINER_LEAVE', 'RCS Simulator - Container Leave',
   'CALLBACK', 'http://rcs-simulator:8091/api/tasks/container-leave', 'POST', 'JSON', true, false),
  ('CALL_ROBOT', 'RCS Simulator - Call Robot',
   'CALLBACK', 'http://rcs-simulator:8091/api/tasks/call-robot', 'POST', 'JSON', true, false);
```

This changeset uses `preConditions onFail="MARK_RAN"` to avoid conflicts if the records already exist. The URLs point to Docker service names, so they work within `docker compose --profile demo`.

---

## Scope Boundaries

### In Scope
- Virtual robot fleet with position simulation
- Container task lifecycle (create → process → complete/fail)
- Simple task assignment (nearest idle robot)
- Grid-based movement simulation (Manhattan path)
- 3D visualization of warehouse layout, robots, containers
- WebSocket real-time state push
- Inbound and outbound demo scenarios
- Docker deployment with demo profile

### Out of Scope
- Real path planning / collision avoidance algorithms
- Robot battery simulation / charging workflow
- Multi-floor warehouse support
- Integration with real RCS systems (that's what ems-proxy already does)
- Performance optimization for 100+ robots (demo targets 4-20 robots)
- Mobile / responsive 3D viewer
- Persistent task history / analytics
