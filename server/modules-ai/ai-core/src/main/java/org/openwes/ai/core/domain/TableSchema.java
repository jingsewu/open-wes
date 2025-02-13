package org.openwes.ai.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

@Data
public class TableSchema {
    private String name;
    private String description;
    private List<String> primaryKeys;
    private Map<String, String> foreignKeys;
    private List<TableColumn> columns;


    @Data
    @Accessors(chain = true)
    public static class TableColumn {
        private String name;
        private String type;
        private String comment;
    }

}
