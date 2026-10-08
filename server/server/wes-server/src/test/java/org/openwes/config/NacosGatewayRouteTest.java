package org.openwes.config;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the gateway seed config so a route cannot silently point at a non-existent service. */
class NacosGatewayRouteTest {

    private static String seedConfig() throws Exception {
        Path directory = Path.of("").toAbsolutePath();
        while (directory != null) {
            Path candidate = directory.resolve("initdb.d").resolve("nacos_config.sql");
            if (Files.exists(candidate)) {
                return Files.readString(candidate, StandardCharsets.UTF_8);
            }
            directory = directory.getParent();
        }
        throw new IllegalStateException("initdb.d/nacos_config.sql not found above "
                + Path.of("").toAbsolutePath());
    }

    @Test
    void pluginRoute_pointsAtTheWesService() throws Exception {
        String sql = seedConfig();
        // the YAML is embedded in a single-line INSERT, so newlines are the literal characters '\' 'n'
        int start = sql.indexOf("- id: plugin\\n");
        int end = sql.indexOf("- id: api-platform\\n");
        assertTrue(start >= 0 && end > start, "plugin route block not found in nacos_config.sql");
        String pluginRoute = sql.substring(start, end);

        assertFalse(pluginRoute.contains("lb://plugin"), "no service named 'plugin' exists");
        assertTrue(pluginRoute.contains("uri: lb://wes"));
        assertTrue(pluginRoute.contains("Path=/plugin/**"));
    }
}
