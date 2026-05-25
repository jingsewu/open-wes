package org.openwes.simulator.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.openwes.simulator.config.SimulatorProperties;
import org.openwes.simulator.domain.SimulatedTask;
import org.openwes.simulator.domain.VirtualRobot;
import org.openwes.simulator.domain.WarehouseLayout;
import org.openwes.simulator.service.LayoutService;
import org.openwes.simulator.service.RobotFleetService;
import org.openwes.simulator.service.TaskExecutionService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/simulator")
@RequiredArgsConstructor
public class SimulatorManagementController {

    private final RobotFleetService fleetService;
    private final TaskExecutionService taskExecutionService;
    private final LayoutService layoutService;
    private final SimulatorProperties properties;

    @GetMapping("/robots")
    public List<VirtualRobot> listRobots() {
        return fleetService.getAllRobots();
    }

    @GetMapping("/tasks")
    public List<SimulatedTask> listTasks() {
        return taskExecutionService.getAllTasks();
    }

    @GetMapping("/layout")
    public WarehouseLayout getLayout() {
        return layoutService.getCurrentLayout();
    }

    @PutMapping("/layout")
    public Map<String, String> updateLayout(@RequestBody WarehouseLayout layout) {
        layoutService.updateLayout(layout);
        fleetService.initializeRobots(layout.getRobots());
        return Map.of("status", "ok");
    }

    @PostMapping("/reset")
    public Map<String, String> reset() {
        fleetService.resetAll();
        taskExecutionService.reset();
        return Map.of("status", "ok");
    }

    @GetMapping("/config")
    public SimulatorConfigDTO getConfig() {
        SimulatorConfigDTO dto = new SimulatorConfigDTO();
        dto.setTickIntervalMs(properties.getTickIntervalMs());
        dto.setDefaultRobotSpeed(properties.getDefaultRobotSpeed());
        dto.setLoadingDelayMs(properties.getLoadingDelayMs());
        dto.setFailureRatePercent(properties.getFailureRatePercent());
        return dto;
    }

    @PutMapping("/config")
    public Map<String, String> updateConfig(@RequestBody SimulatorConfigDTO config) {
        if (config.getTickIntervalMs() != null) properties.setTickIntervalMs(config.getTickIntervalMs());
        if (config.getDefaultRobotSpeed() != null) properties.setDefaultRobotSpeed(config.getDefaultRobotSpeed());
        if (config.getLoadingDelayMs() != null) properties.setLoadingDelayMs(config.getLoadingDelayMs());
        if (config.getFailureRatePercent() != null) properties.setFailureRatePercent(config.getFailureRatePercent());
        return Map.of("status", "ok");
    }

    @PostMapping("/robots/{robotCode}/error")
    public Map<String, String> injectError(@PathVariable String robotCode) {
        VirtualRobot robot = fleetService.getRobot(robotCode);
        if (robot == null) {
            return Map.of("status", "error", "message", "Robot not found: " + robotCode);
        }
        taskExecutionService.failTaskForRobot(robotCode);
        fleetService.setError(robot);
        return Map.of("status", "ok");
    }

    @PostMapping("/robots/{robotCode}/recover")
    public Map<String, String> recoverRobot(@PathVariable String robotCode) {
        VirtualRobot robot = fleetService.getRobot(robotCode);
        if (robot == null) {
            return Map.of("status", "error", "message", "Robot not found: " + robotCode);
        }
        fleetService.recoverFromError(robot);
        return Map.of("status", "ok");
    }

    @PostMapping("/layout/validate")
    public Map<String, Object> validateLayout() {
        WarehouseLayout layout = layoutService.getCurrentLayout();
        List<String> allLocations = new ArrayList<>();
        if (layout.getShelves() != null) {
            layout.getShelves().forEach(s -> {
                if (s.getLocationCodes() != null) allLocations.addAll(s.getLocationCodes());
            });
        }
        if (layout.getWorkstations() != null) {
            layout.getWorkstations().forEach(ws -> allLocations.add(ws.getLocationCode()));
        }
        return Map.of(
                "totalLocations", allLocations.size(),
                "locations", allLocations,
                "message", "Locations listed. Cross-reference with WES location master data manually."
        );
    }

    @Data
    public static class SimulatorConfigDTO {
        private Integer tickIntervalMs;
        private Double defaultRobotSpeed;
        private Integer loadingDelayMs;
        private Integer failureRatePercent;
    }
}
