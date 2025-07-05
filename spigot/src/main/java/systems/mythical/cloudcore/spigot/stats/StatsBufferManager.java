package systems.mythical.cloudcore.spigot.stats;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import systems.mythical.cloudcore.database.StatsManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StatsBufferManager {
    private final StatsManager statsManager;
    private final Plugin plugin;
    private final Map<String, AtomicInteger> buffer = new ConcurrentHashMap<>();
    private final Map<String, String> stringValues = new ConcurrentHashMap<>();
    private int flushTaskId = -1;

    public StatsBufferManager(StatsManager statsManager, Plugin plugin) {
        this.statsManager = statsManager;
        this.plugin = plugin;
    }

    private String key(String user, String worker, String type) {
        return user + ":" + worker + ":" + type;
    }

    public void incrementStat(String user, String worker, String type) {
        buffer.computeIfAbsent(key(user, worker, type), k -> new AtomicInteger(0)).incrementAndGet();
    }

    public void incrementStatBy(String user, String worker, String type, int amount) {
        buffer.computeIfAbsent(key(user, worker, type), k -> new AtomicInteger(0)).addAndGet(amount);
    }

    public void flushAll() {
        // Flush numeric stats
        buffer.forEach((key, value) -> {
            String[] parts = key.split(":", 3);
            if (parts.length == 3) {
                int amount = value.getAndSet(0);
                if (amount > 0) {
                    statsManager.incrementStatBy(parts[0], parts[1], parts[2], amount);
                }
            }
        });

        // Flush string stats
        stringValues.forEach((key, value) -> {
            String[] parts = key.split(":", 3);
            if (parts.length == 3) {
                statsManager.setOrUpdateStat(parts[0], parts[1], parts[2], value);
            }
        });
    }

    public void flushForUser(String user) {
        // Flush numeric stats for user
        buffer.forEach((key, value) -> {
            if (key.startsWith(user + ":")) {
                String[] parts = key.split(":", 3);
                if (parts.length == 3) {
                    int amount = value.getAndSet(0);
                    if (amount > 0) {
                        statsManager.incrementStatBy(parts[0], parts[1], parts[2], amount);
                    }
                }
            }
        });

        // Flush string stats for user
        stringValues.forEach((key, value) -> {
            if (key.startsWith(user + ":")) {
                String[] parts = key.split(":", 3);
                if (parts.length == 3) {
                    statsManager.setOrUpdateStat(parts[0], parts[1], parts[2], value);
                }
            }
        });
    }

    public void startScheduledFlush(long intervalTicks) {
        if (flushTaskId != -1) return;
        flushTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::flushAll, intervalTicks, intervalTicks).getTaskId();
    }

    public void stopScheduledFlush() {
        if (flushTaskId != -1) {
            Bukkit.getScheduler().cancelTask(flushTaskId);
            flushTaskId = -1;
        }
        flushAll();
    }

    public void setStat(String user, String worker, String type, double value) {
        statsManager.setStat(user, worker, type, value);
    }

    public void setStatString(String user, String worker, String type, String value) {
        String key = key(user, worker, type);
        if (value != null) {
            stringValues.put(key, value);
        } else {
            stringValues.remove(key);
        }
    }

    public void setStringValue(String user, String worker, String type, String value) {
        String key = key(user, worker, type);
        if (value != null) {
            statsManager.setOrUpdateStat(user, worker, type, value);
        }
    }
} 