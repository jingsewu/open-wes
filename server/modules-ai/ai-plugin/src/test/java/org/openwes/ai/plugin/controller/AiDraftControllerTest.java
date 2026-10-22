package org.openwes.ai.plugin.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.domain.service.AiDraftGenerationService;
import org.openwes.ai.plugin.dto.AiDraftResult;
import org.openwes.ai.plugin.dto.AiDraftStreamEvent;
import org.openwes.ai.plugin.dto.AiGenerationDTO;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AiDraftControllerTest {

    private AiDraftGenerationService generationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        generationService = mock(AiDraftGenerationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AiDraftController(generationService)).build();
    }

    @Test
    void stream_writesNamedSseEventsWithJsonPayloads() throws Exception {
        AiDraftResult result = AiDraftResult.builder().pluginKey("packing-wave").revisionId(55L).build();
        when(generationService.stream(any())).thenReturn(Flux.just(
                AiDraftStreamEvent.chunk("hello"),
                AiDraftStreamEvent.stage("declarations"),
                AiDraftStreamEvent.result(result)));

        MvcResult started = mockMvc.perform(post("/plugin-platform/ai/draft/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pluginKey\":\"packing-wave\",\"requirement\":\"group orders\"}"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(started))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
                .andExpect(content().string(containsString("event:chunk")))
                .andExpect(content().string(containsString("event:stage")))
                .andExpect(content().string(containsString("event:result")))
                .andExpect(content().string(containsString("\"text\":\"hello\"")))
                .andExpect(content().string(containsString("\"revisionId\":55")));
    }

    @Test
    void stream_rejectsABlankRequirement() throws Exception {
        mockMvc.perform(post("/plugin-platform/ai/draft/stream")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pluginKey\":\"packing-wave\",\"requirement\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generations_returnsTheAuditView() throws Exception {
        when(generationService.generations("packing-wave")).thenReturn(List.of(
                AiGenerationDTO.builder().id(9L).pluginKey("packing-wave").status("SUCCEEDED").attempts(2).build()));

        mockMvc.perform(get("/plugin-platform/ai/generations/packing-wave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("0"))
                .andExpect(jsonPath("$.data[0].id").value(9))
                .andExpect(jsonPath("$.data[0].status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.data[0].attempts").value(2));
    }

    @Test
    void refreshCorpus_returnsTheDocumentCount() throws Exception {
        when(generationService.refreshCorpus()).thenReturn(12);

        mockMvc.perform(post("/plugin-platform/ai/corpus/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(12));
    }
}
