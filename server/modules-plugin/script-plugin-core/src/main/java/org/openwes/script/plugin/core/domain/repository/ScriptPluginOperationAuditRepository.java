package org.openwes.script.plugin.core.domain.repository;

import org.openwes.script.plugin.core.domain.entity.ScriptPluginOperationAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScriptPluginOperationAuditRepository
        extends JpaRepository<ScriptPluginOperationAudit, Long> {

    Page<ScriptPluginOperationAudit> findByPluginKeyOrderByIdDesc(String pluginKey, Pageable pageable);

    Page<ScriptPluginOperationAudit> findAllByOrderByIdDesc(Pageable pageable);
}
