package systems.mythical.cloudcore.bungee.events;

import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import systems.mythical.cloudcore.utils.CloudLogger;

public class OnCommand implements Listener {
    private final CloudLogger cloudLogger;

    public OnCommand(CloudLogger cloudLogger) {
        this.cloudLogger = cloudLogger;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCommand(ChatEvent event) {
        // Skip if it's not a command
        if (!event.isCommand()) {
            return;
        }

        try {
            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            String serverName = player.getServer() != null ? 
                player.getServer().getInfo().getName() : "lobby";

            // Process the command event
            boolean allow = systems.mythical.cloudcore.events.CommandEvent.onPlayerCommand(
                player.getUniqueId(),
                event.getMessage(),
                serverName
            );

            // Cancel the event if it should be blocked
            if (!allow) {
                event.setCancelled(true);
            }

            cloudLogger.debug("Processed command from " + player.getName() + ": " + event.getMessage());
        } catch (Exception e) {
            cloudLogger.error("Error processing command event: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 