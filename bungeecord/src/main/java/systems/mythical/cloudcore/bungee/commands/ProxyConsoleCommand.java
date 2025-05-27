package systems.mythical.cloudcore.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import systems.mythical.cloudcore.console.ConsoleCommand;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;

public class ProxyConsoleCommand extends Command {
    private final CloudCoreBungee plugin;
    private final MessageManager messageManager;

    public ProxyConsoleCommand(CloudCoreBungee plugin) {
        super("proxyconsole", null, "pcex");
        this.plugin = plugin;
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getColoredMessage(Messages.CONSOLE_PLAYERS_ONLY))));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0) {
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getColoredMessage(Messages.CONSOLE_USAGE))));
            return;
        }

        // Join the arguments back into a command string
        String command = String.join(" ", args);

        // Check if the player is allowed to execute console commands
        if (!ConsoleCommand.onConsoleCommand(player.getUniqueId(), command)) {
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getColoredMessage(Messages.CONSOLE_NOT_ALLOWED))));
            return;
        }

        // Execute the command as console
        plugin.getProxy().getPluginManager().dispatchCommand(plugin.getProxy().getConsole(), command);
        player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
            messageManager.getColoredMessage(Messages.CONSOLE_EXECUTED, command))));
    }
} 