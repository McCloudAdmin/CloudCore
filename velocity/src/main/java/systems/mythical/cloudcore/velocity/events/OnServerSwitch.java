package systems.mythical.cloudcore.velocity.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import java.util.logging.Logger;

public class OnServerSwitch {
    private static final Logger logger = Logger.getLogger(OnServerSwitch.class.getName());

    @Subscribe(order = PostOrder.NORMAL)
    public void onServerSwitch(ServerConnectedEvent event) {
        try {
            Player player = event.getPlayer();
            String serverName = event.getServer().getServerInfo().getName();
            
            systems.mythical.cloudcore.events.ServerSwitchEvent.onServerSwitch(
                player.getUsername(),
                player.getUniqueId(),
                serverName
            );
            logger.info("Processed server switch for player: " + player.getUsername() + " to " + serverName);
        } catch (Exception e) {
            logger.severe("Error processing server switch: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 