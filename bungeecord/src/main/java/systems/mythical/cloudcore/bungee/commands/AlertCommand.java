package systems.mythical.cloudcore.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;
import systems.mythical.cloudcore.core.CloudCoreConstants.Permissions;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;

public class AlertCommand extends Command {
    private final CloudCoreBungee plugin;
    private final MessageManager messageManager;

    public AlertCommand(CloudCoreBungee plugin) {
        super("alert", Permissions.ALERT_USE);
        this.plugin = plugin;
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getColoredMessage(Messages.ALERT_USAGE))));
            return;
        }

        // Join the arguments back into a message string
        String message = String.join(" ", args);

        // Send the alert to all online players
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getColoredMessage(Messages.ALERT_FORMAT, message))));
        }

        // Send confirmation to the command sender
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
            messageManager.getColoredMessage(Messages.ALERT_SENT))));
    }
} 