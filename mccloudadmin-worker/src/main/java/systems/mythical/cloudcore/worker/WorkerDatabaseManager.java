package systems.mythical.cloudcore.worker;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

public class WorkerDatabaseManager {
    private final HikariDataSource dataSource;
    private final Logger logger;

    public WorkerDatabaseManager(WorkerConfig config, Logger logger) {
        this.logger = logger;
        
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                config.getDatabaseHost(),
                config.getDatabasePort(),
                config.getDatabaseName()));
        hikariConfig.setUsername(config.getDatabaseUsername());
        hikariConfig.setPassword(config.getDatabasePassword());
        hikariConfig.setMaximumPoolSize(config.getDatabasePoolSize());
        hikariConfig.setMinimumIdle(2);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        this.dataSource = new HikariDataSource(hikariConfig);
        logger.info("Database connection pool initialized");
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Batch insert chat messages into the database
     */
    public void batchInsertChatMessages(List<ChatMessage> messages) {
        if (messages.isEmpty()) {
            return;
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            
            String sql = "INSERT INTO mccloudadmin_chatlogs (uuid, content, server, created_at) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                for (ChatMessage message : messages) {
                    stmt.setString(1, message.getPlayerUuid().toString());
                    stmt.setString(2, message.getContent());
                    stmt.setString(3, message.getServer());
                    stmt.setTimestamp(4, new java.sql.Timestamp(message.getTimestamp()));
                    stmt.addBatch();
                }
                
                int[] results = stmt.executeBatch();
                conn.commit();
                
                int successCount = 0;
                for (int result : results) {
                    if (result >= 0) {
                        successCount++;
                    }
                }
                
                logger.info("Successfully inserted " + successCount + " out of " + messages.size() + " chat messages");
                
                if (successCount < messages.size()) {
                    logger.warning("Failed to insert " + (messages.size() - successCount) + " chat messages");
                }
            }
        } catch (SQLException e) {
            logger.severe("Error batch inserting chat messages: " + e.getMessage());
            throw new RuntimeException("Failed to insert chat messages", e);
        }
    }

    /**
     * Check if the database table exists and create it if it doesn't
     */
    public void ensureTableExists() {
        try (Connection conn = getConnection()) {
            String createTableSql = """
                CREATE TABLE IF NOT EXISTS mccloudadmin_chatlogs (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    uuid VARCHAR(36) NOT NULL,
                    content TEXT NOT NULL,
                    server VARCHAR(64) NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    deleted ENUM('true', 'false') DEFAULT 'false',
                    INDEX idx_uuid (uuid),
                    INDEX idx_created_at (created_at),
                    INDEX idx_server (server)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """;
            
            try (PreparedStatement stmt = conn.prepareStatement(createTableSql)) {
                stmt.execute();
                logger.info("Ensured mccloudadmin_chatlogs table exists");
            }
        } catch (SQLException e) {
            logger.severe("Error ensuring table exists: " + e.getMessage());
            throw new RuntimeException("Failed to create table", e);
        }
    }

    /**
     * Close the database connection pool
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }

    /**
     * Chat message data class
     */
    public static class ChatMessage {
        private final java.util.UUID playerUuid;
        private final String content;
        private final String server;
        private final long timestamp;

        public ChatMessage(java.util.UUID playerUuid, String content, String server, long timestamp) {
            this.playerUuid = playerUuid;
            this.content = content;
            this.server = server;
            this.timestamp = timestamp;
        }

        public java.util.UUID getPlayerUuid() { return playerUuid; }
        public String getContent() { return content; }
        public String getServer() { return server; }
        public long getTimestamp() { return timestamp; }
    }
} 