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
    private final Queue<SimulatedTask> taskQueue = new ConcurrentLinkedQueue<>();

    public void submitTask(SimulatedTask task) {
        allTasks.put(task.getTaskCode(), task);

        WarehouseLayout layout = layoutService.getCurrentLayout();

        // Resolve pickup locations from layout
        List<String> pickups = task.getPickupLocationCodes();
        List<Position> resolvedPositions = pickups.stream()
                .map(loc -> layout.getPositionForLocation(loc))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        task.setPickupPositions(resolvedPositions);
        if (!resolvedPositions.isEmpty()) {
            task.setPickupPosition(resolvedPositions.get(0));
        } else {
            task.setPickupPosition(new Position(0, 0, 0));
        }

        // Resolve destination
        String dest = task.getDestinations() != null && !task.getDestinations().isEmpty()
                ? task.getDestinations().iterator().next() : null;
        if (dest != null) {
            Position pos = layout.getPositionForLocation(dest);
            task.setDestinationPosition(pos != null ? pos : new Position(0, 0, 0));
        }

        // Find nearest idle robot of matching type
        String requiredType = task.getRequiredRobotType();
        Optional<VirtualRobot> robot = fleetService.findNearestIdleRobot(task.getPickupPosition(), requiredType);
        if (robot.isPresent()) {
            assignRobotToTask(robot.get(), task);
        } else {
            task.setStatus(TaskStatus.QUEUED);
            taskQueue.add(task);
            log.info("Task {} queued — no idle {} robots", task.getTaskCode(), requiredType);
        }

        callbackService.reportTaskStatus(task.getTaskCode(), "PROCESSING", null, task.getContainerCode(), null);
    }

    public void cancelTask(String taskCode) {
        SimulatedTask task = allTasks.get(taskCode);
        if (task == null || task.getStatus().isTerminal()) return;

        task.setStatus(TaskStatus.CANCELED);
        if (task.getAssignedRobotCode() != null) {
            VirtualRobot robot = fleetService.getRobot(task.getAssignedRobotCode());
            if (robot != null) {
                if (robot.getBehavior() instanceof KivaBehavior) {
                    ((KivaBehavior) robot.getBehavior()).cleanup(taskCode);
                } else if (robot.getBehavior() instanceof BinRobotBehavior) {
                    ((BinRobotBehavior) robot.getBehavior()).cleanup(taskCode);
                }
                fleetService.releaseRobot(robot);
            }
        }
        log.info("Task {} canceled", taskCode);
    }

    public void tick() {
        double deltaSeconds = properties.getTickIntervalMs() / 1000.0;
        long now = System.currentTimeMillis();

        List<SimulatedTask> completedTasks = new ArrayList<>();

        for (SimulatedTask task : allTasks.values()) {
            if (task.getStatus().isTerminal()) continue;
            if (task.getStatus() == TaskStatus.QUEUED) continue;

            VirtualRobot robot = fleetService.getRobot(task.getAssignedRobotCode());
            if (robot == null || robot.isInError()) continue;

            // Delegate to behavior strategy
            boolean done = robot.getBehavior().executeTick(robot, task, deltaSeconds);
            if (done) {
                completedTasks.add(task);
            }

            // Random failure check
            if (properties.getFailureRatePercent() > 0 && !task.getStatus().isTerminal()) {
                if (ThreadLocalRandom.current().nextInt(100) < properties.getFailureRatePercent()) {
                    failTask(task, robot);
                }
            }
        }

        // Post-process completed tasks
        for (SimulatedTask task : completedTasks) {
            if (task.getStatus() == TaskStatus.COMPLETED) {
                completeTaskCallbacks(task);
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
        taskQueue.clear();
    }

    private void assignRobotToTask(VirtualRobot robot, SimulatedTask task) {
        task.setStatus(TaskStatus.ASSIGNED);
        task.setAssignedRobotCode(robot.getRobotCode());
        fleetService.assignTask(robot, task.getTaskCode(), task.getContainerCode());
        robot.getBehavior().initTask(robot, task);
        log.info("Assigned robot {} to task {} (type: {})", robot.getRobotCode(), task.getTaskCode(), robot.getRobotType());
    }

    private void completeTaskCallbacks(SimulatedTask task) {
        task.setCompletedAt(Instant.now());
        VirtualRobot robot = fleetService.getRobot(task.getAssignedRobotCode());
        if (robot == null) return;

        fleetService.releaseRobot(robot);

        String destination = task.getDestinations() != null && !task.getDestinations().isEmpty()
                ? task.getDestinations().iterator().next() : "UNKNOWN";

        callbackService.reportContainerArrived(
                task.getContainerCode(), destination,
                robot.getRobotCode(), robot.getRobotType().name(),
                task.getTaskGroupCode() != null ? task.getTaskGroupCode() : task.getTaskCode(),
                destination, null, null);

        callbackService.reportTaskStatus(task.getTaskCode(), "WCS Succeeded",
                robot.getRobotCode(), task.getContainerCode(), destination);

        log.info("Task {} completed by robot {} ({})", task.getTaskCode(), robot.getRobotCode(), robot.getRobotType());
    }

    private void failTask(SimulatedTask task, VirtualRobot robot) {
        task.setStatus(TaskStatus.FAILED);
        task.setCompletedAt(Instant.now());
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
            String requiredType = task.getRequiredRobotType();
            Optional<VirtualRobot> robot = fleetService.findNearestIdleRobot(task.getPickupPosition(), requiredType);
            if (robot.isPresent()) {
                it.remove();
                assignRobotToTask(robot.get(), task);
            } else {
                break;
            }
        }
    }
}
