package systems.mythical.cloudcore.bungee.events;

import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import systems.mythical.cloudcore.events.JoinEvent;
import systems.mythical.cloudcore.utils.ProtocolVersionTranslator;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.CommonSettings;
import systems.mythical.cloudcore.maintenance.MaintenanceSystemCommand;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.permissions.WebPanelPermissionManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.net.InetSocketAddress;

public class OnConnect implements Listener {
    private static final Logger logger = Logger.getLogger(OnConnect.class.getName());
    private final DatabaseManager databaseManager;
    private final Logger pluginLogger;
    private final WebPanelPermissionManager webPanelPermissionManager;

    public OnConnect(DatabaseManager databaseManager, Logger pluginLogger) {
        this.databaseManager = databaseManager;
        this.pluginLogger = pluginLogger;
        this.webPanelPermissionManager = WebPanelPermissionManager.getInstance(databaseManager, pluginLogger);
    }

    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onConnect(PostLoginEvent event) {
        try {
            String ip;
            try {
                ip = ((InetSocketAddress) event.getPlayer().getSocketAddress()).getAddress().getHostAddress();
            } catch (Exception e) {
                // Fallback to direct IP if socket address fails
                String fallbackIp = event.getPlayer().getAddress().getAddress().getHostAddress();
                ip = fallbackIp;
                logger.warning("Using fallback IP method for player: " + event.getPlayer().getName());
            }

            // LuckPerms group name
            LuckPerms luckPerms = LuckPermsProvider.get();
            User lpUser = luckPerms.getUserManager().getUser(event.getPlayer().getUniqueId());
            if (lpUser == null) {
                pluginLogger.warning("Could not get LuckPerms user for " + event.getPlayer().getName());
                return;
            }

            Group groupObject = luckPerms.getGroupManager().getGroup(lpUser.getPrimaryGroup());
            String groupName = groupObject != null && groupObject.getDisplayName() != null ? 
                groupObject.getDisplayName() : lpUser.getPrimaryGroup();
            if (groupName == null || groupName.isEmpty()) {
                groupName = "N/A";
            }
            
            // Maintenance check
            CloudSettings settings = CloudSettings.getInstance(databaseManager, pluginLogger);
            MessageManager messageManager = MessageManager.getInstance(databaseManager, pluginLogger);
            CommonSettings.BooleanSetting ENABLE_MAINTENANCE = new CommonSettings.BooleanSetting("enable_maintenance", false);
            boolean maintenanceEnabled = ENABLE_MAINTENANCE.parseValue(settings.getSetting(ENABLE_MAINTENANCE.getName()));
            if (maintenanceEnabled && !MaintenanceSystemCommand.isInMaintenance(event.getPlayer().getUniqueId())) {
                String kickMsg = messageManager.getColoredMessage(Messages.CONNECTION_BLOCKED_MAINTENANCE);
                event.getPlayer().disconnect(TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', kickMsg)));
                pluginLogger.info("Kicked " + event.getPlayer().getName() + " due to maintenance mode.");
                return;
            }

            // Get client information
            String clientName = "Unknown"; // BungeeCord doesn't provide client name directly
            String userVersion = ProtocolVersionTranslator.translateProtocolToString(event.getPlayer().getPendingConnection().getVersion());
            String serverName = event.getPlayer().getServer() != null ? 
                event.getPlayer().getServer().getInfo().getName() : "lobby";

            // Update web panel permissions
            List<String> webPanelPermissions = new ArrayList<>();
            List<String> negativePermissions = new ArrayList<>();

            // Get all permissions the user has (including inherited ones)
            lpUser.getNodes().forEach(node -> {
                if (node.getType() == NodeType.PERMISSION) {
                    PermissionNode permNode = (PermissionNode) node;
                    String permission = permNode.getPermission();
                    if (permission.startsWith("cloudcore.webpanel.")) {
                        if (permNode.getValue()) {
                            webPanelPermissions.add(permission);
                        } else {
                            negativePermissions.add(permission);
                        }
                    }
                }
            });

            // Update the web panel permissions in the database
            webPanelPermissionManager.updateUserPermissions(event.getPlayer().getUniqueId(), webPanelPermissions, negativePermissions);
            pluginLogger.info("Updated web panel permissions for user " + event.getPlayer().getName() + " on join");

            JoinEvent.onPlayerJoin(
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId(),
                ip,
                userVersion,
                clientName,
                serverName,
                groupName
            );
            logger.info("Processed join event for player: " + event.getPlayer().getName() + " with group: " + groupName);
        } catch (Exception e) {
            logger.severe("Error processing join event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
