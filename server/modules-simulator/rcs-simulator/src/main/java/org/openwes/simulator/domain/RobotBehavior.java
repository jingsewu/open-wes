package org.openwes.simulator.domain;

public interface RobotBehavior {
    /** Advance task execution for one tick. Returns true if task is complete. */
    boolean executeTick(VirtualRobot robot, SimulatedTask task, double deltaSeconds);

    /** Check if robot can accept this task. */
    boolean canAcceptTask(SimulatedTask task);

    /** Initialize task state (called when task is first assigned to this robot). */
    void initTask(VirtualRobot robot, SimulatedTask task);

    /** Handle manual process-complete trigger. */
    void processComplete(VirtualRobot robot, SimulatedTask task);
}
