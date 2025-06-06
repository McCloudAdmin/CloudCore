package systems.mythical.cloudcore.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import systems.mythical.cloudcore.velocity.CloudCoreVelocity;
import systems.mythical.cloudcore.joinme.JoinMeTokenManager;

public class PerformJoinCommand implements SimpleCommand {
    private final CloudCoreVelocity plugin;
    private final JoinMeTokenManager tokenManager;

    public PerformJoinCommand(CloudCoreVelocity plugin) {
        this.plugin = plugin;
        this.tokenManager = JoinMeTokenManager.getInstance();
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            source.sendMessage(Component.text("This command can only be used by players").color(TextColor.color(255, 85, 85)));
            return;
        }

        if (args.length != 1) {
            return; // Silently fail - this command should only be used through JoinMe
        }

        Player player = (Player) source;
        String token = args[0];

        // Use the new atomic validation and connection method
        tokenManager.validateAndConnect(token, player.getUniqueId(), (serverName, playerUuid) -> {
            plugin.getServer().getServer(serverName).ifPresent(server -> {
                player.createConnectionRequest(server).connect();
            });
        });
    }
} 