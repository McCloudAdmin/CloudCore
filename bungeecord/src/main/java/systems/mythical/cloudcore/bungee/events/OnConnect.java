package systems.mythical.cloudcore.bungee.events;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import systems.mythical.cloudcore.events.JoinEvent;
import systems.mythical.cloudcore.utils.ProtocolVersionTranslator;

import java.util.logging.Logger;
import java.net.InetSocketAddress;

public class OnConnect implements Listener {
    private static final Logger logger = Logger.getLogger(OnConnect.class.getName());

    @EventHandler(priority = EventPriority.NORMAL)
    public void onConnect(PostLoginEvent event) {
        try {
            String ip;
            try {
                ip = ((InetSocketAddress) event.getPlayer().getSocketAddress()).getAddress().getHostAddress();
            } catch (Exception e) {
                // Fallback to direct IP if socket address fails
                @SuppressWarnings("deprecation")
                String fallbackIp = event.getPlayer().getAddress().getAddress().getHostAddress();
                ip = fallbackIp;
                logger.warning("Using fallback IP method for player: " + event.getPlayer().getName());
            }

            // Get client information
            String clientName = "Unknown"; // BungeeCord doesn't provide client name directly
            String userVersion = ProtocolVersionTranslator.translateProtocolToString(event.getPlayer().getPendingConnection().getVersion());
            String serverName = event.getPlayer().getServer() != null ? 
                event.getPlayer().getServer().getInfo().getName() : "lobby";

            JoinEvent.onPlayerJoin(
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId(),
                ip,
                userVersion,
                clientName,
                serverName
            );
            logger.info("Processed join event for player: " + event.getPlayer().getName());
        } catch (Exception e) {
            logger.severe("Error processing join event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
