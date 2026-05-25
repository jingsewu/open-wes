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
        assertEquals("AGV-001", robot.get().getRobotCode());
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
