package systems.mythical.cloudcore.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import systems.mythical.cloudcore.velocity.CloudCoreVelocity;
import systems.mythical.cloudcore.console.ConsoleCommand;
import systems.mythical.cloudcore.core.CloudCoreConstants.Permissions;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;
import systems.mythical.cloudcore.messages.MessageManager;

import java.util.List;

public class ProxyConsoleCommand implements SimpleCommand {
    private final ProxyServer server;
    private final MessageManager messageManager;
    private final LegacyComponentSerializer legacySerializer;

    public ProxyConsoleCommand(CloudCoreVelocity plugin) {
        this.server = plugin.getServer();
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.legacySerializer = LegacyComponentSerializer.builder().character('&').build();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.CONSOLE_PLAYERS_ONLY)));
            return;
        }

        Player player = (Player) source;

        if (args.length == 0) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.CONSOLE_USAGE)));
            return;
        }

        // Join the arguments back into a command string
        String command = String.join(" ", args);

        // Check if the player is allowed to execute console commands
        if (!ConsoleCommand.onConsoleCommand(player.getUniqueId(), command)) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.CONSOLE_NOT_ALLOWED)));
            return;
        }

        // Execute the command
        server.getCommandManager().executeAsync(server.getConsoleCommandSource(), command);
        player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.CONSOLE_EXECUTED, command)));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.CONSOLE_EXECUTE);
    }
} 