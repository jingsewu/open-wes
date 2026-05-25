package org.openwes.simulator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.simulator.domain.SimulatedTask;
import org.openwes.simulator.domain.VirtualRobot;
import org.openwes.simulator.websocket.RobotStateHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketPushService {

    private final RobotFleetService fleetService;
    private final TaskExecutionService taskExecutionService;
    private final RobotStateHandler handler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(fixedDelayString = "${simulator.tick-interval-ms:200}")
    public void pushState() {
        if (handler.getConnectedCount() == 0) return;

        try {
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("type", "ROBOT_STATE_UPDATE");
            message.put("timestamp", System.currentTimeMillis());

            List<Map<String, Object>> robots = fleetService.getAllRobots().stream()
                    .map(this::robotToMap)
                    .collect(Collectors.toList());
            message.put("robots", robots);

            List<Map<String, Object>> tasks = taskExecutionService.getActiveTasks().stream()
                    .map(this::taskToMap)
                    .collect(Collectors.toList());
            message.put("tasks", tasks);

            handler.broadcast(objectMapper.writeValueAsString(message));
        } catch (Exception e) {
            log.error("Failed to push WebSocket state: {}", e.getMessage());
        }
    }

    private Map<String, Object> robotToMap(VirtualRobot robot) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("robotCode", robot.getRobotCode());
        map.put("robotType", robot.getRobotType());
        map.put("status", robot.getStatus().name());
        map.put("x", robot.getCurrentPosition().getX());
        map.put("y", robot.getCurrentPosition().getY());
        map.put("rotation", robot.getCurrentPosition().getRotation());
        map.put("carriedContainerCode", robot.getCarriedContainerCode());
        map.put("taskCode", robot.getAssignedTaskCode());
        map.put("batteryLevel", robot.getBatteryLevel());
        return map;
    }

    private Map<String, Object> taskToMap(SimulatedTask task) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("taskCode", task.getTaskCode());
        map.put("status", task.getStatus().name());
        map.put("containerCode", task.getContainerCode());
        map.put("startLocation", task.getStartLocation());
        map.put("destination", task.getDestinations() != null && !task.getDestinations().isEmpty()
                ? task.getDestinations().iterator().next() : null);
        map.put("assignedRobot", task.getAssignedRobotCode());
        return map;
    }
}
