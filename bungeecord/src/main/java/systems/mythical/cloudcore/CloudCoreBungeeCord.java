package systems.mythical.cloudcore;

import net.md_5.bungee.api.plugin.Plugin;
import systems.mythical.cloudcore.config.CloudCoreConfig;
import systems.mythical.cloudcore.database.DatabaseManager;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class CloudCoreBungeeCord extends Plugin {
    private CloudCoreConfig config;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        getLogger().info("Starting CloudCore BungeeCord Plugin - Database Connection Test");
        
        try {
            // Initialize configuration
            config = CloudCoreConfig.getInstance(getDataFolder(), getLogger(), false);
            getLogger().info("Configuration loaded successfully");
            
            // Initialize database connection
            databaseManager = new DatabaseManager(config, getLogger());
            getLogger().info("Database connection established successfully");
            
            // Test database connection
            testDatabaseConnection();
            
            getLogger().info("CloudCore BungeeCord Plugin enabled successfully with database connection!");
            
        } catch (Exception e) {
            getLogger().severe("Failed to enable CloudCore BungeeCord Plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        getLogger().info("CloudCore BungeeCord Plugin disabled");
    }
    
    private void testDatabaseConnection() {
        try (Connection connection = databaseManager.getConnection()) {
            getLogger().info("Database connection test successful!");
            getLogger().info("Connected to: " + config.getDatabaseHost() + ":" + config.getDatabasePort() + "/" + config.getDatabaseName());
        } catch (SQLException e) {
            getLogger().severe("Database connection test failed: " + e.getMessage());
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
