package org.openwes.simulator.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class WarehouseLayout {

    private Warehouse warehouse;
    private List<Shelf> shelves;
    private List<Workstation> workstations;
    private List<ChargingStation> chargingStations;
    private List<RobotConfig> robots;

    // locationCode -> Position lookup, built after deserialization
    private transient Map<String, Position> locationPositions;

    public void buildLocationIndex() {
        locationPositions = new ConcurrentHashMap<>();
        if (shelves != null) {
            for (Shelf shelf : shelves) {
                if (shelf.getLocationCodes() != null) {
                    for (int i = 0; i < shelf.getLocationCodes().size(); i++) {
                        String code = shelf.getLocationCodes().get(i);
                        double slotX = shelf.getX() + (i % shelf.getWidth());
                        double slotY = shelf.getY() + (i / shelf.getWidth());
                        locationPositions.put(code, new Position(slotX, slotY, 0));
                    }
                }
            }
        }
        if (workstations != null) {
            for (Workstation ws : workstations) {
                locationPositions.put(ws.getLocationCode(), new Position(ws.getX(), ws.getY(), 0));
            }
        }
        if (chargingStations != null) {
            for (ChargingStation cs : chargingStations) {
                locationPositions.put(cs.getLocationCode(), new Position(cs.getX(), cs.getY(), 0));
            }
        }
    }

    public Position getPositionForLocation(String locationCode) {
        if (locationPositions == null) {
            buildLocationIndex();
        }
        return locationPositions.get(locationCode);
    }

    @Data
    public static class Warehouse {
        private int width;
        private int height;
        private double gridSize;
    }

    @Data
    public static class Shelf {
        private String id;
        private double x;
        private double y;
        private int width;
        private int height;
        private List<String> locationCodes;
    }

    @Data
    public static class Workstation {
        private String id;
        private double x;
        private double y;
        private String type;
        private String locationCode;
    }

    @Data
    public static class ChargingStation {
        private String id;
        private double x;
        private double y;
        private String locationCode;
    }

    @Data
    public static class RobotConfig {
        private String robotCode;
        private String robotType;
        private double startX;
        private double startY;
        private double speed;
    }
}
