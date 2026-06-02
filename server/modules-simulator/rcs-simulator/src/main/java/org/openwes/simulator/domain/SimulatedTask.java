package org.openwes.simulator.domain;

import lombok.Data;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Data
public class SimulatedTask {
    private String taskCode;
    private String taskGroupCode;
    private String containerCode;
    private String containerFace;
    private Collection<String> startLocations;     // was: String startLocation
    private Collection<String> destinations;
    private int priority;
    private int groupPriority;
    private String businessTaskType;
    private String containerTaskType;
    private Long customerTaskId;

    // New: robot type requirement
    private String requiredRobotType;

    private TaskStatus status = TaskStatus.QUEUED;
    private String assignedRobotCode;
    private Position pickupPosition;
    private Position destinationPosition;
    private Instant createdAt = Instant.now();
    private Instant completedAt;

    // Pre-resolved positions for each pickup location
    private transient List<Position> pickupPositions;

    // Convenience method: get pickup location codes as List<String>
    public List<String> getPickupLocationCodes() {
        return startLocations == null ? List.of() : List.copyOf(startLocations);
    }

    // Convenience method: get pickup positions iterator
    public Iterator<Position> getPickupPositionIterator() {
        return pickupPositions == null ? Collections.emptyIterator() : pickupPositions.iterator();
    }

    public boolean isActive() {
        return !status.isTerminal();
    }
}
