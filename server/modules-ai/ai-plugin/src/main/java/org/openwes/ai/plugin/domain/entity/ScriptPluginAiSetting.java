package org.openwes.ai.plugin.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "p_script_plugin_ai_setting", indexes = {
        @Index(unique = true, name = "uk_ai_setting_singleton", columnList = "singletonKey")})
@DynamicUpdate
public class ScriptPluginAiSetting extends UpdateUserPO {

    public static final String DEFAULT_KEY = "DEFAULT";

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, length = 32, unique = true)
    @Comment("Singleton key; always DEFAULT")
    private String singletonKey = DEFAULT_KEY;

    @Column(nullable = false)
    @Comment("Per-user generations per day; 0 disables the limit")
    private Integer dailyGenerationLimit = 20;

    @Column(nullable = false)
    @Comment("Output token cap per model call")
    private Integer maxTokensPerCall = 4096;

    @Column(nullable = false)
    @Comment("Retry cap; values above 3 are clamped to 3")
    private Integer maxAttempts = 3;

    @Version
    private Long version;
}
