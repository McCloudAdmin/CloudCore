package systems.mythical.cloudcore.spigot.hooks;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public abstract class DependencyHandler {

    /**
     * Get the main class of the dependency. <br>
     * <b>Note that you still have to cast it to the correct plugin.</b>
     * 
     * @return main class of plugin, or null if not found.
     */
    public abstract Plugin get();

    /**
        * Check whether CloudCore has hooked this dependency and thus can use it.
     * 
     * @return true if CloudCore hooked into it, false otherwise.
     */
    public abstract boolean isAvailable();

    /**
     * Check to see if this dependency is running on this server
     * 
     * @return true if it is, false otherwise.
     */
    public abstract boolean isInstalled();

    /**
     * Setup the hook between this dependency and CloudCore
     * 
     * @param verbose
     *            Whether to show output or not
     * @return true if correctly setup, false otherwise.
     */
    public abstract boolean setup(boolean verbose);

    public CloudCoreSpigot getPlugin() {
        return (CloudCoreSpigot) Bukkit.getPluginManager().getPlugin("CloudCoreSpigot");
    }
}