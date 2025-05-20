package systems.mythical.cloudcore.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import systems.mythical.cloudcore.core.CloudCore;

public class CloudCoreBungeeCord extends Plugin {
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