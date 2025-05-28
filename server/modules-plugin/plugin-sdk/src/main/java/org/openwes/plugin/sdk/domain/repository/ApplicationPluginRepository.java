package org.openwes.plugin.sdk.domain.repository;

import org.openwes.plugin.api.constants.ApplicationPluginStatusEnum;
import org.openwes.plugin.sdk.domain.entity.ApplicationPlugin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationPluginRepository extends JpaRepository<ApplicationPlugin, Long> {

    ApplicationPlugin findByPluginUniqueKey(String pluginUniqueKey);

    List<ApplicationPlugin> findAllByStatus(ApplicationPluginStatusEnum status);
}
