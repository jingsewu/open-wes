package org.openwes.script.plugin.core.domain.repository;

import org.openwes.script.plugin.core.domain.entity.ScriptPluginRevision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ScriptPluginRevisionRepository extends JpaRepository<ScriptPluginRevision, Long> {

    Optional<ScriptPluginRevision> findTopByPluginIdOrderByRevisionNoDesc(Long pluginId);

    List<ScriptPluginRevision> findAllByPluginIdOrderByRevisionNoDesc(Long pluginId);
}
