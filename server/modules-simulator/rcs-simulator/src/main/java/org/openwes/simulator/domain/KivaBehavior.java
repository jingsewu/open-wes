package org.openwes.simulator.domain;

import lombok.RequiredArgsConstructor;
import org.openwes.simulator.config.SimulatorProperties;
import org.openwes.simulator.service.PathService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class KivaBehavior implements RobotBehavior {

    public enum KivaState {
        INIT, MOVING_TO_POD, LIFTING_POD, MOVING_TO_WS, DROPPING_POD, WAITING_PROCESS
    }

    private final SimulatorProperties properties;
    private final PathService pathService;
    private final Map<String, KivaState> taskStates = new ConcurrentHashMap<>();
    private final Map<String, List<Position>> taskPaths = new ConcurrentHashMap<>();
    private final Map<String, Long> stateEnteredAt = new ConcurrentHashMap<>();
    private final Map<String, String> targetPodId = new ConcurrentHashMap<>();

    @Override
    public boolean canAcceptTask(SimulatedTask task) {
        return true; // KIVA can handle any pod-moving task
    }

    @Override
    public void initTask(VirtualRobot robot, SimulatedTask task) {
        taskStates.put(task.getTaskCode(), KivaState.INIT);
        stateEnteredAt.put(task.getTaskCode(), System.currentTimeMillis());

        // The first pickup location is the pod location
        String podLocationCode = task.getPickupLocationCodes().iterator().next();
        targetPodId.put(task.getTaskCode(), podLocationCode);

        robot.setStatus(RobotStatus.MOVING);
    }

    @Override
    public boolean executeTick(VirtualRobot robot, SimulatedTask task, double deltaSeconds) {
        KivaState state = taskStates.get(task.getTaskCode());
        if (state == null) return false;

        long now = System.currentTimeMillis();

        switch (state) {
            case INIT:
                String podCode = targetPodId.get(task.getTaskCode());
                Position podPos = task.getPickupPosition();
                taskPaths.put(task.getTaskCode(), pathService.calculatePath(robot.getCurrentPosition(), podPos));
                taskStates.put(task.getTaskCode(), KivaState.MOVING_TO_POD);
                break;

            case MOVING_TO_POD:
                advanceTowards(robot, task, task.getPickupPosition(), deltaSeconds);
                if (pathService.hasReached(robot.getCurrentPosition(), task.getPickupPosition())) {
                    robot.setCurrentPosition(task.getPickupPosition().copy());
                    taskStates.put(task.getTaskCode(), KivaState.LIFTING_POD);
                    stateEnteredAt.put(task.getTaskCode(), now);
                }
                break;

            case LIFTING_POD:
                if (isDelayElapsed(task.getTaskCode(), now, properties.getKiva().getLiftDelayMs())) {
                    robot.setCarryingPodId(targetPodId.get(task.getTaskCode()));
                    taskStates.put(task.getTaskCode(), KivaState.MOVING_TO_WS);
                    taskPaths.put(task.getTaskCode(), pathService.calculatePath(
                            robot.getCurrentPosition(), task.getDestinationPosition()));
                    stateEnteredAt.put(task.getTaskCode(), now);
                }
                break;

            case MOVING_TO_WS:
                robot.setStatus(RobotStatus.MOVING);
                advanceTowards(robot, task, task.getDestinationPosition(), deltaSeconds);
                if (pathService.hasReached(robot.getCurrentPosition(), task.getDestinationPosition())) {
                    robot.setCurrentPosition(task.getDestinationPosition().copy());
                    taskStates.put(task.getTaskCode(), KivaState.DROPPING_POD);
                    stateEnteredAt.put(task.getTaskCode(), now);
                }
                break;

            case DROPPING_POD:
                robot.setStatus(RobotStatus.UNLOADING);
                if (isDelayElapsed(task.getTaskCode(), now, properties.getKiva().getLiftDelayMs())) {
                    robot.setCarryingPodId(null); // pod stays at WS
                    taskStates.put(task.getTaskCode(), KivaState.WAITING_PROCESS);
                    stateEnteredAt.put(task.getTaskCode(), now);
                }
                break;

            case WAITING_PROCESS:
                robot.setStatus(RobotStatus.WAITING);
                if (isDelayElapsed(task.getTaskCode(), now, properties.getKiva().getProcessDelayMs())) {
                    completeTask(robot, task);
                    return true;
                }
                break;
        }

        return false;
    }

    @Override
    public void processComplete(VirtualRobot robot, SimulatedTask task) {
        KivaState state = taskStates.get(task.getTaskCode());
        if (state == KivaState.WAITING_PROCESS) {
            completeTask(robot, task);
        }
    }

    private void completeTask(VirtualRobot robot, SimulatedTask task) {
        task.setStatus(TaskStatus.COMPLETED);
        robot.setStatus(RobotStatus.IDLE);
        cleanup(task.getTaskCode());
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
        targetPodId.remove(taskCode);
    }
}
