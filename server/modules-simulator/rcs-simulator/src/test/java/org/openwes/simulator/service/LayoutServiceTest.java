package org.openwes.simulator.service;

import org.junit.jupiter.api.Test;
import org.openwes.simulator.domain.Position;
import org.openwes.simulator.domain.WarehouseLayout;

import static org.junit.jupiter.api.Assertions.*;

class LayoutServiceTest {

    @Test
    void loadDefaultLayout_parsesAllSections() {
        LayoutService service = new LayoutService();
        WarehouseLayout layout = service.loadFromClasspath("layouts/default-layout.json");

        assertNotNull(layout);
        assertEquals(50, layout.getWarehouse().getWidth());
        assertEquals(30, layout.getWarehouse().getHeight());
        assertFalse(layout.getShelves().isEmpty());
        assertFalse(layout.getWorkstations().isEmpty());
        assertFalse(layout.getChargingStations().isEmpty());
        assertEquals(8, layout.getRobots().size());
    }

    @Test
    void loadDefaultLayout_buildsLocationIndex() {
        LayoutService service = new LayoutService();
        WarehouseLayout layout = service.loadFromClasspath("layouts/default-layout.json");
        layout.buildLocationIndex();

        Position wsPos = layout.getPositionForLocation("WS-01");
        assertNotNull(wsPos, "Workstation WS-01 should be in location index");
        assertEquals(2.0, wsPos.getX());
        assertEquals(10.0, wsPos.getY());
    }

    @Test
    void loadDefaultLayout_robotCodesAreUnique() {
        LayoutService service = new LayoutService();
        WarehouseLayout layout = service.loadFromClasspath("layouts/default-layout.json");

        long uniqueCount = layout.getRobots().stream()
                .map(WarehouseLayout.RobotConfig::getRobotCode)
                .distinct()
                .count();
        assertEquals(layout.getRobots().size(), uniqueCount, "Robot codes must be unique");
    }
}
