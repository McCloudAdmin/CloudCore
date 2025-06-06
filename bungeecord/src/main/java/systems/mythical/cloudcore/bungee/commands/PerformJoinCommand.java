package systems.mythical.cloudcore.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;
import systems.mythical.cloudcore.joinme.JoinMeTokenManager;

public class PerformJoinCommand extends Command {
    private final CloudCoreBungee plugin;
    private final JoinMeTokenManager tokenManager;

    public PerformJoinCommand(CloudCoreBungee plugin, JoinMeCommand joinMeCommand) {
        super("performjoin");
        this.plugin = plugin;
        this.tokenManager = JoinMeTokenManager.getInstance();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players");
            return;
        }

        if (args.length != 1) {
            return; // Silently fail - this command should only be used through JoinMe
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        String token = args[0];

        // Use the new atomic validation and connection method
        tokenManager.validateAndConnect(token, player.getUniqueId(), (serverName, playerUuid) -> {
            player.connect(plugin.getProxy().getServerInfo(serverName));
        });
    }
} 