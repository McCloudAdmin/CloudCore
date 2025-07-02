package systems.mythical.cloudcore.spigot.events.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public class StatsOnPlayerDeath implements Listener {
    private final CloudCoreSpigot plugin;

    public StatsOnPlayerDeath(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity() == null) {
            return;
        }
        plugin.getStatsBufferManager().incrementStat(event.getEntity().getUniqueId().toString(), plugin.getWorkerName(), "vanilla_deaths");
    }
}
