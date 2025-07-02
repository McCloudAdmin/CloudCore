package systems.mythical.cloudcore.spigot.events.stats;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public class StatsOnMobKill implements Listener {
    private final CloudCoreSpigot plugin;

    public StatsOnMobKill(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (event.getEntity() == null) {
            return;
        }
        if (event.getEntity() instanceof LivingEntity) {
            Player killer = event.getEntity().getKiller();
            if (killer != null) {
                plugin.getStatsBufferManager().incrementStat(killer.getUniqueId().toString(), plugin.getWorkerName(), "vanilla_mob_kills");
            }
        }
    }
}
