package systems.mythical.cloudcore.spigot.stats;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class MoneyTracker {
    private final Essentials essentials;
    private final StatsBufferManager statsBufferManager;
    private final String workerName;
    private final Map<String, Double> lastBalances = new HashMap<>();
    private int taskId = -1;
    private final Plugin plugin;

    public MoneyTracker(Essentials essentials, StatsBufferManager statsBufferManager, String workerName, Plugin plugin) {
        this.essentials = essentials;
        this.statsBufferManager = statsBufferManager;
        this.workerName = workerName;
        this.plugin = plugin;
    }

    public void start() {
        if (taskId != -1) return;
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                User user = essentials.getUser(player);
                double balance = user.getMoney().doubleValue();
                String uuid = player.getUniqueId().toString();
                // Save the current balance as a stat (absolute value)
                statsBufferManager.setStat(uuid, workerName, "essentials_balance", balance);
                lastBalances.put(uuid, balance);
            }
        }, 0L, 1000L).getTaskId(); // 1000 ticks = 50 seconds
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
    }
} 