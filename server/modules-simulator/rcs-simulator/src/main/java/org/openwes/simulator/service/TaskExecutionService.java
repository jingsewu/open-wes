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
