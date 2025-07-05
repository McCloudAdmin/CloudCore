package systems.mythical.cloudcore.spigot.hooks;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import org.bukkit.entity.Player;
import java.util.List;

public class JobsHandler {
    private final Jobs jobs;
    
    public JobsHandler(Jobs jobs) {
        this.jobs = jobs;
    }

    /**
     * Get the current points a player has in Jobs
     * @param player The player to check
     * @return The current points, or 0 if player has no points or Jobs data
     */
    public double getCurrentPoints(Player player) {
        JobsPlayer jobsPlayer = jobs.getPlayerManager().getJobsPlayer(player);
        if (jobsPlayer != null) {
            List<JobProgression> jobsList = jobsPlayer.getJobProgression();
            if (jobsList != null) {
                double totalPoints = 0;
                for (JobProgression prog : jobsList) {
                    totalPoints += prog.getExperience();
                }
                return totalPoints;
            }
        }
        return 0;
    }

    /**
     * Get the total points a player has obtained in Jobs
     * @param player The player to check
     * @return The total points earned across all jobs, or 0 if player has no points or Jobs data
     */
    public double getTotalPoints(Player player) {
        JobsPlayer jobsPlayer = jobs.getPlayerManager().getJobsPlayer(player);
        if (jobsPlayer != null) {
            List<JobProgression> jobsList = jobsPlayer.getJobProgression();
            if (jobsList != null) {
                double totalPoints = 0;
                for (JobProgression prog : jobsList) {
                    totalPoints += prog.getMaxExperience();
                }
                return totalPoints;
            }
        }
        return 0;
    }

    /**
     * Get the current experience in a specific job
     * @param player The player to check
     * @param jobName The name of the job
     * @return The current experience, or 0 if player doesn't have the job or Jobs data
     */
    public double getCurrentExp(Player player, String jobName) {
        JobsPlayer jobsPlayer = jobs.getPlayerManager().getJobsPlayer(player);
        if (jobsPlayer != null) {
            List<JobProgression> jobsList = jobsPlayer.getJobProgression();
            if (jobsList != null) {
                for (JobProgression prog : jobsList) {
                    if (prog.getJob().getName().equalsIgnoreCase(jobName)) {
                        return prog.getExperience();
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Get the current level in a specific job
     * @param player The player to check
     * @param jobName The name of the job
     * @return The current level, or 0 if player doesn't have the job or Jobs data
     */
    public int getCurrentLevel(Player player, String jobName) {
        JobsPlayer jobsPlayer = jobs.getPlayerManager().getJobsPlayer(player);
        if (jobsPlayer != null) {
            List<JobProgression> jobsList = jobsPlayer.getJobProgression();
            if (jobsList != null) {
                for (JobProgression prog : jobsList) {
                    if (prog.getJob().getName().equalsIgnoreCase(jobName)) {
                        return prog.getLevel();
                    }
                }
            }
        }
        return 0;
    }
} 