package org.openwes.script.plugin.core.domain.repository;

import org.openwes.script.plugin.core.domain.entity.ScriptPluginExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ScriptPluginExecutionLogRepository
        extends JpaRepository<ScriptPluginExecutionLog, Long>,
        JpaSpecificationExecutor<ScriptPluginExecutionLog> {

    long countByPluginKey(String pluginKey);

    @Transactional
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

    @Query("""
            select l.extensionType as extensionType,
                   count(l) as total,
                   sum(case when l.success = false then 1 else 0 end) as failed,
                   avg(l.durationMs) as avgDurationMs,
                   max(l.durationMs) as maxDurationMs
            from ScriptPluginExecutionLog l
            where l.pluginKey = :pluginKey and l.createdAt >= :since
            group by l.extensionType
            """)
    List<ExecutionTypeStatsView> statsByType(@Param("pluginKey") String pluginKey,
                                             @Param("since") long since);
}
