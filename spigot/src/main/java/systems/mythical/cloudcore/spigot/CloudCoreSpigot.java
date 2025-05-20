package systems.mythical.cloudcore.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import systems.mythical.cloudcore.core.CloudCore;

public class CloudCoreSpigot extends JavaPlugin {
    private CloudCore core;
    
    @Override
    public void onEnable() {
        // Initialize core - this will connect to database and exit if connection fails
        core = new CloudCore(getDataFolder(), getLogger());
    }
    
    @Override
    public void onDisable() {
        if (core != null) {
            core.shutdown();
        }
    }
} 