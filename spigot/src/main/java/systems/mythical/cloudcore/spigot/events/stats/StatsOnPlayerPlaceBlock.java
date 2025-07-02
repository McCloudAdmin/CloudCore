package systems.mythical.cloudcore.spigot.events.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public class StatsOnPlayerPlaceBlock implements Listener {
    private final CloudCoreSpigot plugin;

    public StatsOnPlayerPlaceBlock(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPlaceBlock(BlockPlaceEvent event) {
        if (event.getPlayer() == null) {
            return;
        }
        plugin.getStatsBufferManager().incrementStat(event.getPlayer().getUniqueId().toString(), plugin.getWorkerName(), "vanilla_placed_blocks");
    }
}
