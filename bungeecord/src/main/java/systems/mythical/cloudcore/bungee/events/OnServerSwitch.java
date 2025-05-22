package systems.mythical.cloudcore.bungee.events;

import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import java.util.logging.Logger;

public class OnServerSwitch implements Listener {
    private static final Logger logger = Logger.getLogger(OnServerSwitch.class.getName());

    @EventHandler(priority = EventPriority.NORMAL)
    public void onServerSwitch(ServerSwitchEvent event) {
        // Skip if this is the initial connection (from is null)
        if (event.getFrom() == null) {
            return;
        }

        try {
            String serverName = event.getPlayer().getServer().getInfo().getName();
            
            systems.mythical.cloudcore.events.ServerSwitchEvent.onServerSwitch(
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId(),
                serverName
            );
            logger.info("Processed server switch for player: " + event.getPlayer().getName() + " to " + serverName);
        } catch (Exception e) {
            logger.severe("Error processing server switch: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 