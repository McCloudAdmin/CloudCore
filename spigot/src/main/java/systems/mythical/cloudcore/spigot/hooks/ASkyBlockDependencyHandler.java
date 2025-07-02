package systems.mythical.cloudcore.spigot.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public class ASkyBlockDependencyHandler extends DependencyHandler {
    private final CloudCoreSpigot plugin;
    private Plugin askyblockPlugin;

    public ASkyBlockDependencyHandler(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public Plugin get() {
        return askyblockPlugin;
    }

    @Override
    public boolean isAvailable() {
        return askyblockPlugin != null && askyblockPlugin.isEnabled();
    }

    @Override
    public boolean isInstalled() {
        return Bukkit.getPluginManager().getPlugin("ASkyBlock") != null;
    }

    @Override
    public boolean setup(boolean verbose) {
        askyblockPlugin = Bukkit.getPluginManager().getPlugin("ASkyBlock");
        if (askyblockPlugin != null && askyblockPlugin.isEnabled()) {
            if (verbose) plugin.getLogger().info("ASkyBlock found and hooked!");
            return true;
        }
        if (verbose) plugin.getLogger().warning("ASkyBlock not found.");
        return false;
    }
} 