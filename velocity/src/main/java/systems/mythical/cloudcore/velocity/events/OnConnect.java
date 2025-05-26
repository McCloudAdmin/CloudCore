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
import net.kyori.adventure.text.Component;

import java.util.logging.Logger;
import java.util.UUID;

public class OnConnect {
    private static final Logger logger = Logger.getLogger(OnConnect.class.getName());
    private final DatabaseManager databaseManager;
    private final Logger pluginLogger;
    
    public OnConnect(DatabaseManager databaseManager, Logger pluginLogger) {
        this.databaseManager = databaseManager;
        this.pluginLogger = pluginLogger;
    }

    @Subscribe(order = PostOrder.EARLY)
    public void onConnect(LoginEvent event) {
        try {
            Player player = event.getPlayer();
            String username = player.getUsername();
            UUID uuid = player.getUniqueId();
            String ip = player.getRemoteAddress().getAddress().getHostAddress();
            
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
                    pluginLogger.info("Kicked " + username + " (" + uuid + ") due to maintenance mode.");
                }
            }

            // Get client information
            String clientName = "Unknown"; // Velocity doesn't provide client name directly
            String userVersion = ProtocolVersionTranslator.translateProtocolToString(player.getProtocolVersion().getProtocol());
            String serverName = "lobby"; // Default server name for initial connection

            JoinEvent.onPlayerJoin(
                username,
                uuid,
                ip,
                userVersion,
                clientName,
                serverName
            );
            logger.info("Processed join event for player: " + username);
        } catch (Exception e) {
            logger.severe("Error processing join event: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 