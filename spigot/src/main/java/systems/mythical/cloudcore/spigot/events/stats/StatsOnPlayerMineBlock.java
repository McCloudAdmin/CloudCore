package systems.mythical.cloudcore.spigot.events.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public class StatsOnPlayerMineBlock implements Listener {
    private final CloudCoreSpigot plugin;

    public StatsOnPlayerMineBlock(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMineBlock(BlockBreakEvent event) {
        if (event.getPlayer() == null) {
            return;
        }   
        plugin.getStatsBufferManager().incrementStat(event.getPlayer().getUniqueId().toString(), plugin.getWorkerName(), "vanilla_mined_blocks");
    }
}
