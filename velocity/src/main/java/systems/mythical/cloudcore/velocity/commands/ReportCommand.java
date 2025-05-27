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
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;
import systems.mythical.cloudcore.core.CloudCoreConstants.Permissions;

import java.util.List;

public class ReportCommand implements SimpleCommand {
    private final ProxyServer server;
    private final ReportManager reportManager;
    private final MessageManager messageManager;
    private final LegacyComponentSerializer legacySerializer;

    public ReportCommand(CloudCoreVelocity plugin) {
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

        if (args.length < 2) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_USAGE)));
            return;
        }

        String targetName = args[0];
        Player target = server.getPlayer(targetName).orElse(null);

        if (target == null) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_PLAYER_NOT_FOUND)));
            return;
        }

        if (target == player) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_CANNOT_REPORT_SELF)));
            return;
        }

        if (!reportManager.canReport(player.getUniqueId())) {
            long remainingCooldown = reportManager.getRemainingCooldown(player.getUniqueId());
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_COOLDOWN, remainingCooldown)));
            return;
        }

        // Join the remaining arguments as the reason
        String reason = String.join(" ", args).substring(targetName.length() + 1);

        if (reportManager.createReport(player.getUniqueId(), target.getUniqueId(), reason)) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_SUCCESS)));
            
            // Notify staff members
            Component staffNotification = legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_STAFF_NOTIFICATION,
                player.getUsername(), target.getUsername(), reason));
            
            for (Player staff : server.getAllPlayers()) {
                if (staff.hasPermission(Permissions.REPORT_NOTIFY)) {
                    staff.sendMessage(staffNotification);
                }
            }
        } else {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_ERROR)));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return List.of();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.REPORT_USE);
    }
} 