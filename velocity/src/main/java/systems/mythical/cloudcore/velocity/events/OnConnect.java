package systems.mythical.cloudcore.velocity.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import systems.mythical.cloudcore.events.JoinEvent;
import systems.mythical.cloudcore.utils.ProtocolVersionTranslator;

import java.util.logging.Logger;

public class OnConnect {
    private static final Logger logger = Logger.getLogger(OnConnect.class.getName());

    @Subscribe(order = PostOrder.NORMAL)
    public void onConnect(PostLoginEvent event) {
        try {
            Player player = event.getPlayer();
            String ip = player.getRemoteAddress().getAddress().getHostAddress();
            
            // Get client information
            String clientName = player.getClientBrand() != null ? player.getClientBrand() : "Unknown";
            String userVersion = ProtocolVersionTranslator.translateProtocolToString(event.getPlayer().getProtocolVersion().getProtocol());
            String serverName = player.getCurrentServer().isPresent() ? 
                player.getCurrentServer().get().getServerInfo().getName() : "lobby";

            JoinEvent.onPlayerJoin(
                player.getUsername(),
                player.getUniqueId(),
                ip,
                userVersion,
                clientName,
                serverName
            );
            logger.info("Processed join event for player: " + player.getUsername());
        } catch (Exception e) {
            logger.severe("Error processing join event: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 