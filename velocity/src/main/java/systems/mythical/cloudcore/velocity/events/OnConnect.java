package systems.mythical.cloudcore.velocity.events;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import systems.mythical.cloudcore.events.JoinEvent;
import systems.mythical.cloudcore.utils.ProtocolVersionTranslator;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.CommonSettings;
import systems.mythical.cloudcore.maintenance.MaintenanceSystemCommand;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.permissions.WebPanelPermissionManager;
import net.kyori.adventure.text.Component;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PermissionNode;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;
import java.util.UUID;

public class OnConnect {
    private static final CloudLogger cloudLogger = CloudLoggerFactory.get();
    private final DatabaseManager databaseManager;
    private final Logger pluginLogger;
    private final WebPanelPermissionManager webPanelPermissionManager;
    
    public OnConnect(DatabaseManager databaseManager, Logger pluginLogger) {
        this.databaseManager = databaseManager;
        this.pluginLogger = pluginLogger;
        this.webPanelPermissionManager = WebPanelPermissionManager.getInstance(databaseManager, pluginLogger);
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onConnect(LoginEvent event) {
        try {
            Player player = event.getPlayer();
            String username = player.getUsername();
            UUID uuid = player.getUniqueId();
            String ip = player.getRemoteAddress().getAddress().getHostAddress();
            
            LuckPerms luckPerms = LuckPermsProvider.get();
            User lpUser = luckPerms.getUserManager().getUser(uuid);
            if (lpUser == null) {
                pluginLogger.warning("Could not get LuckPerms user for " + username);
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
            if (maintenanceEnabled) {
                if (!MaintenanceSystemCommand.isInMaintenance(uuid)) {
                    String kickMsg = messageManager.getColoredMessage("maintenance.kick_message");
                    Component kickComponent = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(kickMsg);
                    player.disconnect(kickComponent);
                    cloudLogger.info("Kicked " + username + " (" + uuid + ") due to maintenance mode.");
                }
            }

            // Get client information
            String clientName = "Unknown"; // Velocity doesn't provide client name directly
            String userVersion = ProtocolVersionTranslator.translateProtocolToString(player.getProtocolVersion().getProtocol());
            String serverName = "lobby"; // Default server name for initial connection

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
            webPanelPermissionManager.updateUserPermissions(uuid, webPanelPermissions, negativePermissions);
            cloudLogger.debug("Updated web panel permissions for user " + username + " on join");

            JoinEvent.onPlayerJoin(
                username,
                uuid,
                ip,
                userVersion,
                clientName,
                serverName,
                groupName
            );
            cloudLogger.debug("Processed join event for player: " + username + " with group: " + groupName);
        } catch (Exception e) {
            cloudLogger.error("Error processing join event: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 