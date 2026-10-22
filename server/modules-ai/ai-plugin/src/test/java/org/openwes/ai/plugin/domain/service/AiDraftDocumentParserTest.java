package org.openwes.ai.plugin.domain.service;

import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.domain.model.AiDraftDocument;
import org.openwes.ai.plugin.exception.AiDraftParseException;
import org.openwes.ai.plugin.prompt.PluginPromptTemplates;
import org.openwes.script.plugin.api.enums.ExtensionType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiDraftDocumentParserTest {

    private final AiDraftDocumentParser parser = new AiDraftDocumentParser();

    @Test
    void parsesThePromptOutputExample() {
        AiDraftDocument document = parser.parse(PluginPromptTemplates.outputExample(), "wes");

        assertEquals("Packing status endpoint", document.getMeta().getName());
        assertEquals(1, document.getExtensions().size());
        assertEquals(ExtensionType.HTTP_ENDPOINT, document.getExtensions().get(0).getType());
        assertEquals("GET", document.getExtensions().get(0).getHttpMethod());
        assertTrue(document.getScripts().get("Status.groovy").contains("Object handle(Object body, Object ctx)"));
    }

    @Test
    void stripsMarkdownFencesAndSurroundingProse() {
        String raw = "Here is the draft:\n```json\n" + PluginPromptTemplates.outputExample() + "```\nDone.";

        AiDraftDocument document = parser.parse(raw, "wes");

        assertEquals("Status.groovy", document.getScripts().keySet().iterator().next());
    }

    @Test
    void stripsGroovyFencesBeforeTheJsonBlock() {
        String raw = "```groovy\nclass Scratch {}\n```\n```json\n" + PluginPromptTemplates.outputExample() + "```";

        AiDraftDocument document = parser.parse(raw, "station");

        assertEquals(1, document.getExtensions().size());
    }

    @Test
    void rejectsTextWithoutAJsonObject() {
        AiDraftParseException e = assertThrows(AiDraftParseException.class,
                () -> parser.parse("I cannot help with that.", "wes"));
        assertTrue(e.getMessage().contains("no JSON object"));
    }

    @Test
    void rejectsMissingScripts() {
        String raw = """
                {"meta":{"name":"x","changeSummary":"y"},"extensions":[],"configSchema":[],"scripts":{}}
                """;
        AiDraftParseException e = assertThrows(AiDraftParseException.class, () -> parser.parse(raw, "wes"));
        assertTrue(e.getMessage().contains("at least one script"));
    }

    @Test
    void rejectsMissingExtensions() {
        String raw = """
                {"meta":{"name":"x","changeSummary":"y"},"extensions":[],"configSchema":[],
                 "scripts":{"A.groovy":"class A { Object handle(Object b, Object ctx) { return 1 } }"}}
                """;
        AiDraftParseException e = assertThrows(AiDraftParseException.class, () -> parser.parse(raw, "wes"));
        assertTrue(e.getMessage().contains("at least one extension"));
    }

    @Test
    void rejectsUnknownScriptReferences() {
        String raw = """
                {"meta":{"name":"x","changeSummary":"y"},
                 "extensions":[{"type":"HTTP_ENDPOINT","script":"Missing.groovy","method":"handle","httpMethod":"GET","path":"/x"}],
                 "configSchema":[],"scripts":{"A.groovy":"class A { Object handle(Object b, Object ctx) { return 1 } }"}}
                """;
        AiDraftParseException e = assertThrows(AiDraftParseException.class, () -> parser.parse(raw, "wes"));
        assertTrue(e.getMessage().contains("unknown script 'Missing.groovy'"));
    }

    @Test
    void rejectsHttpEndpointWithoutMethod() {
        String raw = """
                {"meta":{"name":"x","changeSummary":"y"},
                 "extensions":[{"type":"HTTP_ENDPOINT","script":"A.groovy","method":"handle","path":"/x"}],
                 "configSchema":[],"scripts":{"A.groovy":"class A { Object handle(Object b, Object ctx) { return 1 } }"}}
                """;
        AiDraftParseException e = assertThrows(AiDraftParseException.class, () -> parser.parse(raw, "wes"));
        assertTrue(e.getMessage().contains("httpMethod"));
    }

    @Test
    void rejectsCustomApiOnWes() {
        String raw = """
                {"meta":{"name":"x","changeSummary":"y"},
                 "extensions":[{"type":"CUSTOM_API","script":"A.groovy","method":"execute","customApiCode":"X"}],
                 "configSchema":[],"scripts":{"A.groovy":"class A { Object execute(Object p, Object ctx) { return 1 } }"}}
                """;
        AiDraftParseException e = assertThrows(AiDraftParseException.class, () -> parser.parse(raw, "wes"));
        assertTrue(e.getMessage().contains("not supported for app wes"));
    }

    @Test
    void rejectsUnknownLifecycleEntity() {
        String raw = """
                {"meta":{"name":"x","changeSummary":"y"},
                 "extensions":[{"type":"LIFECYCLE_LISTENER","script":"A.groovy","method":"afterStatusChange","entityName":"Nope"}],
                 "configSchema":[],"scripts":{"A.groovy":"class A { Object afterStatusChange(Object a, Object b, Object c, Object ctx) { return 1 } }"}}
                """;
        AiDraftParseException e = assertThrows(AiDraftParseException.class, () -> parser.parse(raw, "wes"));
        assertTrue(e.getMessage().contains("entityName"));
        assertTrue(e.getMessage().contains("OutboundPlanOrder"));
    }

    @Test
    void rejectsNonJsonOutputWithTheParserError() {
        AiDraftParseException e = assertThrows(AiDraftParseException.class,
                () -> parser.parse("{ not json }", "wes"));
        assertTrue(e.getMessage().contains("not a valid draft JSON"));
    }
}
