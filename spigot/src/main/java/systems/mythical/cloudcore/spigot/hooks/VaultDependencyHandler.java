package systems.mythical.cloudcore.spigot.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public class VaultDependencyHandler extends DependencyHandler {
    private final CloudCoreSpigot plugin;
    private Plugin vaultPlugin;

    public VaultDependencyHandler(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @Override
    public Plugin get() {
        return vaultPlugin;
    }

    @Override
    public boolean isAvailable() {
        return vaultPlugin != null && vaultPlugin.isEnabled();
    }

    @Override
    public boolean isInstalled() {
        return Bukkit.getPluginManager().getPlugin("Vault") != null;
    }

    @Override
    public boolean setup(boolean verbose) {
        vaultPlugin = Bukkit.getPluginManager().getPlugin("Vault");
        if (vaultPlugin != null && vaultPlugin.isEnabled()) {
            if (verbose) plugin.getLogger().info("Vault found and hooked!");
            return true;
        }
        if (verbose) plugin.getLogger().warning("Vault not found.");
        return false;
    }
} 