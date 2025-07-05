package systems.mythical.cloudcore.spigot.hooks;

import org.bukkit.Bukkit;

import systems.mythical.cloudcore.spigot.CloudCoreSpigot;


public enum CloudCoreSpigotDependency {
    VAULT("Vault", "Vault"),
    ESSENTIALSX("EssentialsX", "EssentialsX"),
    ASKYBLOCK("ASkyBlock", "ASkyBlock"),
    JOBS("Jobs", "Jobs"),
    BEDWARS("BedWars1058", "BedWars"),
    VOTIFIER("NuVotifier", "NuVotifier");

    private final String pluginName;
    private final String displayName;

    CloudCoreSpigotDependency(String pluginName, String displayName) {
        this.pluginName = pluginName;
        this.displayName = displayName;
    }

    public String getPluginName() { return pluginName; }
    public String getDisplayName() { return displayName; }

    public static CloudCoreSpigot getCloudCoreSpigot() {
        return (CloudCoreSpigot) Bukkit.getPluginManager().getPlugin("CloudCoreSpigot");
    }
}
