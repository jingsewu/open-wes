package org.openwes.simulator.domain;

public enum RobotType {
    KIVA("AGV"),
    BIN_ROBOT("BinRobot");

    private final String displayName;

    RobotType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
