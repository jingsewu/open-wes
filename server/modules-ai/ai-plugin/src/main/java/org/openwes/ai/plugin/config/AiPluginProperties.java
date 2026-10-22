package org.openwes.ai.plugin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai-plugin")
public class AiPluginProperties {

    /** Model id recorded on every AI revision; also sent as the per-call model option. */
    private String model = "GLM-4-air";

    /** Hard ceiling applied on top of the DB max_tokens_per_call value. */
    private int maxTokensCeiling = 8192;

    /** Wall-clock limit for one model call, including all streamed chunks. */
    private long requestTimeoutMs = 120_000;

    /**
     * Overall budget for one generation. Keep it below spring.mvc.async.request-timeout so a retry
     * never starts that cannot finish before MVC closes the SSE stream.
     */
    private long streamBudgetMs = 170_000;

    /** Opt-in: requires a configured Pinecone VectorStore. */
    private boolean groundingEnabled = false;

    private int groundingTopK = 3;
}
