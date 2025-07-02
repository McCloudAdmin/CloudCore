package systems.mythical.cloudcore.spigot.events.stats;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public class StatsOnPlayerDamageTake implements Listener {
    private final CloudCoreSpigot plugin;

    public StatsOnPlayerDamageTake(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamageTake(EntityDamageEvent event) {
        if (event.getEntity() == null) {
            return;
        }
        if (event.getEntity() instanceof Player) {
            plugin.getStatsBufferManager().incrementStat(event.getEntity().getUniqueId().toString(),
                    plugin.getWorkerName(), "vanilla_damage_taken");
        }
    }
}
