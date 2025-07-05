package systems.mythical.cloudcore.spigot.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public class BedWarsDependencyHandler extends DependencyHandler {
    private final CloudCoreSpigot plugin;
    private Plugin bedwarsPlugin;

    public BedWarsDependencyHandler(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public Plugin get() {
        return bedwarsPlugin;
    }

    @Override
    public boolean isAvailable() {
        return bedwarsPlugin != null && bedwarsPlugin.isEnabled();
    }

    @Override
    public boolean isInstalled() {
        return Bukkit.getPluginManager().getPlugin("BedWars1058") != null;
    }

    @Override
    public boolean setup(boolean verbose) {
        bedwarsPlugin = Bukkit.getPluginManager().getPlugin("BedWars1058");
        if (bedwarsPlugin != null && bedwarsPlugin.isEnabled()) {
            if (verbose) plugin.getLogger().info("BedWars found and hooked!");
            return true;
        }
        if (verbose) plugin.getLogger().warning("BedWars not found.");
        return false;
    }
} 