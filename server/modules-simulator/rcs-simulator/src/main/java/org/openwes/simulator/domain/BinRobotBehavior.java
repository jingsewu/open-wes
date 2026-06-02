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
                        task.setPickupPosition(task.getDestinationPosition());
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
        positionIterator.remove(taskCode);
    }
}
