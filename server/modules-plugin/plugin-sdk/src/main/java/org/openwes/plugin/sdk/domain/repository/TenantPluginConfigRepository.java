package org.openwes.plugin.sdk.domain.repository;

import org.openwes.plugin.sdk.domain.entity.TenantPluginConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantPluginConfigRepository extends JpaRepository<TenantPluginConfig, Long> {
    TenantPluginConfig findByPluginUniqueKey(String pluginUniqueKey);
}
