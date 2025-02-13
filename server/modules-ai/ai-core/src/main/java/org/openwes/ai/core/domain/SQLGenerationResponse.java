package org.openwes.ai.core.domain;

import lombok.Data;

@Data
public class SQLGenerationResponse {
    private String sql;
    private String executionPlan;
    private boolean valid;
}
