package org.openwes.ai.core.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.openwes.ai.api.dto.AnalysisQueryVO;
import org.openwes.ai.api.dto.AnalysisResult;
import org.openwes.ai.api.dto.CodeGenerationRequest;
import org.openwes.ai.api.dto.JsFunctionGenerationRequest;
import org.openwes.ai.core.service.AiChatService;
import org.openwes.common.utils.user.UserContext;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.sql.SQLException;

@RestController
@RequestMapping("ai")
@RequiredArgsConstructor
@Tag(name = "AI Module Api")
public class AiChatController {

    private final AiChatService aiService;

    @GetMapping("chat")
    public Flux<String> chat(@RequestParam(value = "message") String message) {
        String currentUser = UserContext.getCurrentUser();
        return aiService.chat(message, currentUser);
    }

    @PostMapping("analysis")
    @CrossOrigin
    public AnalysisResult sqlAnalysis(@RequestBody AnalysisQueryVO analysisQueryVO) throws SQLException {
        String currentUser = UserContext.getCurrentUser();
        return aiService.analysis(analysisQueryVO.getQuery(), currentUser);
    }

    @PostMapping("generateJsFunction")
    @CrossOrigin
    public Flux<String> generateJsFunction(@RequestBody JsFunctionGenerationRequest jsFunctionGenerationRequest) {
        String currentUser = UserContext.getCurrentUser();
        return aiService.generateJsFunction(jsFunctionGenerationRequest, currentUser);
    }

    @PostMapping("generateCode")
    @CrossOrigin
    public Flux<String> generateCode(@RequestBody CodeGenerationRequest codeGenerationRequest) {
        String currentUser = UserContext.getCurrentUser();
        return aiService.generateCode(codeGenerationRequest, currentUser);
    }

}
