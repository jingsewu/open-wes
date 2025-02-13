package org.openwes.ai.core.service;

import org.openwes.ai.core.domain.DatabaseSchema;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DatabaseSchemaService {

    DatabaseSchema extractSchema() throws SQLException;

    List<Map<String, Object>> executeSql(String sql);
}
