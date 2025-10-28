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
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.users.User;
import systems.mythical.cloudcore.utils.CloudLogger;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;

public class ReportCommand extends Command {
    private final CloudCoreBungee plugin;
    private final ReportManager reportManager;
    private final MessageManager messageManager;
    private final UserManager userManager;
    private final CloudLogger cloudLogger;

    public ReportCommand(CloudCoreBungee plugin) {
        super("report", Permissions.REPORT_USE);
        this.plugin = plugin;
        this.reportManager = ReportManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.userManager = UserManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.cloudLogger = CloudLoggerFactory.get();
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
        ProxiedPlayer onlineTarget = plugin.getProxy().getPlayer(targetName);
        if (onlineTarget == null) {
            // Case-insensitive fallback
            for (ProxiedPlayer p : plugin.getProxy().getPlayers()) {
                String name = p.getName();
                if (name != null && name.equalsIgnoreCase(targetName)) {
                    onlineTarget = p;
                    break;
                }
            }
        }

        java.util.UUID reportedUuid;
        String reportedDisplayName;
        if (onlineTarget != null) {
            reportedUuid = onlineTarget.getUniqueId();
            reportedDisplayName = onlineTarget.getName();
        } else {
            var userOpt = userManager.getUserByUsername(targetName);
            if (userOpt.isEmpty()) {
                player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_PLAYER_NOT_FOUND)));
                return;
            }
            User u = userOpt.get();
            reportedUuid = u.getUuid();
            reportedDisplayName = u.getUsername();
        }

        if (reportedUuid.equals(player.getUniqueId())) {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_CANNOT_REPORT_SELF)));
            return;
        }

        if (!reportManager.canReport(player.getUniqueId())) {
            long remainingCooldown = reportManager.getRemainingCooldown(player.getUniqueId());
            if (remainingCooldown > 0) {
                player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_COOLDOWN, remainingCooldown)));
            } else {
                player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_ERROR)));
            }
            return;
        }

        // Join the remaining arguments as the reason
        String reason = String.join(" ", args).substring(targetName.length() + 1);
        if (reason.trim().isEmpty()) {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_USAGE)));
            return;
        }

        // Ensure both reporter and reported exist in database to avoid DB errors
        var reporterOpt = userManager.getUserByUuid(player.getUniqueId());
        if (reporterOpt.isEmpty()) {
            player.sendMessage(new TextComponent("§cYour account was not found in our database. Please relog to register."));
            return;
        }
        var reportedOpt = userManager.getUserByUuid(reportedUuid);
        if (reportedOpt.isEmpty()) {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_PLAYER_NOT_FOUND)));
            return;
        }

        if (reportManager.createReport(player.getUniqueId(), reportedUuid, reason)) {
            player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_SUCCESS)));
            
            // Notify staff members
            for (ProxiedPlayer staff : plugin.getProxy().getPlayers()) {
                if (staff.hasPermission(Permissions.REPORT_NOTIFY)) {
                    staff.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_STAFF_NOTIFICATION,
                        player.getName(), reportedDisplayName, reason)));
                }
            }
        } else {
            cloudLogger.error("Report creation failed without exception. reporter=" + player.getUniqueId() + 
                ", reported=" + reportedUuid + ", reason='" + reason + "'");
            player.sendMessage(new TextComponent(messageManager.getColoredMessage(Messages.REPORT_ERROR)));
        }
    }
} 