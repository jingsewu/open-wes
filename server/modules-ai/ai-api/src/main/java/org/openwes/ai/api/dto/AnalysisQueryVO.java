package org.openwes.ai.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class AnalysisQueryVO {
    private String query;
}
