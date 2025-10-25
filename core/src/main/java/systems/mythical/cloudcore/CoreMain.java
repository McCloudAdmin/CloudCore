package systems.mythical.cloudcore;

import systems.mythical.cloudcore.config.CloudCoreConfig;
import systems.mythical.cloudcore.database.DatabaseManager;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class CoreMain {
    private static final Logger logger = Logger.getLogger("CloudCore");
    private static CloudCoreConfig config;
    private static DatabaseManager databaseManager;

    public static void main(String[] args) {
        logger.info("Starting CloudCore - Database Connection Test");
        
        try {
            // Initialize configuration
            config = CloudCoreConfig.getInstance(new File("."), logger, true);
            logger.info("Configuration loaded successfully");
            
            // Initialize database connection
            databaseManager = new DatabaseManager(config, logger);
            logger.info("Database connection established successfully");
            
            // Test database connection
            testDatabaseConnection();
            
            logger.info("CloudCore started successfully with database connection!");
            
        } catch (Exception e) {
            logger.severe("Failed to start CloudCore: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    private static void testDatabaseConnection() {
        try (Connection connection = databaseManager.getConnection()) {
            logger.info("Database connection test successful!");
            logger.info("Connected to: " + config.getDatabaseHost() + ":" + config.getDatabasePort() + "/" + config.getDatabaseName());
        } catch (SQLException e) {
            logger.severe("Database connection test failed: " + e.getMessage());
            throw new RuntimeException("Database connection test failed", e);
        }
    }
    
    public static DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public static CloudCoreConfig getConfig() {
        return config;
    }
    
    public static void shutdown() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        logger.info("CloudCore shutdown complete");
    }
}
