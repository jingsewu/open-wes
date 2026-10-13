package org.openwes.script.plugin.core.domain.repository;

import org.openwes.script.plugin.core.domain.entity.ScriptPluginExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ScriptPluginExecutionLogRepository extends JpaRepository<ScriptPluginExecutionLog, Long> {

    long countByPluginKey(String pluginKey);

    @Modifying
    @Query(value = """
            DELETE FROM p_script_plugin_execution_log
            WHERE plugin_key = :pluginKey
              AND id NOT IN (SELECT id FROM (
                    SELECT id FROM p_script_plugin_execution_log
                    WHERE plugin_key = :pluginKey
                    ORDER BY id DESC LIMIT :keep) newest)
            """, nativeQuery = true)
    int pruneToKeep(@Param("pluginKey") String pluginKey, @Param("keep") int keep);
}
