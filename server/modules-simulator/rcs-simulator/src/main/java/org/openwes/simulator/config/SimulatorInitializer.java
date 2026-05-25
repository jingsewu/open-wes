package org.openwes.simulator.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.simulator.domain.WarehouseLayout;
import org.openwes.simulator.service.LayoutService;
import org.openwes.simulator.service.RobotFleetService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimulatorInitializer implements CommandLineRunner {

    private final SimulatorProperties properties;
    private final LayoutService layoutService;
    private final RobotFleetService fleetService;

    @Override
    public void run(String... args) {
        WarehouseLayout layout = layoutService.load(properties.getLayoutFile());
        fleetService.initializeRobots(layout.getRobots());
        log.info("RCS Simulator initialized — {} robots ready", layout.getRobots().size());
    }
}
