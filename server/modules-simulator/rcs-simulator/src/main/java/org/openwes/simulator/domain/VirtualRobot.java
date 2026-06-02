package org.openwes.simulator.domain;

import lombok.Data;

import java.util.List;

@Data
public class VirtualRobot {
    private String robotCode;
    private RobotType robotType;                    // was: String
    private RobotStatus status = RobotStatus.IDLE;
    private Position currentPosition;
    private String currentLocationCode;
    private String assignedTaskCode;
    private String carriedContainerCode;
    private double speed;
    private double batteryLevel = 1.0;

    // KIVA-specific: currently carried pod id
    private String carryingPodId;

    // BIN_ROBOT-specific: bins in basket (max basketSlots)
    private List<String> basketItems;

    // Strategy reference
    private RobotBehavior behavior;

    public boolean isIdle() {
        return status == RobotStatus.IDLE;
    }

    public boolean isInError() {
        return status == RobotStatus.ERROR;
    }
}
