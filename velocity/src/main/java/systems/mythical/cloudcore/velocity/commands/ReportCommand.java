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
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.users.User;
import systems.mythical.cloudcore.utils.CloudLogger;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;

import java.util.List;

public class ReportCommand implements SimpleCommand {
    private final ProxyServer server;
    private final ReportManager reportManager;
    private final MessageManager messageManager;
    private final LegacyComponentSerializer legacySerializer;
    private final UserManager userManager;
    private final CloudLogger cloudLogger;

    public ReportCommand(CloudCoreVelocity plugin) {
        this.server = plugin.getServer();
        this.reportManager = ReportManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.legacySerializer = LegacyComponentSerializer.builder().character('&').build();
        this.userManager = UserManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.cloudLogger = CloudLoggerFactory.get();
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
        Player onlineTarget = server.getPlayer(targetName).orElse(null);
        if (onlineTarget == null) {
            for (Player p : server.getAllPlayers()) {
                if (p.getUsername() != null && p.getUsername().equalsIgnoreCase(targetName)) {
                    onlineTarget = p;
                    break;
                }
            }
        }

        java.util.UUID reportedUuid;
        String reportedDisplayName;
        if (onlineTarget != null) {
            reportedUuid = onlineTarget.getUniqueId();
            reportedDisplayName = onlineTarget.getUsername();
        } else {
            var userOpt = userManager.getUserByUsername(targetName);
            if (userOpt.isEmpty()) {
                player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_PLAYER_NOT_FOUND)));
                return;
            }
            User u = userOpt.get();
            reportedUuid = u.getUuid();
            reportedDisplayName = u.getUsername();
        }

        if (reportedUuid.equals(player.getUniqueId())) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_CANNOT_REPORT_SELF)));
            return;
        }

        if (!reportManager.canReport(player.getUniqueId())) {
            long remainingCooldown = reportManager.getRemainingCooldown(player.getUniqueId());
            if (remainingCooldown > 0) {
                player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_COOLDOWN, remainingCooldown)));
            } else {
                player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_ERROR)));
            }
            return;
        }

        // Join the remaining arguments as the reason
        String reason = String.join(" ", args).substring(targetName.length() + 1);
        if (reason.trim().isEmpty()) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_USAGE)));
            return;
        }

        // Ensure both reporter and reported exist in database to avoid DB errors
        var reporterOpt = userManager.getUserByUuid(player.getUniqueId());
        if (reporterOpt.isEmpty()) {
            player.sendMessage(legacySerializer.deserialize("&cYour account was not found in our database. Please relog to register."));
            return;
        }
        var reportedOpt = userManager.getUserByUuid(reportedUuid);
        if (reportedOpt.isEmpty()) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_PLAYER_NOT_FOUND)));
            return;
        }

        if (reportManager.createReport(player.getUniqueId(), reportedUuid, reason)) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_SUCCESS)));
            
            // Notify staff members
            Component staffNotification = legacySerializer.deserialize(messageManager.getColoredMessage(Messages.REPORT_STAFF_NOTIFICATION,
                player.getUsername(), reportedDisplayName, reason));
            
            for (Player staff : server.getAllPlayers()) {
                if (staff.hasPermission(Permissions.REPORT_NOTIFY)) {
                    staff.sendMessage(staffNotification);
                }
            }
        } else {
            cloudLogger.error("Report creation failed without exception. reporter=" + player.getUniqueId() + 
                ", reported=" + reportedUuid + ", reason='" + reason + "'");
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