package systems.mythical.mccloudadmin.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static DatabaseConfig instance;
    private final HikariDataSource dataSource;

    private DatabaseConfig() {
        ServerConfig config = ServerConfig.getInstance();
        this.dataSource = createDataSource(config);
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private HikariDataSource createDataSource(ServerConfig config) {
        HikariConfig hikariConfig = new HikariConfig();
        
        // Basic configuration
        hikariConfig.setJdbcUrl(config.getDatabaseUrl());
        hikariConfig.setUsername(config.getDatabaseUsername());
        hikariConfig.setPassword(config.getDatabasePassword());
        
        // Pool configuration
        hikariConfig.setMaximumPoolSize(config.getDatabaseMaxPoolSize());
        hikariConfig.setMinimumIdle(config.getDatabaseMinIdle());
        hikariConfig.setIdleTimeout(config.getDatabaseIdleTimeout());
        hikariConfig.setConnectionTimeout(config.getDatabaseConnectionTimeout());
        
        // Connection test configuration
        hikariConfig.setConnectionTestQuery("SELECT 1");
        
        // Additional MySQL-specific settings
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("useServerPrepStmts", "true");
        
        return new HikariDataSource(hikariConfig);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Database connection pool closed");
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            logger.info("Successfully connected to database");
            return true;
        } catch (SQLException e) {
            logger.error("Failed to connect to database: {}", e.getMessage());
            return false;
        }
    }
} 