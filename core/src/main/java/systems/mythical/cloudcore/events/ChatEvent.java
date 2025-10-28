package systems.mythical.cloudcore.events;

import systems.mythical.cloudcore.chat.ChatLogManager;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.permissions.PermissionChecker;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.CommonSettings;

import java.util.UUID;
import java.util.logging.Logger;
import systems.mythical.cloudcore.utils.CloudLogger;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;

public class ChatEvent {
    private static final Logger logger = Logger.getLogger(ChatEvent.class.getName());
    private static ChatLogManager chatLogManager;
    private static PermissionChecker permissionChecker;
    private static CloudSettings cloudSettings;
    // Redis support removed
    private static CloudLogger cloudLogger = CloudLoggerFactory.get();

    // Permission nodes
    public static final String CHAT_BYPASS_PERMISSION = "cloudcore.chat.bypass";

    // Settings
    private static final CommonSettings.BooleanSetting LOG_CHAT_LOGS = new CommonSettings.BooleanSetting("log_chatlogs", true);

    public static void initialize(DatabaseManager databaseManager) {
        chatLogManager = ChatLogManager.getInstance(databaseManager, logger);
        permissionChecker = PermissionChecker.getInstance();
        cloudSettings = CloudSettings.getInstance(databaseManager, logger);
        
        // Redis support removed: always use direct database logging
        cloudLogger = CloudLoggerFactory.get();
    }

    /**
     * Handles a chat message event
     *
     * @param uuid The UUID of the player who sent the message
     * @param content The content of the message
     * @param server The server where the message was sent
     * @return true if the message should be allowed, false if it should be blocked
     */
    public static boolean onPlayerChat(UUID uuid, String content, String server) {
        if (chatLogManager == null || permissionChecker == null || cloudSettings == null) {
            cloudLogger.error("Managers not initialized! Call ChatEvent.initialize() first.");
            return true; // Allow the message if not initialized
        }

        try {
            // Check if chat logging is enabled
            if (!LOG_CHAT_LOGS.parseValue(cloudSettings.getSetting(LOG_CHAT_LOGS.getName()))) {
                return true; // Allow message but don't log it
            }

            // Directly log to database (Redis removed)
            chatLogManager.logChatMessageAsync(uuid, content, server);
            cloudLogger.debug("Chat message logged directly to database for player " + uuid);
                        
            return true;
        } catch (Exception e) {
            cloudLogger.error("Error handling chat message: " + e.getMessage());
            return true; // Allow the message if there's an error
        }
    }
} 