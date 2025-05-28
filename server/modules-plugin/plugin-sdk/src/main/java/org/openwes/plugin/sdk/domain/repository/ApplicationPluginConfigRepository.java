package org.openwes.plugin.sdk.domain.repository;

import org.openwes.plugin.sdk.domain.entity.ApplicationPluginConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationPluginConfigRepository extends JpaRepository<ApplicationPluginConfig, Long> {
    ApplicationPluginConfig findByPluginUniqueKey(String pluginUniqueKey);
}
