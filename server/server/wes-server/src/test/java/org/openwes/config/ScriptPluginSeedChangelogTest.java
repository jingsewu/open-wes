package org.openwes.config;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the Phase 3 workbench seed so a role or menu key cannot silently disappear. */
class ScriptPluginSeedChangelogTest {

    private static final String CHANGELOG =
            "server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261023.xml";
    private static final String MASTER =
            "server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml";

    private static String read(String relativePath) throws Exception {
        Path directory = Path.of("").toAbsolutePath();
        while (directory != null) {
            Path candidate = directory.resolve(relativePath);
            if (Files.exists(candidate)) {
                return Files.readString(candidate, StandardCharsets.UTF_8);
            }
            directory = directory.getParent();
        }
        throw new IllegalStateException(relativePath + " not found above "
                + Path.of("").toAbsolutePath());
    }

    @Test
    void masterRegistersTheWorkbenchChangelog() throws Exception {
        assertTrue(read(MASTER).contains("db.changelog-20261023.xml"));
    }

    @Test
    void seedsBothWorkbenchRoles() throws Exception {
        String xml = read(CHANGELOG);

        assertTrue(xml.contains("'pluginDeveloper'"));
        assertTrue(xml.contains("'pluginAdmin'"));
        assertTrue(xml.contains("1900000100"));
        assertTrue(xml.contains("1900000101"));
    }

    @Test
    void seedsTheAppMenuAndGatewayPermissions() throws Exception {
        String xml = read(CHANGELOG);

        assertTrue(xml.contains("'plugin-platform'"));
        assertTrue(xml.contains("'pluginPlatform.system.title'"));
        assertTrue(xml.contains("'/plugin-platform/plugins'"));
        assertTrue(xml.contains("'/plugin-platform/audit'"));
        assertTrue(xml.contains("'get:/plugin-platform/'"));
        assertTrue(xml.contains("'post:/plugin-platform/'"));
    }

    @Test
    void grantsTheMenuSetToBothRolesAndAuditToAdmin() throws Exception {
        String xml = read(CHANGELOG);

        for (String id : List.of("1900000301", "1900000302", "1900000303", "1900000304",
                "1900000311", "1900000312", "1900000313", "1900000314", "1900000315")) {
            assertTrue(xml.contains(id), "missing role-menu grant " + id);
        }
    }
}
