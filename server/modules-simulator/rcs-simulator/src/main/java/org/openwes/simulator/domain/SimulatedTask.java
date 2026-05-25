package org.openwes.simulator.domain;

import lombok.Data;

import java.time.Instant;
import java.util.Collection;

@Data
public class SimulatedTask {
    private String taskCode;
    private String taskGroupCode;
    private String containerCode;
    private String containerFace;
    private String startLocation;
    private Collection<String> destinations;
    private int priority;
    private int groupPriority;
    private String businessTaskType;
    private String containerTaskType;
    private Long customerTaskId;

    private TaskStatus status = TaskStatus.QUEUED;
    private String assignedRobotCode;
    private Position pickupPosition;
    private Position destinationPosition;
    private Instant createdAt = Instant.now();
    private Instant completedAt;

    public boolean isActive() {
        return !status.isTerminal();
    }
}
