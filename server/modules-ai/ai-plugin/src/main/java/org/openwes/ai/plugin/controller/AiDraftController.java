package org.openwes.ai.plugin.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.ai.plugin.domain.service.AiDraftGenerationService;
import org.openwes.ai.plugin.dto.AiDraftRequest;
import org.openwes.ai.plugin.dto.AiDraftStreamEvent;
import org.openwes.ai.plugin.dto.AiGenerationDTO;
import org.openwes.common.utils.http.Response;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI draft surface. The SSE endpoint is intentionally not wrapped in {@code Response}: the stream
 * carries its own result/error events. Authentication is the gateway JWT today; workbench role
 * checks run inside {@link AiDraftGenerationService} so the same rules apply to direct service
 * calls.
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin-platform/ai")
@Tag(name = "Script Plugin AI Api")
public class AiDraftController {

    private final AiDraftGenerationService generationService;

    @PostMapping(value = "/draft/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<AiDraftStreamEvent>> stream(@Valid @RequestBody AiDraftRequest request) {
        return generationService.stream(request)
                .map(event -> ServerSentEvent.<AiDraftStreamEvent>builder(event)
                        .event(event.getType())
                        .build());
    }

    @GetMapping("/generations/{pluginKey}")
    public Response<List<AiGenerationDTO>> generations(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(generationService.generations(pluginKey));
    }

    @PostMapping("/corpus/refresh")
    public Response<Integer> refreshCorpus() {
        return Response.success(generationService.refreshCorpus());
    }
}
