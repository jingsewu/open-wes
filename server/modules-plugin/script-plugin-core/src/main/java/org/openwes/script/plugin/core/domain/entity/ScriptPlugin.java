package org.openwes.script.plugin.core.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "p_script_plugin", indexes = {
        @Index(unique = true, name = "uk_script_plugin_key", columnList = "pluginKey"),
        @Index(name = "idx_script_plugin_app_status", columnList = "applySystem,status")
})
@DynamicUpdate
public class ScriptPlugin extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    @Comment("插件key，声明式，全局唯一")
    private String pluginKey;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(nullable = false, length = 512)
    private String description = "";

    /** Target app: wes / station — see ScriptPluginApiConstants. */
    @Column(nullable = false, length = 32)
    private String applySystem;

    @Column(length = 64)
    private String applyModule = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ScriptPluginStatusEnum status;

    /** JSON array of ExtensionDeclarationDTO. */
    @Column(nullable = false, columnDefinition = "json")
    private String extensionPoints;

    /** JSON array of ConfigFieldDTO, nullable. */
    @Column(columnDefinition = "json")
    private String configSchema;

    @Column(nullable = false, length = 16)
    private String sdkVersion;

    private Long currentRevisionId;

    @Column(length = 16)
    private String lastValidationStatus;

    @Column(columnDefinition = "text")
    private String lastValidationError;

    private Long lastValidationAt;

    @Version
    private Long version;

    public void enable() {
        if (status != ScriptPluginStatusEnum.DRAFT && status != ScriptPluginStatusEnum.DISABLED) {
            throw new IllegalStateException("only DRAFT or DISABLED plugins can be enabled");
        }
        this.status = ScriptPluginStatusEnum.ENABLED;
    }

    public void disable() {
        if (status != ScriptPluginStatusEnum.ENABLED) {
            throw new IllegalStateException("only ENABLED plugins can be disabled");
        }
        this.status = ScriptPluginStatusEnum.DISABLED;
    }

    public void archive() {
        if (status == ScriptPluginStatusEnum.ARCHIVED) {
            throw new IllegalStateException("plugin is already archived");
        }
        this.status = ScriptPluginStatusEnum.ARCHIVED;
    }
}
