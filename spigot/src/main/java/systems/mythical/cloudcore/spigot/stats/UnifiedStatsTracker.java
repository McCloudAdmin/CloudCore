package systems.mythical.cloudcore.spigot.stats;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import com.wasteofplastic.askyblock.ASkyBlockAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import systems.mythical.cloudcore.spigot.hooks.JobsHandler;
import systems.mythical.cloudcore.spigot.hooks.EssentialsXHandler;
import systems.mythical.cloudcore.spigot.hooks.BedwarsHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class UnifiedStatsTracker {
    private final Plugin plugin;
    private final Logger logger;
    private final StatsBufferManager statsBuffer;
    private final String workerName;
    private int taskId = -1;

    // Integration handlers
    private final JobsHandler jobsHandler;
    private final EssentialsXHandler essentialsXHandler;
    private final BedwarsHandler bedwarsHandler;
    private final Essentials essentials;
    private final ASkyBlockAPI skyBlockAPI;

    // Cache for money tracking
    private final Map<String, Double> lastBalances = new HashMap<>();

    public UnifiedStatsTracker(Plugin plugin,
                              StatsBufferManager statsBuffer,
                              String workerName,
                              JobsHandler jobsHandler,
                              EssentialsXHandler essentialsXHandler,
                              BedwarsHandler bedwarsHandler,
                              Essentials essentials,
                              ASkyBlockAPI skyBlockAPI) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.statsBuffer = statsBuffer;
        this.workerName = workerName;
        this.jobsHandler = jobsHandler;
        this.essentialsXHandler = essentialsXHandler;
        this.bedwarsHandler = bedwarsHandler;
        this.essentials = essentials;
        this.skyBlockAPI = skyBlockAPI;
    }

    public void start() {
        if (taskId != -1) return;

        // Run every 50 seconds (1000 ticks)
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateAllStats, 100L, 1000L).getTaskId();
        logger.info("UnifiedStatsTracker started with 50-second update interval");
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
            updateAllStats(); // Final update before stopping
        }
    }

    private void updateAllStats() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            String uuid = player.getUniqueId().toString();

            try {
                updateJobsStats(player, uuid);
            } catch (Exception e) {
                logger.warning("Failed to update Jobs stats for " + player.getName() + ": " + e.getMessage());
            }

            try {
                updateEssentialsStats(player, uuid);
            } catch (Exception e) {
                logger.warning("Failed to update Essentials stats for " + player.getName() + ": " + e.getMessage());
            }

            try {
                updateBedwarsStats(player, uuid);
            } catch (Exception e) {
                logger.warning("Failed to update BedWars stats for " + player.getName() + ": " + e.getMessage());
            }

            try {
                updateSkyBlockStats(player, uuid);
            } catch (Exception e) {
                logger.warning("Failed to update SkyBlock stats for " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    private void updateJobsStats(Player player, String uuid) {
        if (jobsHandler != null) {
            statsBuffer.setStat(uuid, workerName, "jobs.current_points", jobsHandler.getCurrentPoints(player));
            statsBuffer.setStat(uuid, workerName, "jobs.total_points", jobsHandler.getTotalPoints(player));
            
            // Uncomment and customize if you want to track specific jobs:
            /*
            String[] jobsToTrack = {"miner", "farmer", "hunter"};
            for (String job : jobsToTrack) {
                statsBuffer.setStat(uuid, workerName, "jobs." + job + ".exp", jobsHandler.getCurrentExp(player, job));
                statsBuffer.setStat(uuid, workerName, "jobs." + job + ".level", jobsHandler.getCurrentLevel(player, job));
            }
            */
        }
    }

    private void updateEssentialsStats(Player player, String uuid) {
        // EssentialsX Handler stats
        if (essentialsXHandler != null) {
            statsBuffer.setStat(uuid, workerName, "essentials.afk", essentialsXHandler.isAfk(player) ? 1.0 : 0.0);
            String geoLocation = essentialsXHandler.getGeoLocation(player);
            if (!"Unknown".equals(geoLocation)) {
                statsBuffer.setStringValue(uuid, workerName, "essentials.geolocation", geoLocation);
            }
        }

        // Money tracking
        if (essentials != null) {
            User user = essentials.getUser(player);
            if (user != null) {
                double balance = user.getMoney().doubleValue();
                statsBuffer.setStat(uuid, workerName, "essentials.balance", balance);
                lastBalances.put(uuid, balance);
            }
        }
    }

    private void updateBedwarsStats(Player player, String uuid) {
        if (bedwarsHandler != null) {
            statsBuffer.setStat(uuid, workerName, "bedwars.kills", (double) bedwarsHandler.getKills(player));
            statsBuffer.setStat(uuid, workerName, "bedwars.deaths", (double) bedwarsHandler.getDeaths(player));
            statsBuffer.setStat(uuid, workerName, "bedwars.wins", (double) bedwarsHandler.getWins(player));
            statsBuffer.setStat(uuid, workerName, "bedwars.losses", (double) bedwarsHandler.getLosses(player));
            statsBuffer.setStat(uuid, workerName, "bedwars.final_kills", (double) bedwarsHandler.getFinalKills(player));
            statsBuffer.setStat(uuid, workerName, "bedwars.final_deaths", (double) bedwarsHandler.getFinalDeaths(player));
            statsBuffer.setStat(uuid, workerName, "bedwars.bed_breaks", (double) bedwarsHandler.getBedBreaks(player));
            statsBuffer.setStat(uuid, workerName, "bedwars.games_played", (double) bedwarsHandler.getGamesPlayed(player));
            statsBuffer.setStat(uuid, workerName, "bedwars.level", (double) bedwarsHandler.getLevel(player));
        }
    }

    private void updateSkyBlockStats(Player player, String uuid) {
        if (skyBlockAPI != null) {
            statsBuffer.setStat(uuid, workerName, "askyblock.island_level", (double) skyBlockAPI.getIslandLevel(player.getUniqueId()));
        }
    }
} 