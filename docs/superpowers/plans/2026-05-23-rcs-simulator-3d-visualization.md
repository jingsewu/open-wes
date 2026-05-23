# RCS Simulator & 3D Warehouse Visualization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a demo-purpose RCS Simulator backend and 3D Warehouse Viewer frontend that showcase Open WES's ability to integrate with robot control systems, allowing turnkey demos without real hardware.

**Architecture:** Two independent services — (1) a Spring Boot RCS Simulator that receives container tasks from WES via HTTP callbacks, simulates robot fleet execution, and pushes state via WebSocket; (2) a React 18 + Three.js 3D Viewer that renders the warehouse and robots in real-time. Both deploy as optional Docker containers with `profiles: demo`.

**Tech Stack:** Java 17 / Spring Boot 3.2 / WebSocket (simulator); React 18 / TypeScript / React Three Fiber / Zustand / Tailwind CSS / Vite (3D viewer); Docker Compose for deployment.

**Spec:** `docs/superpowers/specs/2026-05-23-rcs-simulator-3d-visualization-design.md`

---

## File Map

### RCS Simulator (server/modules-simulator/rcs-simulator/)

| File | Responsibility |
|------|---------------|
| `build.gradle` | Dependencies: spring-boot-starter-web, spring-boot-starter-websocket, spring-boot-starter-actuator, jackson, lombok |
| `src/main/java/org/openwes/simulator/RcsSimulatorApplication.java` | Spring Boot entry point |
| `src/main/java/org/openwes/simulator/config/SimulatorProperties.java` | `@ConfigurationProperties("simulator")` — tick interval, robot speed, loading delay, failure rate, CORS origins |
| `src/main/java/org/openwes/simulator/config/WebSocketConfig.java` | WebSocket endpoint registration + CORS |
| `src/main/java/org/openwes/simulator/config/CorsConfig.java` | REST CORS filter |
| `src/main/java/org/openwes/simulator/domain/Position.java` | x, y, rotation |
| `src/main/java/org/openwes/simulator/domain/RobotStatus.java` | Enum: IDLE, MOVING_TO_PICKUP, LOADING, MOVING_TO_DESTINATION, UNLOADING, CHARGING, ERROR |
| `src/main/java/org/openwes/simulator/domain/VirtualRobot.java` | Robot state POJO |
| `src/main/java/org/openwes/simulator/domain/SimulatedTask.java` | Task state POJO with status lifecycle |
| `src/main/java/org/openwes/simulator/domain/TaskStatus.java` | Enum: QUEUED, ASSIGNED, MOVING_TO_PICKUP, LOADING, MOVING_TO_DESTINATION, UNLOADING, COMPLETED, FAILED, CANCELED |
| `src/main/java/org/openwes/simulator/domain/WarehouseLayout.java` | Layout model: warehouse dimensions, shelves, workstations, charging stations, robot configs |
| `src/main/java/org/openwes/simulator/service/LayoutService.java` | Load/validate/update warehouse layout from JSON |
| `src/main/java/org/openwes/simulator/service/PathService.java` | Manhattan path calculation between grid positions |
| `src/main/java/org/openwes/simulator/service/RobotFleetService.java` | Robot lifecycle: find nearest idle robot, assign task, update positions |
| `src/main/java/org/openwes/simulator/service/TaskExecutionService.java` | Scheduled tick loop: advance all active tasks, trigger callbacks |
| `src/main/java/org/openwes/simulator/service/WesCallbackService.java` | HTTP client that calls WES APIs (containerArrive, containerLeave, taskStatusUpdate) |
| `src/main/java/org/openwes/simulator/service/WebSocketPushService.java` | Push robot/task state to connected 3D Viewers |
| `src/main/java/org/openwes/simulator/controller/TaskReceiveController.java` | REST endpoints receiving WES callbacks (create, cancel, improve-priority, release, call-robot) |
| `src/main/java/org/openwes/simulator/controller/SimulatorManagementController.java` | Management REST API (robots, tasks, layout, config, reset, error inject/recover, validate) |
| `src/main/java/org/openwes/simulator/websocket/RobotStateHandler.java` | WebSocket handler for `/ws/robots` |
| `src/main/resources/application.yml` | Server port 8091, WES callback URLs, simulator defaults |
| `src/main/resources/layouts/default-layout.json` | Default warehouse layout with 8 robots |
| `Dockerfile` | Multi-stage build: gradle → JRE 17 slim |
| `src/test/java/.../service/PathServiceTest.java` | Unit tests for path calculation |
| `src/test/java/.../service/RobotFleetServiceTest.java` | Unit tests for robot assignment |
| `src/test/java/.../service/TaskExecutionServiceTest.java` | Unit tests for task state machine |
| `src/test/java/.../controller/TaskReceiveControllerTest.java` | Integration tests for WES callback endpoints |
| `src/test/java/.../controller/SimulatorManagementControllerTest.java` | Integration tests for management API |

### 3D Warehouse Viewer (3d-viewer/)

| File | Responsibility |
|------|---------------|
| `package.json` | React 18, @react-three/fiber, @react-three/drei, zustand, tailwindcss |
| `vite.config.ts` | Vite config with React plugin |
| `tailwind.config.js` | Tailwind configuration |
| `tsconfig.json` | TypeScript config |
| `index.html` | HTML entry with root div |
| `src/main.tsx` | React DOM render entry |
| `src/App.tsx` | App shell: 3D canvas + overlay panels + connection status |
| `src/types/index.ts` | TypeScript interfaces: RobotState, TaskState, WarehouseLayout, WebSocketMessage |
| `src/stores/simulatorStore.ts` | Zustand store: robots, tasks, layout, connection status, selected robot |
| `src/hooks/useRobotWebSocket.ts` | WebSocket connection with exponential backoff reconnection |
| `src/hooks/useSimulatorApi.ts` | REST API client for simulator management endpoints |
| `src/components/Scene/WarehouseScene.tsx` | Main R3F Canvas + scene assembly |
| `src/components/Scene/Floor.tsx` | Grid-textured ground plane |
| `src/components/Scene/Shelf.tsx` | Parameterized shelf geometry |
| `src/components/Scene/Robot.tsx` | Robot mesh with status color + lerp interpolation + carried container |
| `src/components/Scene/Workstation.tsx` | Workstation geometry |
| `src/components/Scene/CameraController.tsx` | Perspective / top-down / follow-robot camera modes |
| `src/components/Panels/TaskPanel.tsx` | Right sidebar: active tasks + history |
| `src/components/Panels/RobotPanel.tsx` | Left sidebar: robot list + follow button |
| `src/components/Panels/Controls.tsx` | Top bar: speed slider, camera mode, reset |
| `src/components/Panels/ConnectionStatus.tsx` | Connection indicator badge |
| `Dockerfile` | Node build → nginx static serve |
| `nginx.conf` | SPA fallback + proxy headers |

### Integration (project root)

| File | Responsibility |
|------|---------------|
| `server/settings.gradle` | Add `include 'modules-simulator:rcs-simulator'` |
| `docker-compose.yml` | Add rcs-simulator + 3d-viewer services with `profiles: demo` |
| `client/src/pages/monitoring/warehouse3d.tsx` | iframe page embedding 3D viewer |
| `client/src/routes/path2Compoment.tsx` | Add `/monitoring/warehouse3d` route |
| `client/src/locales/en/monitoring.ts` | English translation for menu title |
| `client/src/locales/zh/monitoring.ts` | Chinese translation for menu title |
| `client/src/locales/ja/monitoring.ts` | Japanese translation for menu title |
| `client/src/locales/ko/monitoring.ts` | Korean translation for menu title |
| `server/server/wes-server/src/main/resources/db/changelog/db.changelog-20260523-simulator.xml` | Liquibase: menu entry + callback API seed data |

---

## Phase 1: RCS Simulator Backend

### Task 1: Project Scaffold + Gradle Setup

**Files:**
- Create: `server/modules-simulator/rcs-simulator/build.gradle`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/RcsSimulatorApplication.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/resources/application.yml`
- Modify: `server/settings.gradle`

- [ ] **Step 1: Create build.gradle**

```groovy
plugins {
    id 'org.springframework.boot'
}

bootJar { enabled = true }

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

test { useJUnitPlatform() }
```

- [ ] **Step 2: Add module to settings.gradle**

Add this line to `server/settings.gradle` alongside the other module includes:

```groovy
include 'modules-simulator:rcs-simulator'
```

- [ ] **Step 3: Create application.yml**

```yaml
server:
  port: 8091

spring:
  application:
    name: rcs-simulator

management:
  endpoints:
    web:
      exposure:
        include: health,info

wes:
  callback-url: ${WES_CALLBACK_URL:http://localhost:8090}
  api:
    container-arrive: /api/ems/container/arrive
    container-leave: /api/ems/container/leave
    task-status-update: /api/ems/container/task/status

simulator:
  tick-interval-ms: 200
  default-robot-speed: 2.0
  loading-delay-ms: 1500
  max-robots: 20
  failure-rate-percent: 0
  layout-file: ${LAYOUT_FILE:classpath:layouts/default-layout.json}
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:8092,http://3d-viewer:8092}
```

- [ ] **Step 4: Create Spring Boot application class**

```java
package org.openwes.simulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class RcsSimulatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(RcsSimulatorApplication.class, args);
    }
}
```

- [ ] **Step 5: Verify build compiles**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add server/modules-simulator/rcs-simulator/ server/settings.gradle
git commit -m "feat(simulator): scaffold RCS simulator Gradle project"
```

---

### Task 2: Domain Models

**Files:**
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/Position.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/RobotStatus.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/VirtualRobot.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/TaskStatus.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/SimulatedTask.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/WarehouseLayout.java`

- [ ] **Step 1: Create Position**

```java
package org.openwes.simulator.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private double x;
    private double y;
    private double rotation; // degrees, 0 = facing right

    public double distanceTo(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    public Position copy() {
        return new Position(x, y, rotation);
    }
}
```

- [ ] **Step 2: Create RobotStatus enum**

```java
package org.openwes.simulator.domain;

public enum RobotStatus {
    IDLE,
    MOVING_TO_PICKUP,
    LOADING,
    MOVING_TO_DESTINATION,
    UNLOADING,
    CHARGING,
    ERROR
}
```

- [ ] **Step 3: Create VirtualRobot**

```java
package org.openwes.simulator.domain;

import lombok.Data;

@Data
public class VirtualRobot {
    private String robotCode;
    private String robotType;
    private RobotStatus status = RobotStatus.IDLE;
    private Position currentPosition;
    private String currentLocationCode;
    private String assignedTaskCode;
    private String carriedContainerCode;
    private double speed;
    private double batteryLevel = 1.0;

    public boolean isIdle() {
        return status == RobotStatus.IDLE;
    }

    public boolean isInError() {
        return status == RobotStatus.ERROR;
    }
}
```

- [ ] **Step 4: Create TaskStatus enum**

```java
package org.openwes.simulator.domain;

public enum TaskStatus {
    QUEUED,
    ASSIGNED,
    MOVING_TO_PICKUP,
    LOADING,
    MOVING_TO_DESTINATION,
    UNLOADING,
    COMPLETED,
    FAILED,
    CANCELED;

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELED;
    }
}
```

- [ ] **Step 5: Create SimulatedTask**

```java
package org.openwes.simulator.domain;

import lombok.Data;

import java.time.Instant;
import java.util.Collection;

@Data
public class SimulatedTask {
    private String taskCode;
    private String taskGroupCode;
    private String containerCode;
    private String containerFace;
    private String startLocation;
    private Collection<String> destinations;
    private int priority;
    private int groupPriority;
    private String businessTaskType;
    private String containerTaskType;
    private Long customerTaskId;

    private TaskStatus status = TaskStatus.QUEUED;
    private String assignedRobotCode;
    private Position pickupPosition;
    private Position destinationPosition;
    private Instant createdAt = Instant.now();
    private Instant completedAt;

    public boolean isActive() {
        return !status.isTerminal();
    }
}
```

- [ ] **Step 6: Create WarehouseLayout**

```java
package org.openwes.simulator.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class WarehouseLayout {

    private Warehouse warehouse;
    private List<Shelf> shelves;
    private List<Workstation> workstations;
    private List<ChargingStation> chargingStations;
    private List<RobotConfig> robots;

    // locationCode → Position lookup, built after deserialization
    private transient Map<String, Position> locationPositions;

    public void buildLocationIndex() {
        locationPositions = new ConcurrentHashMap<>();
        if (shelves != null) {
            for (Shelf shelf : shelves) {
                if (shelf.getLocationCodes() != null) {
                    for (int i = 0; i < shelf.getLocationCodes().size(); i++) {
                        String code = shelf.getLocationCodes().get(i);
                        double slotX = shelf.getX() + (i % shelf.getWidth());
                        double slotY = shelf.getY() + (i / shelf.getWidth());
                        locationPositions.put(code, new Position(slotX, slotY, 0));
                    }
                }
            }
        }
        if (workstations != null) {
            for (Workstation ws : workstations) {
                locationPositions.put(ws.getLocationCode(), new Position(ws.getX(), ws.getY(), 0));
            }
        }
        if (chargingStations != null) {
            for (ChargingStation cs : chargingStations) {
                locationPositions.put(cs.getLocationCode(), new Position(cs.getX(), cs.getY(), 0));
            }
        }
    }

    public Position getPositionForLocation(String locationCode) {
        if (locationPositions == null) {
            buildLocationIndex();
        }
        return locationPositions.get(locationCode);
    }

    @Data
    public static class Warehouse {
        private int width;
        private int height;
        private double gridSize;
    }

    @Data
    public static class Shelf {
        private String id;
        private double x;
        private double y;
        private int width;
        private int height;
        private List<String> locationCodes;
    }

    @Data
    public static class Workstation {
        private String id;
        private double x;
        private double y;
        private String type;
        private String locationCode;
    }

    @Data
    public static class ChargingStation {
        private String id;
        private double x;
        private double y;
        private String locationCode;
    }

    @Data
    public static class RobotConfig {
        private String robotCode;
        private String robotType;
        private double startX;
        private double startY;
        private double speed;
    }
}
```

- [ ] **Step 7: Verify build compiles**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/domain/
git commit -m "feat(simulator): add domain models for virtual robots, tasks, and warehouse layout"
```

---

### Task 3: Configuration + Layout Service

**Files:**
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/config/SimulatorProperties.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/service/LayoutService.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/resources/layouts/default-layout.json`
- Test: `server/modules-simulator/rcs-simulator/src/test/java/org/openwes/simulator/service/LayoutServiceTest.java`

- [ ] **Step 1: Write LayoutService test**

```java
package org.openwes.simulator.service;

import org.junit.jupiter.api.Test;
import org.openwes.simulator.domain.Position;
import org.openwes.simulator.domain.WarehouseLayout;

import static org.junit.jupiter.api.Assertions.*;

class LayoutServiceTest {

    @Test
    void loadDefaultLayout_parsesAllSections() {
        LayoutService service = new LayoutService();
        WarehouseLayout layout = service.loadFromClasspath("layouts/default-layout.json");

        assertNotNull(layout);
        assertEquals(50, layout.getWarehouse().getWidth());
        assertEquals(30, layout.getWarehouse().getHeight());
        assertFalse(layout.getShelves().isEmpty());
        assertFalse(layout.getWorkstations().isEmpty());
        assertFalse(layout.getChargingStations().isEmpty());
        assertEquals(8, layout.getRobots().size());
    }

    @Test
    void loadDefaultLayout_buildsLocationIndex() {
        LayoutService service = new LayoutService();
        WarehouseLayout layout = service.loadFromClasspath("layouts/default-layout.json");
        layout.buildLocationIndex();

        Position wsPos = layout.getPositionForLocation("WS-01");
        assertNotNull(wsPos, "Workstation WS-01 should be in location index");
        assertEquals(2.0, wsPos.getX());
        assertEquals(10.0, wsPos.getY());
    }

    @Test
    void loadDefaultLayout_robotCodesAreUnique() {
        LayoutService service = new LayoutService();
        WarehouseLayout layout = service.loadFromClasspath("layouts/default-layout.json");

        long uniqueCount = layout.getRobots().stream()
                .map(WarehouseLayout.RobotConfig::getRobotCode)
                .distinct()
                .count();
        assertEquals(layout.getRobots().size(), uniqueCount, "Robot codes must be unique");
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:test --tests "*.LayoutServiceTest"`
Expected: FAIL — LayoutService class does not exist

- [ ] **Step 3: Create SimulatorProperties**

```java
package org.openwes.simulator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "simulator")
public class SimulatorProperties {
    private int tickIntervalMs = 200;
    private double defaultRobotSpeed = 2.0;
    private int loadingDelayMs = 1500;
    private int maxRobots = 20;
    private int failureRatePercent = 0;
    private String layoutFile = "classpath:layouts/default-layout.json";
    private Cors cors = new Cors();

    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:8092,http://3d-viewer:8092";
    }
}
```

- [ ] **Step 4: Create LayoutService**

```java
package org.openwes.simulator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.openwes.simulator.domain.WarehouseLayout;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Service
public class LayoutService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private WarehouseLayout currentLayout;

    public WarehouseLayout loadFromClasspath(String path) {
        try {
            Resource resource = new ClassPathResource(path);
            return loadFromStream(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load layout from classpath: " + path, e);
        }
    }

    public WarehouseLayout loadFromFile(String path) {
        try {
            Resource resource = new FileSystemResource(path);
            return loadFromStream(resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load layout from file: " + path, e);
        }
    }

    private WarehouseLayout loadFromStream(InputStream stream) throws IOException {
        WarehouseLayout layout = objectMapper.readValue(stream, WarehouseLayout.class);
        layout.buildLocationIndex();
        return layout;
    }

    public WarehouseLayout load(String locationSpec) {
        if (locationSpec.startsWith("classpath:")) {
            currentLayout = loadFromClasspath(locationSpec.substring("classpath:".length()));
        } else {
            currentLayout = loadFromFile(locationSpec);
        }
        log.info("Loaded warehouse layout: {}x{}, {} shelves, {} workstations, {} robots",
                currentLayout.getWarehouse().getWidth(),
                currentLayout.getWarehouse().getHeight(),
                currentLayout.getShelves().size(),
                currentLayout.getWorkstations().size(),
                currentLayout.getRobots().size());
        return currentLayout;
    }

    public WarehouseLayout getCurrentLayout() {
        return currentLayout;
    }

    public void updateLayout(WarehouseLayout layout) {
        layout.buildLocationIndex();
        this.currentLayout = layout;
        log.info("Updated warehouse layout");
    }
}
```

- [ ] **Step 5: Create default-layout.json**

```json
{
  "warehouse": {
    "width": 50,
    "height": 30,
    "gridSize": 1.0
  },
  "shelves": [
    { "id": "A01", "x": 8, "y": 2, "width": 2, "height": 5, "locationCodes": ["A01-01","A01-02","A01-03","A01-04","A01-05","A01-06","A01-07","A01-08","A01-09","A01-10"] },
    { "id": "A02", "x": 12, "y": 2, "width": 2, "height": 5, "locationCodes": ["A02-01","A02-02","A02-03","A02-04","A02-05","A02-06","A02-07","A02-08","A02-09","A02-10"] },
    { "id": "A03", "x": 16, "y": 2, "width": 2, "height": 5, "locationCodes": ["A03-01","A03-02","A03-03","A03-04","A03-05","A03-06","A03-07","A03-08","A03-09","A03-10"] },
    { "id": "B01", "x": 8, "y": 18, "width": 2, "height": 5, "locationCodes": ["B01-01","B01-02","B01-03","B01-04","B01-05","B01-06","B01-07","B01-08","B01-09","B01-10"] },
    { "id": "B02", "x": 12, "y": 18, "width": 2, "height": 5, "locationCodes": ["B02-01","B02-02","B02-03","B02-04","B02-05","B02-06","B02-07","B02-08","B02-09","B02-10"] },
    { "id": "B03", "x": 16, "y": 18, "width": 2, "height": 5, "locationCodes": ["B03-01","B03-02","B03-03","B03-04","B03-05","B03-06","B03-07","B03-08","B03-09","B03-10"] },
    { "id": "C01", "x": 24, "y": 2, "width": 2, "height": 5, "locationCodes": ["C01-01","C01-02","C01-03","C01-04","C01-05","C01-06","C01-07","C01-08","C01-09","C01-10"] },
    { "id": "C02", "x": 28, "y": 2, "width": 2, "height": 5, "locationCodes": ["C02-01","C02-02","C02-03","C02-04","C02-05","C02-06","C02-07","C02-08","C02-09","C02-10"] },
    { "id": "D01", "x": 24, "y": 18, "width": 2, "height": 5, "locationCodes": ["D01-01","D01-02","D01-03","D01-04","D01-05","D01-06","D01-07","D01-08","D01-09","D01-10"] },
    { "id": "D02", "x": 28, "y": 18, "width": 2, "height": 5, "locationCodes": ["D02-01","D02-02","D02-03","D02-04","D02-05","D02-06","D02-07","D02-08","D02-09","D02-10"] }
  ],
  "workstations": [
    { "id": "WS-01", "x": 2, "y": 10, "type": "PICKING", "locationCode": "WS-01" },
    { "id": "WS-02", "x": 2, "y": 20, "type": "PICKING", "locationCode": "WS-02" },
    { "id": "WS-03", "x": 38, "y": 10, "type": "RECEIVING", "locationCode": "WS-03" },
    { "id": "WS-04", "x": 38, "y": 20, "type": "RECEIVING", "locationCode": "WS-04" }
  ],
  "chargingStations": [
    { "id": "CS-01", "x": 45, "y": 2, "locationCode": "CS-01" },
    { "id": "CS-02", "x": 45, "y": 28, "locationCode": "CS-02" }
  ],
  "robots": [
    { "robotCode": "AGV-001", "robotType": "KIVA", "startX": 10, "startY": 12, "speed": 2.0 },
    { "robotCode": "AGV-002", "robotType": "KIVA", "startX": 14, "startY": 12, "speed": 2.0 },
    { "robotCode": "AGV-003", "robotType": "KIVA", "startX": 18, "startY": 12, "speed": 1.8 },
    { "robotCode": "AGV-004", "robotType": "KIVA", "startX": 22, "startY": 12, "speed": 2.0 },
    { "robotCode": "AGV-005", "robotType": "KIVA", "startX": 26, "startY": 12, "speed": 2.2 },
    { "robotCode": "AGV-006", "robotType": "KIVA", "startX": 30, "startY": 12, "speed": 2.0 },
    { "robotCode": "FLT-001", "robotType": "FORKLIFT", "startX": 34, "startY": 10, "speed": 1.5 },
    { "robotCode": "FLT-002", "robotType": "FORKLIFT", "startX": 34, "startY": 20, "speed": 1.5 }
  ]
}
```

- [ ] **Step 6: Run tests to verify they pass**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:test --tests "*.LayoutServiceTest"`
Expected: 3 tests PASS

- [ ] **Step 7: Commit**

```bash
git add server/modules-simulator/rcs-simulator/
git commit -m "feat(simulator): add config properties, layout service, and default warehouse layout"
```

---

### Task 4: Path Service

**Files:**
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/service/PathService.java`
- Test: `server/modules-simulator/rcs-simulator/src/test/java/org/openwes/simulator/service/PathServiceTest.java`

- [ ] **Step 1: Write PathService tests**

```java
package org.openwes.simulator.service;

import org.junit.jupiter.api.Test;
import org.openwes.simulator.domain.Position;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathServiceTest {

    private final PathService pathService = new PathService();

    @Test
    void calculatePath_samePosition_returnsSinglePoint() {
        Position pos = new Position(5, 5, 0);
        List<Position> path = pathService.calculatePath(pos, pos);
        assertEquals(1, path.size());
        assertEquals(5.0, path.get(0).getX());
    }

    @Test
    void calculatePath_horizontalMove_movesXFirst() {
        Position from = new Position(0, 0, 0);
        Position to = new Position(3, 0, 0);
        List<Position> path = pathService.calculatePath(from, to);

        assertTrue(path.size() > 1);
        assertEquals(0.0, path.get(0).getX());
        assertEquals(3.0, path.get(path.size() - 1).getX());
    }

    @Test
    void calculatePath_manhattanPath_movesXThenY() {
        Position from = new Position(0, 0, 0);
        Position to = new Position(3, 4, 0);
        List<Position> path = pathService.calculatePath(from, to);

        // Should reach X=3 before changing Y
        Position lastPoint = path.get(path.size() - 1);
        assertEquals(3.0, lastPoint.getX(), 0.01);
        assertEquals(4.0, lastPoint.getY(), 0.01);
    }

    @Test
    void moveAlongPath_calculatesNewPosition() {
        Position from = new Position(0, 0, 0);
        Position to = new Position(10, 0, 0);
        List<Position> path = pathService.calculatePath(from, to);

        Position newPos = pathService.moveAlongPath(from, path, 3.0);
        assertEquals(3.0, newPos.getX(), 0.01);
        assertEquals(0.0, newPos.getY(), 0.01);
    }

    @Test
    void moveAlongPath_reachesEnd_clampsToDestination() {
        Position from = new Position(0, 0, 0);
        Position to = new Position(2, 0, 0);
        List<Position> path = pathService.calculatePath(from, to);

        Position newPos = pathService.moveAlongPath(from, path, 100.0);
        assertEquals(2.0, newPos.getX(), 0.01);
    }

    @Test
    void hasReachedDestination_atTarget_returnsTrue() {
        Position current = new Position(5.0, 5.0, 0);
        Position target = new Position(5.0, 5.0, 0);
        assertTrue(pathService.hasReached(current, target));
    }

    @Test
    void hasReachedDestination_closeEnough_returnsTrue() {
        Position current = new Position(5.05, 4.95, 0);
        Position target = new Position(5.0, 5.0, 0);
        assertTrue(pathService.hasReached(current, target));
    }

    @Test
    void hasReachedDestination_farAway_returnsFalse() {
        Position current = new Position(0, 0, 0);
        Position target = new Position(5, 5, 0);
        assertFalse(pathService.hasReached(current, target));
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:test --tests "*.PathServiceTest"`
Expected: FAIL — PathService does not exist

- [ ] **Step 3: Implement PathService**

```java
package org.openwes.simulator.service;

import org.openwes.simulator.domain.Position;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PathService {

    private static final double REACH_THRESHOLD = 0.15;

    /**
     * Calculate a Manhattan path from start to end.
     * Moves along X axis first, then Y axis.
     * Returns waypoints at 1-unit intervals.
     */
    public List<Position> calculatePath(Position from, Position to) {
        List<Position> path = new ArrayList<>();
        path.add(from.copy());

        double currentX = from.getX();
        double currentY = from.getY();
        double targetX = to.getX();
        double targetY = to.getY();

        // Move along X
        double stepX = currentX < targetX ? 1.0 : -1.0;
        while (Math.abs(currentX - targetX) > 0.01) {
            double moveX = Math.min(Math.abs(targetX - currentX), 1.0);
            currentX += stepX * moveX;
            double rotation = stepX > 0 ? 0 : 180;
            path.add(new Position(currentX, currentY, rotation));
        }

        // Move along Y
        double stepY = currentY < targetY ? 1.0 : -1.0;
        while (Math.abs(currentY - targetY) > 0.01) {
            double moveY = Math.min(Math.abs(targetY - currentY), 1.0);
            currentY += stepY * moveY;
            double rotation = stepY > 0 ? 90 : 270;
            path.add(new Position(currentX, currentY, rotation));
        }

        return path;
    }

    /**
     * Move a position along a path by a given distance.
     * Returns the new position after movement.
     */
    public Position moveAlongPath(Position current, List<Position> path, double distance) {
        // Find the current segment (closest waypoint ahead of current position)
        int currentIdx = findClosestWaypointIndex(current, path);

        double remaining = distance;
        Position pos = current.copy();

        for (int i = currentIdx; i < path.size() && remaining > 0.01; i++) {
            Position waypoint = path.get(i);
            double segmentDist = Math.abs(pos.getX() - waypoint.getX()) + Math.abs(pos.getY() - waypoint.getY());

            if (segmentDist <= remaining) {
                pos = waypoint.copy();
                remaining -= segmentDist;
            } else {
                // Partial movement toward waypoint
                double ratio = remaining / segmentDist;
                double newX = pos.getX() + (waypoint.getX() - pos.getX()) * ratio;
                double newY = pos.getY() + (waypoint.getY() - pos.getY()) * ratio;
                pos = new Position(newX, newY, waypoint.getRotation());
                remaining = 0;
            }
        }

        return pos;
    }

    public boolean hasReached(Position current, Position target) {
        double dist = Math.abs(current.getX() - target.getX()) + Math.abs(current.getY() - target.getY());
        return dist < REACH_THRESHOLD;
    }

    private int findClosestWaypointIndex(Position current, List<Position> path) {
        int bestIdx = 0;
        double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < path.size(); i++) {
            double dist = current.distanceTo(path.get(i));
            if (dist < bestDist) {
                bestDist = dist;
                bestIdx = i;
            }
        }
        // Return next waypoint (the one we're heading toward)
        return Math.min(bestIdx + 1, path.size() - 1);
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:test --tests "*.PathServiceTest"`
Expected: 8 tests PASS

- [ ] **Step 5: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/
git commit -m "feat(simulator): add Manhattan path calculation service"
```

---

### Task 5: Robot Fleet Service

**Files:**
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/service/RobotFleetService.java`
- Test: `server/modules-simulator/rcs-simulator/src/test/java/org/openwes/simulator/service/RobotFleetServiceTest.java`

- [ ] **Step 1: Write RobotFleetService tests**

```java
package org.openwes.simulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.simulator.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RobotFleetServiceTest {

    private RobotFleetService fleetService;

    @BeforeEach
    void setUp() {
        fleetService = new RobotFleetService();
        // Initialize with 3 robots
        List<WarehouseLayout.RobotConfig> configs = List.of(
                makeConfig("AGV-001", "KIVA", 0, 0, 2.0),
                makeConfig("AGV-002", "KIVA", 10, 10, 2.0),
                makeConfig("AGV-003", "KIVA", 20, 20, 1.5)
        );
        fleetService.initializeRobots(configs);
    }

    @Test
    void initializeRobots_createsAllRobots() {
        assertEquals(3, fleetService.getAllRobots().size());
        assertTrue(fleetService.getAllRobots().stream().allMatch(VirtualRobot::isIdle));
    }

    @Test
    void findNearestIdleRobot_returnsClosest() {
        Position target = new Position(1, 1, 0);
        Optional<VirtualRobot> robot = fleetService.findNearestIdleRobot(target);

        assertTrue(robot.isPresent());
        assertEquals("AGV-001", robot.get().getRobotCode()); // closest to (1,1)
    }

    @Test
    void findNearestIdleRobot_skipsBusyRobots() {
        VirtualRobot agv1 = fleetService.getRobot("AGV-001");
        agv1.setStatus(RobotStatus.MOVING_TO_PICKUP);

        Position target = new Position(1, 1, 0);
        Optional<VirtualRobot> robot = fleetService.findNearestIdleRobot(target);

        assertTrue(robot.isPresent());
        assertEquals("AGV-002", robot.get().getRobotCode());
    }

    @Test
    void findNearestIdleRobot_allBusy_returnsEmpty() {
        fleetService.getAllRobots().forEach(r -> r.setStatus(RobotStatus.MOVING_TO_PICKUP));

        Optional<VirtualRobot> robot = fleetService.findNearestIdleRobot(new Position(0, 0, 0));
        assertTrue(robot.isEmpty());
    }

    @Test
    void assignTask_updatesRobotState() {
        VirtualRobot robot = fleetService.getRobot("AGV-001");
        fleetService.assignTask(robot, "TASK-001", "C-001");

        assertEquals(RobotStatus.MOVING_TO_PICKUP, robot.getStatus());
        assertEquals("TASK-001", robot.getAssignedTaskCode());
        assertEquals("C-001", robot.getCarriedContainerCode());
    }

    @Test
    void releaseRobot_resetsToIdle() {
        VirtualRobot robot = fleetService.getRobot("AGV-001");
        fleetService.assignTask(robot, "TASK-001", "C-001");
        fleetService.releaseRobot(robot);

        assertEquals(RobotStatus.IDLE, robot.getStatus());
        assertNull(robot.getAssignedTaskCode());
        assertNull(robot.getCarriedContainerCode());
    }

    @Test
    void resetAll_resetsAllRobots() {
        fleetService.getAllRobots().forEach(r -> {
            r.setStatus(RobotStatus.ERROR);
            r.setAssignedTaskCode("SOME-TASK");
        });
        fleetService.resetAll();

        assertTrue(fleetService.getAllRobots().stream().allMatch(VirtualRobot::isIdle));
        assertTrue(fleetService.getAllRobots().stream().allMatch(r -> r.getAssignedTaskCode() == null));
    }

    private WarehouseLayout.RobotConfig makeConfig(String code, String type, double x, double y, double speed) {
        WarehouseLayout.RobotConfig config = new WarehouseLayout.RobotConfig();
        config.setRobotCode(code);
        config.setRobotType(type);
        config.setStartX(x);
        config.setStartY(y);
        config.setSpeed(speed);
        return config;
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:test --tests "*.RobotFleetServiceTest"`
Expected: FAIL

- [ ] **Step 3: Implement RobotFleetService**

```java
package org.openwes.simulator.service;

import lombok.extern.slf4j.Slf4j;
import org.openwes.simulator.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RobotFleetService {

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
            robots.put(config.getRobotCode(), robot);
            initialPositions.put(config.getRobotCode(), new Position(config.getStartX(), config.getStartY(), 0));
        }
        log.info("Initialized {} virtual robots", robots.size());
    }

    public Optional<VirtualRobot> findNearestIdleRobot(Position target) {
        return robots.values().stream()
                .filter(VirtualRobot::isIdle)
                .min(Comparator.comparingDouble(r -> r.getCurrentPosition().distanceTo(target)));
    }

    public void assignTask(VirtualRobot robot, String taskCode, String containerCode) {
        robot.setStatus(RobotStatus.MOVING_TO_PICKUP);
        robot.setAssignedTaskCode(taskCode);
        robot.setCarriedContainerCode(containerCode);
        log.info("Assigned task {} to robot {}", taskCode, robot.getRobotCode());
    }

    public void releaseRobot(VirtualRobot robot) {
        robot.setStatus(RobotStatus.IDLE);
        robot.setAssignedTaskCode(null);
        robot.setCarriedContainerCode(null);
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
            Position initial = initialPositions.get(robot.getRobotCode());
            if (initial != null) {
                robot.setCurrentPosition(initial.copy());
            }
        }
        log.info("Reset all robots to initial state");
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:test --tests "*.RobotFleetServiceTest"`
Expected: 7 tests PASS

- [ ] **Step 5: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/
git commit -m "feat(simulator): add robot fleet management service"
```

---

### Task 6: WES Callback Service

**Files:**
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/config/WesProperties.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/service/WesCallbackService.java`
- Test: `server/modules-simulator/rcs-simulator/src/test/java/org/openwes/simulator/service/WesCallbackServiceTest.java`

- [ ] **Step 1: Write WesCallbackService test**

```java
package org.openwes.simulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.simulator.config.WesProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class WesCallbackServiceTest {

    private WesCallbackService callbackService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        WesProperties props = new WesProperties();
        props.setCallbackUrl("http://localhost:8090");
        WesProperties.Api api = new WesProperties.Api();
        api.setContainerArrive("/api/ems/container/arrive");
        api.setContainerLeave("/api/ems/container/leave");
        api.setTaskStatusUpdate("/api/ems/container/task/status");
        props.setApi(api);

        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        callbackService = new WesCallbackService(props, restTemplate);
    }

    @Test
    void reportContainerArrived_sendsPostToWes() {
        mockServer.expect(requestTo("http://localhost:8090/api/ems/container/arrive"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess());

        callbackService.reportContainerArrived(
                "C-001", "WS-01", "AGV-001", "KIVA",
                "task-group-1", "WS-01", null, null);

        mockServer.verify();
    }

    @Test
    void reportTaskStatus_sendsPostToWes() {
        mockServer.expect(requestTo("http://localhost:8090/api/ems/container/task/status"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        callbackService.reportTaskStatus("TASK-001", "PROCESSING", "AGV-001", "C-001", "LOC-001");

        mockServer.verify();
    }

    @Test
    void reportContainerLeave_sendsPostToWes() {
        mockServer.expect(requestTo("http://localhost:8090/api/ems/container/leave"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        callbackService.reportContainerLeave("C-001", "WS-01", "TASK-001", null);

        mockServer.verify();
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:test --tests "*.WesCallbackServiceTest"`
Expected: FAIL

- [ ] **Step 3: Create WesProperties**

```java
package org.openwes.simulator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wes")
public class WesProperties {
    private String callbackUrl;
    private Api api = new Api();

    @Data
    public static class Api {
        private String containerArrive;
        private String containerLeave;
        private String taskStatusUpdate;
    }
}
```

- [ ] **Step 4: Implement WesCallbackService**

```java
package org.openwes.simulator.service;

import lombok.extern.slf4j.Slf4j;
import org.openwes.simulator.config.WesProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class WesCallbackService {

    private final WesProperties wesProperties;
    private final RestTemplate restTemplate;

    public WesCallbackService(WesProperties wesProperties, RestTemplate restTemplate) {
        this.wesProperties = wesProperties;
        this.restTemplate = restTemplate;
    }

    /**
     * Report container arrived at destination (matches ContainerArrivedEvent structure).
     */
    public void reportContainerArrived(String containerCode, String locationCode,
                                        String robotCode, String robotType,
                                        String groupCode, String workLocationCode,
                                        Long workStationId, Long warehouseAreaId) {
        Map<String, Object> containerDetail = new LinkedHashMap<>();
        containerDetail.put("containerCode", containerCode);
        containerDetail.put("locationCode", locationCode);
        containerDetail.put("robotCode", robotCode);
        containerDetail.put("robotType", robotType);
        containerDetail.put("groupCode", groupCode);

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("containerDetails", List.of(containerDetail));
        event.put("workLocationCode", workLocationCode);
        if (workStationId != null) {
            event.put("workStationId", workStationId);
        }
        if (warehouseAreaId != null) {
            event.put("warehouseAreaId", warehouseAreaId);
        }

        post(wesProperties.getApi().getContainerArrive(), event);
    }

    /**
     * Report task status change (matches List<UpdateContainerTaskDTO> structure).
     */
    public void reportTaskStatus(String taskCode, String status,
                                  String robotCode, String containerCode, String locationCode) {
        Map<String, Object> update = new LinkedHashMap<>();
        update.put("taskCode", taskCode);
        update.put("taskStatus", status);
        if (robotCode != null) update.put("robotCode", robotCode);
        if (containerCode != null) update.put("containerCode", containerCode);
        if (locationCode != null) update.put("locationCode", locationCode);

        post(wesProperties.getApi().getTaskStatusUpdate(), List.of(update));
    }

    /**
     * Report container leaving location (matches ContainerOperation structure).
     */
    public void reportContainerLeave(String containerCode, String locationCode,
                                      String taskCode, Long workStationId) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("containerCode", containerCode);
        detail.put("locationCode", locationCode);
        detail.put("taskCode", taskCode);
        detail.put("operationType", "LEAVE");

        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("containerOperationDetails", List.of(detail));
        if (workStationId != null) {
            operation.put("workStationId", workStationId);
        }

        post(wesProperties.getApi().getContainerLeave(), operation);
    }

    private void post(String apiPath, Object body) {
        String url = wesProperties.getCallbackUrl() + apiPath;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, entity, String.class);
            log.info("Callback sent to WES: {} {}", "POST", url);
        } catch (Exception e) {
            log.error("Failed to callback WES at {}: {}", url, e.getMessage());
        }
    }

    @org.springframework.context.annotation.Bean
    public static RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

- [ ] **Step 5: Run tests**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:test --tests "*.WesCallbackServiceTest"`
Expected: 3 tests PASS

- [ ] **Step 6: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/
git commit -m "feat(simulator): add WES callback service for container arrive/leave/status"
```

---

### Task 7: Task Execution Engine

**Files:**
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/service/TaskExecutionService.java`
- Test: `server/modules-simulator/rcs-simulator/src/test/java/org/openwes/simulator/service/TaskExecutionServiceTest.java`

- [ ] **Step 1: Write TaskExecutionService tests**

```java
package org.openwes.simulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.simulator.config.SimulatorProperties;
import org.openwes.simulator.domain.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskExecutionServiceTest {

    private TaskExecutionService executionService;
    private RobotFleetService fleetService;
    private PathService pathService;
    private WesCallbackService callbackService;
    private LayoutService layoutService;
    private SimulatorProperties properties;

    @BeforeEach
    void setUp() {
        fleetService = new RobotFleetService();
        pathService = new PathService();
        callbackService = mock(WesCallbackService.class);
        layoutService = mock(LayoutService.class);
        properties = new SimulatorProperties();
        properties.setTickIntervalMs(200);
        properties.setLoadingDelayMs(0); // instant for tests
        properties.setFailureRatePercent(0);

        executionService = new TaskExecutionService(fleetService, pathService, callbackService, layoutService, properties);

        // Setup layout with positions
        WarehouseLayout layout = new WarehouseLayout();
        WarehouseLayout.Warehouse wh = new WarehouseLayout.Warehouse();
        wh.setWidth(50);
        wh.setHeight(30);
        layout.setWarehouse(wh);
        layout.setShelves(List.of());
        layout.setWorkstations(List.of());
        layout.setChargingStations(List.of());
        layout.setRobots(List.of());
        layout.buildLocationIndex();
        // Add test positions manually
        layout.getLocationPositions().put("SHELF-01", new Position(10, 5, 0));
        layout.getLocationPositions().put("WS-01", new Position(2, 10, 0));
        when(layoutService.getCurrentLayout()).thenReturn(layout);

        // Init robots
        WarehouseLayout.RobotConfig config = new WarehouseLayout.RobotConfig();
        config.setRobotCode("AGV-001");
        config.setRobotType("KIVA");
        config.setStartX(5);
        config.setStartY(5);
        config.setSpeed(100); // fast for tests
        fleetService.initializeRobots(List.of(config));
    }

    @Test
    void submitTask_assignsRobotAndStartsExecution() {
        SimulatedTask task = makeTask("TASK-001", "C-001", "SHELF-01", "WS-01");
        executionService.submitTask(task);

        assertEquals(TaskStatus.ASSIGNED, task.getStatus());
        assertEquals("AGV-001", task.getAssignedRobotCode());
    }

    @Test
    void submitTask_noIdleRobot_queuesTask() {
        // Occupy the only robot
        VirtualRobot robot = fleetService.getRobot("AGV-001");
        robot.setStatus(RobotStatus.MOVING_TO_PICKUP);

        SimulatedTask task = makeTask("TASK-002", "C-002", "SHELF-01", "WS-01");
        executionService.submitTask(task);

        assertEquals(TaskStatus.QUEUED, task.getStatus());
    }

    @Test
    void cancelTask_setsStatusAndReleasesRobot() {
        SimulatedTask task = makeTask("TASK-001", "C-001", "SHELF-01", "WS-01");
        executionService.submitTask(task);
        executionService.cancelTask("TASK-001");

        assertEquals(TaskStatus.CANCELED, task.getStatus());
        assertTrue(fleetService.getRobot("AGV-001").isIdle());
    }

    @Test
    void tick_advancesTaskThroughStates() {
        SimulatedTask task = makeTask("TASK-001", "C-001", "SHELF-01", "WS-01");
        executionService.submitTask(task);

        // Run enough ticks to complete the full cycle (robot speed is 100, distances are small)
        for (int i = 0; i < 200; i++) {
            executionService.tick();
        }

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        verify(callbackService, atLeastOnce()).reportContainerArrived(
                eq("C-001"), anyString(), eq("AGV-001"), eq("KIVA"),
                anyString(), anyString(), any(), any());
    }

    @Test
    void getActiveTasks_returnsOnlyNonTerminal() {
        SimulatedTask task1 = makeTask("TASK-001", "C-001", "SHELF-01", "WS-01");
        SimulatedTask task2 = makeTask("TASK-002", "C-002", "SHELF-01", "WS-01");
        executionService.submitTask(task1);

        // Complete task1
        for (int i = 0; i < 200; i++) {
            executionService.tick();
        }

        // Submit task2
        executionService.submitTask(task2);

        List<SimulatedTask> active = executionService.getActiveTasks();
        assertEquals(1, active.size());
        assertEquals("TASK-002", active.get(0).getTaskCode());
    }

    private SimulatedTask makeTask(String taskCode, String containerCode, String startLoc, String destLoc) {
        SimulatedTask task = new SimulatedTask();
        task.setTaskCode(taskCode);
        task.setContainerCode(containerCode);
        task.setStartLocation(startLoc);
        task.setDestinations(List.of(destLoc));
        task.setPriority(10);
        task.setGroupPriority(10);
        task.setBusinessTaskType("PICKING");
        task.setContainerTaskType("OUTBOUND");
        return task;
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:test --tests "*.TaskExecutionServiceTest"`
Expected: FAIL

- [ ] **Step 3: Implement TaskExecutionService**

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
public class TaskExecutionService {

    private final RobotFleetService fleetService;
    private final PathService pathService;
    private final WesCallbackService callbackService;
    private final LayoutService layoutService;
    private final SimulatorProperties properties;

    private final Map<String, SimulatedTask> allTasks = new ConcurrentHashMap<>();
    private final Map<String, List<Position>> taskPaths = new ConcurrentHashMap<>();
    private final Map<String, Long> stateEnteredAt = new ConcurrentHashMap<>();
    private final Queue<SimulatedTask> taskQueue = new ConcurrentLinkedQueue<>();

    public void submitTask(SimulatedTask task) {
        allTasks.put(task.getTaskCode(), task);

        WarehouseLayout layout = layoutService.getCurrentLayout();
        Position pickupPos = layout.getPositionForLocation(task.getStartLocation());
        Position destPos = null;
        if (task.getDestinations() != null && !task.getDestinations().isEmpty()) {
            destPos = layout.getPositionForLocation(task.getDestinations().iterator().next());
        }
        task.setPickupPosition(pickupPos != null ? pickupPos : new Position(0, 0, 0));
        task.setDestinationPosition(destPos != null ? destPos : new Position(0, 0, 0));

        // Try to assign a robot immediately
        Optional<VirtualRobot> robot = fleetService.findNearestIdleRobot(task.getPickupPosition());
        if (robot.isPresent()) {
            assignRobotToTask(robot.get(), task);
        } else {
            task.setStatus(TaskStatus.QUEUED);
            taskQueue.add(task);
            log.info("Task {} queued — no idle robots", task.getTaskCode());
        }

        // Report PROCESSING status to WES
        callbackService.reportTaskStatus(task.getTaskCode(), "PROCESSING", null, task.getContainerCode(), null);
    }

    public void cancelTask(String taskCode) {
        SimulatedTask task = allTasks.get(taskCode);
        if (task == null || task.getStatus().isTerminal()) return;

        task.setStatus(TaskStatus.CANCELED);
        taskPaths.remove(taskCode);
        stateEnteredAt.remove(taskCode);

        if (task.getAssignedRobotCode() != null) {
            VirtualRobot robot = fleetService.getRobot(task.getAssignedRobotCode());
            if (robot != null) {
                fleetService.releaseRobot(robot);
            }
        }
        log.info("Task {} canceled", taskCode);
    }

    public void tick() {
        double deltaSeconds = properties.getTickIntervalMs() / 1000.0;
        long now = System.currentTimeMillis();

        for (SimulatedTask task : allTasks.values()) {
            if (task.getStatus().isTerminal()) continue;
            if (task.getStatus() == TaskStatus.QUEUED) continue;

            VirtualRobot robot = fleetService.getRobot(task.getAssignedRobotCode());
            if (robot == null || robot.isInError()) continue;

            switch (task.getStatus()) {
                case ASSIGNED:
                case MOVING_TO_PICKUP:
                    advanceMovement(task, robot, task.getPickupPosition(), deltaSeconds, TaskStatus.MOVING_TO_PICKUP, TaskStatus.LOADING, now);
                    break;
                case LOADING:
                    advanceDelay(task, now, TaskStatus.MOVING_TO_DESTINATION, RobotStatus.MOVING_TO_DESTINATION);
                    if (task.getStatus() == TaskStatus.MOVING_TO_DESTINATION) {
                        taskPaths.put(task.getTaskCode(),
                                pathService.calculatePath(robot.getCurrentPosition(), task.getDestinationPosition()));
                    }
                    break;
                case MOVING_TO_DESTINATION:
                    advanceMovement(task, robot, task.getDestinationPosition(), deltaSeconds, TaskStatus.MOVING_TO_DESTINATION, TaskStatus.UNLOADING, now);
                    break;
                case UNLOADING:
                    advanceDelay(task, now, TaskStatus.COMPLETED, null);
                    if (task.getStatus() == TaskStatus.COMPLETED) {
                        completeTask(task, robot);
                    }
                    break;
                default:
                    break;
            }

            // Random failure check
            if (properties.getFailureRatePercent() > 0 && !task.getStatus().isTerminal()) {
                if (ThreadLocalRandom.current().nextInt(100) < properties.getFailureRatePercent()) {
                    failTask(task, robot);
                }
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
                        fleetService.setError(robot);
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
        taskPaths.clear();
        stateEnteredAt.clear();
        taskQueue.clear();
    }

    private void assignRobotToTask(VirtualRobot robot, SimulatedTask task) {
        task.setStatus(TaskStatus.ASSIGNED);
        task.setAssignedRobotCode(robot.getRobotCode());
        fleetService.assignTask(robot, task.getTaskCode(), task.getContainerCode());

        // Calculate path to pickup
        taskPaths.put(task.getTaskCode(),
                pathService.calculatePath(robot.getCurrentPosition(), task.getPickupPosition()));
        stateEnteredAt.put(task.getTaskCode(), System.currentTimeMillis());
        log.info("Assigned robot {} to task {}", robot.getRobotCode(), task.getTaskCode());
    }

    private void advanceMovement(SimulatedTask task, VirtualRobot robot, Position target,
                                  double deltaSeconds, TaskStatus movingState, TaskStatus nextState, long now) {
        if (task.getStatus() != movingState) {
            task.setStatus(movingState);
            robot.setStatus(movingState == TaskStatus.MOVING_TO_PICKUP ? RobotStatus.MOVING_TO_PICKUP : RobotStatus.MOVING_TO_DESTINATION);
        }

        List<Position> path = taskPaths.get(task.getTaskCode());
        if (path == null) return;

        double distance = robot.getSpeed() * deltaSeconds;
        Position newPos = pathService.moveAlongPath(robot.getCurrentPosition(), path, distance);
        robot.setCurrentPosition(newPos);

        if (pathService.hasReached(newPos, target)) {
            robot.setCurrentPosition(target.copy());
            task.setStatus(nextState);
            robot.setStatus(nextState == TaskStatus.LOADING ? RobotStatus.LOADING : RobotStatus.UNLOADING);
            stateEnteredAt.put(task.getTaskCode(), now);
        }
    }

    private void advanceDelay(SimulatedTask task, long now, TaskStatus nextState, RobotStatus nextRobotStatus) {
        Long entered = stateEnteredAt.get(task.getTaskCode());
        if (entered != null && (now - entered) >= properties.getLoadingDelayMs()) {
            task.setStatus(nextState);
            if (nextRobotStatus != null) {
                VirtualRobot robot = fleetService.getRobot(task.getAssignedRobotCode());
                if (robot != null) {
                    robot.setStatus(nextRobotStatus);
                }
            }
            stateEnteredAt.put(task.getTaskCode(), now);
        }
    }

    private void completeTask(SimulatedTask task, VirtualRobot robot) {
        task.setCompletedAt(Instant.now());
        fleetService.releaseRobot(robot);
        taskPaths.remove(task.getTaskCode());
        stateEnteredAt.remove(task.getTaskCode());

        String destination = task.getDestinations() != null && !task.getDestinations().isEmpty()
                ? task.getDestinations().iterator().next() : "UNKNOWN";

        callbackService.reportContainerArrived(
                task.getContainerCode(), destination,
                robot.getRobotCode(), robot.getRobotType(),
                task.getTaskGroupCode() != null ? task.getTaskGroupCode() : task.getTaskCode(),
                destination, null, null);

        callbackService.reportTaskStatus(task.getTaskCode(), "WCS Succeeded",
                robot.getRobotCode(), task.getContainerCode(), destination);

        log.info("Task {} completed by robot {}", task.getTaskCode(), robot.getRobotCode());
    }

    private void failTask(SimulatedTask task, VirtualRobot robot) {
        task.setStatus(TaskStatus.FAILED);
        task.setCompletedAt(Instant.now());
        taskPaths.remove(task.getTaskCode());
        stateEnteredAt.remove(task.getTaskCode());
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
            Optional<VirtualRobot> robot = fleetService.findNearestIdleRobot(task.getPickupPosition());
            if (robot.isPresent()) {
                it.remove();
                assignRobotToTask(robot.get(), task);
            } else {
                break; // No more idle robots
            }
        }
    }
}
```

- [ ] **Step 4: Run tests**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:test --tests "*.TaskExecutionServiceTest"`
Expected: 5 tests PASS

- [ ] **Step 5: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/
git commit -m "feat(simulator): add task execution engine with state machine and scheduled tick"
```

---

### Task 8: WebSocket Push + Handler

**Files:**
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/config/WebSocketConfig.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/config/CorsConfig.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/websocket/RobotStateHandler.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/service/WebSocketPushService.java`

- [ ] **Step 1: Create WebSocketConfig**

```java
package org.openwes.simulator.config;

import lombok.RequiredArgsConstructor;
import org.openwes.simulator.websocket.RobotStateHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final RobotStateHandler robotStateHandler;
    private final SimulatorProperties properties;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String[] origins = properties.getCors().getAllowedOrigins().split(",");
        registry.addHandler(robotStateHandler, "/ws/robots")
                .setAllowedOrigins(origins);
    }
}
```

- [ ] **Step 2: Create CorsConfig**

```java
package org.openwes.simulator.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final SimulatorProperties properties;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        for (String origin : properties.getCors().getAllowedOrigins().split(",")) {
            config.addAllowedOrigin(origin.trim());
        }
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
```

- [ ] **Step 3: Create RobotStateHandler**

```java
package org.openwes.simulator.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RobotStateHandler extends TextWebSocketHandler {

    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("3D Viewer connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("3D Viewer disconnected: {}", session.getId());
    }

    public void broadcast(String json) {
        TextMessage message = new TextMessage(json);
        sessions.removeIf(s -> !s.isOpen());
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                log.warn("Failed to send to session {}: {}", session.getId(), e.getMessage());
            }
        }
    }

    public int getConnectedCount() {
        sessions.removeIf(s -> !s.isOpen());
        return sessions.size();
    }
}
```

- [ ] **Step 4: Create WebSocketPushService**

```java
package org.openwes.simulator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.simulator.config.SimulatorProperties;
import org.openwes.simulator.domain.SimulatedTask;
import org.openwes.simulator.domain.VirtualRobot;
import org.openwes.simulator.websocket.RobotStateHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketPushService {

    private final RobotFleetService fleetService;
    private final TaskExecutionService taskExecutionService;
    private final RobotStateHandler handler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelayString = "${simulator.tick-interval-ms:200}")
    public void pushState() {
        if (handler.getConnectedCount() == 0) return;

        try {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("type", "ROBOT_STATE_UPDATE");
            message.put("timestamp", System.currentTimeMillis());

            List<Map<String, Object>> robots = fleetService.getAllRobots().stream()
                    .map(this::robotToMap)
                    .collect(Collectors.toList());
            message.put("robots", robots);

            List<Map<String, Object>> tasks = taskExecutionService.getActiveTasks().stream()
                    .map(this::taskToMap)
                    .collect(Collectors.toList());
            message.put("tasks", tasks);

            handler.broadcast(objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.error("Failed to push WebSocket state: {}", e.getMessage());
        }
    }

    private Map<String, Object> robotToMap(VirtualRobot robot) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("robotCode", robot.getRobotCode());
        map.put("robotType", robot.getRobotType());
        map.put("status", robot.getStatus().name());
        map.put("x", robot.getCurrentPosition().getX());
        map.put("y", robot.getCurrentPosition().getY());
        map.put("rotation", robot.getCurrentPosition().getRotation());
        map.put("carriedContainerCode", robot.getCarriedContainerCode());
        map.put("taskCode", robot.getAssignedTaskCode());
        map.put("batteryLevel", robot.getBatteryLevel());
        return map;
    }

    private Map<String, Object> taskToMap(SimulatedTask task) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("taskCode", task.getTaskCode());
        map.put("status", task.getStatus().name());
        map.put("containerCode", task.getContainerCode());
        map.put("startLocation", task.getStartLocation());
        map.put("destination", task.getDestinations() != null && !task.getDestinations().isEmpty()
                ? task.getDestinations().iterator().next() : null);
        map.put("assignedRobot", task.getAssignedRobotCode());
        return map;
    }
}
```

- [ ] **Step 5: Verify build compiles**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:compileJava`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/
git commit -m "feat(simulator): add WebSocket push service and CORS config for 3D viewer"
```

---

### Task 9: REST Controllers

**Files:**
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/controller/TaskReceiveController.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/controller/SimulatorManagementController.java`
- Create: `server/modules-simulator/rcs-simulator/src/main/java/org/openwes/simulator/config/SimulatorInitializer.java`
- Test: `server/modules-simulator/rcs-simulator/src/test/java/org/openwes/simulator/controller/TaskReceiveControllerTest.java`

- [ ] **Step 1: Write TaskReceiveController integration test**

```java
package org.openwes.simulator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskReceiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createTask_returns200() throws Exception {
        String body = """
                {
                    "messageId": 1,
                    "data": [{
                        "customerTaskId": 100,
                        "businessTaskType": "PICKING",
                        "containerTaskType": "OUTBOUND",
                        "taskCode": "TEST-001",
                        "taskPriority": 10,
                        "taskGroupPriority": 10,
                        "containerCode": "C-001",
                        "startLocation": "A01-01",
                        "destinations": ["WS-01"]
                    }]
                }
                """;

        mockMvc.perform(post("/api/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void cancelTask_returns200() throws Exception {
        String body = """
                {
                    "messageId": 2,
                    "data": ["NONEXISTENT-TASK"]
                }
                """;

        mockMvc.perform(post("/api/tasks/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:test --tests "*.TaskReceiveControllerTest"`
Expected: FAIL

- [ ] **Step 3: Create SimulatorInitializer** (loads layout + initializes robots at startup)

```java
package org.openwes.simulator.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.simulator.domain.WarehouseLayout;
import org.openwes.simulator.service.LayoutService;
import org.openwes.simulator.service.RobotFleetService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimulatorInitializer implements CommandLineRunner {

    private final SimulatorProperties properties;
    private final LayoutService layoutService;
    private final RobotFleetService fleetService;

    @Override
    public void run(String... args) {
        WarehouseLayout layout = layoutService.load(properties.getLayoutFile());
        fleetService.initializeRobots(layout.getRobots());
        log.info("RCS Simulator initialized — {} robots ready", layout.getRobots().size());
    }
}
```

- [ ] **Step 4: Create TaskReceiveController**

```java
package org.openwes.simulator.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.simulator.domain.SimulatedTask;
import org.openwes.simulator.service.TaskExecutionService;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskReceiveController {

    private final TaskExecutionService taskExecutionService;

    @PostMapping("/create")
    public Map<String, String> createTasks(@RequestBody CallbackMessage<List<CreateTaskPayload>> message) {
        log.info("Received {} container tasks from WES", message.getData().size());
        for (CreateTaskPayload payload : message.getData()) {
            SimulatedTask task = new SimulatedTask();
            task.setTaskCode(payload.getTaskCode());
            task.setTaskGroupCode(payload.getTaskGroupCode());
            task.setContainerCode(payload.getContainerCode());
            task.setContainerFace(payload.getContainerFace());
            task.setStartLocation(payload.getStartLocation());
            task.setDestinations(payload.getDestinations());
            task.setPriority(payload.getTaskPriority() != null ? payload.getTaskPriority() : 0);
            task.setGroupPriority(payload.getTaskGroupPriority() != null ? payload.getTaskGroupPriority() : 0);
            task.setBusinessTaskType(payload.getBusinessTaskType());
            task.setContainerTaskType(payload.getContainerTaskType());
            task.setCustomerTaskId(payload.getCustomerTaskId());

            if (task.getTaskCode() == null) {
                task.setTaskCode("SIM-" + System.currentTimeMillis() + "-" + payload.getCustomerTaskId());
            }

            taskExecutionService.submitTask(task);
        }
        return Map.of("status", "ok");
    }

    @PostMapping("/cancel")
    public Map<String, String> cancelTasks(@RequestBody CallbackMessage<List<String>> message) {
        log.info("Cancel request for tasks: {}", message.getData());
        for (String taskCode : message.getData()) {
            taskExecutionService.cancelTask(taskCode);
        }
        return Map.of("status", "ok");
    }

    @PostMapping("/improve-priority")
    public Map<String, String> improvePriority(@RequestBody CallbackMessage<Map<String, Object>> message) {
        log.info("Priority improvement request: {}", message.getData());
        // For demo: log and acknowledge, no complex re-ordering
        return Map.of("status", "ok");
    }

    @PostMapping("/release")
    public Map<String, String> releaseTasks(@RequestBody CallbackMessage<List<String>> message) {
        log.info("Release request for tasks: {}", message.getData());
        return Map.of("status", "ok");
    }

    @PostMapping("/container-leave")
    public Map<String, String> containerLeave(@RequestBody CallbackMessage<Object> message) {
        log.info("Container leave notification: {}", message.getData());
        return Map.of("status", "ok");
    }

    @PostMapping("/call-robot")
    public Map<String, String> callRobot(@RequestBody CallbackMessage<Object> message) {
        log.info("Call robot request: {}", message.getData());
        return Map.of("status", "ok");
    }

    @Data
    public static class CallbackMessage<T> {
        private Long messageId;
        private T data;
    }

    @Data
    public static class CreateTaskPayload {
        private Long customerTaskId;
        private String businessTaskType;
        private String containerTaskType;
        private String taskCode;
        private String taskGroupCode;
        private Integer taskPriority;
        private Integer taskGroupPriority;
        private String containerCode;
        private String containerFace;
        private String containerSpecCode;
        private String startLocation;
        private Collection<String> destinations;
    }
}
```

- [ ] **Step 5: Create SimulatorManagementController**

```java
package org.openwes.simulator.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.openwes.simulator.config.SimulatorProperties;
import org.openwes.simulator.domain.SimulatedTask;
import org.openwes.simulator.domain.VirtualRobot;
import org.openwes.simulator.domain.WarehouseLayout;
import org.openwes.simulator.service.LayoutService;
import org.openwes.simulator.service.RobotFleetService;
import org.openwes.simulator.service.TaskExecutionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
@RequiredArgsConstructor
public class SimulatorManagementController {

    private final RobotFleetService fleetService;
    private final TaskExecutionService taskExecutionService;
    private final LayoutService layoutService;
    private final SimulatorProperties properties;

    @GetMapping("/robots")
    public List<VirtualRobot> listRobots() {
        return fleetService.getAllRobots();
    }

    @GetMapping("/tasks")
    public List<SimulatedTask> listTasks() {
        return taskExecutionService.getAllTasks();
    }

    @GetMapping("/layout")
    public WarehouseLayout getLayout() {
        return layoutService.getCurrentLayout();
    }

    @PutMapping("/layout")
    public Map<String, String> updateLayout(@RequestBody WarehouseLayout layout) {
        layoutService.updateLayout(layout);
        fleetService.initializeRobots(layout.getRobots());
        return Map.of("status", "ok");
    }

    @PostMapping("/reset")
    public Map<String, String> reset() {
        fleetService.resetAll();
        taskExecutionService.reset();
        return Map.of("status", "ok");
    }

    @GetMapping("/config")
    public SimulatorConfigDTO getConfig() {
        SimulatorConfigDTO dto = new SimulatorConfigDTO();
        dto.setTickIntervalMs(properties.getTickIntervalMs());
        dto.setDefaultRobotSpeed(properties.getDefaultRobotSpeed());
        dto.setLoadingDelayMs(properties.getLoadingDelayMs());
        dto.setFailureRatePercent(properties.getFailureRatePercent());
        return dto;
    }

    @PutMapping("/config")
    public Map<String, String> updateConfig(@RequestBody SimulatorConfigDTO config) {
        if (config.getTickIntervalMs() != null) properties.setTickIntervalMs(config.getTickIntervalMs());
        if (config.getDefaultRobotSpeed() != null) properties.setDefaultRobotSpeed(config.getDefaultRobotSpeed());
        if (config.getLoadingDelayMs() != null) properties.setLoadingDelayMs(config.getLoadingDelayMs());
        if (config.getFailureRatePercent() != null) properties.setFailureRatePercent(config.getFailureRatePercent());
        return Map.of("status", "ok");
    }

    @PostMapping("/robots/{robotCode}/error")
    public Map<String, String> injectError(@PathVariable String robotCode) {
        VirtualRobot robot = fleetService.getRobot(robotCode);
        if (robot == null) {
            return Map.of("status", "error", "message", "Robot not found: " + robotCode);
        }
        taskExecutionService.failTaskForRobot(robotCode);
        fleetService.setError(robot);
        return Map.of("status", "ok");
    }

    @PostMapping("/robots/{robotCode}/recover")
    public Map<String, String> recoverRobot(@PathVariable String robotCode) {
        VirtualRobot robot = fleetService.getRobot(robotCode);
        if (robot == null) {
            return Map.of("status", "error", "message", "Robot not found: " + robotCode);
        }
        fleetService.recoverFromError(robot);
        return Map.of("status", "ok");
    }

    @PostMapping("/layout/validate")
    public Map<String, Object> validateLayout() {
        WarehouseLayout layout = layoutService.getCurrentLayout();
        List<String> allLocations = new java.util.ArrayList<>();
        if (layout.getShelves() != null) {
            layout.getShelves().forEach(s -> {
                if (s.getLocationCodes() != null) allLocations.addAll(s.getLocationCodes());
            });
        }
        if (layout.getWorkstations() != null) {
            layout.getWorkstations().forEach(ws -> allLocations.add(ws.getLocationCode()));
        }
        return Map.of(
                "totalLocations", allLocations.size(),
                "locations", allLocations,
                "message", "Locations listed. Cross-reference with WES location master data manually."
        );
    }

    @Data
    public static class SimulatorConfigDTO {
        private Integer tickIntervalMs;
        private Double defaultRobotSpeed;
        private Integer loadingDelayMs;
        private Integer failureRatePercent;
    }
}
```

- [ ] **Step 6: Run tests**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:test --tests "*.TaskReceiveControllerTest"`
Expected: 2 tests PASS

- [ ] **Step 7: Commit**

```bash
git add server/modules-simulator/rcs-simulator/src/
git commit -m "feat(simulator): add REST controllers for WES callbacks and simulator management"
```

---

### Task 10: Simulator Dockerfile

**Files:**
- Create: `server/modules-simulator/rcs-simulator/Dockerfile`

- [ ] **Step 1: Create Dockerfile**

```dockerfile
FROM gradle:8.5-jdk17 AS builder
WORKDIR /app
COPY server/ .
RUN ./gradlew :modules-simulator:rcs-simulator:bootJar -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/modules-simulator/rcs-simulator/build/libs/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
```

- [ ] **Step 2: Verify bootJar builds**

Run: `cd server && ./gradlew :modules-simulator:rcs-simulator:bootJar`
Expected: BUILD SUCCESSFUL, JAR created in `modules-simulator/rcs-simulator/build/libs/`

- [ ] **Step 3: Commit**

```bash
git add server/modules-simulator/rcs-simulator/Dockerfile
git commit -m "feat(simulator): add Dockerfile for RCS simulator"
```

---

## Phase 2: 3D Warehouse Viewer

### Task 11: Project Scaffold

**Files:**
- Create: `3d-viewer/package.json`
- Create: `3d-viewer/tsconfig.json`
- Create: `3d-viewer/vite.config.ts`
- Create: `3d-viewer/tailwind.config.js`
- Create: `3d-viewer/postcss.config.js`
- Create: `3d-viewer/index.html`
- Create: `3d-viewer/src/main.tsx`
- Create: `3d-viewer/src/index.css`

- [ ] **Step 1: Create package.json**

```json
{
  "name": "open-wes-3d-viewer",
  "private": true,
  "version": "0.1.0",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "@react-three/drei": "^9.105.0",
    "@react-three/fiber": "^8.16.0",
    "react": "^18.3.0",
    "react-dom": "^18.3.0",
    "three": "^0.164.0",
    "zustand": "^4.5.0"
  },
  "devDependencies": {
    "@types/react": "^18.3.0",
    "@types/react-dom": "^18.3.0",
    "@types/three": "^0.164.0",
    "@vitejs/plugin-react": "^4.3.0",
    "autoprefixer": "^10.4.0",
    "postcss": "^8.4.0",
    "tailwindcss": "^3.4.0",
    "typescript": "^5.4.0",
    "vite": "^5.4.0"
  }
}
```

- [ ] **Step 2: Create tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  },
  "include": ["src"]
}
```

- [ ] **Step 3: Create vite.config.ts**

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 8092,
  },
})
```

- [ ] **Step 4: Create tailwind.config.js**

```javascript
/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {},
  },
  plugins: [],
}
```

- [ ] **Step 5: Create postcss.config.js**

```javascript
export default {
  plugins: {
    tailwindcss: {},
    autoprefixer: {},
  },
}
```

- [ ] **Step 6: Create index.html**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>Open WES - 3D Warehouse Viewer</title>
</head>
<body>
  <div id="root" style="width: 100vw; height: 100vh;"></div>
  <script type="module" src="/src/main.tsx"></script>
</body>
</html>
```

- [ ] **Step 7: Create src/index.css**

```css
@tailwind base;
@tailwind components;
@tailwind utilities;

body {
  margin: 0;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}
```

- [ ] **Step 8: Create src/main.tsx**

```tsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import App from './App'
import './index.css'

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
)
```

- [ ] **Step 9: Install dependencies and verify build**

Run: `cd 3d-viewer && npm install && npx tsc --noEmit`
Expected: No TypeScript errors (App.tsx doesn't exist yet, so create a placeholder first)

- [ ] **Step 10: Commit**

```bash
git add 3d-viewer/
git commit -m "feat(3d-viewer): scaffold React 18 + R3F + Tailwind project"
```

---

### Task 12: TypeScript Types + Zustand Store + WebSocket Hook

**Files:**
- Create: `3d-viewer/src/types/index.ts`
- Create: `3d-viewer/src/stores/simulatorStore.ts`
- Create: `3d-viewer/src/hooks/useRobotWebSocket.ts`
- Create: `3d-viewer/src/hooks/useSimulatorApi.ts`

- [ ] **Step 1: Create types**

```typescript
// 3d-viewer/src/types/index.ts

export interface RobotState {
  robotCode: string
  robotType: string
  status: RobotStatusType
  x: number
  y: number
  rotation: number
  carriedContainerCode: string | null
  taskCode: string | null
  batteryLevel: number
}

export type RobotStatusType =
  | 'IDLE'
  | 'MOVING_TO_PICKUP'
  | 'LOADING'
  | 'MOVING_TO_DESTINATION'
  | 'UNLOADING'
  | 'CHARGING'
  | 'ERROR'

export interface TaskState {
  taskCode: string
  status: string
  containerCode: string
  startLocation: string
  destination: string | null
  assignedRobot: string | null
}

export interface WebSocketMessage {
  type: 'ROBOT_STATE_UPDATE'
  timestamp: number
  robots: RobotState[]
  tasks: TaskState[]
}

export interface WarehouseConfig {
  width: number
  height: number
  gridSize: number
}

export interface ShelfConfig {
  id: string
  x: number
  y: number
  width: number
  height: number
  locationCodes: string[]
}

export interface WorkstationConfig {
  id: string
  x: number
  y: number
  type: string
  locationCode: string
}

export interface ChargingStationConfig {
  id: string
  x: number
  y: number
  locationCode: string
}

export interface RobotConfig {
  robotCode: string
  robotType: string
  startX: number
  startY: number
  speed: number
}

export interface WarehouseLayout {
  warehouse: WarehouseConfig
  shelves: ShelfConfig[]
  workstations: WorkstationConfig[]
  chargingStations: ChargingStationConfig[]
  robots: RobotConfig[]
}

export type ConnectionStatus = 'connected' | 'connecting' | 'disconnected'

export type CameraMode = 'perspective' | 'topdown' | 'follow'
```

- [ ] **Step 2: Create Zustand store**

```typescript
// 3d-viewer/src/stores/simulatorStore.ts

import { create } from 'zustand'
import type { RobotState, TaskState, WarehouseLayout, ConnectionStatus, CameraMode } from '@/types'

interface SimulatorState {
  // Data
  robots: RobotState[]
  previousRobots: RobotState[]
  tasks: TaskState[]
  completedTasks: TaskState[]
  layout: WarehouseLayout | null
  lastUpdateTime: number

  // UI state
  connectionStatus: ConnectionStatus
  selectedRobotCode: string | null
  cameraMode: CameraMode
  simulationSpeed: number

  // Actions
  updateRobotState: (robots: RobotState[], tasks: TaskState[]) => void
  setLayout: (layout: WarehouseLayout) => void
  setConnectionStatus: (status: ConnectionStatus) => void
  selectRobot: (robotCode: string | null) => void
  setCameraMode: (mode: CameraMode) => void
  setSimulationSpeed: (speed: number) => void
}

const MAX_COMPLETED_TASKS = 20

export const useSimulatorStore = create<SimulatorState>((set, get) => ({
  robots: [],
  previousRobots: [],
  tasks: [],
  completedTasks: [],
  layout: null,
  lastUpdateTime: 0,

  connectionStatus: 'disconnected',
  selectedRobotCode: null,
  cameraMode: 'perspective',
  simulationSpeed: 1.0,

  updateRobotState: (robots, tasks) => {
    const state = get()
    // Track completed tasks
    const activeCodes = new Set(tasks.map(t => t.taskCode))
    const newlyCompleted = state.tasks.filter(t => !activeCodes.has(t.taskCode))

    set({
      previousRobots: state.robots,
      robots,
      tasks,
      completedTasks: [...newlyCompleted, ...state.completedTasks].slice(0, MAX_COMPLETED_TASKS),
      lastUpdateTime: Date.now(),
    })
  },

  setLayout: (layout) => set({ layout }),
  setConnectionStatus: (status) => set({ connectionStatus: status }),
  selectRobot: (robotCode) => set({ selectedRobotCode: robotCode }),
  setCameraMode: (mode) => set({ cameraMode: mode }),
  setSimulationSpeed: (speed) => set({ simulationSpeed: speed }),
}))
```

- [ ] **Step 3: Create WebSocket hook with reconnection**

```typescript
// 3d-viewer/src/hooks/useRobotWebSocket.ts

import { useEffect, useRef, useCallback } from 'react'
import { useSimulatorStore } from '@/stores/simulatorStore'
import type { WebSocketMessage } from '@/types'

const BASE_DELAY = 1000
const MAX_DELAY = 30000

export function useRobotWebSocket(url: string) {
  const wsRef = useRef<WebSocket | null>(null)
  const retryCountRef = useRef(0)
  const retryTimerRef = useRef<number>()
  const updateRobotState = useSimulatorStore(s => s.updateRobotState)
  const setConnectionStatus = useSimulatorStore(s => s.setConnectionStatus)

  const connect = useCallback(() => {
    setConnectionStatus('connecting')
    const ws = new WebSocket(url)

    ws.onopen = () => {
      retryCountRef.current = 0
      setConnectionStatus('connected')
    }

    ws.onmessage = (event) => {
      try {
        const msg: WebSocketMessage = JSON.parse(event.data)
        if (msg.type === 'ROBOT_STATE_UPDATE') {
          updateRobotState(msg.robots, msg.tasks)
        }
      } catch {
        // ignore parse errors
      }
    }

    ws.onclose = () => {
      setConnectionStatus('disconnected')
      scheduleReconnect()
    }

    ws.onerror = () => {
      ws.close()
    }

    wsRef.current = ws
  }, [url, updateRobotState, setConnectionStatus])

  const scheduleReconnect = useCallback(() => {
    const delay = Math.min(BASE_DELAY * Math.pow(2, retryCountRef.current), MAX_DELAY)
    retryCountRef.current++
    retryTimerRef.current = window.setTimeout(connect, delay)
  }, [connect])

  useEffect(() => {
    connect()
    return () => {
      if (retryTimerRef.current) clearTimeout(retryTimerRef.current)
      wsRef.current?.close()
    }
  }, [connect])
}
```

- [ ] **Step 4: Create simulator API hook**

```typescript
// 3d-viewer/src/hooks/useSimulatorApi.ts

import { useCallback } from 'react'
import { useSimulatorStore } from '@/stores/simulatorStore'
import type { WarehouseLayout } from '@/types'

const API_BASE = import.meta.env.VITE_SIMULATOR_API_URL || 'http://localhost:8091'

async function apiFetch<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })
  return res.json()
}

export function useSimulatorApi() {
  const setLayout = useSimulatorStore(s => s.setLayout)

  const fetchLayout = useCallback(async () => {
    const layout = await apiFetch<WarehouseLayout>('/api/simulator/layout')
    setLayout(layout)
    return layout
  }, [setLayout])

  const resetSimulator = useCallback(async () => {
    await apiFetch('/api/simulator/reset', { method: 'POST' })
  }, [])

  const updateConfig = useCallback(async (config: Record<string, unknown>) => {
    await apiFetch('/api/simulator/config', {
      method: 'PUT',
      body: JSON.stringify(config),
    })
  }, [])

  const injectError = useCallback(async (robotCode: string) => {
    await apiFetch(`/api/simulator/robots/${robotCode}/error`, { method: 'POST' })
  }, [])

  const recoverRobot = useCallback(async (robotCode: string) => {
    await apiFetch(`/api/simulator/robots/${robotCode}/recover`, { method: 'POST' })
  }, [])

  return { fetchLayout, resetSimulator, updateConfig, injectError, recoverRobot }
}
```

- [ ] **Step 5: Verify TypeScript compiles**

Run: `cd 3d-viewer && npx tsc --noEmit`
Expected: No errors (may need placeholder App.tsx — create empty `export default function App() { return null }` if needed)

- [ ] **Step 6: Commit**

```bash
git add 3d-viewer/src/
git commit -m "feat(3d-viewer): add TypeScript types, Zustand store, and WebSocket/API hooks"
```

---

### Task 13: 3D Scene Components

**Files:**
- Create: `3d-viewer/src/components/Scene/Floor.tsx`
- Create: `3d-viewer/src/components/Scene/Shelf.tsx`
- Create: `3d-viewer/src/components/Scene/Robot.tsx`
- Create: `3d-viewer/src/components/Scene/Workstation.tsx`
- Create: `3d-viewer/src/components/Scene/CameraController.tsx`
- Create: `3d-viewer/src/components/Scene/WarehouseScene.tsx`

- [ ] **Step 1: Create Floor component**

```tsx
// 3d-viewer/src/components/Scene/Floor.tsx

import { Grid } from '@react-three/drei'

interface FloorProps {
  width: number
  height: number
}

export function Floor({ width, height }: FloorProps) {
  return (
    <group position={[width / 2, 0, height / 2]}>
      {/* Ground plane */}
      <mesh rotation={[-Math.PI / 2, 0, 0]} receiveShadow>
        <planeGeometry args={[width, height]} />
        <meshStandardMaterial color="#e8e8e8" />
      </mesh>
      {/* Grid overlay */}
      <Grid
        args={[width, height]}
        cellSize={1}
        cellThickness={0.5}
        cellColor="#d0d0d0"
        sectionSize={5}
        sectionThickness={1}
        sectionColor="#b0b0b0"
        fadeDistance={100}
        position={[0, 0.01, 0]}
      />
    </group>
  )
}
```

- [ ] **Step 2: Create Shelf component**

```tsx
// 3d-viewer/src/components/Scene/Shelf.tsx

import { Text } from '@react-three/drei'
import type { ShelfConfig } from '@/types'

interface ShelfProps {
  config: ShelfConfig
}

const SHELF_HEIGHT = 2.5
const SHELF_COLOR = '#5b7fa5'

export function Shelf({ config }: ShelfProps) {
  return (
    <group position={[config.x + config.width / 2, SHELF_HEIGHT / 2, config.y + config.height / 2]}>
      <mesh castShadow>
        <boxGeometry args={[config.width, SHELF_HEIGHT, config.height]} />
        <meshStandardMaterial color={SHELF_COLOR} opacity={0.85} transparent />
      </mesh>
      <Text
        position={[0, SHELF_HEIGHT / 2 + 0.3, 0]}
        rotation={[-Math.PI / 2, 0, 0]}
        fontSize={0.5}
        color="#333"
        anchorX="center"
        anchorY="middle"
      >
        {config.id}
      </Text>
    </group>
  )
}
```

- [ ] **Step 3: Create Robot component with lerp interpolation**

```tsx
// 3d-viewer/src/components/Scene/Robot.tsx

import { useRef } from 'react'
import { useFrame } from '@react-three/fiber'
import { Text } from '@react-three/drei'
import * as THREE from 'three'
import type { RobotState, RobotStatusType } from '@/types'

interface RobotProps {
  robot: RobotState
  isSelected: boolean
  onClick: () => void
}

const STATUS_COLORS: Record<RobotStatusType, string> = {
  IDLE: '#4a90d9',
  MOVING_TO_PICKUP: '#27ae60',
  LOADING: '#f39c12',
  MOVING_TO_DESTINATION: '#27ae60',
  UNLOADING: '#f39c12',
  CHARGING: '#95a5a6',
  ERROR: '#e74c3c',
}

const ROBOT_Y = 0.25
const LERP_FACTOR = 0.15

export function Robot({ robot, isSelected, onClick }: RobotProps) {
  const groupRef = useRef<THREE.Group>(null)

  useFrame(() => {
    if (!groupRef.current) return
    const targetX = robot.x
    const targetZ = robot.y
    groupRef.current.position.x = THREE.MathUtils.lerp(groupRef.current.position.x, targetX, LERP_FACTOR)
    groupRef.current.position.z = THREE.MathUtils.lerp(groupRef.current.position.z, targetZ, LERP_FACTOR)

    const targetRotation = (-robot.rotation * Math.PI) / 180
    groupRef.current.rotation.y = THREE.MathUtils.lerp(groupRef.current.rotation.y, targetRotation, LERP_FACTOR)
  })

  const color = STATUS_COLORS[robot.status]
  const isForklift = robot.robotType === 'FORKLIFT'
  const bodyWidth = isForklift ? 0.8 : 0.6
  const bodyDepth = isForklift ? 1.0 : 0.6

  return (
    <group ref={groupRef} position={[robot.x, ROBOT_Y, robot.y]} onClick={onClick}>
      {/* Robot body */}
      <mesh castShadow>
        <boxGeometry args={[bodyWidth, 0.3, bodyDepth]} />
        <meshStandardMaterial color={color} />
      </mesh>

      {/* Direction indicator */}
      <mesh position={[0, 0.2, -bodyDepth / 2 + 0.1]}>
        <coneGeometry args={[0.1, 0.2, 4]} />
        <meshStandardMaterial color="#fff" />
      </mesh>

      {/* Status LED */}
      <mesh position={[0, 0.25, 0]}>
        <sphereGeometry args={[0.06]} />
        <meshStandardMaterial color={color} emissive={color} emissiveIntensity={0.8} />
      </mesh>

      {/* Selection ring */}
      {isSelected && (
        <mesh position={[0, 0.01, 0]} rotation={[-Math.PI / 2, 0, 0]}>
          <ringGeometry args={[0.5, 0.6, 32]} />
          <meshBasicMaterial color="#fff200" />
        </mesh>
      )}

      {/* Carried container */}
      {robot.carriedContainerCode && (
        <mesh position={[0, 0.35, 0]} castShadow>
          <boxGeometry args={[0.5, 0.3, 0.5]} />
          <meshStandardMaterial color="#e67e22" />
        </mesh>
      )}

      {/* Label */}
      <Text
        position={[0, 0.6, 0]}
        fontSize={0.2}
        color="#333"
        anchorX="center"
        anchorY="bottom"
      >
        {robot.robotCode}
      </Text>
    </group>
  )
}
```

- [ ] **Step 4: Create Workstation component**

```tsx
// 3d-viewer/src/components/Scene/Workstation.tsx

import { Text } from '@react-three/drei'
import type { WorkstationConfig } from '@/types'

interface WorkstationProps {
  config: WorkstationConfig
}

const WS_COLORS: Record<string, string> = {
  PICKING: '#2ecc71',
  RECEIVING: '#3498db',
}

export function Workstation({ config }: WorkstationProps) {
  const color = WS_COLORS[config.type] || '#9b59b6'

  return (
    <group position={[config.x, 0.4, config.y]}>
      {/* Workstation desk */}
      <mesh castShadow>
        <boxGeometry args={[1.5, 0.8, 1.5]} />
        <meshStandardMaterial color={color} opacity={0.9} transparent />
      </mesh>
      {/* Label */}
      <Text
        position={[0, 1.0, 0]}
        fontSize={0.3}
        color="#333"
        anchorX="center"
        anchorY="bottom"
      >
        {config.id}
      </Text>
      <Text
        position={[0, 0.7, 0]}
        fontSize={0.18}
        color="#666"
        anchorX="center"
        anchorY="bottom"
      >
        {config.type}
      </Text>
    </group>
  )
}
```

- [ ] **Step 5: Create CameraController**

```tsx
// 3d-viewer/src/components/Scene/CameraController.tsx

import { useRef, useEffect } from 'react'
import { useThree, useFrame } from '@react-three/fiber'
import { OrbitControls } from '@react-three/drei'
import * as THREE from 'three'
import { useSimulatorStore } from '@/stores/simulatorStore'

export function CameraController() {
  const controlsRef = useRef<any>(null)
  const { camera } = useThree()
  const cameraMode = useSimulatorStore(s => s.cameraMode)
  const selectedRobotCode = useSimulatorStore(s => s.selectedRobotCode)
  const robots = useSimulatorStore(s => s.robots)
  const layout = useSimulatorStore(s => s.layout)

  // Set initial camera position
  useEffect(() => {
    if (!layout) return
    const w = layout.warehouse.width
    const h = layout.warehouse.height
    if (cameraMode === 'topdown') {
      camera.position.set(w / 2, Math.max(w, h), h / 2)
      camera.lookAt(w / 2, 0, h / 2)
    } else {
      camera.position.set(w * 0.8, w * 0.5, h * 0.8)
      camera.lookAt(w / 2, 0, h / 2)
    }
  }, [cameraMode, layout, camera])

  // Follow selected robot
  useFrame(() => {
    if (cameraMode !== 'follow' || !selectedRobotCode) return
    const robot = robots.find(r => r.robotCode === selectedRobotCode)
    if (!robot || !controlsRef.current) return

    const target = new THREE.Vector3(robot.x, 0, robot.y)
    controlsRef.current.target.lerp(target, 0.1)
  })

  return <OrbitControls ref={controlsRef} enableDamping dampingFactor={0.1} />
}
```

- [ ] **Step 6: Create WarehouseScene**

```tsx
// 3d-viewer/src/components/Scene/WarehouseScene.tsx

import { Canvas } from '@react-three/fiber'
import { useSimulatorStore } from '@/stores/simulatorStore'
import { Floor } from './Floor'
import { Shelf } from './Shelf'
import { Robot } from './Robot'
import { Workstation } from './Workstation'
import { CameraController } from './CameraController'

export function WarehouseScene() {
  const layout = useSimulatorStore(s => s.layout)
  const robots = useSimulatorStore(s => s.robots)
  const selectedRobotCode = useSimulatorStore(s => s.selectedRobotCode)
  const selectRobot = useSimulatorStore(s => s.selectRobot)

  return (
    <Canvas shadows camera={{ fov: 50, near: 0.1, far: 500 }}>
      <ambientLight intensity={0.6} />
      <directionalLight position={[30, 40, 20]} intensity={0.8} castShadow />

      <CameraController />

      {layout && (
        <>
          <Floor width={layout.warehouse.width} height={layout.warehouse.height} />
          {layout.shelves.map(shelf => (
            <Shelf key={shelf.id} config={shelf} />
          ))}
          {layout.workstations.map(ws => (
            <Workstation key={ws.id} config={ws} />
          ))}
        </>
      )}

      {robots.map(robot => (
        <Robot
          key={robot.robotCode}
          robot={robot}
          isSelected={robot.robotCode === selectedRobotCode}
          onClick={() => selectRobot(robot.robotCode === selectedRobotCode ? null : robot.robotCode)}
        />
      ))}
    </Canvas>
  )
}
```

- [ ] **Step 7: Verify TypeScript compiles**

Run: `cd 3d-viewer && npx tsc --noEmit`
Expected: No errors

- [ ] **Step 8: Commit**

```bash
git add 3d-viewer/src/components/Scene/
git commit -m "feat(3d-viewer): add 3D scene components — floor, shelves, robots, workstations, camera"
```

---

### Task 14: UI Panels + App Shell

**Files:**
- Create: `3d-viewer/src/components/Panels/ConnectionStatus.tsx`
- Create: `3d-viewer/src/components/Panels/TaskPanel.tsx`
- Create: `3d-viewer/src/components/Panels/RobotPanel.tsx`
- Create: `3d-viewer/src/components/Panels/Controls.tsx`
- Create: `3d-viewer/src/App.tsx`

- [ ] **Step 1: Create ConnectionStatus**

```tsx
// 3d-viewer/src/components/Panels/ConnectionStatus.tsx

import { useSimulatorStore } from '@/stores/simulatorStore'
import type { ConnectionStatus as StatusType } from '@/types'

const STATUS_CONFIG: Record<StatusType, { label: string; color: string; bg: string }> = {
  connected: { label: 'Connected', color: 'text-green-700', bg: 'bg-green-100' },
  connecting: { label: 'Reconnecting...', color: 'text-yellow-700', bg: 'bg-yellow-100' },
  disconnected: { label: 'Disconnected', color: 'text-red-700', bg: 'bg-red-100' },
}

export function ConnectionStatus() {
  const status = useSimulatorStore(s => s.connectionStatus)
  const config = STATUS_CONFIG[status]

  return (
    <div className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium ${config.color} ${config.bg}`}>
      <span className={`w-2 h-2 rounded-full ${status === 'connected' ? 'bg-green-500' : status === 'connecting' ? 'bg-yellow-500 animate-pulse' : 'bg-red-500'}`} />
      {config.label}
    </div>
  )
}
```

- [ ] **Step 2: Create TaskPanel**

```tsx
// 3d-viewer/src/components/Panels/TaskPanel.tsx

import { useSimulatorStore } from '@/stores/simulatorStore'

const STATUS_BADGES: Record<string, string> = {
  QUEUED: 'bg-gray-200 text-gray-800',
  ASSIGNED: 'bg-blue-100 text-blue-800',
  MOVING_TO_PICKUP: 'bg-green-100 text-green-800',
  LOADING: 'bg-yellow-100 text-yellow-800',
  MOVING_TO_DESTINATION: 'bg-green-100 text-green-800',
  UNLOADING: 'bg-yellow-100 text-yellow-800',
  COMPLETED: 'bg-emerald-100 text-emerald-800',
  FAILED: 'bg-red-100 text-red-800',
  CANCELED: 'bg-gray-100 text-gray-500',
}

export function TaskPanel() {
  const tasks = useSimulatorStore(s => s.tasks)
  const completedTasks = useSimulatorStore(s => s.completedTasks)

  return (
    <div className="absolute right-0 top-12 bottom-0 w-72 bg-white/90 backdrop-blur border-l border-gray-200 overflow-y-auto p-3">
      <h3 className="text-sm font-semibold text-gray-700 mb-2">Active Tasks ({tasks.length})</h3>
      {tasks.length === 0 && <p className="text-xs text-gray-400">No active tasks</p>}
      {tasks.map(task => (
        <div key={task.taskCode} className="mb-2 p-2 bg-gray-50 rounded text-xs">
          <div className="flex justify-between items-center mb-1">
            <span className="font-mono font-medium">{task.taskCode}</span>
            <span className={`px-1.5 py-0.5 rounded text-[10px] ${STATUS_BADGES[task.status] || 'bg-gray-100'}`}>
              {task.status}
            </span>
          </div>
          <div className="text-gray-500">
            <div>Container: {task.containerCode}</div>
            <div>{task.startLocation} → {task.destination || '?'}</div>
            {task.assignedRobot && <div>Robot: {task.assignedRobot}</div>}
          </div>
        </div>
      ))}

      {completedTasks.length > 0 && (
        <>
          <h3 className="text-sm font-semibold text-gray-700 mt-4 mb-2">Recent ({completedTasks.length})</h3>
          {completedTasks.map(task => (
            <div key={task.taskCode} className="mb-1 p-1.5 bg-gray-50 rounded text-[10px] text-gray-400">
              {task.taskCode} — {task.containerCode}
            </div>
          ))}
        </>
      )}
    </div>
  )
}
```

- [ ] **Step 3: Create RobotPanel**

```tsx
// 3d-viewer/src/components/Panels/RobotPanel.tsx

import { useState } from 'react'
import { useSimulatorStore } from '@/stores/simulatorStore'
import type { RobotStatusType } from '@/types'

const STATUS_DOT: Record<RobotStatusType, string> = {
  IDLE: 'bg-blue-400',
  MOVING_TO_PICKUP: 'bg-green-400',
  LOADING: 'bg-yellow-400',
  MOVING_TO_DESTINATION: 'bg-green-400',
  UNLOADING: 'bg-yellow-400',
  CHARGING: 'bg-gray-400',
  ERROR: 'bg-red-500',
}

export function RobotPanel() {
  const [collapsed, setCollapsed] = useState(false)
  const robots = useSimulatorStore(s => s.robots)
  const selectedRobotCode = useSimulatorStore(s => s.selectedRobotCode)
  const selectRobot = useSimulatorStore(s => s.selectRobot)
  const setCameraMode = useSimulatorStore(s => s.setCameraMode)

  const handleFollowRobot = (robotCode: string) => {
    selectRobot(robotCode)
    setCameraMode('follow')
  }

  if (collapsed) {
    return (
      <button
        onClick={() => setCollapsed(false)}
        className="absolute left-0 top-12 bg-white/90 backdrop-blur px-2 py-1 border border-gray-200 rounded-r text-xs"
      >
        Robots ({robots.length})
      </button>
    )
  }

  return (
    <div className="absolute left-0 top-12 bottom-0 w-56 bg-white/90 backdrop-blur border-r border-gray-200 overflow-y-auto p-3">
      <div className="flex justify-between items-center mb-2">
        <h3 className="text-sm font-semibold text-gray-700">Robots ({robots.length})</h3>
        <button onClick={() => setCollapsed(true)} className="text-gray-400 hover:text-gray-600 text-xs">Hide</button>
      </div>
      {robots.map(robot => (
        <div
          key={robot.robotCode}
          className={`mb-1.5 p-2 rounded text-xs cursor-pointer transition-colors ${
            robot.robotCode === selectedRobotCode ? 'bg-blue-50 border border-blue-200' : 'bg-gray-50 hover:bg-gray-100'
          }`}
          onClick={() => selectRobot(robot.robotCode === selectedRobotCode ? null : robot.robotCode)}
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-1.5">
              <span className={`w-2 h-2 rounded-full ${STATUS_DOT[robot.status]}`} />
              <span className="font-mono font-medium">{robot.robotCode}</span>
            </div>
            <button
              onClick={(e) => { e.stopPropagation(); handleFollowRobot(robot.robotCode) }}
              className="text-blue-500 hover:text-blue-700 text-[10px]"
            >
              Follow
            </button>
          </div>
          <div className="mt-1 text-gray-400">
            {robot.status} {robot.carriedContainerCode ? `| ${robot.carriedContainerCode}` : ''}
          </div>
          {/* Battery bar */}
          <div className="mt-1 h-1 bg-gray-200 rounded-full">
            <div
              className={`h-full rounded-full ${robot.batteryLevel > 0.3 ? 'bg-green-400' : 'bg-red-400'}`}
              style={{ width: `${robot.batteryLevel * 100}%` }}
            />
          </div>
        </div>
      ))}
    </div>
  )
}
```

- [ ] **Step 4: Create Controls**

```tsx
// 3d-viewer/src/components/Panels/Controls.tsx

import { useSimulatorStore } from '@/stores/simulatorStore'
import { useSimulatorApi } from '@/hooks/useSimulatorApi'
import { ConnectionStatus } from './ConnectionStatus'
import type { CameraMode } from '@/types'

const CAMERA_LABELS: Record<CameraMode, string> = {
  perspective: 'Perspective',
  topdown: 'Top-Down',
  follow: 'Follow',
}

export function Controls() {
  const cameraMode = useSimulatorStore(s => s.cameraMode)
  const setCameraMode = useSimulatorStore(s => s.setCameraMode)
  const simulationSpeed = useSimulatorStore(s => s.simulationSpeed)
  const setSimulationSpeed = useSimulatorStore(s => s.setSimulationSpeed)
  const { resetSimulator, updateConfig } = useSimulatorApi()

  const handleSpeedChange = async (speed: number) => {
    setSimulationSpeed(speed)
    await updateConfig({ defaultRobotSpeed: 2.0 * speed })
  }

  const cameraModes: CameraMode[] = ['perspective', 'topdown', 'follow']

  return (
    <div className="absolute top-0 left-0 right-0 h-12 bg-white/90 backdrop-blur border-b border-gray-200 flex items-center px-4 gap-4 z-10">
      <h1 className="text-sm font-bold text-gray-800 mr-4">Open WES 3D Viewer</h1>

      <ConnectionStatus />

      {/* Camera mode */}
      <div className="flex items-center gap-1 ml-4">
        <span className="text-xs text-gray-500">Camera:</span>
        {cameraModes.map(mode => (
          <button
            key={mode}
            onClick={() => setCameraMode(mode)}
            className={`px-2 py-0.5 rounded text-xs ${
              cameraMode === mode ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            {CAMERA_LABELS[mode]}
          </button>
        ))}
      </div>

      {/* Speed slider */}
      <div className="flex items-center gap-2 ml-4">
        <span className="text-xs text-gray-500">Speed:</span>
        <input
          type="range"
          min="0.5"
          max="5"
          step="0.5"
          value={simulationSpeed}
          onChange={e => handleSpeedChange(parseFloat(e.target.value))}
          className="w-20 h-1"
        />
        <span className="text-xs font-mono w-8">{simulationSpeed}x</span>
      </div>

      {/* Reset */}
      <button
        onClick={resetSimulator}
        className="ml-auto px-3 py-1 rounded text-xs bg-red-50 text-red-600 hover:bg-red-100"
      >
        Reset
      </button>
    </div>
  )
}
```

- [ ] **Step 5: Create App.tsx**

```tsx
// 3d-viewer/src/App.tsx

import { useEffect } from 'react'
import { WarehouseScene } from '@/components/Scene/WarehouseScene'
import { Controls } from '@/components/Panels/Controls'
import { TaskPanel } from '@/components/Panels/TaskPanel'
import { RobotPanel } from '@/components/Panels/RobotPanel'
import { useRobotWebSocket } from '@/hooks/useRobotWebSocket'
import { useSimulatorApi } from '@/hooks/useSimulatorApi'

const WS_URL = import.meta.env.VITE_SIMULATOR_WS_URL || 'ws://localhost:8091/ws/robots'

export default function App() {
  useRobotWebSocket(WS_URL)
  const { fetchLayout } = useSimulatorApi()

  useEffect(() => {
    fetchLayout()
  }, [fetchLayout])

  return (
    <div className="w-screen h-screen relative">
      <WarehouseScene />
      <Controls />
      <RobotPanel />
      <TaskPanel />
    </div>
  )
}
```

- [ ] **Step 6: Verify build**

Run: `cd 3d-viewer && npx tsc --noEmit && npx vite build`
Expected: No errors, build output in `dist/`

- [ ] **Step 7: Commit**

```bash
git add 3d-viewer/src/
git commit -m "feat(3d-viewer): add UI panels (tasks, robots, controls) and App shell"
```

---

### Task 15: 3D Viewer Dockerfile

**Files:**
- Create: `3d-viewer/Dockerfile`
- Create: `3d-viewer/nginx.conf`

- [ ] **Step 1: Create nginx.conf**

```nginx
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff2?)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

- [ ] **Step 2: Create Dockerfile**

```dockerfile
FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
```

- [ ] **Step 3: Commit**

```bash
git add 3d-viewer/Dockerfile 3d-viewer/nginx.conf
git commit -m "feat(3d-viewer): add Dockerfile and nginx config"
```

---

## Phase 3: Integration & Deployment

### Task 16: Docker Compose + .gitignore

**Files:**
- Modify: `docker-compose.yml`
- Modify: `.gitignore`

- [ ] **Step 1: Add demo services to docker-compose.yml**

Append to the `services:` section in `docker-compose.yml`:

```yaml
  # === Demo Services (start with: docker compose --profile demo up -d) ===

  rcs-simulator:
    build:
      context: ./server
      dockerfile: modules-simulator/rcs-simulator/Dockerfile
    container_name: rcs-simulator
    ports:
      - "8091:8091"
    environment:
      - WES_CALLBACK_URL=http://gateway-server:8090
      - LAYOUT_FILE=classpath:layouts/default-layout.json
      - CORS_ORIGINS=http://localhost:8092,http://3d-viewer:80
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8091/actuator/health"]
      interval: 10s
      timeout: 5s
      retries: 3
    depends_on:
      - gateway-server
    networks:
      - my-network
    profiles:
      - demo

  3d-viewer:
    build:
      context: ./3d-viewer
    container_name: 3d-viewer
    ports:
      - "8092:80"
    depends_on:
      rcs-simulator:
        condition: service_healthy
    networks:
      - my-network
    profiles:
      - demo
```

- [ ] **Step 2: Add 3d-viewer node_modules to .gitignore**

Append to `.gitignore`:

```
3d-viewer/node_modules/
3d-viewer/dist/
```

- [ ] **Step 3: Commit**

```bash
git add docker-compose.yml .gitignore
git commit -m "feat(integration): add RCS simulator and 3D viewer to Docker Compose with demo profile"
```

---

### Task 17: WES Frontend Integration (iframe page + route + i18n)

**Files:**
- Create: `client/src/pages/monitoring/warehouse3d.tsx`
- Modify: `client/src/routes/path2Compoment.tsx`
- Modify: `client/src/locales/en/monitoring.ts` (and zh, ja, ko)

- [ ] **Step 1: Create warehouse3d page**

First, check the existing GrafanaDashboard pattern to replicate for the 3D viewer:

```tsx
// client/src/pages/monitoring/warehouse3d.tsx

import React from "react"

const Warehouse3D: React.FC = () => {
    // Construct URL from current hostname, matching the pattern used by GrafanaDashboard
    const viewerUrl = window.location.protocol + "//" + window.location.hostname + ":8092"

    return (
        <iframe
            src={viewerUrl}
            style={{
                width: "100%",
                height: "calc(100vh - 100px)",
                border: "none"
            }}
            title="3D Warehouse Viewer"
        />
    )
}

export default Warehouse3D
```

- [ ] **Step 2: Add route to path2Compoment.tsx**

Add after the existing monitoring routes (around line 662):

```typescript
{
    path: "/monitoring/warehouse3d",
    name: (
        <Translation>
            {(t) => t("monitoring.warehouse3d.title")}
        </Translation>
    ),
    component: lazy(() => import("@/pages/monitoring/warehouse3d"))
},
```

- [ ] **Step 3: Add i18n translations**

Add to each locale's monitoring translations:

**English** (`client/src/locales/en/monitoring.ts`): Add `"monitoring.warehouse3d.title": "3D Warehouse View"`

**Chinese** (`client/src/locales/zh/monitoring.ts`): Add `"monitoring.warehouse3d.title": "3D\u4ed3\u5e93\u89c6\u56fe"`

**Japanese** (`client/src/locales/ja/monitoring.ts`): Add `"monitoring.warehouse3d.title": "3D\u5009\u5eab\u30d3\u30e5\u30fc"`

**Korean** (`client/src/locales/ko/monitoring.ts`): Add `"monitoring.warehouse3d.title": "3D \ucc3d\uace0 \ubdf0"`

- [ ] **Step 4: Commit**

```bash
git add client/src/pages/monitoring/warehouse3d.tsx client/src/routes/path2Compoment.tsx client/src/locales/
git commit -m "feat(frontend): add 3D warehouse viewer page with iframe embedding and i18n"
```

---

### Task 18: Liquibase Changeset (menu + callback API seed data)

**Files:**
- Create: `server/server/wes-server/src/main/resources/db/changelog/db.changelog-20260523-simulator.xml`
- Modify: `server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml`

- [ ] **Step 1: Check existing changelog master for include pattern**

Read `server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml` to see how changelogs are included.

- [ ] **Step 2: Create changeset XML**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.9.xsd">

    <!-- Menu entry for 3D Warehouse View under Monitoring app -->
    <changeSet id="20260523-001" author="simulator">
        <preConditions onFail="MARK_RAN">
            <tableExists tableName="u_menu"/>
            <sqlCheck expectedResult="0">
                SELECT COUNT(*) FROM u_menu WHERE menu_code = 'monitoring_warehouse3d'
            </sqlCheck>
        </preConditions>
        <comment>Add 3D Warehouse View menu entry under Monitoring app</comment>
        <sql>
            INSERT INTO u_menu (system_code, menu_code, menu_name, menu_path, parent_code, order_num, menu_type, version, create_time, update_time)
            SELECT 'monitoring', 'monitoring_warehouse3d', '3D Warehouse View', '/monitoring/warehouse3d',
                   menu_code, 50, 'C', 0, NOW(), NOW()
            FROM u_menu WHERE system_code = 'monitoring' AND parent_code IS NULL LIMIT 1;
        </sql>
    </changeSet>

    <!-- Callback API seed data for RCS Simulator -->
    <changeSet id="20260523-002" author="simulator">
        <preConditions onFail="MARK_RAN">
            <tableExists tableName="u_api"/>
            <sqlCheck expectedResult="0">
                SELECT COUNT(*) FROM u_api WHERE code = 'CONTAINER_TASK_CREATE'
            </sqlCheck>
        </preConditions>
        <comment>Seed RCS Simulator callback API configurations for demo</comment>
        <sql>
            INSERT INTO u_api (code, name, api_type, url, method, `format`, enabled, sync_callback, version, create_time, update_time)
            VALUES
                ('CONTAINER_TASK_CREATE', 'RCS Simulator - Create Task', 'CALLBACK', 'http://rcs-simulator:8091/api/tasks/create', 'POST', 'application/json', true, false, 0, NOW(), NOW()),
                ('CONTAINER_TASK_CANCEL', 'RCS Simulator - Cancel Task', 'CALLBACK', 'http://rcs-simulator:8091/api/tasks/cancel', 'POST', 'application/json', true, false, 0, NOW(), NOW()),
                ('CONTAINER_TASK_IMPROVE_PRIORITY', 'RCS Simulator - Improve Priority', 'CALLBACK', 'http://rcs-simulator:8091/api/tasks/improve-priority', 'POST', 'application/json', true, false, 0, NOW(), NOW()),
                ('CONTAINER_TASK_RELEASE', 'RCS Simulator - Release Task', 'CALLBACK', 'http://rcs-simulator:8091/api/tasks/release', 'POST', 'application/json', true, false, 0, NOW(), NOW()),
                ('CONTAINER_LEAVE', 'RCS Simulator - Container Leave', 'CALLBACK', 'http://rcs-simulator:8091/api/tasks/container-leave', 'POST', 'application/json', true, false, 0, NOW(), NOW()),
                ('CALL_ROBOT', 'RCS Simulator - Call Robot', 'CALLBACK', 'http://rcs-simulator:8091/api/tasks/call-robot', 'POST', 'application/json', true, false, 0, NOW(), NOW());
        </sql>
    </changeSet>

</databaseChangeLog>
```

- [ ] **Step 3: Register in db.changelog-master.xml**

Add to the end of the include list:

```xml
<include file="db/changelog/db.changelog-20260523-simulator.xml"/>
```

- [ ] **Step 4: Commit**

```bash
git add server/server/wes-server/src/main/resources/db/changelog/
git commit -m "feat(integration): add Liquibase changeset for monitoring menu and RCS callback API seed data"
```

---

### Task 19: End-to-End Smoke Test

This is a manual verification task — no new files to create.

- [ ] **Step 1: Build everything**

```bash
cd server && ./gradlew :modules-simulator:rcs-simulator:bootJar
cd ../3d-viewer && npm run build
```
Expected: Both build successfully

- [ ] **Step 2: Run simulator standalone**

```bash
cd server && java -jar modules-simulator/rcs-simulator/build/libs/*.jar
```
Expected: Application starts on port 8091, logs "RCS Simulator initialized — 8 robots ready"

- [ ] **Step 3: Verify management API**

```bash
curl http://localhost:8091/api/simulator/robots | python -m json.tool
curl http://localhost:8091/actuator/health
```
Expected: Returns 8 robots in IDLE state; health check returns UP

- [ ] **Step 4: Verify task creation endpoint**

```bash
curl -X POST http://localhost:8091/api/tasks/create \
  -H "Content-Type: application/json" \
  -d '{
    "messageId": 1,
    "data": [{
      "customerTaskId": 100,
      "businessTaskType": "PICKING",
      "containerTaskType": "OUTBOUND",
      "taskCode": "SMOKE-001",
      "taskPriority": 10,
      "taskGroupPriority": 10,
      "containerCode": "C-001",
      "startLocation": "A01-01",
      "destinations": ["WS-01"]
    }]
  }'
```
Expected: Returns `{"status":"ok"}`, logs show task assignment to nearest robot

- [ ] **Step 5: Verify WebSocket**

Open `ws://localhost:8091/ws/robots` in a WebSocket client (e.g., browser console or wscat).
Expected: Receives JSON messages with robot positions every 200ms

- [ ] **Step 6: Commit all remaining changes**

```bash
git add -A
git commit -m "feat(simulator): complete RCS simulator and 3D warehouse viewer implementation"
```
