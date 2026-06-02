package org.openwes.simulator.domain;

public enum RobotStatus {
    IDLE,
    MOVING,
    LOADING,
    UNLOADING,
    WAITING,      // new: waiting for human processing (KIVA)
    CHARGING,
    ERROR
}
