package systems.mythical.cloudcore.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import systems.mythical.cloudcore.velocity.CloudCoreVelocity;
import systems.mythical.cloudcore.core.CloudCoreConstants.Permissions;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;
import systems.mythical.cloudcore.messages.MessageManager;

import java.util.List;

public class AlertCommand implements SimpleCommand {
    private final ProxyServer server;
    private final MessageManager messageManager;
    private final LegacyComponentSerializer legacySerializer;

    public AlertCommand(CloudCoreVelocity plugin) {
        this.server = plugin.getServer();
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.legacySerializer = LegacyComponentSerializer.builder().character('&').build();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();


        if (args.length == 0) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ALERT_USAGE)));
            return;
        }

        // Join the arguments back into a message string
        String message = String.join(" ", args);

        // Send the alert to all online players
        Component formattedMessage = legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ALERT_FORMAT, message));
        for (Player player : server.getAllPlayers()) {
            player.sendMessage(formattedMessage);
        }
        server.getConsoleCommandSource().sendMessage(formattedMessage);

        // Send confirmation to the command sender
        source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ALERT_SENT)));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.ALERT_USE);
    }
} 