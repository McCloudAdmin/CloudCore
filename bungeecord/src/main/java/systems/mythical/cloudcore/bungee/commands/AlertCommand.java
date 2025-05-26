package systems.mythical.cloudcore.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.CommonSettings;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;

public class AlertCommand extends Command {
    private final CloudCoreBungee plugin;
    private final MessageManager messageManager;
    private final CloudSettings cloudSettings;
    private static final CommonSettings.BooleanSetting ENABLE_ALERT_COMMAND = new CommonSettings.BooleanSetting("enable_alert_command", true);

    public AlertCommand(CloudCoreBungee plugin) {
        super("alert", "cloudadmin.alert");
        this.plugin = plugin;
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.cloudSettings = CloudSettings.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check if alert command is enabled
        if (!ENABLE_ALERT_COMMAND.parseValue(cloudSettings.getSetting(ENABLE_ALERT_COMMAND.getName()))) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getColoredMessage("alert.disabled"))));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getColoredMessage("alert.usage"))));
            return;
        }

        // Join the arguments back into a message string
        String message = String.join(" ", args);

        // Send the alert to all online players
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getColoredMessage("alert.format", message))));
        }

        // Send confirmation to the command sender
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
            messageManager.getColoredMessage("alert.sent"))));
    }
} 