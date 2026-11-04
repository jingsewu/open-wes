package org.openwes.ai.plugin.domain.model;

/**
 * Effective generation policy for one request. Values from the DB are clamped here so no caller
 * can exceed the product guardrails: at most 3 attempts and a positive token cap.
 */
public record AiSettings(int dailyGenerationLimit, int maxTokensPerCall, int maxAttempts) {

    public static final int DEFAULT_MAX_TOKENS = 4096;
    public static final int MAX_ATTEMPTS_CAP = 3;

    public AiSettings {
        dailyGenerationLimit = Math.max(0, dailyGenerationLimit);
        maxAttempts = Math.min(MAX_ATTEMPTS_CAP, Math.max(1, maxAttempts));
        maxTokensPerCall = maxTokensPerCall > 0 ? maxTokensPerCall : DEFAULT_MAX_TOKENS;
    }

    public static AiSettings defaults() {
        return new AiSettings(20, DEFAULT_MAX_TOKENS, MAX_ATTEMPTS_CAP);
    }
}
