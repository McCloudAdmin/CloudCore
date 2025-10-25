package systems.mythical.cloudcore;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import systems.mythical.cloudcore.config.CloudCoreConfig;
import systems.mythical.cloudcore.database.DatabaseManager;

import javax.inject.Inject;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

@Plugin(id = "cloudcore-velocity", name = "CloudCore-Velocity", version = "1.0-SNAPSHOT", 
        description = "CloudCore Velocity Plugin with Database Connection", authors = {"Mythical Systems"})
public class CloudCoreVelocity {
    private final ProxyServer server;
    private final Logger logger;
    private CloudCoreConfig config;
    private DatabaseManager databaseManager;

    @Inject
    public CloudCoreVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Starting CloudCore Velocity Plugin - Database Connection Test");
        
        try {
            // Initialize configuration
            config = CloudCoreConfig.getInstance(new File("plugins/CloudCore-Velocity"), logger, false);
            logger.info("Configuration loaded successfully");
            
            // Initialize database connection
            databaseManager = new DatabaseManager(config, logger);
            logger.info("Database connection established successfully");
            
            // Test database connection
            testDatabaseConnection();
            
            logger.info("CloudCore Velocity Plugin enabled successfully with database connection!");
            
        } catch (Exception e) {
            logger.severe("Failed to enable CloudCore Velocity Plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        logger.info("CloudCore Velocity Plugin disabled");
    }
    
    private void testDatabaseConnection() {
        try (Connection connection = databaseManager.getConnection()) {
            logger.info("Database connection test successful!");
            logger.info("Connected to: " + config.getDatabaseHost() + ":" + config.getDatabasePort() + "/" + config.getDatabaseName());
        } catch (SQLException e) {
            logger.severe("Database connection test failed: " + e.getMessage());
            throw new RuntimeException("Database connection test failed", e);
        }
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public CloudCoreConfig getConfig() {
        return config;
    }
}
