package org.openwes.script.plugin.api.contract;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.RepositoryRoot;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PluginDocumentationConsistencyTest {

    private static final String BEGIN = "<!-- BEGIN GENERATED: script-plugin-archetypes -->";
    private static final String END = "<!-- END GENERATED: script-plugin-archetypes -->";

    @Test
    void generatedArchetypeTable_matchesTheContractCatalog() throws IOException {
        String docs = readDocs().replace("\r\n", "\n");
        int begin = docs.indexOf(BEGIN);
        int end = docs.indexOf(END);

        assertTrue(begin >= 0, "backend.md is missing " + BEGIN);
        assertTrue(end > begin, "backend.md is missing " + END + " after the begin marker");

        String documented = docs.substring(begin + BEGIN.length(), end).trim();
        assertEquals(PluginContractCatalog.toMarkdownTable().trim(), documented,
                "Regenerate the block from PluginContractCatalog.toMarkdownTable()");
    }

    @Test
    void backendDocumentation_doesNotReferenceRemovedApis() throws IOException {
        String docs = readDocs();

        assertFalse(docs.contains("IContainerTaskGeneratorAction"),
                "backend.md must not reference the deleted IContainerTaskGeneratorAction");
        assertFalse(docs.contains("getGeneratorPlugin"),
                "backend.md must not reference the deleted getGeneratorPlugin accessor");
        assertFalse(docs.contains("PluginUtils.getExtractObject"),
                "lookup is PluginSdkUtils.getExtractObject; PluginUtils.getExtractObject does not exist");
    }

    private String readDocs() throws IOException {
        return Files.readString(RepositoryRoot.find().resolve("docs/standards/backend.md"),
                StandardCharsets.UTF_8);
    }
}
