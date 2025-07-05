package systems.mythical.cloudcore.spigot.hooks;


import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

public class VoteHandler implements org.bukkit.event.Listener {
    private final CloudCoreSpigot plugin;

    public VoteHandler(CloudCoreSpigot plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onVotifierEvent(VotifierEvent event) {
        Vote vote = event.getVote();
        String username = vote.getUsername();
        String serviceName = vote.getServiceName();

        // Increment general vote count
        plugin.getStatsBufferManager().incrementStat(username, plugin.getWorkerName(), "votes_total");

        // Increment service-specific vote count
        String serviceSpecificStat = "votes_" + serviceName.toLowerCase().replace(" ", "_");
        plugin.getStatsBufferManager().incrementStat(username, plugin.getWorkerName(), serviceSpecificStat);
    }
} 