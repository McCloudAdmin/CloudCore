package systems.mythical.cloudcore.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import systems.mythical.cloudcore.config.CloudCoreConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseManager {
    private final HikariDataSource dataSource;
    private final Logger logger;

    public DatabaseManager(CloudCoreConfig config, Logger logger) {
        this.logger = logger;
        
        // Explicitly load MySQL driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
        
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s",
                config.getDatabaseHost(),
                config.getDatabasePort(),
                config.getDatabaseName()));
        hikariConfig.setUsername(config.getDatabaseUsername());
        hikariConfig.setPassword(config.getDatabasePassword());
        
        // HikariCP specific settings
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setIdleTimeout(300000);
        hikariConfig.setConnectionTimeout(10000);
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        try {
            this.dataSource = new HikariDataSource(hikariConfig);
            // Test the connection immediately
            try (Connection conn = dataSource.getConnection()) {
                if (!conn.isValid(5)) {
                    throw new SQLException("Initial connection test failed - connection is not valid");
                }
            }
            logger.info("Database connection pool initialized successfully!");
        } catch (SQLException e) {
            String errorMessage = String.format(
                "CRITICAL ERROR: Failed to initialize database connection!\n" +
                "Connection Details:\n" +
                "- Host: %s\n" +
                "- Port: %d\n" +
                "- Database: %s\n" +
                "- Username: %s\n" +
                "Error: %s",
                config.getDatabaseHost(),
                config.getDatabasePort(),
                config.getDatabaseName(),
                config.getDatabaseUsername(),
                e.getMessage()
            );
            
            // Log the error multiple times to ensure visibility
            for (int i = 0; i < 5; i++) {
                logger.severe("================================================");
                logger.severe(errorMessage);
                logger.severe("================================================");
            }
            
            // Throw a runtime exception to prevent the server from starting
            throw new RuntimeException("Database connection failed - Server cannot start", e);
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            Connection conn = dataSource.getConnection();
            if (!conn.isValid(5)) {
                String error = "Database connection is not valid!";
                logger.severe("================================================");
                logger.severe(error);
                logger.severe("================================================");
                throw new SQLException(error);
            }
            return conn;
        } catch (SQLException e) {
            String error = "Failed to get database connection: " + e.getMessage();
            logger.severe("================================================");
            logger.severe(error);
            logger.severe("================================================");
            throw e;
        }
    }

    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                dataSource.close();
                logger.info("Database connection pool closed successfully!");
            } catch (Exception e) {
                logger.severe("Error closing database connection pool: " + e.getMessage());
            }
        }
    }
} 