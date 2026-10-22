package org.openwes.ai.plugin.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.zhipuai.ZhiPuAiChatOptions;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiPromptAssembler {

    private final AiPluginProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Prompt assemble(String requirement, ScriptPluginDTO plugin, ScriptPluginRevisionDTO baseRevision,
                           List<String> groundingSnippets, List<String> previousErrors, int maxTokens) {
        String app = plugin.getApplySystem();
        StringBuilder user = new StringBuilder();

        user.append("## Requirement\n").append(requirement).append("\n\n");
        user.append("## Plugin\n")
                .append("pluginKey: ").append(plugin.getPluginKey()).append('\n')
                .append("name: ").append(plugin.getName()).append('\n')
                .append("description: ").append(plugin.getDescription() == null ? "" : plugin.getDescription())
                .append('\n')
                .append("status: ").append(plugin.getStatus()).append('\n')
                .append("applyModule: ").append(plugin.getApplyModule() == null ? "" : plugin.getApplyModule())
                .append("\n\n");

        List<ExtensionType> allowedTypes = PluginContractCatalog.typesForApp(app);
        user.append("## Host contract (authoritative)\n")
                .append(PluginContractCatalog.toPromptFragment(app, allowedTypes)).append('\n');

        user.append("## Base revision\n")
                .append(baseRevision == null ? "No base revision; create the plugin from scratch."
                        : renderBaseRevision(baseRevision)).append("\n\n");

        user.append("## Retrieved reference snippets\n")
                .append(groundingSnippets.isEmpty() ? "(none)" : String.join("\n---\n", groundingSnippets))
                .append("\n\n");

        user.append("## Errors from previous attempts (fix every one)\n")
                .append(previousErrors.isEmpty() ? "(none)" : String.join("\n", previousErrors))
                .append('\n');

        ZhiPuAiChatOptions options = ZhiPuAiChatOptions.builder()
                .model(properties.getModel())
                .temperature(0.0)
                .maxTokens(maxTokens)
                .build();

        return new Prompt(List.of(
                new SystemMessage(PluginPromptTemplates.systemPrompt(app)),
                new UserMessage(user.toString())), options);
    }

    private String renderBaseRevision(ScriptPluginRevisionDTO revision) {
        Map<String, Object> base = new LinkedHashMap<>();
        base.put("revisionNo", revision.getRevisionNo());
        base.put("changeSummary", revision.getChangeSummary());
        base.put("scripts", revision.getScripts());
        base.put("extensions", revision.getExtensionPoints());
        base.put("configSchema", revision.getConfigSchema());
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(base);
        } catch (JsonProcessingException e) {
            // The base revision was already stored as a validated document; this cannot happen
            // for a revision loaded from the database.
            throw new IllegalStateException("cannot render base revision", e);
        }
    }
}
