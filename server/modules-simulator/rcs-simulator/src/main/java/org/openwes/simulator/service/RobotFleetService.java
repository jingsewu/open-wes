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
