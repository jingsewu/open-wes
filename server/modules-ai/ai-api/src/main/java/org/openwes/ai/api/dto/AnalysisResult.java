package org.openwes.ai.api.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
public class AnalysisResult {
    private String sql;
    private String explanation;
    private List<Map<String, Object>> data;
    private List<ColumnMetadata> columns;

    @Data
    @Accessors(chain = true)
    public static class ColumnMetadata {
        private String name;
        private String dataType;
        private String description;
    }



}
