package org.openwes.script.plugin.core.domain.repository;

import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.entity.ScriptPlugin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ScriptPluginRepository extends JpaRepository<ScriptPlugin, Long> {

    Optional<ScriptPlugin> findByPluginKey(String pluginKey);

    List<ScriptPlugin> findByApplySystemAndStatusOrderByPluginKeyAsc(String applySystem, ScriptPluginStatusEnum status);

    // No status filter: the registry version must stay monotonic across disables, otherwise a
    // version-poll on a node whose copy still has the plugin enabled would never see the change.
    @Query("select coalesce(max(p.updateTime), 0) from ScriptPlugin p where p.applySystem = :applySystem")
    long findMaxUpdateTime(@Param("applySystem") String applySystem);
}
