package org.openwes.ai.plugin.domain.repository;

import org.openwes.ai.plugin.domain.entity.ScriptPluginAiSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScriptPluginAiSettingRepository extends JpaRepository<ScriptPluginAiSetting, Long> {

    Optional<ScriptPluginAiSetting> findBySingletonKey(String singletonKey);
}
