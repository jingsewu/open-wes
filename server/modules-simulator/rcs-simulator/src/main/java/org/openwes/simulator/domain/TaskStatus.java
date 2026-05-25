package org.openwes.simulator.domain;

public enum TaskStatus {
    QUEUED,
    ASSIGNED,
    MOVING_TO_PICKUP,
    LOADING,
    MOVING_TO_DESTINATION,
    UNLOADING,
    COMPLETED,
    FAILED,
    CANCELED;

    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED || this == CANCELED;
    }
}
