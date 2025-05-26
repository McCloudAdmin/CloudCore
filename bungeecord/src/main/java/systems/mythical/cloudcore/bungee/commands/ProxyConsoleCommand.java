package systems.mythical.cloudcore.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import systems.mythical.cloudcore.console.ConsoleCommand;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.CommonSettings;

public class ProxyConsoleCommand extends Command {
    private final CloudCoreBungee plugin;
    private final MessageManager messageManager;
    private final CloudSettings cloudSettings;
    private static final CommonSettings.BooleanSetting ENABLE_CONSOLE_COMMAND = new CommonSettings.BooleanSetting("enable_console_command", true);

    public ProxyConsoleCommand(CloudCoreBungee plugin) {
        super("proxyconsole", null, "pcex");
        this.plugin = plugin;
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.cloudSettings = CloudSettings.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        // Check if console commands are enabled
        if (!ENABLE_CONSOLE_COMMAND.parseValue(cloudSettings.getSetting(ENABLE_CONSOLE_COMMAND.getName()))) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getColoredMessage("console.disabled"))));
            return;
        }

        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getColoredMessage("console.players_only"))));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0) {
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getColoredMessage("console.usage"))));
            return;
        }

        // Join the arguments back into a command string
        String command = String.join(" ", args);

        // Check if the player is allowed to execute console commands
        if (!ConsoleCommand.onConsoleCommand(player.getUniqueId(), command)) {
            player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
                messageManager.getColoredMessage("console.not_allowed"))));
            return;
        }

        // Execute the command as console
        plugin.getProxy().getPluginManager().dispatchCommand(plugin.getProxy().getConsole(), command);
        player.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&', 
            messageManager.getColoredMessage("console.executed", command))));
    }
} 