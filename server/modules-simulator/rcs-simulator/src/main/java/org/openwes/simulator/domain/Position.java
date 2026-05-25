package org.openwes.simulator.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Position {
    private double x;
    private double y;
    private double rotation; // degrees, 0 = facing right

    public double distanceTo(Position other) {
        return Math.abs(this.x - other.x) + Math.abs(this.y - other.y);
    }

    public Position copy() {
        return new Position(x, y, rotation);
    }
}
