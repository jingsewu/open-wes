package org.openwes.script.plugin.core.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;

/**
 * Append-only operation trail. No setters and no update path: rows are written once by the audit
 * service. Payloads (scripts, prompts, config values, request bodies, test arguments/results) are
 * deliberately absent.
 */
@Getter
@Entity
@Table(name = "p_script_plugin_operation_audit", indexes = {
        @Index(name = "idx_script_audit_plugin", columnList = "pluginKey,id"),
        @Index(name = "idx_script_audit_created", columnList = "createdAt")})
public class ScriptPluginOperationAudit {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Plugin key; unknown when the request key was blank")
    private String pluginKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Comment("Audited operation")
    private ScriptPluginAuditActionEnum action;

    @Column
    @Comment("Target revision, when the action has one")
    private Long revisionId;

    @Column(nullable = false, length = 64)
    @Comment("Acting username, or system for internal work")
    private String operator;

    @Column(nullable = false)
    @Comment("Whether the operation succeeded")
    private boolean success;

    @Column(length = 1024)
    @Comment("Failure reason or short summary; never payload data")
    private String detail;

    @Column(nullable = false)
    @Comment("Creation timestamp in epoch milliseconds")
    private Long createdAt;

    private ScriptPluginOperationAudit() {
    }

    public static ScriptPluginOperationAudit record(String pluginKey, ScriptPluginAuditActionEnum action,
                                                    Long revisionId, String operator, boolean success,
                                                    String detail) {
        ScriptPluginOperationAudit audit = new ScriptPluginOperationAudit();
        audit.pluginKey = pluginKey;
        audit.action = action;
        audit.revisionId = revisionId;
        audit.operator = operator;
        audit.success = success;
        audit.detail = detail;
        audit.createdAt = System.currentTimeMillis();
        return audit;
    }
}
