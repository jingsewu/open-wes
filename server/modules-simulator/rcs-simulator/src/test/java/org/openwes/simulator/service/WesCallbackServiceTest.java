package org.openwes.simulator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.simulator.config.WesProperties;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class WesCallbackServiceTest {

    private WesCallbackService callbackService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        WesProperties props = new WesProperties();
        props.setCallbackUrl("http://localhost:8090");
        WesProperties.Api api = new WesProperties.Api();
        api.setContainerArrive("/api/ems/container/arrive");
        api.setContainerLeave("/api/ems/container/leave");
        api.setTaskStatusUpdate("/api/ems/container/task/status");
        props.setApi(api);

        RestTemplate restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        callbackService = new WesCallbackService(props, restTemplate);
    }

    @Test
    void reportContainerArrived_sendsPostToWes() {
        mockServer.expect(requestTo("http://localhost:8090/api/ems/container/arrive"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess());

        callbackService.reportContainerArrived(
                "C-001", "WS-01", "AGV-001", "KIVA",
                "task-group-1", "WS-01", null, null);

        mockServer.verify();
    }

    @Test
    void reportTaskStatus_sendsPostToWes() {
        mockServer.expect(requestTo("http://localhost:8090/api/ems/container/task/status"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        callbackService.reportTaskStatus("TASK-001", "PROCESSING", "AGV-001", "C-001", "LOC-001");

        mockServer.verify();
    }

    @Test
    void reportContainerLeave_sendsPostToWes() {
        mockServer.expect(requestTo("http://localhost:8090/api/ems/container/leave"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess());

        callbackService.reportContainerLeave("C-001", "WS-01", "TASK-001", null);

        mockServer.verify();
    }
}
