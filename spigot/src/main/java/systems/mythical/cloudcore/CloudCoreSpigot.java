package systems.mythical.cloudcore;

import org.bukkit.plugin.java.JavaPlugin;
import systems.mythical.cloudcore.config.CloudCoreConfig;
import systems.mythical.cloudcore.database.DatabaseManager;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class CloudCoreSpigot extends JavaPlugin {
    private CloudCoreConfig config;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        getLogger().info("Starting CloudCore Spigot Plugin - Database Connection Test");
        
        try {
            // Initialize configuration
            config = CloudCoreConfig.getInstance(getDataFolder(), getLogger(), true);
            getLogger().info("Configuration loaded successfully");
            
            // Initialize database connection
            databaseManager = new DatabaseManager(config, getLogger());
            getLogger().info("Database connection established successfully");
            
            // Test database connection
            testDatabaseConnection();
            
            getLogger().info("CloudCore Spigot Plugin enabled successfully with database connection!");
            
        } catch (Exception e) {
            getLogger().severe("Failed to enable CloudCore Spigot Plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        getLogger().info("CloudCore Spigot Plugin disabled");
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
    
    public CloudCoreConfig getCloudCoreConfig() {
        return config;
    }
}
