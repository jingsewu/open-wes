package org.openwes.ai.plugin.domain.repository;

import org.openwes.ai.plugin.domain.entity.ScriptPluginAiGeneration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScriptPluginAiGenerationRepository extends JpaRepository<ScriptPluginAiGeneration, Long> {

    List<ScriptPluginAiGeneration> findTop50ByPluginKeyOrderByIdDesc(String pluginKey);
}
