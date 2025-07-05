package systems.mythical.cloudcore.spigot.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public class JobsDependencyHandler extends DependencyHandler {
    private final CloudCoreSpigot plugin;
    private Plugin jobsPlugin;

    public JobsDependencyHandler(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public Plugin get() {
        return jobsPlugin;
    }

    @Override
    public boolean isAvailable() {
        return jobsPlugin != null && jobsPlugin.isEnabled();
    }

    @Override
    public boolean isInstalled() {
        return Bukkit.getPluginManager().getPlugin("Jobs") != null;
    }

    @Override
    public boolean setup(boolean verbose) {
        jobsPlugin = Bukkit.getPluginManager().getPlugin("Jobs");
        if (jobsPlugin != null && jobsPlugin.isEnabled()) {
            if (verbose) plugin.getLogger().info("Jobs found and hooked!");
            return true;
        }
        if (verbose) plugin.getLogger().warning("Jobs not found.");
        return false;
    }
} 