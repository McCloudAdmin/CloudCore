package systems.mythical.cloudcore.spigot.hooks;

import org.bukkit.ChatColor;

import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

import java.util.HashMap;
import java.util.Map;

public class DependencyManager {

    private final Map<CloudCoreSpigotDependency, DependencyHandler> handlers = new HashMap<>();

    private final CloudCoreSpigot plugin;

    public DependencyManager(final CloudCoreSpigot instance) {
        this.plugin = instance;
        // Register all supported dependencies
        handlers.put(CloudCoreSpigotDependency.VAULT, new VaultDependencyHandler(plugin));
        handlers.put(CloudCoreSpigotDependency.ESSENTIALSX, new EssentialsXDependencyHandler(plugin));
        handlers.put(CloudCoreSpigotDependency.ASKYBLOCK, new ASkyBlockDependencyHandler(plugin));
    }

    /**
     * Loads all dependencies used for CloudCore. <br>
     * CloudCore will check for dependencies and shows the output on the console.
     */
    public void loadDependencies() {
        plugin.getLogger().info(ChatColor.YELLOW + "---------------[CloudCore Dependencies]---------------");
        for (var entry : handlers.entrySet()) {
            var dep = entry.getKey();
            var handler = entry.getValue();
            if (handler.setup(true)) {
                plugin.getLogger().info(ChatColor.GREEN + dep.getDisplayName() + " hooked successfully!");
            } else {
                plugin.getLogger().warning(dep.getDisplayName() + " not found or failed to hook.");
            }
        }
        plugin.getLogger().info(ChatColor.YELLOW + "-----------------------------------------------------");
    }

    public boolean isAvailable(CloudCoreSpigotDependency dep) {
        var handler = handlers.get(dep);
        return handler != null && handler.isAvailable();
    }

    public DependencyHandler getHandler(CloudCoreSpigotDependency dep) {
        return handlers.get(dep);
    }

}
