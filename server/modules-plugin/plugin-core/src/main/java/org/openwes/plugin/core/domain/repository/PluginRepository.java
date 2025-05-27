package org.openwes.plugin.core.domain.repository;

import org.openwes.plugin.api.constants.PluginStatusEnum;
import org.openwes.plugin.core.domain.entity.Plugin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PluginRepository extends JpaRepository<Plugin, Long> {

    List<Plugin> findByCode(String code);

    List<Plugin> findAllByPluginStatus(PluginStatusEnum pluginStatus);
}
