package systems.mythical.cloudcore.spigot.events.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import systems.mythical.cloudcore.spigot.CloudCoreSpigot;
import java.util.UUID;

public class StatsOnPlayerLeave implements Listener {
    private final CloudCoreSpigot plugin;

    public StatsOnPlayerLeave(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        plugin.getStatsBufferManager().incrementStat(event.getPlayer().getUniqueId().toString(),
                plugin.getWorkerName(), "vanilla_leaves");
        // Time played stat
        UUID uuid = event.getPlayer().getUniqueId();
        Long joinTime = StatsOnPlayerJoin.joinTimes.remove(uuid);
        if (joinTime != null) {
            long secondsPlayed = (System.currentTimeMillis() - joinTime) / 1000L;
            if (secondsPlayed > 0) {
                plugin.getStatsBufferManager().incrementStatBy(uuid.toString(), plugin.getWorkerName(), "vanilla_time_played_seconds", (int) secondsPlayed);
            }
        }
    }
}
