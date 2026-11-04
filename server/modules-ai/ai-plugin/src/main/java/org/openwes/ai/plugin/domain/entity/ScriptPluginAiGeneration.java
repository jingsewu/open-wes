package org.openwes.ai.plugin.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.ai.plugin.domain.enums.AiGenerationStatusEnum;
import org.openwes.common.utils.id.IdGenerator;

@Data
@Entity
@Table(name = "p_script_plugin_ai_generation", indexes = {
        @Index(name = "idx_ai_generation_plugin", columnList = "pluginKey,id"),
        @Index(name = "idx_ai_generation_user", columnList = "userName,createdAt")})
public class ScriptPluginAiGeneration {

    private static final int MAX_ERROR_LENGTH = 1024;

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, length = 64)
    private String pluginKey;

    @Column(nullable = false, length = 64)
    private String userName;

    @Column(length = 64)
    private String model;

    @Column(length = 32)
    private String promptVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("RUNNING/SUCCEEDED/FAILED/CANCELLED")
    private AiGenerationStatusEnum status;

    @Column(nullable = false)
    private int attempts;

    private Integer promptTokens;

    private Integer completionTokens;

    private Long durationMs;

    @Column(length = MAX_ERROR_LENGTH)
    private String errorMessage;

    @Column(nullable = false)
    private Long createdAt;

    private Long finishedAt;

    public static ScriptPluginAiGeneration start(String pluginKey, String userName,
                                                 String model, String promptVersion) {
        ScriptPluginAiGeneration row = new ScriptPluginAiGeneration();
        row.pluginKey = pluginKey;
        row.userName = userName;
        row.model = model;
        row.promptVersion = promptVersion;
        row.status = AiGenerationStatusEnum.RUNNING;
        row.attempts = 0;
        row.createdAt = System.currentTimeMillis();
        return row;
    }

    public void recordAttempt(int attempts) {
        if (status != AiGenerationStatusEnum.RUNNING) {
            throw new IllegalStateException("generation is already finished");
        }
        this.attempts = attempts;
    }

    public void succeed(int attempts, Integer promptTokens, Integer completionTokens, long durationMs) {
        finish(AiGenerationStatusEnum.SUCCEEDED, attempts, null, durationMs);
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
    }

    public void fail(int attempts, String errorMessage, long durationMs) {
        finish(AiGenerationStatusEnum.FAILED, attempts, errorMessage, durationMs);
    }

    public void cancel(int attempts, long durationMs) {
        finish(AiGenerationStatusEnum.CANCELLED, attempts, null, durationMs);
    }

    private void finish(AiGenerationStatusEnum finalStatus, int attempts, String error, long durationMs) {
        if (status != AiGenerationStatusEnum.RUNNING) {
            throw new IllegalStateException("generation is already finished");
        }
        this.status = finalStatus;
        this.attempts = attempts;
        this.durationMs = durationMs;
        this.errorMessage = truncate(error);
        this.finishedAt = System.currentTimeMillis();
    }

    private String truncate(String error) {
        if (error == null) {
            return null;
        }
        return error.length() <= MAX_ERROR_LENGTH ? error : error.substring(0, MAX_ERROR_LENGTH);
    }
}
