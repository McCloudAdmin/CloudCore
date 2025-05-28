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
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.logging.Logger;
import java.net.InetSocketAddress;

public class OnConnect implements Listener {
    private static final Logger logger = Logger.getLogger(OnConnect.class.getName());
    private final DatabaseManager databaseManager;
    private final Logger pluginLogger;

    public OnConnect(DatabaseManager databaseManager, Logger pluginLogger) {
        this.databaseManager = databaseManager;
        this.pluginLogger = pluginLogger;
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
            LuckPerms luckPerms = LuckPermsProvider.get();
            String group = luckPerms.getUserManager().getUser(event.getPlayer().getUniqueId()).getPrimaryGroup();
            if (group == null || group.isEmpty()) {
                group = "default";
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

            JoinEvent.onPlayerJoin(
                event.getPlayer().getName(),
                event.getPlayer().getUniqueId(),
                ip,
                userVersion,
                clientName,
                serverName,
                group
            );
            logger.info("Processed join event for player: " + event.getPlayer().getName() + " with group: " + group);
        } catch (Exception e) {
            logger.severe("Error processing join event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
