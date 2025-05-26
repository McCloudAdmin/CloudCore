package systems.mythical.cloudcore.velocity.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;

import java.util.logging.Logger;

public class OnCommand {
    private static final Logger logger = Logger.getLogger(OnCommand.class.getName());

    @Subscribe(order = PostOrder.NORMAL)
    public void onCommand(CommandExecuteEvent event) {
        if (!(event.getCommandSource() instanceof Player)) {
            return; // Skip if command is not from a player
        }

        try {
            Player player = (Player) event.getCommandSource();
            String serverName = player.getCurrentServer().isPresent() ? 
                player.getCurrentServer().get().getServerInfo().getName() : "lobby";

            // Process the command event
            boolean allow = systems.mythical.cloudcore.events.CommandEvent.onPlayerCommand(
                player.getUniqueId(),
                "/" + event.getCommand(), // Add / prefix since CommandExecuteEvent doesn't include it
                serverName
            );

            // Cancel the event if it should be blocked
            if (!allow) {
                event.setResult(CommandExecuteEvent.CommandResult.denied());
            }

            logger.info("Processed command from " + player.getUsername() + ": /" + event.getCommand());
        } catch (Exception e) {
            logger.severe("Error processing command event: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 