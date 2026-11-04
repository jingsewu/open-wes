package org.openwes.ai.plugin.prompt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginPromptTemplatesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void outputExample_isValidJsonInThePlatformDtoShape() throws Exception {
        JsonNode example = objectMapper.readTree(PluginPromptTemplates.outputExample());

        assertTrue(example.hasNonNull("meta"));
        assertTrue(example.get("extensions").isArray());
        assertEquals("HTTP_ENDPOINT", example.get("extensions").get(0).get("type").asText());
        assertEquals("handle", example.get("extensions").get(0).get("method").asText());
        assertEquals("GET", example.get("extensions").get(0).get("httpMethod").asText());
        assertTrue(example.get("scripts").has("Status.groovy"));
        assertTrue(example.get("scripts").get("Status.groovy").asText().contains("Object handle(Object body, Object ctx)"));
    }

    @Test
    void systemPrompt_carriesTheVersionRulesAndOutputExample() {
        String prompt = PluginPromptTemplates.systemPrompt("wes");

        assertTrue(prompt.contains("plugin-draft-v1"));
        assertTrue(prompt.contains("Return exactly one JSON object"));
        assertTrue(prompt.contains(PluginPromptTemplates.outputExample()));
        assertTrue(prompt.contains("ctx.configString"));
        assertTrue(prompt.contains("never contains credentials"));
        assertTrue(prompt.contains("CUSTOM_API is available on station only"));
        assertTrue(prompt.contains("decide internally"));
        assertTrue(prompt.contains("hard execution timeout"));
        assertTrue(prompt.contains("let the exception propagate"));
    }

    @Test
    void wesExample_showsAWaveScript() {
        assertTrue(PluginPromptTemplates.examples("wes").contains("OUTBOUND_WAVE_WAVE_ACTION"));
        assertFalse(PluginPromptTemplates.examples("station").contains("OUTBOUND_WAVE_WAVE_ACTION"));
        assertTrue(PluginPromptTemplates.examples("station").contains("ctx.bean('ptl')"));
        assertFalse(PluginPromptTemplates.examples("station").contains("ctx.ptl()"));
    }
}
