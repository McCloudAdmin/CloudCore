package systems.mythical.cloudcore.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import systems.mythical.cloudcore.reports.ReportManager;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.velocity.CloudCoreVelocity;

import java.util.List;

public class ReportCommand implements SimpleCommand {
    private final CloudCoreVelocity plugin;
    private final ProxyServer server;
    private final ReportManager reportManager;
    private final MessageManager messageManager;
    private final LegacyComponentSerializer legacySerializer;

    public ReportCommand(CloudCoreVelocity plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.reportManager = ReportManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.legacySerializer = LegacyComponentSerializer.builder().character('&').build();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            source.sendMessage(legacySerializer.deserialize("&cThis command can only be used by players."));
            return;
        }

        Player player = (Player) source;

        if (!reportManager.isReportSystemEnabled()) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage("report.system_disabled")));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage("report.usage")));
            return;
        }

        String targetName = args[0];
        Player target = server.getPlayer(targetName).orElse(null);

        if (target == null) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage("report.player_not_found")));
            return;
        }

        if (target == player) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage("report.cannot_report_self")));
            return;
        }

        if (!reportManager.canReport(player.getUniqueId())) {
            long remainingCooldown = reportManager.getRemainingCooldown(player.getUniqueId());
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage("report.cooldown", remainingCooldown)));
            return;
        }

        // Join the remaining arguments as the reason
        String reason = String.join(" ", args).substring(targetName.length() + 1);

        if (reportManager.createReport(player.getUniqueId(), target.getUniqueId(), reason)) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage("report.success")));
            
            // Notify staff members
            Component staffNotification = legacySerializer.deserialize(messageManager.getColoredMessage("report.staff_notification",
                player.getUsername(), target.getUsername(), reason));
            
            for (Player staff : server.getAllPlayers()) {
                if (staff.hasPermission("cloudadmin.report.notify")) {
                    staff.sendMessage(staffNotification);
                }
            }
        } else {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage("report.error")));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("cloudadmin.report");
    }
} 