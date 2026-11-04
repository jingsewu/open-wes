package org.openwes.script.plugin.core.domain.repository;

import org.openwes.script.plugin.core.domain.entity.ScriptPluginConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScriptPluginConfigRepository extends JpaRepository<ScriptPluginConfig, Long> {

    Optional<ScriptPluginConfig> findByPluginId(Long pluginId);
}
