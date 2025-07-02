package systems.mythical.cloudcore.spigot.events.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public class StatsOnPlayerKill implements Listener { 
    private final CloudCoreSpigot plugin;

    public StatsOnPlayerKill(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            plugin.getStatsBufferManager().incrementStat(event.getEntity().getKiller().getUniqueId().toString(), plugin.getWorkerName(), "vanilla_kills");
        }
    }
}
