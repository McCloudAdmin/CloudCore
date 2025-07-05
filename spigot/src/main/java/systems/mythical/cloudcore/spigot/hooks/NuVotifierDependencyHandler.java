package systems.mythical.cloudcore.spigot.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public class NuVotifierDependencyHandler extends DependencyHandler {
    private final CloudCoreSpigot plugin;
    private Plugin essentialsPlugin;

    public NuVotifierDependencyHandler(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public Plugin get() {
        return essentialsPlugin;
    }

    @Override
    public boolean isAvailable() {
        return essentialsPlugin != null && essentialsPlugin.isEnabled();
    }

    @Override
    public boolean isInstalled() {
        return Bukkit.getPluginManager().getPlugin("Votifier") != null;
    }

    @Override
    public boolean setup(boolean verbose) {
        essentialsPlugin = Bukkit.getPluginManager().getPlugin("Votifier");
        if (essentialsPlugin != null && essentialsPlugin.isEnabled()) {
            if (verbose) plugin.getLogger().info("Votifier found and hooked!");
            return true;
        }
        if (verbose) plugin.getLogger().warning("Votifier not found.");
        return false;
    }
} 