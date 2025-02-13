package org.openwes.ai.core.service;

import org.openwes.ai.api.dto.AnalysisResult;
import org.openwes.ai.api.dto.CodeGenerationRequest;
import org.openwes.ai.api.dto.JsFunctionGenerationRequest;
import reactor.core.publisher.Flux;

import java.sql.SQLException;

public interface AiChatService {

    Flux<String> generateSql(String message, String conversationId, String contextWithErrors) throws SQLException;

    Flux<String> chat(String message, String conversationId);

    AnalysisResult analysis(String message, String currentUser) throws SQLException;

    Flux<String> generateJsFunction(JsFunctionGenerationRequest jsFunctionGenerationRequest, String currentUser);

    Flux<String> generateCode(CodeGenerationRequest codeGenerationRequest, String currentUser);
}
