package org.openwes.plugin.sdk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.openwes.plugin.api.constants.PluginCacheConstants;
import org.openwes.plugin.api.dto.PluginConfigDTO;
import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class PluginService {

    private final PluginManager pluginManager;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${pf4j.plugin.jar.dir:plugins/jar}")
    private String pluginDir;

    @Value("${pf4j.plugin.config.dir:plugins/config}")
    private String pluginConfigDir;

    public void install(String filePath) {
        String pluginId = pluginManager.loadPlugin(Path.of(filePath));

        if (pluginId == null) {
            throw new IllegalArgumentException("plugin: " + filePath + " can not be found");
        }

        pluginManager.startPlugin(pluginId);

    }

    @CacheEvict(cacheNames = PluginCacheConstants.PLUGIN_CONFIG_CACHE_KEY, key = "#pluginConfigDTO.pluginUniqueKey")
    public void config(PluginConfigDTO pluginConfigDTO) {
        try {
            Path path = getConfigPath(pluginConfigDTO.getPluginUniqueKey());

            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path.getParent());
                    Files.createFile(path);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to create config file for plugin: " + pluginConfigDTO.getPluginUniqueKey(), e);
                }
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), pluginConfigDTO.getConfigInfo());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save config for plugin: " + pluginConfigDTO.getPluginUniqueKey(), e);
        }

    }

    @Cacheable(cacheNames = PluginCacheConstants.PLUGIN_CONFIG_CACHE_KEY, key = "#pluginUniqueKey")
    public String get(String pluginUniqueKey) {
        try {
            Path path = getConfigPath(pluginUniqueKey);
            if (!Files.exists(path)) {
                return "";
            }

            return objectMapper.readValue(path.toFile(), String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config for plugin: " + pluginUniqueKey, e);
        }
    }

    public void start(String pluginUniqueKey) {
        pluginManager.startPlugin(pluginUniqueKey);
    }

    public void stop(String pluginUniqueKey) {
        pluginManager.stopPlugin(pluginUniqueKey);
    }

    private Path getConfigPath(String pluginId) {
        return Paths.get(pluginConfigDir, pluginId + ".json");
    }

    public void uninstall(String pluginUniqueKey) throws IOException {

        PluginWrapper plugin = pluginManager.getPlugin(pluginUniqueKey);

        pluginManager.unloadPlugin(pluginUniqueKey);

        //TODO Does this need to backup rather than remove?
        Path path = getConfigPath(pluginUniqueKey);
        if (Files.exists(path)) {
            Files.delete(path);
        }

        if (Files.exists(plugin.getPluginPath())) {
            Files.delete(plugin.getPluginPath());
        }
    }
}
