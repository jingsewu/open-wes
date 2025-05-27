package org.openwes.plugin.sdk.domain.repository;

import org.openwes.plugin.api.constants.TenantPluginStatusEnum;
import org.openwes.plugin.sdk.domain.entity.TenantPlugin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantPluginRepository extends JpaRepository<TenantPlugin, Long> {

    TenantPlugin findByPluginUniqueKey(String pluginUniqueKey);

    List<TenantPlugin> findAllByStatus(TenantPluginStatusEnum status);
}
