package systems.mythical.cloudcore.spigot.events.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import systems.mythical.cloudcore.spigot.CloudCoreSpigot;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StatsOnPlayerJoin implements Listener {
    private final CloudCoreSpigot plugin;

    // Track join times for time played stat
    public static final Map<UUID, Long> joinTimes = new ConcurrentHashMap<>();

    public StatsOnPlayerJoin(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        plugin.getStatsBufferManager().incrementStat(event.getPlayer().getUniqueId().toString(), plugin.getWorkerName(), "vanilla_joins");
        joinTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }
}
