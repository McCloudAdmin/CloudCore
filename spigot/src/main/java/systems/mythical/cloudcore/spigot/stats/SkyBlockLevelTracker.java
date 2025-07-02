package systems.mythical.cloudcore.spigot.stats;

import com.wasteofplastic.askyblock.ASkyBlockAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class SkyBlockLevelTracker {
    private final ASkyBlockAPI api;
    private final StatsBufferManager statsBufferManager;
    private final String workerName;
    private int taskId = -1;
    private final Plugin plugin;

    public SkyBlockLevelTracker(ASkyBlockAPI api, StatsBufferManager statsBufferManager, String workerName, Plugin plugin) {
        this.api = api;
        this.statsBufferManager = statsBufferManager;
        this.workerName = workerName;
        this.plugin = plugin;
    }

    public void start() {
        if (taskId != -1) return;
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                long level = api.getIslandLevel(player.getUniqueId());
                statsBufferManager.setStat(player.getUniqueId().toString(), workerName, "askyblock_island_level", level);
            }
        }, 0L, 1000L).getTaskId(); // every 50 seconds
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
} 