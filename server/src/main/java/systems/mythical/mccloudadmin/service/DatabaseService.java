package systems.mythical.mccloudadmin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.mythical.mccloudadmin.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseService {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseService.class);
    private static DatabaseService instance;
    private final DatabaseConfig databaseConfig;

    private DatabaseService() {
        this.databaseConfig = DatabaseConfig.getInstance();
    }

    public static synchronized DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    public List<Map<String, Object>> listTables() {
        List<Map<String, Object>> tables = new ArrayList<>();
        
        try (Connection conn = databaseConfig.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            
            try (ResultSet rs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    Map<String, Object> tableInfo = new HashMap<>();
                    tableInfo.put("name", rs.getString("TABLE_NAME"));
                    tableInfo.put("type", rs.getString("TABLE_TYPE"));
                    tableInfo.put("schema", rs.getString("TABLE_SCHEM"));
                    tableInfo.put("remarks", rs.getString("REMARKS"));
                    
                    // Get column count
                    try (ResultSet columns = metaData.getColumns(catalog, null, rs.getString("TABLE_NAME"), "%")) {
                        int columnCount = 0;
                        while (columns.next()) {
                            columnCount++;
                        }
                        tableInfo.put("columnCount", columnCount);
                    }
                    
                    tables.add(tableInfo);
                }
            }
            
            logger.info("Successfully retrieved {} tables", tables.size());
            return tables;
        } catch (SQLException e) {
            logger.error("Error listing tables: {}", e.getMessage());
            throw new RuntimeException("Failed to list tables", e);
        }
    }

    public List<Map<String, Object>> getTableStructure(String tableName) {
        List<Map<String, Object>> columns = new ArrayList<>();
        
        try (Connection conn = databaseConfig.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = conn.getCatalog();
            
            try (ResultSet rs = metaData.getColumns(catalog, null, tableName, "%")) {
                while (rs.next()) {
                    Map<String, Object> columnInfo = new HashMap<>();
                    columnInfo.put("name", rs.getString("COLUMN_NAME"));
                    columnInfo.put("type", rs.getString("TYPE_NAME"));
                    columnInfo.put("size", rs.getInt("COLUMN_SIZE"));
                    columnInfo.put("nullable", rs.getBoolean("IS_NULLABLE"));
                    columnInfo.put("defaultValue", rs.getString("COLUMN_DEF"));
                    columnInfo.put("remarks", rs.getString("REMARKS"));
                    
                    columns.add(columnInfo);
                }
            }
            
            logger.info("Successfully retrieved structure for table: {}", tableName);
            return columns;
        } catch (SQLException e) {
            logger.error("Error getting table structure for {}: {}", tableName, e.getMessage());
            throw new RuntimeException("Failed to get table structure", e);
        }
    }
} 