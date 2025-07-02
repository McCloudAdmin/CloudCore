package systems.mythical.cloudcore.spigot.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public class EssentialsXDependencyHandler extends DependencyHandler {
    private final CloudCoreSpigot plugin;
    private Plugin essentialsPlugin;

    public EssentialsXDependencyHandler(CloudCoreSpigot plugin) {
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
        return Bukkit.getPluginManager().getPlugin("Essentials") != null;
    }

    @Override
    public boolean setup(boolean verbose) {
        essentialsPlugin = Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentialsPlugin != null && essentialsPlugin.isEnabled()) {
            if (verbose) plugin.getLogger().info("EssentialsX found and hooked!");
            return true;
        }
        if (verbose) plugin.getLogger().warning("EssentialsX not found.");
        return false;
    }
} 