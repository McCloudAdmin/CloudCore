package systems.mythical.cloudcore.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import systems.mythical.cloudcore.core.CloudCore;
import systems.mythical.cloudcore.database.DatabaseManager;

public class CloudCoreSpigot extends JavaPlugin {
    private CloudCore cloudCore;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        // Initialize CloudCore
        cloudCore = new CloudCore(getDataFolder(), getLogger());
        
        // Initialize database connection
        databaseManager = new DatabaseManager(cloudCore.getConfig(), getLogger());
        
        getLogger().info("CloudCore Spigot plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        if (cloudCore != null) {
            cloudCore.shutdown();
        }
        getLogger().info("CloudCore Spigot plugin has been disabled!");
    }
} 