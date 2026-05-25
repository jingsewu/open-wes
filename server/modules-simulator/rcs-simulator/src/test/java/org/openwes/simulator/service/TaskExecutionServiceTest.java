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
