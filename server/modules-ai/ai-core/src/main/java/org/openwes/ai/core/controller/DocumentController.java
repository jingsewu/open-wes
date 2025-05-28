package org.openwes.ai.core.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.openwes.ai.core.rag.DocumentationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("ai/document")
@RequiredArgsConstructor
@Tag(name = "AI Module Api")
public class DocumentController {

    private final DocumentationService documentationService;

    @PostMapping("/load-data")
    public void load() {
        documentationService.scrapeAndStoreDocumentation("https://docs.openwes.top");
    }
}
