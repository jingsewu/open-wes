package org.openwes.plugin.sdk.domain.service;

import lombok.RequiredArgsConstructor;
import org.openwes.plugin.api.constants.PluginCacheConstants;
import org.openwes.plugin.api.constants.ApplicationPluginStatusEnum;
import org.openwes.plugin.api.dto.PluginConfigDTO;
import org.openwes.plugin.sdk.domain.entity.ApplicationPlugin;
import org.openwes.plugin.sdk.domain.entity.ApplicationPluginConfig;
import org.openwes.plugin.sdk.domain.repository.ApplicationPluginConfigRepository;
import org.openwes.plugin.sdk.domain.repository.ApplicationPluginRepository;
import org.openwes.plugin.sdk.domain.transfer.ApplicationPluginConfigTransfer;
import org.pf4j.PluginManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PluginService {

    private final ApplicationPluginRepository pluginRepository;
    private final PluginManager pluginManager;
    private final ApplicationPluginConfigRepository pluginConfigRepository;
    private final ApplicationPluginConfigTransfer pluginConfigTransfer;

    @CacheEvict(cacheNames = PluginCacheConstants.PLUGIN_STARTED_CACHE_KEY)
    public void install(String filePath) {
        String pluginId = pluginManager.loadPlugin(Path.of(filePath));

        if (pluginId == null) {
            throw new IllegalArgumentException("plugin: " + filePath + " can not be found");
        }

        pluginManager.startPlugin(pluginId);

        ApplicationPlugin plugin = new ApplicationPlugin();
        plugin.setPluginUniqueKey(pluginId);
        plugin.setStatus(ApplicationPluginStatusEnum.STARTED);
        pluginRepository.save(plugin);
    }

    @CacheEvict(cacheNames = PluginCacheConstants.PLUGIN_CONFIG_CACHE_KEY, key = "#pluginConfigDTO.pluginUniqueKey")
    public void config(PluginConfigDTO pluginConfigDTO) {
        ApplicationPluginConfig tenantPluginConfig = pluginConfigTransfer.toDO(pluginConfigDTO);
        pluginConfigRepository.save(tenantPluginConfig);
    }

    @Cacheable(cacheNames = PluginCacheConstants.PLUGIN_CONFIG_CACHE_KEY, key = "#pluginUniqueKey")
    public ApplicationPluginConfig get(String pluginUniqueKey) {
        return pluginConfigRepository.findByPluginUniqueKey(pluginUniqueKey);
    }

    @CacheEvict(cacheNames = PluginCacheConstants.PLUGIN_STARTED_CACHE_KEY)
    public void start(String pluginUniqueKey) {
        ApplicationPlugin plugin = pluginRepository.findByPluginUniqueKey(pluginUniqueKey);
        plugin.start();
        pluginRepository.save(plugin);
    }

    @CacheEvict(cacheNames = PluginCacheConstants.PLUGIN_STARTED_CACHE_KEY)
    public void stop(String pluginUniqueKey) {
        ApplicationPlugin plugin = pluginRepository.findByPluginUniqueKey(pluginUniqueKey);
        plugin.stop();
        pluginRepository.save(plugin);
    }

    @CacheEvict(cacheNames = PluginCacheConstants.PLUGIN_STARTED_CACHE_KEY)
    public List<ApplicationPlugin> getAllStartedPlugins() {
        return pluginRepository.findAllByStatus(ApplicationPluginStatusEnum.STARTED);
    }
}
