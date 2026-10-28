package org.openwes.ai.plugin.prompt;

import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.config.AiPluginProperties;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatOptions;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiPromptAssemblerTest {

    private final AiPluginProperties properties = new AiPluginProperties();
    private final AiPromptAssembler assembler = new AiPromptAssembler(properties);

    private ScriptPluginDTO plugin() {
        ScriptPluginDTO plugin = new ScriptPluginDTO();
        plugin.setPluginKey("packing-wave");
        plugin.setName("Packing wave");
        plugin.setDescription("Groups packing orders into waves");
        plugin.setApplySystem("wes");
        plugin.setStatus(ScriptPluginStatusEnum.DISABLED);
        return plugin;
    }

    private ScriptPluginRevisionDTO baseRevision() {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        declaration.setScript("Wave.groovy");
        declaration.setMethod("wave");
        ScriptPluginRevisionDTO revision = new ScriptPluginRevisionDTO();
        revision.setRevisionNo(3);
        revision.setChangeSummary("initial");
        revision.setScripts(Map.of("Wave.groovy", "class Wave { Object wave(Object o, Object ctx) { return o } }"));
        revision.setExtensionPoints(List.of(declaration));
        revision.setConfigSchema(List.of(new ConfigFieldDTO()));
        return revision;
    }

    @Test
    void assemble_usesTemperatureZeroAndTheConfiguredModel() {
        properties.setModel("deepseek-chat");

        Prompt prompt = assembler.assemble("group orders", plugin(), null, List.of(), List.of(), 2048);

        OpenAiChatOptions options = assertInstanceOf(OpenAiChatOptions.class, prompt.getOptions());
        assertEquals("deepseek-chat", options.getModel());
        assertEquals(0.0, options.getTemperature());
        assertEquals(2048, options.getMaxTokens());
    }

    @Test
    void assemble_includesContractRequirementBaseRevisionGroundingAndErrors() {
        Prompt prompt = assembler.assemble("group orders into waves of ten", plugin(), baseRevision(),
                List.of("reference snippet"), List.of("declarations: method mismatch"), 4096);

        List<Message> messages = prompt.getInstructions();
        assertEquals(2, messages.size());
        assertInstanceOf(SystemMessage.class, messages.get(0));
        String user = messages.get(1).getText();

        assertTrue(user.contains("group orders into waves of ten"));
        assertTrue(user.contains("packing-wave"));
        assertTrue(user.contains("Contract version: 1.0"));
        assertTrue(user.contains("OUTBOUND_WAVE_WAVE_ACTION"));
        assertTrue(user.contains("\"revisionNo\" : 3"));
        assertTrue(user.contains("reference snippet"));
        assertTrue(user.contains("declarations: method mismatch"));
    }

    @Test
    void assemble_omitsEmptySectionsCleanly() {
        Prompt prompt = assembler.assemble("status endpoint", plugin(), null, List.of(), List.of(), 1024);

        String user = prompt.getInstructions().get(1).getText();
        assertTrue(user.contains("(none)"));
        assertTrue(user.contains("No base revision"));
    }
}
