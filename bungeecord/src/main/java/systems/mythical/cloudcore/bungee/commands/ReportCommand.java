package systems.mythical.cloudcore.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import systems.mythical.cloudcore.reports.ReportManager;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;

public class ReportCommand extends Command {
    private final CloudCoreBungee plugin;
    private final ReportManager reportManager;
    private final MessageManager messageManager;

    public ReportCommand(CloudCoreBungee plugin) {
        super("report", "cloudadmin.report");
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

        if (!reportManager.isReportSystemEnabled()) {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage("report.system_disabled")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage("report.usage")));
            return;
        }

        String targetName = args[0];
        ProxiedPlayer target = plugin.getProxy().getPlayer(targetName);

        if (target == null) {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage("report.player_not_found")));
            return;
        }

        if (target == player) {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage("report.cannot_report_self")));
            return;
        }

        if (!reportManager.canReport(player.getUniqueId())) {
            long remainingCooldown = reportManager.getRemainingCooldown(player.getUniqueId());
            player.sendMessage(new TextComponent(messageManager.getColoredMessage("report.cooldown", remainingCooldown)));
            return;
        }

        // Join the remaining arguments as the reason
        String reason = String.join(" ", args).substring(targetName.length() + 1);

        if (reportManager.createReport(player.getUniqueId(), target.getUniqueId(), reason)) {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage("report.success")));
            
            // Notify staff members
            for (ProxiedPlayer staff : plugin.getProxy().getPlayers()) {
                if (staff.hasPermission("cloudadmin.report.notify")) {
                    staff.sendMessage(new TextComponent(messageManager.getColoredMessage("report.staff_notification",
                        player.getName(), target.getName(), reason)));
                }
            }
        } else {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage("report.error")));
        }
    }
} 