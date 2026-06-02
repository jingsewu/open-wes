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
