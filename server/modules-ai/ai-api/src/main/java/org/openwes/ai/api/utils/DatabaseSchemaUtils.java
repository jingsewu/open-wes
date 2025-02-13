package org.openwes.ai.api.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.openwes.ai.api.dto.AnalysisResult;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Set;

public class DatabaseSchemaUtils {

    private static final Set<String> numberClassSet = Sets.newHashSet(
            "java.lang.Long",
            "java.lang.Integer",
            "java.lang.Byte",
            "java.lang.Short");

    public static String getColumnType(String columnClassName, String columnName) {
        if (columnName.contains("time")) {
            return "TIMESTAMP";
        }
        if (numberClassSet.contains(columnClassName)) {
            return "NUMBER";
        }
        return "STRING";
    }

    public static List<AnalysisResult.ColumnMetadata> getColumnMetadata(String sql, DataSource dataSource) {

        List<AnalysisResult.ColumnMetadata> columns = Lists.newArrayList();
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery(sql);

                ResultSetMetaData metaData = resultSet.getMetaData();
                int columnCount = metaData.getColumnCount();

                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnLabel(i);
                    String columnClassName = metaData.getColumnClassName(i);

                    AnalysisResult.ColumnMetadata dpColumnVO = new AnalysisResult.ColumnMetadata()
                            .setName(columnName).setDataType(DatabaseSchemaUtils.getColumnType(columnClassName, columnName));
                    columns.add(dpColumnVO);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e.getMessage());
        }

        return columns;
    }
}
