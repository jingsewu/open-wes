package org.openwes.script.plugin.core.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.script.plugin.api.enums.ExtensionType;

@Data
@Entity
@Table(name = "p_script_plugin_execution_log", indexes = {
        @Index(name = "idx_script_exec_plugin", columnList = "pluginKey,id")})
public class ScriptPluginExecutionLog {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, length = 64)
    private String pluginKey;

    @Column(nullable = false)
    private Long revisionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ExtensionType extensionType;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false)
    private long durationMs;

    @Column(length = 1024)
    private String errorMessage;

    @Column(nullable = false)
    private Long createdAt;
}
