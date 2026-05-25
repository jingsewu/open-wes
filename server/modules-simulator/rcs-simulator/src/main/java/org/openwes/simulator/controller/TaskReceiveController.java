package org.openwes.simulator.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.simulator.domain.SimulatedTask;
import org.openwes.simulator.service.TaskExecutionService;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskReceiveController {

    private final TaskExecutionService taskExecutionService;

    @PostMapping("/create")
    public Map<String, String> createTasks(@RequestBody CallbackMessage<List<CreateTaskPayload>> message) {
        log.info("Received {} container tasks from WES", message.getData().size());
        for (CreateTaskPayload payload : message.getData()) {
            SimulatedTask task = new SimulatedTask();
            task.setTaskCode(payload.getTaskCode());
            task.setTaskGroupCode(payload.getTaskGroupCode());
            task.setContainerCode(payload.getContainerCode());
            task.setContainerFace(payload.getContainerFace());
            task.setStartLocation(payload.getStartLocation());
            task.setDestinations(payload.getDestinations());
            task.setPriority(payload.getTaskPriority() != null ? payload.getTaskPriority() : 0);
            task.setGroupPriority(payload.getTaskGroupPriority() != null ? payload.getTaskGroupPriority() : 0);
            task.setBusinessTaskType(payload.getBusinessTaskType());
            task.setContainerTaskType(payload.getContainerTaskType());
            task.setCustomerTaskId(payload.getCustomerTaskId());

            if (task.getTaskCode() == null) {
                task.setTaskCode("SIM-" + System.currentTimeMillis() + "-" + payload.getCustomerTaskId());
            }

            taskExecutionService.submitTask(task);
        }
        return Map.of("status", "ok");
    }

    @PostMapping("/cancel")
    public Map<String, String> cancelTasks(@RequestBody CallbackMessage<List<String>> message) {
        log.info("Cancel request for tasks: {}", message.getData());
        for (String taskCode : message.getData()) {
            taskExecutionService.cancelTask(taskCode);
        }
        return Map.of("status", "ok");
    }

    @PostMapping("/improve-priority")
    public Map<String, String> improvePriority(@RequestBody CallbackMessage<Map<String, Object>> message) {
        log.info("Priority improvement request: {}", message.getData());
        // For demo: log and acknowledge, no complex re-ordering
        return Map.of("status", "ok");
    }

    @PostMapping("/release")
    public Map<String, String> releaseTasks(@RequestBody CallbackMessage<List<String>> message) {
        log.info("Release request for tasks: {}", message.getData());
        return Map.of("status", "ok");
    }

    @PostMapping("/container-leave")
    public Map<String, String> containerLeave(@RequestBody CallbackMessage<Object> message) {
        log.info("Container leave notification: {}", message.getData());
        return Map.of("status", "ok");
    }

    @PostMapping("/call-robot")
    public Map<String, String> callRobot(@RequestBody CallbackMessage<Object> message) {
        log.info("Call robot request: {}", message.getData());
        return Map.of("status", "ok");
    }

    @Data
    public static class CallbackMessage<T> {
        private Long messageId;
        private T data;
    }

    @Data
    public static class CreateTaskPayload {
        private Long customerTaskId;
        private String businessTaskType;
        private String containerTaskType;
        private String taskCode;
        private String taskGroupCode;
        private Integer taskPriority;
        private Integer taskGroupPriority;
        private String containerCode;
        private String containerFace;
        private String containerSpecCode;
        private String startLocation;
        private Collection<String> destinations;
    }
}
