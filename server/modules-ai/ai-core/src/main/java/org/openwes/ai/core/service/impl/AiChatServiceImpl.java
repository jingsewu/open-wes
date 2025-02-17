package org.openwes.ai.core.service.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.ai.api.dto.AnalysisResult;
import org.openwes.ai.api.dto.CodeGenerationRequest;
import org.openwes.ai.api.dto.JsFunctionGenerationRequest;
import org.openwes.ai.api.utils.DatabaseSchemaUtils;
import org.openwes.ai.core.domain.DatabaseSchema;
import org.openwes.ai.core.service.AiChatService;
import org.openwes.ai.core.service.DatabaseSchemaService;
import org.openwes.ai.core.template.AiPromptTemplate;
import org.openwes.ai.core.tool.SystemConfigTool;
import org.openwes.common.utils.language.core.LanguageContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class AiChatServiceImpl implements AiChatService {

    private final ChatModel chatModel;
    private final DatabaseSchemaService databaseSchemaService;
    private final ChatMemory chatMemory;
    private final DataSource dataSource;
    private final SystemConfigTool systemConfigTool;

    @Override
    public Flux<String> generateSql(String message, String conversationId, String contextWithErrors) throws SQLException {

        DatabaseSchema schema = databaseSchemaService.extractSchema();
        String schemaPrompt = schema.createSchemaPrompt();

        PromptTemplate template = new PromptTemplate(AiPromptTemplate.SQL_PROMPT_TEMPLATE);
        template.add("schema", schemaPrompt);
        template.add("description", message);
        template.add("previousErrors", contextWithErrors);

        return executeAI(message, conversationId + "sql", template);
    }

    @Override
    public String chat(String message, String conversationId) {

        PromptTemplate template = new PromptTemplate(AiPromptTemplate.QA_PROMPT_TEMPLATE);
        template.add("question", message);
        template.add("language", LanguageContext.getLanguage());

        return executeAIAndReturnString(message, conversationId + "chat", template);
    }

    private String executeAIAndReturnString(String message, String conversationId, PromptTemplate template) {

        String relevantHistory = chatMemory.get(conversationId, 10)
                .stream()
                .map(this::formatMessage)
                .collect(Collectors.joining("\n"));

        template.add("context", relevantHistory);

        chatMemory.add(conversationId, new UserMessage(message));

        String content = ChatClient.create(chatModel).prompt(template.create()).tools(systemConfigTool).call().content();
        chatMemory.add(conversationId, new AssistantMessage(content));

        return content;

    }

    @Override
    public AnalysisResult analysis(String message, String currentUser) throws SQLException {

        List<String> previousErrors = new ArrayList<>();

        String sql = "";
        String explanation = "";
        List<Map<String, Object>> rows = new ArrayList<>();
        List<AnalysisResult.ColumnMetadata> columnMetadata = Lists.newArrayList();

        for (int i = 0; i < 3; i++) {

            String contextWithErrors = createContextWithErrors(previousErrors);

            Flux<String> flux = generateSql(message, currentUser, contextWithErrors);
            Mono<String> reduce = flux.map(v -> v).reduce(String::concat);

            try {
                String response = Objects.requireNonNull(reduce.block());
                sql = handleSQL(response);
                explanation = response.split("### EXPLANATION ###")[1].split("### END EXPLANATION ###")[0].trim();
                columnMetadata = DatabaseSchemaUtils.getColumnMetadata(sql, dataSource);
                log.info("SQL: {} , EXPLANATION: {}", sql, explanation);
                rows = databaseSchemaService.executeSql(sql);

                return new AnalysisResult().setColumns(columnMetadata).setSql(sql).setExplanation(explanation).setData(rows);
            } catch (Exception e) {
                log.error("Error executing SQL: {}", sql, e);
                previousErrors.add(e.getMessage());
            }
        }

        return new AnalysisResult().setColumns(columnMetadata).setSql(sql).setExplanation(explanation).setData(rows);
    }

    @Override
    public Flux<String> generateJsFunction(JsFunctionGenerationRequest jsFunctionGenerationRequest, String currentUser) {
        PromptTemplate template = new PromptTemplate(AiPromptTemplate.JAVA_SCRIPT_COVERT_PROMPT_TEMPLATE);
        template.add("inputFormat", jsFunctionGenerationRequest.getInputFormat());
        template.add("outputFormat", jsFunctionGenerationRequest.getOutputFormat());
        template.add("transformationRules", jsFunctionGenerationRequest.getTransformationRules());

        return executeAI("", currentUser, template);
    }

    @Override
    public Flux<String> generateCode(CodeGenerationRequest codeGenerationRequest, String currentUser) {
        PromptTemplate template = new PromptTemplate(AiPromptTemplate.JAVA_CODE_COMPLETION_PROMPT_TEMPLATE);
        template.add("codeContext", codeGenerationRequest.getCodeContext());
        template.add("lineContent", codeGenerationRequest.getLineContent());
        template.add("language", codeGenerationRequest.getLanguage());

        return executeAIWithoutMemory(currentUser, template);
    }

    private String createContextWithErrors(List<String> previousErrors) {
        if (previousErrors.isEmpty()) {
            return "";
        }

        return "\nPrevious failed attempts and their errors:\n" +
                String.join("\n", previousErrors);
    }

    private String handleSQL(String response) {

        String sql = response.split("### SQL ###")[1].split("### END SQL ###")[0].trim();

        if (sql.contains("```sql")) {
            return sql.substring(sql.indexOf("```sql") + 7, sql.length() - 3);
        }
        return sql;
    }

    private Flux<String> executeAI(String message, String conversationId, PromptTemplate template) {

        String relevantHistory = chatMemory.get(conversationId, 10)
                .stream()
                .map(this::formatMessage)
                .collect(Collectors.joining("\n"));

        // Add context to template
        template.add("context", relevantHistory);

        chatMemory.add(conversationId, new UserMessage(message));

        // Create a StringBuilder to accumulate the response
        StringBuilder fullResponse = new StringBuilder();

        return chatModel.stream(template.createMessage()).map(chunk -> {
            fullResponse.append(chunk);
            return chunk;
        }).doOnComplete(() -> {
            // Only save to chat memory once we have the complete response
            chatMemory.add(conversationId, new AssistantMessage(fullResponse.toString()));
        });
    }


    private Flux<String> executeAIWithoutMemory(String conversationId, PromptTemplate template) {
        return chatModel.stream(template.createMessage()).map(chunk -> chunk);
    }

    private String formatMessage(Message msg) {
        if (msg instanceof UserMessage) {
            return "User Query: " + msg.getText();
        } else if (msg instanceof AssistantMessage) {
            return "Generated SQL: " + msg.getText();
        }
        return msg.getText();
    }

}
