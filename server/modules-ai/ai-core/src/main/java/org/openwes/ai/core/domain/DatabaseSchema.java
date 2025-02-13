package org.openwes.ai.core.domain;

import lombok.Data;

import java.util.Map;

@Data
public class DatabaseSchema {

    private Map<String, TableSchema> tables;

    public String createSchemaPrompt() {
        StringBuilder prompt = new StringBuilder("Database Schema:\n\n");

        if (this.tables == null) {
            return prompt.toString();
        }

        this.tables.forEach((tableName, tableInfo) -> {
            prompt.append("Table: ").append(tableName).append(", description: ").append(tableInfo.getDescription()).append("\n");
            prompt.append("Columns:\n");

            tableInfo.getColumns().forEach(tableColumn -> {
                prompt.append("- ").append(tableColumn.getName())
                        .append(" (").append(tableColumn.getType()).append(")")
                        .append(" ").append(tableColumn.getComment()).append(" ");
                if (tableInfo.getPrimaryKeys().contains(tableColumn.getName())) {
                    prompt.append(" [PRIMARY KEY]");
                }
                if (tableInfo.getForeignKeys().containsKey(tableColumn.getName())) {
                    prompt.append(" [FOREIGN KEY -> ")
                            .append(tableInfo.getForeignKeys().get(tableColumn.getName()))
                            .append("]");
                }
                prompt.append("\n");
            });
            prompt.append("\n");
        });

        return prompt.toString();
    }
}
