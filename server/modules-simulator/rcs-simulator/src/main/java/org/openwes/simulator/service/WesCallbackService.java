package org.openwes.simulator.service;

import lombok.extern.slf4j.Slf4j;
import org.openwes.simulator.config.WesProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
public class WesCallbackService {

    private final WesProperties wesProperties;
    private final RestTemplate restTemplate;

    public WesCallbackService(WesProperties wesProperties, RestTemplate restTemplate) {
        this.wesProperties = wesProperties;
        this.restTemplate = restTemplate;
    }

    public void reportContainerArrived(String containerCode, String locationCode,
                                        String robotCode, String robotType,
                                        String groupCode, String workLocationCode,
                                        Long workStationId, Long warehouseAreaId) {
        Map<String, Object> containerDetail = new LinkedHashMap<>();
        containerDetail.put("containerCode", containerCode);
        containerDetail.put("locationCode", locationCode);
        containerDetail.put("robotCode", robotCode);
        containerDetail.put("robotType", robotType);
        containerDetail.put("groupCode", groupCode);

        Map<String, Object> event = new LinkedHashMap<>();
        event.put("containerDetails", List.of(containerDetail));
        event.put("workLocationCode", workLocationCode);
        if (workStationId != null) {
            event.put("workStationId", workStationId);
        }
        if (warehouseAreaId != null) {
            event.put("warehouseAreaId", warehouseAreaId);
        }

        post(wesProperties.getApi().getContainerArrive(), event);
    }

    public void reportTaskStatus(String taskCode, String status,
                                  String robotCode, String containerCode, String locationCode) {
        Map<String, Object> update = new LinkedHashMap<>();
        update.put("taskCode", taskCode);
        update.put("taskStatus", status);
        if (robotCode != null) update.put("robotCode", robotCode);
        if (containerCode != null) update.put("containerCode", containerCode);
        if (locationCode != null) update.put("locationCode", locationCode);

        post(wesProperties.getApi().getTaskStatusUpdate(), List.of(update));
    }

    public void reportContainerLeave(String containerCode, String locationCode,
                                      String taskCode, Long workStationId) {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("containerCode", containerCode);
        detail.put("locationCode", locationCode);
        detail.put("taskCode", taskCode);
        detail.put("operationType", "LEAVE");

        Map<String, Object> operation = new LinkedHashMap<>();
        operation.put("containerOperationDetails", List.of(detail));
        if (workStationId != null) {
            operation.put("workStationId", workStationId);
        }

        post(wesProperties.getApi().getContainerLeave(), operation);
    }

    private void post(String apiPath, Object body) {
        String url = wesProperties.getCallbackUrl() + apiPath;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(url, entity, String.class);
            log.info("Callback sent to WES: {} {}", "POST", url);
        } catch (Exception e) {
            log.error("Failed to callback WES at {}: {}", url, e.getMessage());
        }
    }

    @org.springframework.context.annotation.Bean
    public static RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
