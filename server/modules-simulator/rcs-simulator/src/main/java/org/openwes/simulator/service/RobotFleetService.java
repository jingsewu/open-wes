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
