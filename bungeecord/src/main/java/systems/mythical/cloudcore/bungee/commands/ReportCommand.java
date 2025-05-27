package systems.mythical.cloudcore.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import systems.mythical.cloudcore.reports.ReportManager;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;
import systems.mythical.cloudcore.core.CloudCoreConstants.Permissions;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;

public class ReportCommand extends Command {
    private final CloudCoreBungee plugin;
    private final ReportManager reportManager;
    private final MessageManager messageManager;

    public ReportCommand(CloudCoreBungee plugin) {
        super("report", Permissions.REPORT_USE);
        this.plugin = plugin;
        this.reportManager = ReportManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(new TextComponent(ChatColor.RED + "This command can only be used by players."));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length < 2) {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_USAGE)));
            return;
        }

        String targetName = args[0];
        ProxiedPlayer target = plugin.getProxy().getPlayer(targetName);

        if (target == null) {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_PLAYER_NOT_FOUND)));
            return;
        }

        if (target == player) {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_CANNOT_REPORT_SELF)));
            return;
        }

        if (!reportManager.canReport(player.getUniqueId())) {
            long remainingCooldown = reportManager.getRemainingCooldown(player.getUniqueId());
            player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_COOLDOWN, remainingCooldown)));
            return;
        }

        // Join the remaining arguments as the reason
        String reason = String.join(" ", args).substring(targetName.length() + 1);

        if (reportManager.createReport(player.getUniqueId(), target.getUniqueId(), reason)) {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_SUCCESS)));
            
            // Notify staff members
            for (ProxiedPlayer staff : plugin.getProxy().getPlayers()) {
                if (staff.hasPermission(Permissions.REPORT_NOTIFY)) {
                    staff.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_STAFF_NOTIFICATION,
                        player.getName(), target.getName(), reason)));
                }
            }
        } else {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_ERROR)));
        }
    }
} 