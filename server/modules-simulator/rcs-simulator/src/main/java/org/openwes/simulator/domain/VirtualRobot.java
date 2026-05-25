package org.openwes.simulator.domain;

import lombok.Data;

@Data
public class VirtualRobot {
    private String robotCode;
    private String robotType;
    private RobotStatus status = RobotStatus.IDLE;
    private Position currentPosition;
    private String currentLocationCode;
    private String assignedTaskCode;
    private String carriedContainerCode;
    private double speed;
    private double batteryLevel = 1.0;

    public boolean isIdle() {
        return status == RobotStatus.IDLE;
    }

    public boolean isInError() {
        return status == RobotStatus.ERROR;
    }
}
