package org.openwes.ai.core.service.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.openwes.ai.core.domain.DatabaseSchema;
import org.openwes.ai.core.domain.TableSchema;
import org.openwes.ai.core.service.DatabaseSchemaService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DatabaseSchemaServiceImpl implements DatabaseSchemaService {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public DatabaseSchema extractSchema() throws SQLException {
        DatabaseSchema schema = new DatabaseSchema();
        Map<String, TableSchema> tables = new HashMap<>();

        // Query to fetch tables directly from the openwes database
        String query = "SELECT TABLE_NAME,TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = Database()";

        List<Map<String, Object>> maps = jdbcTemplate.queryForList(query);
        for (Map<String, Object> map : maps) {
            String tableName = (String) map.get("TABLE_NAME");
            String tableComment = (String) map.get("TABLE_COMMENT");

            TableSchema tableSchema = new TableSchema();
            tableSchema.setName(tableName);
            tableSchema.setDescription(tableComment);

            // Get columns for each table using SHOW CREATE TABLE
            List<TableSchema.TableColumn> columns = buildColumns(tableName);
            tableSchema.setColumns(columns);

            // Get primary keys
            List<String> primaryKeys = new ArrayList<>();
            tableSchema.setPrimaryKeys(primaryKeys);

            // Get foreign keys
            Map<String, String> foreignKeys = new HashMap<>();
            tableSchema.setForeignKeys(foreignKeys);

            tables.put(tableName, tableSchema);
        }

        schema.setTables(tables);
        return schema;
    }

    private List<TableSchema.TableColumn> buildColumns(String tableName) throws SQLException {
        List<TableSchema.TableColumn> columns = Lists.newArrayList();

        String sql = "SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT " +
                "FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = ? AND TABLE_SCHEMA = DATABASE()";

        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, tableName);

        for (Map<String, Object> row : result) {
            String columnName = (String) row.get("COLUMN_NAME");
            String columnType = (String) row.get("COLUMN_TYPE");
            String columnComment = (String) row.get("COLUMN_COMMENT");
            columns.add(new TableSchema.TableColumn()
                    .setName(columnName)
                    .setType(columnType)
                    .setComment(columnComment));

        }
        return columns;
    }


    @Override
    public List<Map<String, Object>> executeSql(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
}
