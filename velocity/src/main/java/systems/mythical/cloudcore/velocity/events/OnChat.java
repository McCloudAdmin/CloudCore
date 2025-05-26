package systems.mythical.cloudcore.velocity.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;

import java.util.logging.Logger;

public class OnChat {
    private static final Logger logger = Logger.getLogger(OnChat.class.getName());

    @SuppressWarnings("deprecation")
	@Subscribe(order = PostOrder.NORMAL)
    public void onChat(PlayerChatEvent event) {
        try {
            Player player = event.getPlayer();
            String serverName = player.getCurrentServer().isPresent() ? 
                player.getCurrentServer().get().getServerInfo().getName() : "lobby";

            // Process the chat event
            boolean allow = systems.mythical.cloudcore.events.ChatEvent.onPlayerChat(
                player.getUniqueId(),
                event.getMessage(),
                serverName
            );

            // Cancel the event if it should be blocked
            if (!allow) {
                event.setResult(PlayerChatEvent.ChatResult.denied());
            }

            logger.info("Processed chat message from " + player.getUsername());
        } catch (Exception e) {
            logger.severe("Error processing chat event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}