package org.openwes.plugin.sdk.domain.service;

import org.openwes.plugin.api.constants.PluginCacheConstants;
import org.openwes.plugin.api.constants.TenantPluginStatusEnum;
import org.openwes.plugin.api.dto.TenantPluginConfigDTO;
import org.openwes.plugin.sdk.domain.entity.TenantPlugin;
import org.openwes.plugin.sdk.domain.entity.TenantPluginConfig;
import org.openwes.plugin.sdk.domain.repository.TenantPluginConfigRepository;
import org.openwes.plugin.sdk.domain.repository.TenantPluginRepository;
import org.openwes.plugin.sdk.domain.transfer.TenantPluginConfigTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantPluginService {

    private final TenantPluginConfigTransfer tenantPluginConfigTransfer;
    private final TenantPluginConfigRepository tenantPluginConfigRepository;
    private final TenantPluginRepository tenantPluginRepository;

    @CacheEvict(cacheNames = PluginCacheConstants.TENANT_PLUGIN_CONFIG_CACHE_KEY, key = "#tenantPluginConfigDTO.pluginUniqueKey")
    public void config(TenantPluginConfigDTO tenantPluginConfigDTO) {
        TenantPluginConfig tenantPluginConfig = tenantPluginConfigTransfer.toDO(tenantPluginConfigDTO);
        tenantPluginConfigRepository.save(tenantPluginConfig);
    }

    @Cacheable(cacheNames = PluginCacheConstants.TENANT_PLUGIN_CONFIG_CACHE_KEY, key = "#pluginUniqueKey")
    public TenantPluginConfig get(String pluginUniqueKey) {
        return tenantPluginConfigRepository.findByPluginUniqueKey(pluginUniqueKey);
    }

    @Cacheable(cacheNames = PluginCacheConstants.TENANT_STARTED_PLUGIN_CACHE_KEY)
    public List<String> getStartedTenantPluginIds() {
        List<TenantPlugin> tenantPlugins = tenantPluginRepository.findAllByStatus(TenantPluginStatusEnum.STARTED);
        return tenantPlugins.stream().map(TenantPlugin::getPluginUniqueKey).toList();
    }

    @CacheEvict(cacheNames = PluginCacheConstants.TENANT_STARTED_PLUGIN_CACHE_KEY)
    public void start(String pluginUniqueKey) {
        TenantPlugin tenantPlugin = tenantPluginRepository.findByPluginUniqueKey(pluginUniqueKey);
        tenantPlugin.start();
        tenantPluginRepository.save(tenantPlugin);
    }

    @CacheEvict(cacheNames = PluginCacheConstants.TENANT_STARTED_PLUGIN_CACHE_KEY)
    public void stop(String pluginUniqueKey) {
        TenantPlugin tenantPlugin = tenantPluginRepository.findByPluginUniqueKey(pluginUniqueKey);
        tenantPlugin.stop();
        tenantPluginRepository.save(tenantPlugin);
    }
}
