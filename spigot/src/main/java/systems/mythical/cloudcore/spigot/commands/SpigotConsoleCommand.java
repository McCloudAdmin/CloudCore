package systems.mythical.cloudcore.spigot.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import systems.mythical.cloudcore.spigot.CloudCoreSpigot;
import systems.mythical.cloudcore.console.ConsoleCommand;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;

public class SpigotConsoleCommand implements CommandExecutor {
    private final CloudCoreSpigot plugin;
    private final MessageManager messageManager;

    public SpigotConsoleCommand(CloudCoreSpigot plugin) {
        this.plugin = plugin;
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.CONSOLE_PLAYERS_ONLY)));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.CONSOLE_USAGE)));
            return true;
        }

        String cmd = String.join(" ", args);

        if (!ConsoleCommand.onConsoleCommand(player.getUniqueId(), cmd)) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.CONSOLE_NOT_ALLOWED)));
            return true;
        }

        // Execute the command as console
        Bukkit.getScheduler().runTask(plugin, () ->
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)
        );
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',
            messageManager.getColoredMessage(Messages.CONSOLE_EXECUTED, cmd)));
        return true;
    }
} 