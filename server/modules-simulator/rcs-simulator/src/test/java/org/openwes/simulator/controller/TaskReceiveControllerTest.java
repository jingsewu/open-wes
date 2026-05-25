package org.openwes.simulator.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskReceiveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createTask_returns200() throws Exception {
        String body = """
                {
                    "messageId": 1,
                    "data": [{
                        "customerTaskId": 100,
                        "businessTaskType": "PICKING",
                        "containerTaskType": "OUTBOUND",
                        "taskCode": "TEST-001",
                        "taskPriority": 10,
                        "taskGroupPriority": 10,
                        "containerCode": "C-001",
                        "startLocation": "A01-01",
                        "destinations": ["WS-01"]
                    }]
                }
                """;

        mockMvc.perform(post("/api/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    void cancelTask_returns200() throws Exception {
        String body = """
                {
                    "messageId": 2,
                    "data": ["NONEXISTENT-TASK"]
                }
                """;

        mockMvc.perform(post("/api/tasks/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }
}
